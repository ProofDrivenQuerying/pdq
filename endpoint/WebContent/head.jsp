<head>
	<%@page contentType="text/html; charset=UTF-8" %>
	<meta http-equiv="content-type" content="application/xhtml+xml; charset=UTF-8"/>
	<title>PDQ Endpoint</title>
	<%
	if (request.getLocalAddr().equals(request.getRemoteAddr())) {
		out.println("<base href=\"http://localhost:8081/pdq/\" />");
	} else {
		out.println("<base href=\"http://www.cs.ox.ac.uk/pdq/\"/>");
	}
	%>
	<meta name="Copyright" content="Copyright &copy; 2014 Oxford University" />
	<meta name="Keywords" content="PDQ, endpoint" />
	<link rel="stylesheet" type="text/css" href="style/default.css" />
</head>
