package net.rn.clouds.chat.dao;

import java.sql.Timestamp;
import java.util.List;

import net.rn.clouds.chat.model.CloudName;

/**
 * <p>
 * Generic DAO layer for CloudNames
 * </p>
 * <p>
 * Generated at Mon Sep 22 13:58:04 IST 2014
 * </p>
 * 
 * @author Salto-db Generator v1.0.16 / EJB3 + Spring/Hibernate DAO + TestCases
 * @see http://www.hibernate.org/328.html
 */
public interface CloudNameDAO extends GenericDAO<CloudName, Integer> {

    /*
     * TODO : Add specific businesses daos here. These methods will be
     * overwrited if you re-generate this interface. You might want to extend
     * this interface and to change the dao factory to return an instance of the
     * new implemenation in buildCloudNameDAO()
     */

    /**
     * Find CloudName by entityCloudId
     */
    public List<CloudName> findByEntityCloudId(Integer entityCloudId);

    /**
     * Find CloudName by cloudName
     */
    public List<CloudName> findByCloudName(String cloudName);

    /**
     * Find CloudName by term
     */
    public List<CloudName> findByTerm(Integer term);

    /**
     * Find CloudName by expirationDate
     */
    public List<CloudName> findByExpirationDate(Timestamp expirationDate);

    /**
     * Find CloudName by statusId
     */
    public List<CloudName> findByStatusId(Integer statusId);

    /**
     * Find CloudName by paymentId
     */
    public List<CloudName> findByPaymentId(Integer paymentId);

    /**
     * Find CloudName by synonym
     */
    public List<CloudName> findBySynonym(String synonym);

    /**
     * Find CloudName by createdDate
     */
    public List<CloudName> findByCreatedDate(Timestamp createdDate);

    /**
     * Find CloudName by rnPolicyConsent
     */
    public List<CloudName> findByRNConsent(String rnPolicyConsent);

    /**
     * Find CloudName by cspPolicyConsent
     */
    public List<CloudName> findByCSPConsent(String cspPolicyConsent);
}