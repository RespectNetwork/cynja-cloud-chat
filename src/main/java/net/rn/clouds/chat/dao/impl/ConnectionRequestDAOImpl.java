/**
 * 
 */
package net.rn.clouds.chat.dao.impl;

import java.util.Collection;
import java.util.List;

import net.rn.clouds.chat.dao.ConnectionRequestDAO;
import net.rn.clouds.chat.model.ConnectingClouds;
import net.rn.clouds.chat.model.ConnectionRequest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import biz.neustar.clouds.chat.model.Connection;

/**
 * @author Noopur Pandey
 *
 */
public class ConnectionRequestDAOImpl extends
		AbstractHibernateDAO<ConnectionRequest, ConnectingClouds> implements
		ConnectionRequestDAO {
	
	/* (non-Javadoc)
	 * @see net.rn.clouds.chat.dao.ConnectionRequestDAO#getConnectionRequest(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ConnectionRequest> getConnectionRequest(String cloud1,
			String cloud2) {
		
		return findByCriteria(Restrictions.or(Restrictions.and(Restrictions.eq(
				"connectingClouds.requestingCloudNumber", cloud1), Restrictions
				.eq("connectingClouds.acceptingCloudNumber", cloud2)), Restrictions.and(Restrictions.eq(
						"connectingClouds.requestingCloudNumber", cloud2), Restrictions
						.eq("connectingClouds.acceptingCloudNumber", cloud1))));
	}
		
	/* (non-Javadoc)
	 * @see net.rn.clouds.chat.dao.ConnectionRequestDAO#createConnection(net.rn.clouds.chat.model.ConnectionRequest)
	 */
	@Override
	public void requestConnection(ConnectionRequest connectionRequest) {
		
		Session session = getSession();
		session.beginTransaction();
		session.save(connectionRequest);
		session.getTransaction().commit();
		session.close();
	}	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.rn.clouds.chat.dao.ConnectionRequestDAO#viewConnectionsAsParent(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	//@SuppressWarnings("unchecked")
	public List<ConnectionRequest> viewConnections(Collection<String> children) {
		return findByCriteria(Restrictions.or(Restrictions.in(
				"connectingClouds.requestingCloudNumber", children),
				Restrictions.in("connectingClouds.acceptingCloudNumber",
						children)));
	}	
		
	/* (non-Javadoc)
	 * @see net.rn.clouds.chat.dao.ConnectionRequestDAO#blockUnblock(net.rn.clouds.chat.model.ConnectionRequest)
	 */
	@Override
	public void updateRequest(ConnectionRequest connectionRequest) {
		
		Session session = getSession();
		session.beginTransaction();
		session.update(connectionRequest);
		session.getTransaction().commit();
		session.close();
		
	}
	
	/* (non-Javadoc)
	 * @see net.rn.clouds.chat.dao.ConnectionRequestDAO#deleteRequest(net.rn.clouds.chat.model.ConnectionRequest)
	 */
	@Override
	public void deleteRequest(ConnectionRequest connectionRequest) {
		
		Session session = getSession();
		session.beginTransaction();
		session.delete(connectionRequest);
		session.getTransaction().commit();
		session.close();
		
	}

	/* (non-Javadoc)
     * @see net.rn.clouds.chat.dao.ChatHistoryDAO#getNotification(java.lang.String)
     */
    @Override
	public List<Connection> getNotification(String cloudNumber) {
    	
    	Session session = getSession();
    	Transaction transaction = session.beginTransaction();
    	
    	List<Connection> connections = session.createQuery("from ConnectionRequest cr where "
    			+ "(cr.connectingClouds.requestingCloudNumber='"+cloudNumber+"' and cr.connectionId in "
    					+ "(select ch.connection_id from ChatMessage ch where ch.status='UNREAD' and ch.messageBy!=cr.requestingConnectionName )) or "
    			+"(cr.connectingClouds.acceptingCloudNumber='"+cloudNumber+"' and cr.connectionId in "
    					+ "(select ch.connection_id from ChatMessage ch where ch.status='UNREAD' and ch.messageBy!=cr.acceptingConnectionName))").list();
    	transaction.commit();
    	session.close();
    	return connections;
	}
}
