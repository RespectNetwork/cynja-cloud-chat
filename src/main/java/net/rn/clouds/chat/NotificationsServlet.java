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
import net.rn.clouds.chat.service.impl.ConnectionImpl;
import net.rn.clouds.chat.util.Utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import biz.neustar.clouds.chat.CynjaCloudChat;
import biz.neustar.clouds.chat.util.JsonUtil;
import xdi2.core.syntax.XDIAddress;

/**
 * @author Noopur Pandey
 *
 */
public class NotificationsServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7591749842095897057L;
	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsServlet.class);
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try{
			XDIAddress cloud = Utility.creteXDIAddress(req.getParameter("cloud"));
			String cloudSecretToken = req.getParameter("cloudSecretToken");
			
			ConnectionImpl[] connections = (ConnectionImpl[])CynjaCloudChat.connectionServiceImpl.notifications(cloud, cloudSecretToken);
			
			JsonObject jsonObject = JsonUtil.notificationToJson(connections);
			resp.setContentType("appliction/json");
			JsonUtil.write(resp.getWriter(), jsonObject);

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
