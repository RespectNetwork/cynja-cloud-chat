/**
 * 
 */
package net.rn.clouds.chat.constants;

/**
 * @author Noopur Pandey
 *
 */
public enum Status {
	
	NEW("NEW"), 
	CLOUD_APPROVAL_PENDING("CLOUD_APPROVAL_PENDING"),
	CHILD_APPROVAL_PENDING("CHILD_APPROVAL_PENDING"),
	APPROVED("APPROVED"),
	BLOCKED_BY_REQUESTER("BLOCKED_BY_REQUESTER"),
	BLOCKED_BY_ACCEPTOR("BLOCKED_BY_ACCEPTOR"),
	BLOCKED("BLOCKED");
	
	private String status;
	
	private Status(String status){
		this.status = status;
	}
	
	public String getStatus(){
		return status;
	}

}
