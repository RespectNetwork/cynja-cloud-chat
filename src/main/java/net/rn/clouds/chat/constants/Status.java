/**
 * 
 */
package net.rn.clouds.chat.constants;

/**
 * @author Noopur Pandey
 *
 */
public enum Status {
	
	NEW("new"), 
	CLOUD_APPROVAL_PENDING("cloudApprovalPending"),
	CHILD_APPROVAL_PENDING("childApprovalPending"),
	APPROVED("approved"),
	BLOCKED_BY_REQUESTER("blockedByRequester"),
	BLOCKED_BY_ACCEPTOR("blockedByAcceptor"),
	BLOCKED("blocked");
	
	private String status;
	
	private Status(String status){
		this.status = status;
	}
	
	public String getStatus(){
		return status;
	}

}
