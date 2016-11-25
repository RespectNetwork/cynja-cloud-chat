package net.rn.clouds.chat.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "cloud_name")
@SuppressWarnings("serial")
public class CloudName implements Serializable {

    /**
     * Attribute cloudNameId.
     */
    private Integer cloudNameId;

    /**
     * Attribute entityCloud
     */
    private EntityCloud entityCloud;

    /**
     * Attribute cloudName.
     */
    private String cloudName;

    /**
     * Attribute term.
     */
    private Integer term;

    /**
     * Attribute expirationDate.
     */
    private Timestamp expirationDate;

    /**
     * Attribute statusId.
     */
    private Integer statusId;   

    /**
     * Attribute createdDate.
     */
    private Timestamp createdDate;

    /**
     * Attribute rnPolicyConsent
     */
    private char rnPolicyConsent;

    /**
     * Attribute cspPolicyConsent
     */
    private char cspPolicyConsent;

    /**
     * <p>
     * </p>
     * 
     * @return cloudNameId
     */
    @Basic
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cloud_name_id")
    public Integer getCloudNameId() {
        return cloudNameId;
    }

    /**
     * @param cloudNameId
     *            new value for cloudNameId
     */
    public void setCloudNameId(Integer cloudNameId) {
        this.cloudNameId = cloudNameId;
    }

    /**
     * get entityCloud
     */
    @ManyToOne
    @JoinColumn(name = "entity_cloud_id")
    public EntityCloud getEntityCloud() {
        return this.entityCloud;
    }

    /**
     * set entityCloud
     */
    public void setEntityCloud(EntityCloud entityCloud) {
        this.entityCloud = entityCloud;
    }

    /**
     * <p>
     * </p>
     * 
     * @return cloudName
     */
    @Basic
    @Column(name = "cloud_name", length = 255)
    public String getCloudName() {
        return cloudName;
    }

    /**
     * @param cloudName
     *            new value for cloudName
     */
    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    /**
     * <p>
     * </p>
     * 
     * @return term
     */
    @Basic
    @Column(name = "term")
    public Integer getTerm() {
        return term;
    }

    /**
     * @param term
     *            new value for term
     */
    public void setTerm(Integer term) {
        this.term = term;
    }

    /**
     * <p>
     * </p>
     * 
     * @return expirationDate
     */
    @Basic
    @Column(name = "expiration_date")
    public Timestamp getExpirationDate() {
        return expirationDate;
    }

    /**
     * @param expirationDate
     *            new value for expirationDate
     */
    public void setExpirationDate(Timestamp expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * <p>
     * </p>
     * 
     * @return statusId
     */
    @Basic
    @Column(name = "status_id")
    public Integer getStatusId() {
        return statusId;
    }

    /**
     * @param statusId
     *            new value for statusId
     */
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }    

    /**
     * <p>
     * </p>
     * 
     * @return createdDate
     */
    @Basic
    @Column(name = "created_date")
    public Timestamp getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate
     *            new value for createdDate
     */
    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * 
     * @return rnPolicyConsent
     */
    @Basic
    @Column(name = "rn_trust_consent")
    public char getRNPolicyConsent() {
        return rnPolicyConsent;
    }

    /**
     * @param rnPolicyConsent
     *            new value for rnPolicyConsent
     */
    public void setRNPolicyConsent(char rnPolicyConsent) {
        this.rnPolicyConsent = rnPolicyConsent;
    }

    /**
     * 
     * @return cspPolicyConsent
     */
    @Basic
    @Column(name = "csp_terms_consent")
    public char getCSPPolicyConsent() {
        return cspPolicyConsent;
    }

    /**
     * @param cspPolicyConsent
     *            new value for cspPolicyConsent
     */
    public void setCSPPolicyConsent(char cspPolicyConsent) {
        this.cspPolicyConsent = cspPolicyConsent;
    }
}
