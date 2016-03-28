/**
 * 
 */
package net.rn.clouds.chat.dao.impl;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import net.rn.clouds.chat.dao.ConnectionProfileDAO;
import net.rn.clouds.chat.model.ConnectionProfile;


/**
 * @author Noopur Pandey
 *
 */
public class ConnectionProfileHibernateDAO extends
AbstractHibernateDAO<ConnectionProfile, String> 
implements  ConnectionProfileDAO{

	/* (non-Javadoc)
	 * @see net.rn.clouds.chat.dao.ConnectionProfileDAO#getProfile(java.lang.String)
	 */
	@Override
	public List<ConnectionProfile> getProfile(String cloudNumber) {
		
		return findByCriteria(Restrictions.eq("cloudNumber", cloudNumber));
	}

}
