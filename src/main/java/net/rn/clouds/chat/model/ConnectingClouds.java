/**
 * 
 */
package net.rn.clouds.chat.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Noopur Pandey
 *
 */
@Embeddable
public class ConnectingClouds implements Serializable{

	@Column(name = "requesting_cloud_number")
	protected String requestingCloudNumber;
		
	@Column(name = "accepting_cloud_number")
	private String acceptingCloudNumber;
	
	public ConnectingClouds(){}
	
	public ConnectingClouds(String requestingCloudNumber, String acceptingCloudNumber){
				
		this.requestingCloudNumber = requestingCloudNumber;
		this.acceptingCloudNumber = acceptingCloudNumber;
	}		
	
	public void setRequestingCloudNumber(String requestingCloudNumber){
		this.requestingCloudNumber = requestingCloudNumber;
	}
	
	public String getRequestingCloudNumber(){
		return requestingCloudNumber;
	}
	
	public void setAcceptingCloudNumber(String acceptingCloudNumber){
		this.acceptingCloudNumber = acceptingCloudNumber;
	}
	
	public String getAcceptingCloudNumber(){
		return acceptingCloudNumber;
	}
	
	@Override
	public int hashCode() {
	    return requestingCloudNumber.hashCode() + acceptingCloudNumber.hashCode(); 
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null) return false;
	    if (!(obj instanceof ConnectingClouds))
	        return false;

	    ConnectingClouds other = (ConnectingClouds) obj;
	    if (requestingCloudNumber == null) {
	        if (other.requestingCloudNumber != null) return false;
	    } else if (!requestingCloudNumber.equals(other.requestingCloudNumber))
	        return false;
	    if (acceptingCloudNumber == null) {
	        if (other.acceptingCloudNumber != null) return false;
	    } else if (!acceptingCloudNumber.equals(other.acceptingCloudNumber))
	        return false;
	    return true;
	}   
}
