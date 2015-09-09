<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page language="java" session="true" %>
<%@page import="uk.ac.ox.cs.pdq.endpoint.util.SessionAttributes"%>
<%@include file="/error/head.jsp" %>
<body>
    <%
    Object message = session.getAttribute(SessionAttributes.MESSAGE);
    if (message != null) {
    	out.println(message);
    } else {
       	out.println("An unidentified error occurred. Please report to website administrators.");
    }
    %>
</body>
</html>
