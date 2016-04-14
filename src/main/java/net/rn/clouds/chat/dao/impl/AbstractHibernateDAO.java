/**
 * 
 */
package net.rn.clouds.chat.dao.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import net.rn.clouds.chat.dao.GenericDAO;
import net.rn.clouds.chat.hibernate.HibernateConf;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

/**
 * @author Noopur Pandey
 * 
 */
public class AbstractHibernateDAO<T, ID extends Serializable> extends HibernateDaoSupport implements GenericDAO<T, ID> {

    private Class<T> persistentClass;

    @SuppressWarnings("unchecked")
    public AbstractHibernateDAO() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    @SuppressWarnings("unchecked")
    public T getById(ID id) {

        HibernateConf config = HibernateConf.getInstance();
        Session session = config.getSessionFactory().openSession();

        Transaction transaction = session.beginTransaction();

        return (T) session.get(getPersistentClass(), id);

    }

    @SuppressWarnings("unchecked")
    public T getById(ID id, boolean lock) {
        if (lock) {
            return (T) getSessionFactory().getCurrentSession().get(getPersistentClass(), id, LockOptions.UPGRADE);
        } else
            return getById(id);
    }

    @SuppressWarnings("unchecked")
    public T loadById(ID id) {
        return (T) getSessionFactory().getCurrentSession().load(getPersistentClass(), id);
    }

    public void save(T entity) {
        Session session = getSession();
        Transaction transaction = session.beginTransaction();
        session.save(entity);
        transaction.commit();
        session.close();
    }

    public void update(T entity) {
        getSessionFactory().getCurrentSession().update(entity);
    }

    public void saveOrUpdate(T entity) {
        getSessionFactory().getCurrentSession().saveOrUpdate(entity);
    }

    public void delete(T entity) {
        getSessionFactory().getCurrentSession().delete(entity);
    }

    public void deleteById(ID id) {
        getSessionFactory().getCurrentSession().delete(loadById(id));
    }

    public List<T> findAll() {
        return findByCriteria();
    }

    /**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion) {
        try {

            HibernateConf config = HibernateConf.getInstance();
            Session session = config.getSessionFactory().openSession();

            Transaction transaction = session.beginTransaction();

            Criteria crit = session.createCriteria(getPersistentClass());
            for (Criterion c : criterion) {
                crit.add(c);
            }

            transaction.commit();
            List<T> list = crit.list();
            session.close();
            return list;

        } catch (HibernateException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return null;
        }

    }

    /**
     * Find by criteria.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<T> findByCriteria(Map criterias) {

        Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(getPersistentClass());
        criteria.add(Restrictions.allEq(criterias));
        return criteria.list();
    }

    /**
     * This method will execute an HQL query and return the number of affected
     * entities.
     */
    protected int executeQuery(String query, String namedParams[], Object params[]) {
        Query q = getSessionFactory().getCurrentSession().createQuery(query);

        if (namedParams != null) {
            for (int i = 0; i < namedParams.length; i++) {
                q.setParameter(namedParams[i], params[i]);
            }
        }

        return q.executeUpdate();
    }

    protected int executeQuery(String query) {
        return executeQuery(query, null, null);
    }

    /**
     * This method will execute a Named HQL query and return the number of
     * affected entities.
     */
    protected int executeNamedQuery(String namedQuery, String namedParams[], Object params[]) {
        Query q = getSessionFactory().getCurrentSession().getNamedQuery(namedQuery);

        if (namedParams != null) {
            for (int i = 0; i < namedParams.length; i++) {
                q.setParameter(namedParams[i], params[i]);
            }
        }

        return q.executeUpdate();
    }

    protected int executeNamedQuery(String namedQuery) {
        return executeNamedQuery(namedQuery, null, null);
    }

    public Session getSession() {

        HibernateConf config = HibernateConf.getInstance();
        return config.getSessionFactory().openSession();
    }

    /**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(int offset, int limit, String sortOrder, String sortBy, Criterion... criterion) {
            Session session = getSession();
            Transaction transaction = session.beginTransaction();

            Criteria crit = session.createCriteria(getPersistentClass());
            for (Criterion c : criterion) {
                crit.add(c);
            }

            if ("asc".equalsIgnoreCase(sortOrder)) {
                crit.addOrder(Order.asc(sortBy));
            } else if ("desc".equalsIgnoreCase(sortOrder)) {
                crit.addOrder(Order.desc(sortBy));
            } else {
                crit.addOrder(Order.desc(sortBy));
            }
            crit.setFirstResult(offset);
            crit.setMaxResults(limit);
            transaction.commit();
            List<T> list = crit.list();
            session.close();
            return list;
    }
}
