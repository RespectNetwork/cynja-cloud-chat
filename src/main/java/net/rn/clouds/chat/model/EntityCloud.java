/**
 * 
 */
package net.rn.clouds.chat.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author Noopur Pandey
 *
 */
@Entity
@Table(name = "entity_cloud")
@SuppressWarnings("serial")
public class EntityCloud {	    
    
	 /**
     * Attribute entityCloudId.
     */
	@Id
    @GeneratedValue
	@Column(name = "entity_cloud_id")
    private Integer entityCloudId;    

    /**
     * Attribute cloudNumber.
     */
	@Column(name = "cloud_number")
    private String cloudNumber;

    /**
     * Attribute cloudType.
     */
	@Column(name = "cloud_type")
    private String cloudType;

    /**
     * Attribute guardianId.
     */
	@Column(name = "guardian_id")
    private Integer guardianId;

    /**
     * Attribute dob.
     */
	@Column(name = "dob")
    private Date dob;

    /**
     * Attribute email.
     */
	@Column(name = "email")
    private String email;

    /**
     * Attribute phone.
     */
	@Column(name = "phone")
    private String phone;

    /**
     * Attribute term.
     */
	@Column(name = "term")
    private Integer term;

    /**
     * Attribute expirationDate.
     */
	@Column(name = "expiration_date")
    private Timestamp expirationDate;

    /**
     * Attribute statusId.
     */
	@Column(name = "status_id")
    private Integer statusId;

    /**
     * Attribute payment
     */
	@Column(name = "payment_id")
    private Integer payment;

    /**
     * Attribute synonymCount.
     */
	@Column(name = "synonym_count")
    private Integer synonymCount;

    /**
     * Attribute createdDate.
     */
	@Column(name = "created_date")
    private Timestamp createdDate; 		

	/**
     * Guardian consent for creating dependent.
     */
	@Column(name = "guardian_consent")
    private String guardianConsent;

	/**
     * List of CloudName
     */
    private List<CloudName> cloudNames = null;
	
    /**    
     * @return entityCloudId
     */
    @Basic
    @Id
    @GeneratedValue
    @Column(name = "entity_cloud_id")
    public Integer getEntityCloudId() {
        return entityCloudId;
    }

    /**
     * @param entityCloudId
     *            new value for entityCloudId
     */
    public void setEntityCloudId(Integer entityCloudId) {
        this.entityCloudId = entityCloudId;
    }
   
    /**    
     * @return cloudNumber
     */
    @Basic
    @Column(name = "cloud_number", length = 128)
    public String getCloudNumber() {
        return cloudNumber;
    }

    /**
     * @param cloudNumber
     *            new value for cloudNumber
     */
    public void setCloudNumber(String cloudNumber) {
        this.cloudNumber = cloudNumber;
    }

    /**     
     * @return cloudType
     */
    @Basic
    @Column(name = "cloud_type", length = 25)
    public String getCloudType() {
        return cloudType;
    }

    /**
     * @param cloudType
     *            new value for cloudType
     */
    public void setCloudType(String cloudType) {
        this.cloudType = cloudType;
    }

    /**     
     * @return guardianId
     */
    @Basic
    @Column(name = "guardian_id")
    public Integer getGuardianId() {
        return guardianId;
    }

    /**
     * @param guardianId
     *            new value for guardianId
     */
    public void setGuardianId(Integer guardianId) {
        this.guardianId = guardianId;
    }

    /**
     * @return dob
     */
    @Basic
    @Column(name = "dob")
    public Date getDob() {
        return dob;
    }

    /**
     * @param dob
     *            new value for dob
     */
    public void setDob(Date dob) {
        this.dob = dob;
    }

    /**
     * @return email
     */
    @Basic
    @Column(name = "email", length = 128)
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     *            new value for email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return phone
     */
    @Basic
    @Column(name = "phone", length = 36)
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone
     *            new value for phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
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
     * @return synonymCount
     */
    @Basic
    @Column(name = "synonym_count")
    public Integer getSynonymCount() {
        return synonymCount;
    }

    /**
     * @param synonymCount
     *            new value for synonymCount
     */
    public void setSynonymCount(Integer synonymCount) {
        this.synonymCount = synonymCount;
    }

    /**
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
     * @return guardianConsent
     */
    @Basic
    @Column(name = "guardian_consent")
    public String getGuardianConsent() {
        return guardianConsent;
    }

    /**
     * @param guardianConsent
     *            new value for guardianConsent
     */
    public void setGuardianConsent(String guardianConsent) {
        this.guardianConsent = guardianConsent;
    }

    /**
     * Get the list of CloudName
     */
    @OneToMany(mappedBy = "entityCloud", cascade = { CascadeType.ALL })
    public List<CloudName> getCloudNames() {
        return this.cloudNames;
    }

    /**
     * Set the list of CloudName
     */
    public void setCloudNames(List<CloudName> cloudNames) {
        this.cloudNames = cloudNames;
    }
}
