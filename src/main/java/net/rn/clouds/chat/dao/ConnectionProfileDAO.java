/**
 * 
 */
package net.rn.clouds.chat.dao;

import java.util.List;

import net.rn.clouds.chat.model.ConnectionProfile;

/**
 * @author Noopur Pandey
 *
 */
public interface ConnectionProfileDAO {	
	
	/**
	 * @param cloudNumber
	 */
	public List<ConnectionProfile> getProfile(String cloudNumber);
}
