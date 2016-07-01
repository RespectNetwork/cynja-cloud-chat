package biz.neustar.clouds.chat.service;

import java.util.List;

import net.rn.clouds.chat.model.ChatMessage;
import biz.neustar.clouds.chat.model.Connection;
import biz.neustar.clouds.chat.model.Log;
import biz.neustar.clouds.chat.model.QueryInfo;
import biz.neustar.clouds.chat.websocket.WebSocketMessageHandler;

public interface LogService {

    public Integer addLog(WebSocketMessageHandler fromWebSocketMessageHandler, Connection connection, String line);

    public Log[] getLogs(Connection connection);

    public List<ChatMessage> getChatHistory(Connection connection, QueryInfo queryInfo);
}
