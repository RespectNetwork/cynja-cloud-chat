/**
 * 
 */
package net.rn.clouds.chat.dao.impl;

import java.util.List;
import java.util.Set;

import net.rn.clouds.chat.dao.ConnectionProfileDAO;
import net.rn.clouds.chat.model.ConnectionProfile;
import org.hibernate.criterion.Restrictions;

/**
 * @author Noopur Pandey
 *
 */
public class ConnectionProfileDAOImpl extends
AbstractHibernateDAO<ConnectionProfile, String> implements
ConnectionProfileDAO {

	/* (non-Javadoc)
	 * @see net.rn.clouds.chat.dao.ConnectionRequestProfileDAO#viewConnections(java.lang.String)
	 */
	@Override
	public List<ConnectionProfile> viewConnections(Set<String> children) {
				
		return findByCriteria(Restrictions.in("cloudNumber", children));
	}

}
