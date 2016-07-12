/**
 * 
 */
package net.rn.clouds.chat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rn.clouds.chat.exceptions.ChatSystemException;
import net.rn.clouds.chat.exceptions.ChatValidationException;
import net.rn.clouds.chat.util.Utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.syntax.XDIAddress;
import biz.neustar.clouds.chat.CynjaCloudChat;

/**
 * @author Noopur Pandey
 *
 */
public class UnblockServlet extends HttpServlet{
	
	private static final long serialVersionUID = 2049298539409005496L;
	private static final Logger LOGGER = LoggerFactory.getLogger(UnblockServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try{		

			XDIAddress cloud = Utility.creteXDIAddress(req.getParameter("cloud"));
			XDIAddress cloud1 = Utility.creteXDIAddress(req.getParameter("cloud1"));
			XDIAddress cloud2 = Utility.creteXDIAddress(req.getParameter("cloud2"));

			String parentSecretToken = req.getParameter("cloudSecretToken");
			CynjaCloudChat.connectionServiceImpl.unblockConnection(cloud, parentSecretToken, cloud1, cloud2);

		}catch(ChatValidationException ve){
			LOGGER.error("ErrorCode: [{}] : ErrorMessage: {}", ve.getErrorCode(), ve.getErrorDescription(), ve);
			Utility.handleChatException(resp, ve.getErrorCode(), ve.getErrorDescription());

		}catch(ChatSystemException se){
			LOGGER.error("ErrorCode: [{}] : ErrorMessage: {}", se.getErrorCode(), se.getErrorDescription(), se);
			Utility.handleChatException(resp, se.getErrorCode(), se.getErrorDescription());
		}
	}

}
