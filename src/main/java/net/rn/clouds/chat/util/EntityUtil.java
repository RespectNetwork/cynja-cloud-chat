/**
 * 
 */
package net.rn.clouds.chat.util;

import java.util.List;

import net.rn.clouds.chat.dao.impl.EntityCloudHibernateDAO;
import net.rn.clouds.chat.model.EntityCloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Noopur Pandey
 *
 */
public class EntityUtil {

	private static Logger LOGGER = LoggerFactory.getLogger(EntityUtil.class);
	
	/**
	 * @param cloudNumber
	 * @return
	 */
	public static boolean isDependent(String cloudNumber){		
		boolean isDependent = false;
		
		EntityCloud cloudEntity = getCloudEntity(cloudNumber);
		if(cloudEntity.getGuardianId()!= null){
			isDependent = true;
		}
		
		return isDependent;
	}
	
	/**
	 * @param childCloudNumber
	 * @return
	 */
	public static String getGuardianCloudNumber(String childCloudNumber){		
		EntityCloud guardianEntity = getGuardianEntity(childCloudNumber);
		
		String guardianCloudNumber = "";
		if(guardianEntity!=null){
			guardianCloudNumber = guardianEntity.getCloudNumber();
		}
		
		return guardianCloudNumber;
	}
	
	/**
	 * @param cloudNumber
	 * @return
	 */
	public static EntityCloud getCloudEntity(String cloudNumber){
		
		LOGGER.debug("Getting Cloud Entity of {}: "+cloudNumber);
		EntityCloudHibernateDAO dao = new EntityCloudHibernateDAO();
		List<EntityCloud> entityList = dao.findByCloudNumber(cloudNumber);
		
		LOGGER.debug("Extracting Cloud Entity from List");
		EntityCloud cloudEntity = null;									
		if(entityList!=null && entityList.size()>0){
			cloudEntity = entityList.get(0);
		}
		return cloudEntity;
	}
	
	/**
	 * @param childCloudNumber
	 * @return
	 */
	public static EntityCloud getGuardianEntity(String childCloudNumber){
		
		EntityCloud childEntity = getCloudEntity(childCloudNumber);											
		
		LOGGER.debug("Getting Guardian Entity");
		EntityCloudHibernateDAO dao = new EntityCloudHibernateDAO();
		EntityCloud guardianEntity = null;
		if(childEntity!=null && childEntity.getGuardianId()!=null){
			guardianEntity = dao.getById(childEntity.getGuardianId());
		}			
		
		return guardianEntity;
	}
		
}
