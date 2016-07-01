/**
 * 
 */
package net.rn.clouds.chat.dao.impl;

import java.util.List;

import net.rn.clouds.chat.dao.ChatHistoryDAO;
import net.rn.clouds.chat.model.ChatMessage;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * Author: kvats Date: Apr 1, 2016 Time: 7:02:04 PM
 */
public class ChatHistoryDAOImpl extends AbstractHibernateDAO<ChatMessage, String> implements ChatHistoryDAO {

    @Override
    public String saveMessage(ChatMessage chatMessage) {
        return save(chatMessage);
    }

    @Override
    public List<ChatMessage> viewChatHistory(int connectionId, int offset, int limit, String sort) {
        Criterion criterion = Restrictions.eq("connection_id", connectionId);
        return findByCriteria(offset, limit, sort, "createdTime", criterion);
    }

}
