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

import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.parser.ParserException;
import biz.neustar.clouds.chat.CynjaCloudChat;
import biz.neustar.clouds.chat.model.Log;
import biz.neustar.clouds.chat.util.JsonUtil;

import com.google.gson.JsonArray;

/**
 * @author Noopur Pandey
 *
 */
public class LogsServlet extends HttpServlet{
	
	private static final long serialVersionUID = 2806072987404647289L;
	private static final Logger LOGGER = LoggerFactory.getLogger(LogsServlet.class);
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		XDIAddress cloud = null;
		XDIAddress cloud1 = null;
		XDIAddress cloud2 = null;
		
		try{
			cloud = XDIAddress.create(req.getParameter("cloud"));
		}catch(ParserException pe){
			LOGGER.error("Incorrect cloud format: "+req.getParameter("cloud"));
			throw new ParserException("Incorrect cloud format: "+req.getParameter("cloud"));
		}
		
		try{
			cloud1 = XDIAddress.create(req.getParameter("cloud1"));
		}catch(ParserException pe){
			LOGGER.error("Incorrect cloud format: "+req.getParameter("cloud1"));
			throw new ParserException("Incorrect cloud format: "+req.getParameter("cloud1"));
		}
		
		try{
			cloud2 = XDIAddress.create(req.getParameter("cloud2"));
		}catch(ParserException pe){
			LOGGER.error("Incorrect cloud format: "+req.getParameter("cloud2"));
			throw new ParserException("Incorrect cloud format: "+req.getParameter("cloud2"));
		}

		String cloudSecretToken = req.getParameter("cloudSecretToken");
		Log[] logs = CynjaCloudChat.connectionServiceImpl.logsConnection(cloud, cloudSecretToken, cloud1, cloud2);

		JsonArray jsonArray = new JsonArray();

		for (Log log : logs) {

			jsonArray.add(JsonUtil.logToJson(log));
		}

		resp.setContentType("appliction/json");
		JsonUtil.write(resp.getWriter(), jsonArray);
	}

}