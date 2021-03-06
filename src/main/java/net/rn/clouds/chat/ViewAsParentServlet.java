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

import xdi2.core.syntax.XDIAddress;
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

		try{
			XDIAddress parent = Utility.createXDIAddress(req.getParameter("parent"));
			String parentSecretToken = req.getParameter("parentSecretToken");

			ConnectionImpl[] connections = (ConnectionImpl[])CynjaCloudChat.connectionServiceImpl.viewConnectionsAsParent(parent, parentSecretToken);

			JsonObject jsonObject = JsonUtil.connectionToJson(connections);
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
