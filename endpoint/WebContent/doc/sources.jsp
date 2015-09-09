<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="uk.ac.ox.cs.pdq.builder.SchemaDiscoverer"%>
<%@page import="uk.ac.ox.cs.pdq.db.AccessMethod.AccessMethodTypes"%>
<%@page import="uk.ac.ox.cs.pdq.db.AccessMethod"%>
<%@page import="uk.ac.ox.cs.pdq.db.Attribute"%>
<%@page import="uk.ac.ox.cs.pdq.db.Relation"%>
<%@page import="uk.ac.ox.cs.pdq.db.Schema"%>
<%@page import="uk.ac.ox.cs.pdq.util.Types"%>
<%@page import="uk.ac.ox.cs.pdq.endpoint.util.ServletContextAttributes"%>
<%@page import="java.util.Map"%>
<%@include file="../head.jsp" %>
<body>
    <div id="content">
        <%@include file="../header.jsp" %>
        <%@include file="../menu.jsp" %>
        <div id="main">
            This pages lists out all pre-defined sources that may be used when
            defining <a href="doc/schema.jsp">schema</a> files.
            <ul>
            <%
            Map<String, SchemaDiscoverer> sources = (Map) session.getServletContext().getAttribute(ServletContextAttributes.SOURCES);
            for (String name: sources.keySet()) {
            	%>
				<li>
					<a href="doc/sources.jsp#<%=name%>" class="top" id="<%=name%>"><%=name%></a>
		 			<div class="list">
					<ul class="hide">
					<%
					SchemaDiscoverer disco = sources.get(name);
					Schema schema = disco.discover();
					for (Relation r: schema.getRelations()) {
						%>
						<li>
							<%=r.getName()%>
							<br/>
							<table class="display">
								<tr><th>Attribute name</th><th>Attribute type</th></tr>
							<%
							for (Attribute att: r.getAttributes()) {
								%>
								<tr><td><%=att.getName()%></td><td><%=Types.simpleName(att.getType())%></td></tr>
								<%
							}
							%>
							<%
								if (!r.getAccessMethods().isEmpty()) {
							%>
									<tr><th>Access method name</th><th>Required inputs</th></tr>
									<%
									if (r.getAccessMethods().isEmpty()) {
										%><tr>
										<td><%="Inaccessible"%></td>
										</tr><%
									} else {
										for (AccessMethod b: r.getAccessMethods()) {
											%>
												<tr>
													<td><%=b.getName() %></td>
													<td>
													<%
													if (b.getType() == AccessMethodTypes.FREE) {
														out.println("None");
													} else {
														String sep = "";
														for (Integer i: b.getInputs()) {
															out.print(sep + r.getAttribute(i - 1));
															sep = ", ";
														}
													}
													%>
													</td>
												</tr>
												<%
											}
									}
							}
							%>
							</table>
						</li>
						<%
					}
					%>
					</ul>
					</div>
				</li>            	
            	<%
            }
            %>
            </ul>
        </div>
        <%@include file="../footer.jsp" %>
    </div>
 </body>
</html>
