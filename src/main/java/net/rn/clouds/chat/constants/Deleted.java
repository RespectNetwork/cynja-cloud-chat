/**
 * 
 */
package net.rn.clouds.chat.constants;

/**
 * @author Noopur Pandey
 *
 */
public enum Deleted {
	
	DELETED_BY_REQUESTER("deletedByRequester"),
	DELETED_BY_ACCEPTOR("deletedByAcceptor"),
	RENEWED_BY_REQUESTER("renewedByRequester"),
	RENEWED_BY_ACCEPTOR("renewedByAcceptor");
	
	private String deleted;
	
	private Deleted(String deleted){
		this.deleted = deleted;
	}
	
	public String getDeleted(){
		return deleted;
	}

}
