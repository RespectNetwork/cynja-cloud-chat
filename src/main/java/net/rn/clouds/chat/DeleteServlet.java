/**
 * 
 */
package net.rn.clouds.chat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;

import net.rn.clouds.chat.constants.ChatErrors;
import net.rn.clouds.chat.exceptions.ChatSystemException;
import net.rn.clouds.chat.exceptions.ChatValidationException;
import net.rn.clouds.chat.util.Utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.syntax.XDIAddress;
import biz.neustar.clouds.chat.CynjaCloudChat;
import biz.neustar.clouds.chat.model.Connection;

/**
 * @author Noopur Pandey
 *
 */
public class DeleteServlet  extends HttpServlet{

	private static final long serialVersionUID = 2524699264338535286L;
	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteServlet.class);
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try{		
			XDIAddress cloud = Utility.creteXDIAddress(req.getParameter("cloud"));
			XDIAddress cloud1 = Utility.creteXDIAddress(req.getParameter("cloud1"));
			XDIAddress cloud2 = Utility.creteXDIAddress(req.getParameter("cloud2"));

			String cloudSecretToken = req.getParameter("cloudSecretToken");

			Connection connection = CynjaCloudChat.connectionServiceImpl.deleteConnection(cloud, cloudSecretToken, cloud1, cloud2);

			Session[] sessions = CynjaCloudChat.sessionService.getSessions(connection);

			for (Session session : sessions) {

				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Connection has been deleted."));
			}
		}catch(ChatValidationException ve){

			LOGGER.error("ErrorCode: [{}] : ErrorMessage: {}", ve.getErrorCode(), ve.getErrorDescription(), ve);
			Utility.handleChatException(resp, ve.getErrorCode(), ve.getErrorDescription());

		}catch(ChatSystemException se){

			LOGGER.error("ErrorCode: [{}] : ErrorMessage: {}", se.getErrorCode(), se.getErrorDescription(), se);
			Utility.handleChatException(resp, se.getErrorCode(), se.getErrorDescription());

		}catch(Exception ex){

			LOGGER.error("ErrorCode: [{}] : ErrorMessage: {}",ChatErrors.SYSTEM_ERROR.getErrorCode(), ex.getMessage(), ex);
			Utility.handleChatException(resp, ChatErrors.SYSTEM_ERROR.getErrorCode(), ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
	}
}
