/**
 * 
 */
package net.rn.clouds.chat.dao;

import java.util.List;

import net.rn.clouds.chat.model.EntityCloud;

/**
 * @author Noopur Pandey
 *
 */
public interface EntityCloudDAO {
	

    /**
     * Find EntityCloud by cloudNumber
     */
    public List<EntityCloud> findByCloudNumber(String cloudNumber);
    

    /**
     * Find EntityCloud by guardianId
     */
    public List<EntityCloud> findByGuardianId(Integer guardianId);

    /**
     * Find EntityCloud by dob
     */
}
