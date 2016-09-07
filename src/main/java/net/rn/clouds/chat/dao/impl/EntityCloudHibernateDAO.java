/**
 * 
 */
package net.rn.clouds.chat.dao.impl;

import java.util.List;

import net.rn.clouds.chat.dao.EntityCloudDAO;
import net.rn.clouds.chat.model.EntityCloud;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

/**
 * @author Noopur Pandey
 *
 */
public class EntityCloudHibernateDAO extends
AbstractHibernateDAO<EntityCloud, Integer> implements EntityCloudDAO{	 

    /**
     * Find EntityCloud by cloudNumber
     */
	@Override
    public List<EntityCloud> findByCloudNumber(String cloudNumber) {
        return findByCriteria(Restrictions.eq("cloudNumber", cloudNumber));
    }

    /**
     * Find EntityCloud by guardianId
     */
	@Override
    public List<EntityCloud> findByGuardianId(Integer guardianId) {
        return findByCriteria(Restrictions.eq("guardianId", guardianId));
    }

	/* (non-Javadoc)
	 * @see net.rn.clouds.chat.dao.EntityCloudDAO#findDependentByGuardian(java.lang.String)
	 */
	@Override
	public List<String> findDependentByGuardian(String cloudNumber) {
		Session session = getSession();
    	Transaction transaction = session.beginTransaction();
    	
    	List<String> list = session.createQuery("select cloudNumber from EntityCloud where guardianId = "
    			+ "(select entityCloudId from EntityCloud where cloudNumber='"+cloudNumber+"')").list();
    	
    	transaction.commit();
    	session.close();
    	
    	return list;
	}
}
