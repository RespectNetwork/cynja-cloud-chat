/**
 * 
 */
package net.rn.clouds.chat.dao;

import java.util.List;

import net.rn.clouds.chat.model.ConnectionRequestProfile;

/**
 * @author Noopur Pandey
 *
 */
public interface ConnectionRequestProfileDAO {

	/**
	 * @param children
	 * @return
	 */
	public List<ConnectionRequestProfile> viewConnections(String children);
}
