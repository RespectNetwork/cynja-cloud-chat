package biz.neustar.clouds.chat.util;

import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;

import javax.websocket.Session;

import net.rn.clouds.chat.model.ChatMessage;
import net.rn.clouds.chat.service.impl.ConnectionImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.clouds.chat.CynjaCloudChat;
import biz.neustar.clouds.chat.model.Connection;
import biz.neustar.clouds.chat.model.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

public class JsonUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
	private static final Gson gson = new GsonBuilder()
	.setDateFormat(DateFormat.FULL, DateFormat.FULL)
	.disableHtmlEscaping()
	.serializeNulls()
	.create();

	public static String toString(JsonElement jsonElement) {

		LOGGER.info("Enter JsonUtil.toString()");
		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(stringWriter);
		gson.toJson(jsonElement, jsonWriter);
		LOGGER.info("Exit JsonUtil.toString()");
		return stringWriter.getBuffer().toString();
	}

	public static void write(Writer writer, JsonElement jsonElement) {

		LOGGER.info("Enter JsonUtil.write()");
		JsonWriter jsonWriter = new JsonWriter(writer);
		jsonWriter.setIndent("  ");
		gson.toJson(jsonElement, jsonWriter);
		LOGGER.info("Exit JsonUtil.write()");
	}

	public static JsonObject connectionsToJson(Connection[] connections) {

		LOGGER.info("Enter JsonUtil.connectionsToJson()");
		JsonObject childrenJsonObject = new JsonObject();

		for (Connection connection : connections) {

			JsonArray childJsonArray = childrenJsonObject.getAsJsonArray(connection.getChild1().toString());

			if (childJsonArray == null) {

				childJsonArray = new JsonArray();
				childrenJsonObject.add(connection.getChild1().toString(), childJsonArray);
			}

			JsonObject child1JsonObject = new JsonObject();
			child1JsonObject.add("child", gson.toJsonTree(connection.getChild2().toString()));
			
			if(connection.getConnectionName() != null) {
			    child1JsonObject.add("name", gson.toJsonTree(connection.getConnectionName().toString()));
			} else {
			    //In case of old connections where we did not store the name of the connection, we can discovery it from discovery service.
			    child1JsonObject.add("name", null);
			}
			
			child1JsonObject.add("approved", gson.toJsonTree(connection.isApproved1()));
			child1JsonObject.add("blocked", gson.toJsonTree(connection.isBlocked1()));

			Session[] sessions = CynjaCloudChat.sessionService.getSessions(connection);

			JsonArray sessionsJsonArray = new JsonArray();

			for (Session session : sessions) {

				JsonObject sessionJsonObject = new JsonObject();
				sessionJsonObject.add("id", gson.toJsonTree(session.getId()));
				sessionJsonObject.add("open", gson.toJsonTree(session.isOpen()));

				sessionsJsonArray.add(sessionJsonObject);
			}

			child1JsonObject.add("sessions", sessionsJsonArray);

			childJsonArray.add(child1JsonObject);
		}
		LOGGER.info("Exit JsonUtil.connectionsToJson()");

		return childrenJsonObject;
	}
	
	public static JsonObject connectionToJson(Connection[] connections) {

		LOGGER.info("Enter JsonUtil.connectionToJson()");
		JsonObject childrenJsonObject = new JsonObject();

		for (Connection connection : connections) {
			
			ConnectionImpl connectionImpl = null;
			if(connection == null){
				continue;
			}else if(connection instanceof ConnectionImpl){
				connectionImpl = (ConnectionImpl)connection;
			} 

			JsonArray childJsonArray = childrenJsonObject.getAsJsonArray(connectionImpl.getChild1().toString());

			if (childJsonArray == null) {
				LOGGER.info("Creating connection array for cloud: {}", connectionImpl.getChild1().toString());

				childJsonArray = new JsonArray();
				childrenJsonObject.add(connectionImpl.getChild1().toString(), childJsonArray);
			}

			JsonObject child1JsonObject = new JsonObject();
			child1JsonObject.add("cloud", gson.toJsonTree(connectionImpl.getChild2().toString()));			
			child1JsonObject.add("name", gson.toJsonTree(connectionImpl.getConnectionName().toString()));						
			child1JsonObject.add("approved", gson.toJsonTree(connectionImpl.isApproved1()));
			child1JsonObject.add("isApprovalRequired", gson.toJsonTree(connectionImpl.isApprovalRequired()));
			child1JsonObject.add("blocked", gson.toJsonTree(connectionImpl.isBlocked1()));
			child1JsonObject.add("blockedBy", gson.toJsonTree(connectionImpl.getBlockedBy1()));
			child1JsonObject.add("firstName", gson.toJsonTree(connectionImpl.getFirstName()));
			child1JsonObject.add("lastName", gson.toJsonTree(connectionImpl.getLastName()));
			child1JsonObject.add("nickName", gson.toJsonTree(connectionImpl.getNickName()));
			child1JsonObject.add("avatar", gson.toJsonTree(connectionImpl.getAvatar()));
			LOGGER.info("child1JsonObject: {}", child1JsonObject.toString());

			Session[] sessions = CynjaCloudChat.sessionService.getSessions(connectionImpl);

			JsonArray sessionsJsonArray = new JsonArray();

			for (Session session : sessions) {
				LOGGER.info("session object: {}", sessions.toString());
				JsonObject sessionJsonObject = new JsonObject();
				sessionJsonObject.add("id", gson.toJsonTree(session.getId()));
				sessionJsonObject.add("open", gson.toJsonTree(session.isOpen()));

				sessionsJsonArray.add(sessionJsonObject);
			}

			child1JsonObject.add("sessions", sessionsJsonArray);

			childJsonArray.add(child1JsonObject);
		}
		LOGGER.info("Exit JsonUtil.connectionToJson()");

		return childrenJsonObject;
	}

	public static JsonObject logToJson(Log log) {

		JsonObject logJsonObject = new JsonObject();
		logJsonObject.add("chatChild1", new JsonPrimitive(log.getFromWebSocketMessageHandler().getChild1().toString()));
		logJsonObject.add("chatChild2", new JsonPrimitive(log.getFromWebSocketMessageHandler().getChild2().toString()));
		logJsonObject.add("connectionChild1", new JsonPrimitive(log.getFromWebSocketMessageHandler().getConnection().getChild1().toString()));
		logJsonObject.add("connectionChild2", new JsonPrimitive(log.getFromWebSocketMessageHandler().getConnection().getChild2().toString()));
		logJsonObject.add("message", new JsonPrimitive(log.getLine()));
		logJsonObject.add("date", gson.toJsonTree(log.getDate()));

		return logJsonObject;
	}
	
	public static JsonObject chatHistoryToJson(ChatMessage log) {

		JsonObject logJsonObject = new JsonObject();
        logJsonObject.add("messageId", new JsonPrimitive(log.getChatHistoryId()));
        logJsonObject.add("messageBy", new JsonPrimitive(log.getMessageBy()));
        logJsonObject.add("message", new JsonPrimitive(log.getMessage()));
        logJsonObject.add("date", gson.toJsonTree(log.getCreatedTime()));

        return logJsonObject;
    }

	public static JsonObject notificationToJson(Connection[] connections) {

		LOGGER.info("Enter JsonUtil.notificationToJson()");
		JsonObject notificationJsonObject = new JsonObject();

		for (Connection connection : connections) {

			ConnectionImpl connectionImpl = null;
			if(connection == null){
				continue;
			}else if(connection instanceof ConnectionImpl){
				connectionImpl = (ConnectionImpl)connection;
			}

			if(connectionImpl != null && connectionImpl.getChild1() != null){
				JsonArray cloudJsonArray = notificationJsonObject.getAsJsonArray(connectionImpl.getChild1().toString());

				if (cloudJsonArray == null) {

					LOGGER.info("Creating JSON array of message notification for cloud: {}",connectionImpl.getChild1().toString());
					cloudJsonArray = new JsonArray();
					notificationJsonObject.add(connectionImpl.getChild1().toString(), cloudJsonArray);
				}

				JsonObject child1JsonObject = new JsonObject();
				child1JsonObject.add("cloud", gson.toJsonTree(connectionImpl.getChild2().toString()));			
				child1JsonObject.add("name", gson.toJsonTree(connectionImpl.getConnectionName().toString()));
				cloudJsonArray.add(child1JsonObject);
			}
		}
		LOGGER.info("Exit JsonUtil.notificationToJson()");

		return notificationJsonObject;
	}
}
