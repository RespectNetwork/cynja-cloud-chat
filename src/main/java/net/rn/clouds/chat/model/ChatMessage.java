/**
 * Copyright (c) 2015 Respect Network Corp. All Rights Reserved.
 */
package net.rn.clouds.chat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Author: kvats Date: Apr 1, 2016 Time: 6:52:04 PM
 */
@Entity
@Table(name = "chat_history")
public class ChatMessage {
    @Id
    @GeneratedValue
    @Column(name = "chat_history_id")
    private Integer chatHistoryId;
    @Column(name = "connection_id")
    private Integer connection_id;
    private String message;
    @Column(name = "message_by")
    private String messageBy;
    @Column(name = "created_time")
    private long createdTime;
    @Column(name = "status")
    private String status;

    /**
     * @return the chatHistoryId
     */
    public Integer getChatHistoryId() {
        return chatHistoryId;
    }

    /**
     * @param chatHistoryId
     *            the chatHistoryId to set
     */
    public void setChatHistoryId(Integer chatHistoryId) {
        this.chatHistoryId = chatHistoryId;
    }

    /**
     * @return the connection_id
     */
    public Integer getConnection_id() {
        return connection_id;
    }

    /**
     * @param connection_id
     *            the connection_id to set
     */
    public void setConnection_id(Integer connection_id) {
        this.connection_id = connection_id;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the messageBy
     */
    public String getMessageBy() {
        return messageBy;
    }

    /**
     * @param messageBy
     *            the messageBy to set
     */
    public void setMessageBy(String messageBy) {
        this.messageBy = messageBy;
    }

    /**
     * @return the createdTime
     */
    public long getCreatedTime() {
        return createdTime;
    }

    /**
     * @param createdTime
     *            the createdTime to set
     */
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

	/**
	 * @return the status READ/UNREAD
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 */
	public void setStatus(String status) {
		this.status = status;
	}
}
