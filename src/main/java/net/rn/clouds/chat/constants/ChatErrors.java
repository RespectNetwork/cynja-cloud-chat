/**
 * 
 */
package net.rn.clouds.chat.constants;

/**
 * @author Noopur Pandey
 *
 */
public enum ChatErrors {
	
	SYSTEM_ERROR(9000, "System error occured."),
	CLOUD_NOT_FOUND(9001, " cloud not found."),
    AUTHENTICATOION_FAILED(9002, "Authentication failed for "), 
    CONNECTION_REQUEST_NOT_FOUND(9003,"Connection request not found."),
    INVALID_CLOUD1_PROVIDED(9004, "Invalid cloud1 provided."),
    CONNECTION_ALREADY_EXISTS(9005, "Connection already exists."),
    ACTION_ALREADY_PERFORMED(9006, "This action is already performed."),
    UNAUTHORIZED_ACTION(9007,"You are not authorized to perform the action."),
    APPROVE_THE_CONNECTION_FIRST(9008,"You can not perform the action until the request is approved."),
    BLOCK_THE_CONNECTION_FIRST(9009,"You can not unblock until the request is blocked. ");
	
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
