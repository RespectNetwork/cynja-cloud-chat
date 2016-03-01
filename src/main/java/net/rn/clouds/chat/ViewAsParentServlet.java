/**
 * 
 */
package net.rn.clouds.chat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rn.clouds.chat.service.impl.ConnectionImpl;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.parser.ParserException;
import biz.neustar.clouds.chat.CynjaCloudChat;
import biz.neustar.clouds.chat.util.JsonUtil;

import com.google.gson.JsonObject;

/**
 * @author Noopur Pandey
 *
 */
public class ViewAsParentServlet extends HttpServlet{

	private static final long serialVersionUID = 2049298539409005496L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ViewAsParentServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		XDIAddress parent = null;
		
		try{			
			parent = XDIAddress.create(req.getParameter("parent"));
		}catch(ParserException pe){
			LOGGER.error("Incorrect cloud format: "+req.getParameter("parent"));
			throw new ParserException("Incorrect cloud format: "+req.getParameter("parent"));
		}
		String parentSecretToken = req.getParameter("parentSecretToken");

		ConnectionImpl[] connections = (ConnectionImpl[])CynjaCloudChat.connectionServiceImpl.viewConnectionsAsParent(parent, parentSecretToken);
					
		JsonObject jsonObject = JsonUtil.connectionToJson(connections);

		resp.setContentType("appliction/json");
		JsonUtil.write(resp.getWriter(), jsonObject);			
	}
}
