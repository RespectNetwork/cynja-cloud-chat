/**
 * 
 */
package net.rn.clouds.chat.service.impl;

import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.XDIAddress;
import biz.neustar.clouds.chat.model.Connection;

/**
 * @author Noopur Pandey
 *
 */
public class ConnectionImpl implements Connection{
	
	private XDIAddress child1;
	private XDIAddress child2;	
	private boolean isApprovalRequired;
	private boolean isApproved1;	
	private boolean isApproved2;
	private boolean isBlocked1;
	private boolean isBlocked2;
	private String blockedBy1;
	private String blockedBy2;
	private CloudName connectionName;
	private String firstName;
	private String lastName;
	private String nickName;
	private String avatar;
	
	public ConnectionImpl(XDIAddress child1, XDIAddress child2, boolean isApprovalRequired, 
			boolean isApproved1, boolean isApproved2, boolean isBlocked1, boolean isBlocked2, 
			CloudName connectionName, String blockedBy1, String blockedBy2){
		this.child1 = child1;
		this.child2 = child2;		
		this.isApprovalRequired = isApprovalRequired;
		this.isApproved1 = isApproved1;
		this.isApproved2 = isApproved2;
		this.isBlocked1 = isBlocked1;
		this.isBlocked2 = isBlocked2;
		this.connectionName = connectionName;	
		this.blockedBy1 = blockedBy1;
		this.blockedBy2 = blockedBy2;
	}	
		
	public ConnectionImpl(XDIAddress child1, XDIAddress child2){
		this.child1 = child1;
		this.child2 = child2;		
	}		
	
	public Boolean isApprovalRequired(){
		return isApprovalRequired;
	}					
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.model.Connection#getChild1()
	 */
	@Override
	public XDIAddress getChild1(){
		return child1;
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.model.Connection#getChild2()
	 */
	@Override
	public XDIAddress getChild2(){
		return child2;
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.model.Connection#isApproved1()
	 */
	@Override
	public Boolean isApproved1(){
		return isApproved1;
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.model.Connection#isApproved2()
	 */
	@Override
	public Boolean isApproved2(){
		return isApproved2;
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.model.Connection#isBlocked1()
	 */
	@Override
	public Boolean isBlocked1(){
		return isBlocked1;
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.model.Connection#isBlocked2()
	 */
	@Override
	public Boolean isBlocked2(){
		return isBlocked2;
	}
		
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.model.Connection#getConnectionName()
	 */
	@Override
	public CloudName getConnectionName(){
		return connectionName;
	}
	
	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getFirstName(){
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName(){
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getNickName(){
		return nickName;
	}
	
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	
	public String getBlockedBy1(){
		return blockedBy1;
	}
	
	public String getBlockedBy2(){
		return blockedBy2;
	}
}
