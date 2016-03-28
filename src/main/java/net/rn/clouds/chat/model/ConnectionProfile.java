/**
 * 
 */
package net.rn.clouds.chat.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Noopur Pandey
 *
 */
@Entity
@Table(name = "connection_profile")
@SuppressWarnings("serial")
public class ConnectionProfile implements Serializable{

	@Id
	@Column(name = "cloud_number")
	/*@ManyToOne (targetEntity = ConnectionRequest.class)
    @JoinColumns({
        @JoinColumn(name="accepting_cloud_number", referencedColumnName="accepting_cloud_number"),
        @JoinColumn(name="requesting_cloud_number", referencedColumnName="requesting_cloud_number")
    })*/
	private String cloudNumber;
	
	@Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "nick_name")
    private String nickName;
    
    @Column(name = "avatar")
    private String avatar;

    @Column(name = "last_updated")
    Date lastUpdated;

	public String getCloudNumber() {
		return cloudNumber;
	}

	public void setCloudNumber(String cloudNumber) {
		this.cloudNumber = cloudNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}

