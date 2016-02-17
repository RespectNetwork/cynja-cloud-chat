/**
 * 
 */
package net.rn.clouds.chat.dao.impl;

import java.util.List;

import net.rn.clouds.chat.dao.EntityCloudDAO;
import net.rn.clouds.chat.model.EntityCloud;

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
}
