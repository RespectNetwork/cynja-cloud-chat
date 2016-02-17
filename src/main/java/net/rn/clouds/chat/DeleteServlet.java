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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.parser.ParserException;
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
		Connection connection = CynjaCloudChat.connectionServiceImpl.deleteConnection(cloud, cloudSecretToken, cloud1, cloud2);

		Session[] sessions = CynjaCloudChat.sessionService.getSessions(connection);

		for (Session session : sessions) { 

			session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Connection has been deleted."));
		}
	}
}
