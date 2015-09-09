package uk.ac.ox.cs.pdq.endpoint;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.endpoint.util.SessionAttributes;

import com.google.common.base.Preconditions;

/**
 * This abstract servlet is a parent of all PDQ servlets.
 *  
 * @author Julien LEBLAY
 */
public abstract class PDQServlet extends HttpServlet {

	/** */
	private static final long serialVersionUID = -6537846117607060962L;

	/** Static logger */
	private static final Logger log = Logger.getLogger(PDQServlet.class);

	/** The session */
	protected HttpSession session = null;

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void processRequest(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		this.session = ((HttpServletRequest) request).getSession();
		this.session.removeAttribute(SessionAttributes.EXCEPTION);
		this.session.removeAttribute(SessionAttributes.MESSAGE);
	}
	
	public void returnError(HttpServletResponse response, String general) throws IOException {
		this.returnError(response, general, null);
	}
	
	public void returnError(HttpServletResponse response, String general, Exception exception) throws IOException {
		Preconditions.checkArgument(response != null);
		Preconditions.checkArgument(general != null);
		String message = "<span class=\"error\">" + general + "</span>";
		if (exception != null) {
			message += "<br/><span class=\"exception\">" +  exception.getMessage() + "</span>";
			if (exception.getCause() != null) {
				message += "<br/><span class=\"exception cause\">" +  exception.getCause() + "</span>";
			}
		}
		this.session.setAttribute(SessionAttributes.MESSAGE, message);
		response.sendError(500);
	}
}