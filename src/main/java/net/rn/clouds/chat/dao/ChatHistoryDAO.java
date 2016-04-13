/**
 * Copyright (c) 2015 Respect Network Corp. All Rights Reserved.
 */
package net.rn.clouds.chat.dao;

import java.util.List;

import net.rn.clouds.chat.model.ChatMessage;

/**
 * Author: kvats Date: Apr 1, 2016 Time: 7:03:04 PM
 */
public interface ChatHistoryDAO {

    /**
     * 
     * @param chatMessage
     * @return
     */
    public void saveMessage(ChatMessage chatMessage);

    public List<ChatMessage> viewChatHistory(int connectionId, int offset, int limit, String sort);
}