package biz.neustar.clouds.chat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.websocket.DeploymentException;

import net.rn.clouds.chat.exceptions.ChatSystemException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.discovery.XDIDiscoveryClient;
import biz.neustar.clouds.chat.websocket.WebSocketEndpoint;


public class InitFilter implements Filter {

	public static XDIDiscoveryClient XDI_DISCOVERY_CLIENT;
	private static final String DISCOVERY_CLIENT_URL = "csp.cspDiscoveryClientURL";
	private static final Logger LOGGER = LoggerFactory.getLogger(InitFilter.class);

	static {

		Properties props = new Properties();
		String propFileName = "app.properties";

		InputStream inputStream = InitFilter.class.getClassLoader().getResourceAsStream(propFileName);
		try {

			props.load(inputStream);
			String url = props.getProperty(DISCOVERY_CLIENT_URL);
			XDI_DISCOVERY_CLIENT = new XDIDiscoveryClient(url);

		} catch (IOException e) {
			LOGGER.error("ErrorCode: [500] : ErrorMessage: {}", e.getMessage(), e);
			throw new ChatSystemException();
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		try {

			WebSocketEndpoint.install(filterConfig.getServletContext());
		} catch (DeploymentException ex) {

			throw new ServletException(ex.getMessage(), ex);
		}
	}

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

	}
}
