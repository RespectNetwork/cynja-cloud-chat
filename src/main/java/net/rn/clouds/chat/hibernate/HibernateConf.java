/**
 * 
 */
package net.rn.clouds.chat.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * @author Noopur Pandey
 *
 */
public class HibernateConf {
	private static Configuration configuration = null; 
	private static SessionFactory sessionFactory = null; 
	private static HibernateConf hibernateUtil = null;  
	
	private HibernateConf() { }   
	
	public static HibernateConf getInstance() { 
		if(hibernateUtil == null) { 
			
			configure(); 
			hibernateUtil = new HibernateConf(); 
		} 
		return hibernateUtil; 
	}   
	
	private static void configure() { 
		
		configuration = (new Configuration()).configure();
		StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().
				applySettings(configuration.getProperties());
		sessionFactory = configuration.buildSessionFactory(builder.build());
	}   
	
	public SessionFactory getSessionFactory() { 
		
		return sessionFactory; 
	}  
}
