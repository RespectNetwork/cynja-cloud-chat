/**
 * 
 */
package net.rn.clouds.chat.dao.impl;

import java.util.List;

import net.rn.clouds.chat.dao.ConnectionRequestProfileDAO;
import net.rn.clouds.chat.model.ConnectionRequest;
import net.rn.clouds.chat.model.ConnectionRequestProfile;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author Noopur Pandey
 *
 */
public class ConnectionRequestProfileDAOImpl extends
AbstractHibernateDAO<ConnectionRequestProfile, ConnectionRequest> implements
ConnectionRequestProfileDAO {

	/* (non-Javadoc)
	 * @see net.rn.clouds.chat.dao.ConnectionRequestProfileDAO#viewConnections(java.lang.String)
	 */
	@Override
	public List<ConnectionRequestProfile> viewConnections(String children) {
		
		Session session = getSession();
		session.beginTransaction();		
		
		String hql = "from ConnectionRequest cr left join ConnectionProfile cp on "
				+ "(cp.cloudNumber=cr.connectingClouds.requestingCloudNumber or "
				+ "cp.cloudNumber = cr.connectingClouds.acceptingCloudNumber) where "
				+ "(cr.connectingClouds.requestingCloudNumber in("+children+") and "
				+ "(cp.cloudNumber IS NULL or cp.cloudNumber = cr.connectingClouds.acceptingCloudNumber)) or "
				+ "(cr.connectingClouds.acceptingCloudNumber in ("+children+") and "
				+ "(cp.cloudNumber IS NULL or cp.cloudNumber = cr.connectingClouds.requestingCloudNumber))";
		Query query = session.createQuery(hql);
		List<ConnectionRequestProfile> connections = query.list();
		
		/*String sql = "select * from connection_request cr left join connection_profile cp on "
				+ "(cp.cloud_number=cr.requesting_cloud_number or "
				+ "cp.cloud_number = cr.accepting_cloud_number) where "
				+ "(cr.requesting_cloud_number in ("+children+") "
				+ "and (cp.cloud_number IS NULL or cp.cloud_number = cr.accepting_cloud_number)) or "
				+ "(cr.accepting_cloud_number in ("+children+") "
				+ "and (cp.cloud_number IS NULL or cp.cloud_number = cr.requesting_cloud_number))";
		List<ConnectionRequestProfile> connections = session.createSQLQuery(sql).list();*/
		session.getTransaction().commit();
		session.close();
		
		return connections;
	}

}
