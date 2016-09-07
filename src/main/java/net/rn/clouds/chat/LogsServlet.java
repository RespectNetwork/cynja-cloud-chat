/**
 * 
 */
package net.rn.clouds.chat;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rn.clouds.chat.constants.ChatErrors;
import net.rn.clouds.chat.exceptions.ChatSystemException;
import net.rn.clouds.chat.exceptions.ChatValidationException;
import net.rn.clouds.chat.model.ChatMessage;
import net.rn.clouds.chat.util.Utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.syntax.XDIAddress;
import biz.neustar.clouds.chat.CynjaCloudChat;
import biz.neustar.clouds.chat.model.QueryInfo;
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
		
		try{
			XDIAddress cloud = Utility.creteXDIAddress(req.getParameter("cloud"));
			XDIAddress cloud1 = Utility.creteXDIAddress(req.getParameter("cloud1"));
			XDIAddress cloud2 = Utility.creteXDIAddress(req.getParameter("cloud2"));
			String cloudSecretToken = req.getParameter("cloudSecretToken");

			QueryInfo queryInfo = Utility.createQueryInfo(req);

			List<ChatMessage> logs = CynjaCloudChat.connectionServiceImpl.chatHistory(cloud, cloudSecretToken, cloud1, cloud2, queryInfo);

			JsonArray jsonArray = new JsonArray();
			resp.setContentType("appliction/json");

			for (ChatMessage log : logs) {
				jsonArray.add(JsonUtil.chatHistoryToJson(log));
			}

			JsonUtil.write(resp.getWriter(), jsonArray);
			
			CynjaCloudChat.connectionServiceImpl.updateChatStatus(cloud1, logs);

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
