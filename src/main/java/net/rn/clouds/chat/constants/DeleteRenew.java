/**
 * 
 */
package net.rn.clouds.chat.constants;

/**
 * @author Noopur Pandey
 *
 */
public enum DeleteRenew {
	
	DELETED_BY_REQUESTER("DELETED_BY_REQUESTER"),
	DELETED_BY_ACCEPTOR("DELETED_BY_ACCEPTOR"),
	RENEWED_BY_REQUESTER("RENEWED_BY_REQUESTER"),
	RENEWED_BY_ACCEPTOR("RENEWED_BY_ACCEPTOR");
	
	private String deleteRenew;
	
	private DeleteRenew(String deleteRenew){
		this.deleteRenew = deleteRenew;
	}
	
	public String getDeleteRenew(){
		return deleteRenew;
	}

}
