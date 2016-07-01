/**
 * Copyright (c) 2016 Respect Network Corp. All Rights Reserved.
 */
package biz.neustar.clouds.chat.service.impl;

import java.util.List;

import net.rn.clouds.chat.dao.ChatHistoryDAO;
import net.rn.clouds.chat.dao.impl.ChatHistoryDAOImpl;
import net.rn.clouds.chat.model.ChatMessage;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.clouds.chat.model.Connection;
import biz.neustar.clouds.chat.model.Log;
import biz.neustar.clouds.chat.model.QueryInfo;
import biz.neustar.clouds.chat.service.LogService;
import biz.neustar.clouds.chat.websocket.WebSocketMessageHandler;

/**
 * Author: kvats Date: Apr 4, 2016 Time: 7:10:06 PM
 */
public class MySqlLogServiceImpl implements LogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlLogServiceImpl.class);
    private final ChatHistoryDAO chatHistoryDAO;

    public MySqlLogServiceImpl() {
        this.chatHistoryDAO = new ChatHistoryDAOImpl();
    }

    @Override
    public String addLog(WebSocketMessageHandler fromWebSocketMessageHandler, Connection connection, String message) {
        LOGGER.info("Add log for connection between cloud: {} and cloud: {}", connection.getChild1(),
                connection.getChild2());
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConnection_id(getConnectionId(connection));
        chatMessage.setMessageBy(connection.getChild1().toString());
        chatMessage.setMessage(message);
        DateTime date = new DateTime(DateTimeZone.UTC);
        chatMessage.setCreatedTime(date.getMillis());
        return chatHistoryDAO.saveMessage(chatMessage);
    }

    /**
     * @param connection
     */
    private Integer getConnectionId(Connection connection) {
        Integer id = connection.getChild1().hashCode() * connection.getChild2().hashCode();
        LOGGER.debug("Connection id is: {}", id);
        return Math.abs(id);
    }

    @Override
    public Log[] getLogs(Connection connection) {
        return null;
    }

    @Override
    public List<ChatMessage> getChatHistory(Connection connection, QueryInfo queryInfo) {
        LOGGER.info("Get chat logs for connection between cloud: {} and cloud: {}", connection.getChild1(),
                connection.getChild2());
        return chatHistoryDAO.viewChatHistory(getConnectionId(connection), queryInfo.getOffset(), queryInfo.getLimit(),
                queryInfo.getSortOrder());
    }

}
