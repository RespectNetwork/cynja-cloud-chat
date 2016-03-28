/**
 * 
 */
package net.rn.clouds.chat.model;

import java.io.Serializable;

/**
 * @author Noopur Pandey
 *
 */
public class ConnectionRequestProfile implements Serializable{

	private ConnectionRequest connectionRequest;
	private ConnectionProfile connectionProfile;
	
	public ConnectionRequest getConnectionRequest() {
		return connectionRequest;
	}
	public void setConnectionRequest(ConnectionRequest connectionRequest) {
		this.connectionRequest = connectionRequest;
	}
	public ConnectionProfile getConnectionProfile() {
		return connectionProfile;
	}
	public void setConnectionProfile(ConnectionProfile connectionProfile) {
		this.connectionProfile = connectionProfile;
	}	
}
