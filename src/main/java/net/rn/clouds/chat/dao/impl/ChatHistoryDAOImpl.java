/**
 * 
 */
package net.rn.clouds.chat.dao.impl;

import java.util.List;

import net.rn.clouds.chat.dao.ChatHistoryDAO;
import net.rn.clouds.chat.model.ChatMessage;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * Author: kvats Date: Apr 1, 2016 Time: 7:02:04 PM
 */
public class ChatHistoryDAOImpl extends AbstractHibernateDAO<ChatMessage, String> implements ChatHistoryDAO {

    @Override
    public Integer saveMessage(ChatMessage chatMessage) {
        return save(chatMessage);
    }

    @Override
    public List<ChatMessage> viewChatHistory(int connectionId, int offset, int limit, String sort, String status) {
        Criterion criterion = null;
        if(status != null){
        	criterion = Restrictions.and(Restrictions.eq("connection_id", connectionId),
        			Restrictions.eq("status", status));
        }else{
        	criterion = Restrictions.eq("connection_id", connectionId);
        }
        return findByCriteria(offset, limit, sort, "createdTime", criterion);
    }

	/* (non-Javadoc)
	 * @see net.rn.clouds.chat.dao.ChatHistoryDAO#updateMessageStatus(net.rn.clouds.chat.model.ChatMessage)
	 */
	@Override
	public void updateMessageStatus(Integer[] chatHistoryId) {
		Session session = getSession();
        Transaction transaction = session.beginTransaction();
        String hql = "UPDATE ChatMessage set status='READ' "  +
                "WHERE chatHistoryId in (:chatHistoryId)";
        Query query = session.createQuery(hql);
        query.setParameterList("chatHistoryId", chatHistoryId);
        query.executeUpdate();
        transaction.commit();
        session.close();
	}
}
