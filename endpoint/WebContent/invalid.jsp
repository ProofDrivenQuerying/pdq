<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@include file="head.jsp" %>
<body>
    <h1>Error</h1>
    <div>
    <%
    if (session.getAttribute("exception") != null) {
       	Exception ex = ((Exception) session.getAttribute("exception"));
       	ex.printStackTrace();
       	out.write(ex.getMessage());
       	Throwable cause = ex.getCause();
       	if (cause != null) {
           	out.write(cause.getMessage());
       	}
    } else if (session.getAttribute("message") != null) {
        %><%=(session.getAttribute("message")) %><%
    } else {
        %>"<%=session.getAttribute("query")%>" is not a valid query.<%
    }
    %>
    </div>
    <%@include file="footer.jsp" %>
</body>
</html>
