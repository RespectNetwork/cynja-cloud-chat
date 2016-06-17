/**
 * 
 */
package net.rn.clouds.chat.exceptions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Noopur Pandey
 *
 */
public class ChatErrorHandler extends HttpServlet{

	public void doGet(HttpServletRequest request,
            HttpServletResponse response)
    throws ServletException, IOException{
		
		processError(request, response);
	}
	
	public void doPost(HttpServletRequest request,
            HttpServletResponse response)
    throws ServletException, IOException{
		
		processError(request, response);
		
	}
	
	private void processError(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        Throwable throwable = (Throwable) request
                .getAttribute("javax.servlet.error.exception");       
        
        response.setContentType("text/html");
		 
	    PrintWriter out = response.getWriter();	    
	    out.println(throwable.toString());
	}
}
