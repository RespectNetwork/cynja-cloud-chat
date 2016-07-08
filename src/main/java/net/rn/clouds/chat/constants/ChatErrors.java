/**
 * 
 */
package net.rn.clouds.chat.constants;

/**
 * @author Noopur Pandey
 *
 */
public enum ChatErrors {
	
	SYSTEM_ERROR(500, "Sorry! Unable to process your request. Please contact customer support or try again after sometime."),
	CLOUD_NOT_FOUND(9001, " cloud not found."),
    AUTHENTICATOION_FAILED(9002, "Authentication failed for "), 
    CONNECTION_REQUEST_NOT_FOUND(9003,"Connection request not found."),
    INVALID_CLOUD_PROVIDED(9004, "Invalid cloud provided "),
    CONNECTION_ALREADY_EXISTS(9005, "Connection already exists."),
    ALREADY_APPROVED(9006, "This connection is already approved."),
    NOT_AUTHORIZED_TO_APPROVE(9007,"You are not authorized to approve the connection request."),
    APPROVE_THE_CONNECTION_FIRST(9008,"You can block a connection once it is approved."),
    BLOCK_THE_CONNECTION_FIRST(9009,"This connection is already unblocked."),
    INVALID_CONNECTION_REQUEST(9010,"This connection request is invalid."),
    ALREADY_BLOCKED(9011, "This connection is already blocked."),
    NOT_AUTHORIZED_TO_UNBLOCK(9012,"You are not authorized to unblock the connection request."),
	PENDING_FOR_APPROVAL(9013,"This connection request already exists and pending for approval."),
	CONNECTION_BLOCKED(9014,"This connection is blocked.");
	
	/**
     * Error code.
     */
    private int errorCode;
    /**
     * Error message.
     */
    private String errorMessage;

    private ChatErrors(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * @return the errorCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode
     *            the errorCode to set
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage
     *            the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
