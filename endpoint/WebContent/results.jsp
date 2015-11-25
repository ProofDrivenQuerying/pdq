<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page contentType="text/html; charset=UTF-8" %>
<%@page import="uk.ac.ox.cs.pdq.plan.LeftDeepPlan"%>
<%@page import="uk.ac.ox.cs.pdq.io.pretty.AlgebraLikeLinearPlanWriter"%>
<%@page import="uk.ac.ox.cs.pdq.endpoint.util.PlanningSession"%>
<%@page import="uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor"%>
<%@page import="uk.ac.ox.cs.pdq.runtime.exec.Middleware"%>
<%@page import="uk.ac.ox.cs.pdq.endpoint.util.BufferedProgressLogger"%>
<%@page import="uk.ac.ox.cs.pdq.EventHandler"%>
<%@page import="java.io.PrintStream"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.util.concurrent.TimeoutException"%>
<%@page import="java.util.concurrent.TimeUnit"%>
<%@page import="uk.ac.ox.cs.pdq.plan.Plan"%>
<%@page import="java.util.concurrent.Future"%>
<%@page import="java.util.Map"%>
<%@page import="uk.ac.ox.cs.pdq.endpoint.util.RequestParameters"%>
<%@page import="uk.ac.ox.cs.pdq.endpoint.util.SessionAttributes"%>
<head>
	<meta http-equiv="content-type" content="application/xhtml+xml; charset=UTF-8"/>
	<link rel="stylesheet" type="text/css" href="style/embedded.css" />
</head>
<body>
	<p>
    <%
    Object message = request.getAttribute(SessionAttributes.MESSAGE);
    if (message != null) {
    	out.println(message);
    }
    %>
	</p>
	<p>
	<%
    String planningSessionId = request.getParameter(RequestParameters.PLANNING_SESSION);
    if (planningSessionId != null) {
    	Map<String,PlanningSession> plannings = (Map) session.getAttribute(SessionAttributes.PLANNING_SESSIONS);
    	PlanningSession pSession = plannings.get(planningSessionId);
    	Plan plan = null;
    	try {
        	plan = pSession.getFuture().get(1, TimeUnit.SECONDS);
    	} catch (TimeoutException e) {}
    	%>
    	<table>
	   		<%=pSession.getLogger().getLog()%>
    	</table>
    	<br/>
    	<%if (plan != null) {
       		ByteArrayOutputStream bos = new ByteArrayOutputStream();
       		AlgebraLikeLinearPlanWriter.to(new PrintStream(bos)).write((LeftDeepPlan) plan);
       		%>

       		<span class="title">Plan:</span><br/>
       		<%=bos.toString("UTF-8").replaceAll("\n", "<br/>")%>
   			<br/>

   			<span class="title">Cost:</span><br/>
   			<%=plan.getCost()%>

            <br/><a href="download?<%=RequestParameters.PLANNING_SESSION%>=<%=session.getAttribute(SessionAttributes.LAST_PLANNING_SESSION)%>">Download XML</a>
            <br/><a href="exec.jsp?<%=RequestParameters.PLANNING_SESSION%>=<%=session.getAttribute(SessionAttributes.LAST_PLANNING_SESSION)%>" target="execution">Execute the plan</a>
    		
        <%
       	} else if (pSession.getFuture().isDone()) {
			%><span>No plan found.</span><%
		} else {%>
          	<script>
          	setTimeout(function(){
          		   window.location.reload(1);
          		}, 1000);
          	</script>
        <%}
    }
	%>
	</p>
</body>
</html>
