package uk.ac.ox.cs.pdq.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.builder.SchemaDiscoverer;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.CostParameters.CostTypes;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.endpoint.util.BufferedProgressLogger;
import uk.ac.ox.cs.pdq.endpoint.util.PlanningSession;
import uk.ac.ox.cs.pdq.endpoint.util.RequestParameters;
import uk.ac.ox.cs.pdq.endpoint.util.ServletContextAttributes;
import uk.ac.ox.cs.pdq.endpoint.util.SessionAttributes;
import uk.ac.ox.cs.pdq.endpoint.util.WebBasedStatisticsLogger;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.Planner;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PlannerTypes;
import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;

/**
 * This servlet functions like an endpoint. It simply execute a given query on 
 * a given datasource (passed as parameters) and output the results as the 
 * response. 
 * @author Julien LEBLAY
 */
@WebServlet("/upload")
@MultipartConfig
public class PlannerServlet extends PDQServlet {

	/** */
	private static final long serialVersionUID = -2476124495066132007L;

	/** Static logger */
	private static final Logger log = Logger.getLogger(PlannerServlet.class);

	@Override
	public final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.processRequest(request, response);
	}

	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		super.processRequest(request, response);
		
		// Clean-up all existing planning sessions
		request.getSession().removeAttribute(SessionAttributes.PLANNING_SESSIONS);
		
        Part schemaFile = request.getPart(RequestParameters.SCHEMA);
        Part queryFile = request.getPart(RequestParameters.QUERY);
    	if (schemaFile.getSize() == 0 || queryFile.getSize() == 0) {
    		this.returnError(response, "Schema and query files are both required.");
    		return;
    	}
    	
    	Map<String, SchemaDiscoverer> disco = (Map) request.getServletContext()
    			.getAttribute(ServletContextAttributes.SOURCES);

    	// Parsing schema
		Schema schema = null;
    	try(InputStream sis = schemaFile.getInputStream()) {
    		schema = new SchemaReader(disco).read(sis);
    	} catch (ReaderException e) {
    		this.returnError(response, "Schema file could not be read.", e);
    		return;
    	}

    	// Parsing query
		final Query<?> query;
    	try(InputStream qis = queryFile.getInputStream()) {
			query = new QueryReader(schema).read(qis);
    	} catch (ReaderException e) {
    		this.returnError(response, "Query file could not be read.", e);
    		return;
    	}
    	
    	if (schema == null || query == null) {
    		this.returnError(response, "Schema and query files are both required.");
    		return;
    	}

       	try {
    		// Prepare planner
       		PlannerParameters plannerParams = new PlannerParameters();
       		CostParameters costParams = new CostParameters();
       		ReasoningParameters reasoningParams = new ReasoningParameters();
    		
       		plannerParams.setSeed(1);
    		plannerParams.setPlannerType(PlannerTypes.valueOf(request.getParameter(RequestParameters.PLANNER)));
    		plannerParams.setMaxIterations(request.getParameter(RequestParameters.MAX_ITERATIONS));
    		plannerParams.setTimeout(request.getParameter(RequestParameters.TIMEOUT));
    		plannerParams.setQueryMatchInterval(Integer.parseInt(request.getParameter(RequestParameters.QUERY_MATCH_INTERVAL)));
    		plannerParams.setLogIntervals(5);
    		plannerParams.setShortLogIntervals(1);
    		
    		costParams.setCostType(CostTypes.valueOf(request.getParameter(RequestParameters.COST)));
    		
    		reasoningParams.setBlockingInterval(Integer.parseInt(request.getParameter(RequestParameters.BLOCKING_INTERVAL)));

    		// Launch planner
    		try (WebBasedStatisticsLogger pLog = new WebBasedStatisticsLogger()) {
        		final Planner planner = new Planner(plannerParams, costParams, reasoningParams, schema, pLog);
        		EventHandler eventLogger = new IntervalEventDrivenLogger(pLog, 5, 10); 
        		planner.registerEventHandler(eventLogger);
        		String planningSessionId = Long.toHexString(System.nanoTime());
//        		planner.validate();

        		ExecutorService executor = Executors.newFixedThreadPool(1);
    			Future<Plan> future = executor.submit(new Callable<Plan>() {
    				@Override
    				public Plan call() {
    					try {
    						return planner.search(query);
    					} catch (PlannerException e) {
    						try {
        						PlannerServlet.this.returnError(response, "An error occured while planning", e);
        					} catch (IOException e2) {
        						e2.printStackTrace();
        						log.error(e2);
    						}
    						return null;
    					}
    				}
    			});
        		response.sendRedirect("results.jsp?" + RequestParameters.PLANNING_SESSION + "=" + planningSessionId);

        		this.savePlanningSession(planningSessionId, schema, query, future, pLog);
    		}
    	} catch (Exception e) {
    		this.returnError(response, "The planner could not be initialized.", e);
    		return;
    	}
	}
	
	/**
	 * Stores the given schema, query, future and logger in a new planning session
	 * and in the session under the given id.
	 * @param planningId
	 * @param schema
	 * @param query
	 * @param future
	 * @param logger
	 */
	private void savePlanningSession(String planningId, Schema schema, Query query, Future<Plan> future, BufferedProgressLogger logger) {
		this.session.setAttribute(SessionAttributes.LAST_PLANNING_SESSION, planningId);
		Map<String, PlanningSession> planners = (Map) this.session.getAttribute(SessionAttributes.PLANNING_SESSIONS);
		if (planners == null) {
			planners = new LinkedHashMap<>();
    		this.session.setAttribute(SessionAttributes.PLANNING_SESSIONS, planners);
		}
		planners.put(planningId, new PlanningSession(schema, query, future, logger));
	}
}