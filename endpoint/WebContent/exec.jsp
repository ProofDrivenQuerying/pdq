<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="java.util.Map"%>
<%@page import="java.util.concurrent.Future"%>
<%@page import="uk.ac.ox.cs.pdq.fol.Term"%>
<%@page import="uk.ac.ox.cs.pdq.endpoint.util.PlanningSession"%>
<%@page import="uk.ac.ox.cs.pdq.db.Schema"%>
<%@page import="uk.ac.ox.cs.pdq.runtime.RuntimeParameters"%>
<%@page import="uk.ac.ox.cs.pdq.runtime.RuntimeParameters.ExecutorTypes"%>
<%@page import="uk.ac.ox.cs.pdq.endpoint.util.RequestParameters"%>
<%@page import="uk.ac.ox.cs.pdq.fol.Query"%>
<%@page import="uk.ac.ox.cs.pdq.endpoint.util.HTMLTuplePrinter"%>
<%@page import="uk.ac.ox.cs.pdq.plan.Plan"%>
<%@page import="uk.ac.ox.cs.pdq.fol.ConjunctiveQuery"%>
<%@page import="uk.ac.ox.cs.pdq.runtime.Runtime"%>
<%@page import="uk.ac.ox.cs.pdq.endpoint.util.SessionAttributes"%>
<%@include file="head.jsp" %>
<body>
	<div id="content">
	    <%@include file="header.jsp" %>
	    <%@include file="menu.jsp" %>
	    <div id="main">
	    	<%
	    		// Fetched the planning session id
	    	    		String planningSession = request.getParameter(RequestParameters.PLANNING_SESSION);
	    	    		if (planningSession == null) {
	    	    			out.println("Unable to find plan file.");
	    	    		}
	    	    		// Among all available futures...
	    	    		Map<String, PlanningSession> sessions = (Map) session.getAttribute(SessionAttributes.PLANNING_SESSIONS);
	    	    		if (sessions == null) {
	    	    			out.println("Unable to find plan file.");
	    	    		}
	    	    		// ... retrieve the one with the right session id...
	    	    		PlanningSession pSession = sessions.get(planningSession);
	    	    		if (pSession == null) {
	    	    			out.println("Unable to find plan file.");
	    	    		}
	    	    		// ... and get the plan.
	    	    		Plan plan = pSession.getFuture().get();
	    	    		if (plan == null) {
	    	    			out.println("Unable to find plan file.");
	    	    		}
	    	    		Query<?> query = pSession.getQuery();
	    	    		long start = System.currentTimeMillis();
	    	%>
			
			
			<table class="display">
			<tr>
				<%
					for (Term t: query.getFree()) {
							out.println("<th>" + t + "</th>");
						}
				%>
			</tr>
			<%
				RuntimeParameters params = new RuntimeParameters();
			    		params.setExecutorType(ExecutorTypes.PIPELINED);
			    		Runtime runtime = new Runtime(params, pSession.getSchema());
			    		runtime.registerEventHandler(new HTMLTuplePrinter(out));
				    	runtime.evaluatePlan(plan, pSession.getQuery());
			%>
			</table>
	    	Evaluation time: <%=System.currentTimeMillis() - start %> ms.
		</div>
	    <%@include file="footer.jsp" %>
	</div>
 </body>
</html>
