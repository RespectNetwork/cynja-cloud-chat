package net.rn.clouds.chat.dao.impl;

import java.sql.Timestamp;
import java.util.List;

import net.rn.clouds.chat.dao.CloudNameDAO;
import net.rn.clouds.chat.model.CloudName;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudNameHibernateDAO extends AbstractHibernateDAO<CloudName, Integer> implements CloudNameDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudNameHibernateDAO.class);

    /**
     * Find CloudName by entityCloudId
     */
    @SuppressWarnings("unchecked")
    public List<CloudName> findByEntityCloudId(Integer entityCloudId) {
        return findByCriteria(Restrictions.eq("entityCloud.entityCloudId", entityCloudId));
    }

    /**
     * Find CloudName by cloudName
     */
    public List<CloudName> findByCloudName(String cloudName) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
            transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(CloudName.class);
            criteria.add(Restrictions.eq("cloudName", cloudName));
            criteria.list();
        } finally {
            if (transaction != null) {
                transaction.commit();
            }
            if (session != null) {
                session.close();
            }
        }
        return findByCriteria(Restrictions.eq("cloudName", cloudName));
    }

    /**
     * Find CloudName by term
     */
    public List<CloudName> findByTerm(Integer term) {
        return findByCriteria(Restrictions.eq("term", term));
    }

    /**
     * Find CloudName by expirationDate
     */
    public List<CloudName> findByExpirationDate(Timestamp expirationDate) {
        return findByCriteria(Restrictions.eq("expirationDate", expirationDate));
    }

    /**
     * Find CloudName by statusId
     */
    public List<CloudName> findByStatusId(Integer statusId) {
        return findByCriteria(Restrictions.eq("statusId", statusId));
    }

    /**
     * Find CloudName by paymentId
     */
    @SuppressWarnings("unchecked")
    public List<CloudName> findByPaymentId(Integer paymentId) {
        return findByCriteria(Restrictions.eq("payment.paymentId", paymentId));
    }

    /**
     * Find CloudName by synonym
     */
    public List<CloudName> findBySynonym(String synonym) {
        return findByCriteria(Restrictions.eq("synonym", synonym));
    }

    /**
     * Find CloudName by createdDate
     */
    public List<CloudName> findByCreatedDate(Timestamp createdDate) {
        return findByCriteria(Restrictions.eq("createdDate", createdDate));
    }

    /**
     * Find CloudName by rnPolicyConsent
     */
    public List<CloudName> findByRNConsent(String rnPolicyConsent) {
        return findByCriteria(Restrictions.eq("rnPolicyConsent", rnPolicyConsent));
    }

    /**
     * Find CloudName by cspPolicyConsent
     */
    public List<CloudName> findByCSPConsent(String cspPolicyConsent) {
        return findByCriteria(Restrictions.eq("cspPolicyConsent", cspPolicyConsent));
    }
}
