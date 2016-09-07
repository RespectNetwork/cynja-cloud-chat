/**
 * 
 */
package net.rn.clouds.chat.dao;

import java.util.Collection;
import java.util.List;

import biz.neustar.clouds.chat.model.Connection;
import net.rn.clouds.chat.model.ConnectionRequest;

/**
 * @author Noopur Pandey
 *
 */
public interface ConnectionRequestDAO {
	
	/**
	 * @param cloud1
	 * @param cloud2
	 * @return List<ConnectionRequest>
	 */
	public List<ConnectionRequest> getConnectionRequest(String cloud1, String cloud2);
	
	/**
	 * @param connectionRequest
	 */
	public void requestConnection(ConnectionRequest connectionRequest);				
	
	/**
	 * @param children
	 * @return List<Connection>
	 */
	public List<ConnectionRequest> viewConnections(Collection<String> children);		
	
	/**
	 * @param connectionRequest
	 */
	public void updateRequest(ConnectionRequest connectionRequest);
	
	/**
	 * @param connectionRequest
	 */
	public void deleteRequest(ConnectionRequest connectionRequest);

	/**
	 * @param cloudNumber
	 * @return List<Connection>
	 */
	public List<Object[]> getNotification(String cloudNumbers);
}
