package biz.neustar.clouds.chat.websocket;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Decoder;
import javax.websocket.DeploymentException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;
import javax.websocket.Session;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.syntax.XDIAddress;
import biz.neustar.clouds.chat.CynjaCloudChat;
import biz.neustar.clouds.chat.model.Connection;

public class WebSocketEndpoint extends javax.websocket.Endpoint {

	private static final Logger log = LoggerFactory.getLogger(WebSocketEndpoint.class);

	private static final String PATH = "/{version}/chat/{child1}/{child2}";

	public static final List<WebSocketMessageHandler> WEBSOCKETMESSAGEHANDLERS = new ArrayList<WebSocketMessageHandler> ();

	public static void install(ServletContext servletContext) throws DeploymentException {

		// find server container

		ServerContainer serverContainer = (ServerContainer) servletContext.getAttribute("javax.websocket.server.ServerContainer");
		if (serverContainer == null) throw new DeploymentException("Cannot find ServerContainer");

		// init websocket endpoint

		List<String> subprotocols = Arrays.asList(new String[] { "cynja-chat" });
		List<Extension> extensions = null;
		List<Class<? extends Encoder>> encoders = null;
		List<Class<? extends Decoder>> decoders = null;

		ServerEndpointConfig.Configurator serverEndpointConfigConfigurator = new ServerEndpointConfig.Configurator() {

		};

		ServerEndpointConfig.Builder serverEndpointConfigBuilder = ServerEndpointConfig.Builder.create(
				WebSocketEndpoint.class, 
				PATH);

		serverEndpointConfigBuilder.subprotocols(subprotocols);
		serverEndpointConfigBuilder.extensions(extensions);
		serverEndpointConfigBuilder.encoders(encoders);
		serverEndpointConfigBuilder.decoders(decoders);
		serverEndpointConfigBuilder.configurator(serverEndpointConfigConfigurator);

		ServerEndpointConfig serverEndpointConfig = serverEndpointConfigBuilder.build();

		serverContainer.addEndpoint(serverEndpointConfig);

		// done

		log.info("Installed WebSocket endpoint at " + PATH + " with subprotocols " + subprotocols);
	}

	public static void send(WebSocketMessageHandler fromWebSocketMessageHandler, String line, Integer messageId) {

		for (WebSocketMessageHandler webSocketMessageHandler : WEBSOCKETMESSAGEHANDLERS) {

			if (fromWebSocketMessageHandler.getChild2().equals(webSocketMessageHandler.getChild1())){

				if(fromWebSocketMessageHandler.getConnection().isBlocked2() || !fromWebSocketMessageHandler.getConnection().isApproved2()){
					continue;
				}
			}

			if ((fromWebSocketMessageHandler.getChild1().equals(webSocketMessageHandler.getChild2()) &&
					fromWebSocketMessageHandler.getChild2().equals(webSocketMessageHandler.getChild1())) || (
							fromWebSocketMessageHandler.getChild1().equals(webSocketMessageHandler.getChild1()) &&
							fromWebSocketMessageHandler.getChild2().equals(webSocketMessageHandler.getChild2()))) {

				webSocketMessageHandler.send(fromWebSocketMessageHandler, line, messageId);
			}
		}
	}

	@Override
	public void onOpen(Session session, EndpointConfig endpointConfig) {

		// set timeout

		long oldMaxIdleTimeout = session.getMaxIdleTimeout();
		long newMaxIdleTimeout = 0;
		session.setMaxIdleTimeout(newMaxIdleTimeout);

		if (log.isDebugEnabled()) log.debug("Changed max idle timeout of session " + session.getId() + " from " + oldMaxIdleTimeout + " to " + newMaxIdleTimeout);

		// init message handler

		ServerEndpointConfig serverEndpointConfig = (ServerEndpointConfig) endpointConfig;

		try {

			// parse parameters

			String version = session.getPathParameters().get("version");
			XDIAddress child1 = XDIAddress.create(URLDecoder.decode(session.getPathParameters().get("child1"), "UTF-8"));
			String child1SecretToken = session.getRequestParameterMap().get("child1SecretToken").get(0);
			XDIAddress child2 = XDIAddress.create(URLDecoder.decode(session.getPathParameters().get("child2"), "UTF-8"));

			// check connection

			Connection connection = null;
			if("1".equalsIgnoreCase(version)){
				connection = CynjaCloudChat.connectionService.findConnection(child1, child1SecretToken, child2);
			}else if("v2".equalsIgnoreCase(version)){
				connection = CynjaCloudChat.connectionServiceImpl.findConnection(child1, child1SecretToken, child2);
			}

			if (connection == null) {

				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Connection not found."));
				return;
			}

			if (! Boolean.TRUE.equals(connection.isApproved1())){

				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Connection is not approved yet."));
				return;
			}

			if (Boolean.TRUE.equals(connection.isBlocked1())){

				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Connection is temporarily blocked."));
				return;
			}

			// add session to connection

			CynjaCloudChat.sessionService.addSession(connection, session);

			// create message handler

			WebSocketMessageHandler webSocketMessageHandler = new WebSocketMessageHandler(session, connection.getChild1(), connection.getChild2(), connection);

			session.addMessageHandler(webSocketMessageHandler);
			WEBSOCKETMESSAGEHANDLERS.add(webSocketMessageHandler);

			log.info("WebSocket session " + session.getId() + " opened (" + serverEndpointConfig.getPath() + ") between " + child1 + " and " + child2);
		} catch (Exception ex) {

			try {

				String reason = "Cannot add message handler: " + ex.getMessage();
				log.error(reason, ex);

				if (reason.length() > 120) reason = reason.substring(0, 120);

				session.close(new CloseReason(CloseCodes.PROTOCOL_ERROR, reason));
			} catch (IOException ex2) {

				throw new RuntimeException(ex2.getMessage(), ex2);
			}
		}
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {

		// find message handler and connection

		WebSocketMessageHandler webSocketMessageHandler = (WebSocketMessageHandler) session.getMessageHandlers().iterator().next();
		Connection connection = webSocketMessageHandler.getConnection();

		// remove session from connection

		CynjaCloudChat.sessionService.removeSession(connection, session);

		// remove message handler

		session.removeMessageHandler(webSocketMessageHandler);
		WEBSOCKETMESSAGEHANDLERS.remove(webSocketMessageHandler);

		log.info("WebSocket session " + session.getId() + " closed.");
	}

	@Override
	public void onError(Session session, Throwable throwable) {

		log.error("WebSocket session " + session.getId() + " error: " + throwable.getMessage(), throwable);
	}
}