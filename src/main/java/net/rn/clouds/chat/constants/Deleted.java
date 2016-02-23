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
	DELETED_BY_ACCEPTOR("deletedByAcceptor");
	
	private String deleted;
	
	private Deleted(String deleted){
		this.deleted = deleted;
	}
	
	public String getDeleted(){
		return deleted;
	}

}
