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
    public Integer saveMessage(ChatMessage chatMessage);

    /**
     * @param connectionId
     * @param offset
     * @param limit
     * @param sort
     * @param status
     * @return
     */
    public List<ChatMessage> viewChatHistory(int connectionId, int offset, int limit, String sort, String status);

    /**
     * @param chatMessage
     */
    public void updateMessageStatus(Integer[] chatHistoryId);
}
