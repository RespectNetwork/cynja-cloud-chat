/**
 * 
 */
package net.rn.clouds.chat.exceptions;

/**
 * @author Noopur Pandey
 *
 */
public class ChatValidationException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
	
	private int errorCode;
    private String errorDescription;        

    public ChatValidationException() {    
    }
    
    public ChatValidationException(int errorCode, String errorDescription) {    	
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;        
    }
    
    public ChatValidationException(int errorCode, String errorDescription, Exception e) {    	
        super(e);
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;       
    }
    
    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Throwable#toString()
     */
    @Override
    public String toString() {
    	String message = "{"+this.errorDescription+"}";
    	return message;
    }

}
