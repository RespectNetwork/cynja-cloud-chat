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
     * @param cloudNumber
     * @return
     */
    public List<String> findDependentByGuardian(String cloudNumber);

    /**
     * Find by cloudNumber or cloudName
     * @param cloud
     * @return
     */
    public List<Object[]> findByCloud(String cloud);
}
