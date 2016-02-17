/**
 * 
 */
package net.rn.clouds.chat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rn.clouds.chat.service.impl.ConnectionImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.parser.ParserException;
import biz.neustar.clouds.chat.CynjaCloudChat;
import biz.neustar.clouds.chat.util.JsonUtil;

import com.google.gson.JsonObject;

/**
 * @author Noopur Pandey
 *
 */
public class ViewAsCloudServlet extends HttpServlet {

	private static final long serialVersionUID = -5562095868444661045L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ViewAsCloudServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		XDIAddress cloud = null;
		
		try{			
			cloud = XDIAddress.create(req.getParameter("cloud"));
		}catch(ParserException pe){
			LOGGER.error("Incorrect cloud format: "+req.getParameter("cloud"));
			throw new ParserException("Incorrect cloud format: "+req.getParameter("cloud"));
		}
		
		String cloudSecretToken = req.getParameter("cloudSecretToken");

		ConnectionImpl[] connections = (ConnectionImpl[])CynjaCloudChat.connectionServiceImpl.viewConnectionsAsChild(cloud, cloudSecretToken);

		JsonObject jsonObject = JsonUtil.connectionToJson(connections);

		resp.setContentType("appliction/json");
		JsonUtil.write(resp.getWriter(), jsonObject);
	}
}
