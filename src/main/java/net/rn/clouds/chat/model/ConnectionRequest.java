/**
 * 
 */
package net.rn.clouds.chat.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Noopur Pandey
 *
 */
@Entity
@Table(name = "connection_request")
@SuppressWarnings("serial")
public class ConnectionRequest implements Serializable{
	
	@EmbeddedId
	private ConnectingClouds connectingClouds;		

	@Column(name = "approving_cloud_number")
	private String approvingCloudNumber;
	
	@Column(name = "requesting_connection_name")
	private String requestingConnectionName;
	
	@Column(name = "accepting_connection_name")
	private String acceptingConnectionName;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "delete_renew")
	private String deleteRenew;		

	@Column(name = "creation_date")
	private Date creationDate;
	
	@Column(name = "blocked_by_requester")
	private String blockedByRequester;
	
	@Column(name = "blocked_by_acceptor")
	private String blockedByAcceptor;

	public ConnectingClouds getConnectingClouds() {
		return connectingClouds;
	}

	public void setConnectingClouds(ConnectingClouds connectingClouds) {
		this.connectingClouds = connectingClouds;
	}

	public String getApprovingCloudNumber() {
		return approvingCloudNumber;
	}

	public void setApprovingCloudNumber(String approvingCloudNumber) {
		this.approvingCloudNumber = approvingCloudNumber;
	}

	public String getRequestingConnectionName() {
		return requestingConnectionName;
	}

	public void setRequestingConnectionName(String requestingConnectionName) {
		this.requestingConnectionName = requestingConnectionName;
	}

	public String getAcceptingConnectionName() {
		return acceptingConnectionName;
	}

	public void setAcceptingConnectionName(String acceptingConnectionName) {
		this.acceptingConnectionName = acceptingConnectionName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getDeleteRenew() {
		return deleteRenew;
	}

	public void setDeleteRenew(String deleteRenew) {
		this.deleteRenew = deleteRenew;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getBlockedByRequester() {
		return blockedByRequester;
	}

	public void setBlockedByRequester(String blockedByRequester) {
		this.blockedByRequester = blockedByRequester;
	}

	public String getBlockedByAcceptor() {
		return blockedByAcceptor;
	}

	public void setBlockedByAcceptor(String blockedByAcceptor) {
		this.blockedByAcceptor = blockedByAcceptor;
	}
}
