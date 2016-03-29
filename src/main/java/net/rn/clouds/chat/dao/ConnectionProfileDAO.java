/**
 * 
 */
package net.rn.clouds.chat.dao;

import java.util.List;
import java.util.Set;

import net.rn.clouds.chat.model.ConnectionProfile;

/**
 * @author Noopur Pandey
 *
 */
public interface ConnectionProfileDAO {

	/**
	 * @param children
	 * @return
	 */
	public List<ConnectionProfile> viewConnections(Set<String> children);
}
