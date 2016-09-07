package biz.neustar.clouds.chat.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.rn.clouds.chat.model.ChatMessage;
import biz.neustar.clouds.chat.model.Connection;
import biz.neustar.clouds.chat.model.Log;
import biz.neustar.clouds.chat.model.QueryInfo;
import biz.neustar.clouds.chat.service.LogService;
import biz.neustar.clouds.chat.websocket.WebSocketMessageHandler;

public class DefaultLogService implements LogService {

	public static final int MAX_LOG_SIZE = 20;

	private Map<Integer, LinkedList<Log>> logMap;

	public DefaultLogService() {

		this.logMap = new HashMap<Integer, LinkedList<Log>> ();
	}

	public Integer addLog(WebSocketMessageHandler fromWebSocketMessageHandler, Connection connection, String line, boolean isOnline) {
		int hashCode = connection.getChild1().hashCode() * connection.getChild2().hashCode();
		LinkedList<Log> logList = this.logMap.get(Integer.valueOf(hashCode));

		if (logList == null) {

			logList = new LinkedList<Log> ();
			this.logMap.put(Integer.valueOf(hashCode), logList);
		}

		logList.add(new Log(fromWebSocketMessageHandler, connection, line, new Date()));
		if (logList.size() > MAX_LOG_SIZE) logList.pop();
		return 0;
	}

	public Log[] getLogs(Connection connection) {

		int hashCode = connection.getChild1().hashCode() * connection.getChild2().hashCode();
		List<Log> logList = this.logMap.get(Integer.valueOf(hashCode));

		if (logList == null) return new Log[0];

		return logList.toArray(new Log[logList.size()]);
	}

    @Override
    public List<ChatMessage> getChatHistory(Connection connection, QueryInfo queryInfo) {
        // TODO Auto-generated method stub
        return null;
    }

	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.LogService#updateMessageStatus(biz.neustar.clouds.chat.model.Connection, biz.neustar.clouds.chat.model.QueryInfo)
	 */
	@Override
	public void updateMessageStatus(Integer[] chatHistoryId) {
		// TODO Auto-generated method stub

	}
}
