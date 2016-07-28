/**
 * 
 */
package net.rn.clouds.chat.constants;

/**
 * @author Noopur Pandey
 *
 */
public enum MessageStatus {
	
	READ("READ"),
	UNREAD("UNREAD");
	
	private String status;
	
	private MessageStatus(String status){
		this.status = status;
	}
	
	public String getStatus(){
		return status;
	}

}
