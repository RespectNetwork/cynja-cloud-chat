/**
 * 
 */
package net.rn.clouds.chat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rn.clouds.chat.constants.ChatErrors;
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
public class RequestServlet extends HttpServlet{
	
	private static final long serialVersionUID = 2049298539409005496L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
		
		try{
			XDIAddress cloud1 = Utility.createXDIAddress(req.getParameter("cloud1"));
			XDIAddress cloud2 = Utility.createXDIAddress(req.getParameter("cloud2"));

			String requestingCloudSecretToken = req.getParameter("cloud1SecretToken");

			CynjaCloudChat.connectionServiceImpl.requestConnection(cloud1, requestingCloudSecretToken, cloud2);

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
