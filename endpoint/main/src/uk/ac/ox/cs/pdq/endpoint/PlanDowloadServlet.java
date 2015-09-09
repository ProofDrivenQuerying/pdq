package uk.ac.ox.cs.pdq.endpoint;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.endpoint.util.PlanningSession;
import uk.ac.ox.cs.pdq.endpoint.util.RequestParameters;
import uk.ac.ox.cs.pdq.endpoint.util.SessionAttributes;
import uk.ac.ox.cs.pdq.io.xml.PlanWriter;
import uk.ac.ox.cs.pdq.plan.Plan;

/**
 * This servlet functions like an endpoint. It simply execute a given query on 
 * a given datasource (passed as parameters) and output the results as the 
 * response. 
 * @author Julien LEBLAY
 */
public class PlanDowloadServlet extends PDQServlet {

	/** */
	private static final long serialVersionUID = -5686246345051239612L;

	/** Static logger */
	private static final Logger log = Logger.getLogger(PlanDowloadServlet.class);

	@Override
	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.processRequest(request, response);
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.processRequest(request, response);

		try {
    		// Fetched the planning session id
    		String planningSession = request.getParameter(RequestParameters.PLANNING_SESSION);
    		if (planningSession == null) {
    			this.returnError(response, "Unable to find plan file.");
    			return;
    		}
    		// Among all available futures...
    		Map<String, PlanningSession> sessions = (Map) this.session.getAttribute(SessionAttributes.PLANNING_SESSIONS);
    		if (sessions == null) {
    			this.returnError(response, "Unable to find plan file.");
    			return;
    		}
    		// ... retrieve the one with the right session id...
    		PlanningSession pSession = sessions.get(planningSession);
    		if (pSession == null) {
    			this.returnError(response, "Unable to find plan file.");
    			return;
    		}
    		// ... and get the plan.
    		Plan plan = pSession.getFuture().get();
    		if (plan == null) {
    			this.returnError(response, "Unable to find plan file.");
    			return;
    		}
    		try (PrintStream ps = new PrintStream(response.getOutputStream())) {
        		PlanWriter.to(ps).write(plan);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    		this.returnError(response, "Unidentified error.");
    		return;
    	}
	}
}