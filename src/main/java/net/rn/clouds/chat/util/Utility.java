/**
 * Copyright (c) 2016 Respect Network Corp. All Rights Reserved.
 */
package net.rn.clouds.chat.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rn.clouds.chat.constants.ChatErrors;
import net.rn.clouds.chat.exceptions.ChatSystemException;
import net.rn.clouds.chat.exceptions.ChatValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.parser.ParserException;
import biz.neustar.clouds.chat.model.QueryInfo;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Author: kvats Date: Apr 5, 2016 Time: 4:07:05 PM
 */
public class Utility {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utility.class);

    public static QueryInfo createQueryInfo(HttpServletRequest req) {
        QueryInfo queryInfo = new QueryInfo();
        if (req.getParameter("offset") != null) {
            queryInfo.setOffset(Integer.valueOf(req.getParameter("offset")));
        }
        if (req.getParameter("limit") != null) {
            queryInfo.setLimit(Integer.valueOf(req.getParameter("limit")));
        }
        queryInfo.setSortOrder(req.getParameter("offset"));
        return queryInfo;
    }

    public static XDIAddress creteXDIAddress(String cloud){
    	LOGGER.info("Enter creteXDIAddress for cloud: {}", cloud);
    	try{
				return XDIAddress.create(cloud);
		}catch(ParserException pe){
			LOGGER.error("Incorrect cloud format: "+cloud);
			throw new ChatValidationException(ChatErrors.INVALID_CLOUD_PROVIDED.getErrorCode(), ChatErrors.INVALID_CLOUD_PROVIDED.getErrorMessage()+cloud);
		}
    }

    public static void handleChatException(HttpServletResponse resp, int errorCode, String errorMessage){
    	
    	LOGGER.info("Enter handleChatException for errorCode: {} errorMessage: {}", errorCode, errorMessage);
    	resp.setContentType("appliction/json");
    	resp.setStatus(HttpServletResponse.SC_OK);
    	JsonObject logJsonObject = new JsonObject();
    	logJsonObject.add("errorCode", new JsonPrimitive(errorCode));
    	logJsonObject.add("errorMessage", new JsonPrimitive(errorMessage));
    	
		try {
			resp.getWriter().write(logJsonObject.toString());
		} catch (IOException e) {
			LOGGER.error("ErrorCode: [{}] errorMessage: {}", ChatErrors.SYSTEM_ERROR, e.getMessage(), e);
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
    }
}
