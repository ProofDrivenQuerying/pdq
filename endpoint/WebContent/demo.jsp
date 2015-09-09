<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="uk.ac.ox.cs.pdq.cost.CostParameters.CostTypes"%>
<%@page import="uk.ac.ox.cs.pdq.planner.PlannerParameters.PlannerTypes"%>
<%@include file="head.jsp" %>
<body>
	<div id="content">
	    <%@include file="header.jsp" %>
	    <%@include file="menu.jsp" %>
	    <div id="main">
		<form action="plan" method="post" enctype="multipart/form-data" target="results">
		<fieldset>
			<table class="layout">
				<tr>
					<td>
						<label for="schema">Schema file</label> (<a title="Find out how to create a schema file." href="doc/schema.jsp" target="help">need help ?</a>)<br />
						<input type="file" value="" name="schema"/>
					</td>
					<td>
						<label for="query">Query file</label> (<a title="Find out how to create a query file." href="doc/query.jsp" target="help">need help ?</a>)<br />
						<input type="file" value="" name="query"/>
					</td>
					<td rowspan="4">
						<input id="plan" type="submit" value="Plan"/>
					</td>
				</tr>
				<tr>
					<td>
						<label for="timeout">Timeout</label><br />
						<input type="text" value="<%="\u221E"%>" name="timeout"/>
					</td>
					<td>
						<label for="max_iterations">Max. iterations</label><br />
						<input type="text" value="<%="\u221E"%>" name="max_iterations"/>
					</td>
				</tr>
				<tr>
					<td>
						<label for="planner">Planning algorithm</label><br/>
						<select name="planner" id="planner">
						<%
						for (PlannerTypes type: PlannerTypes.values()) {
							out.println("<option value=\"" + type + "\" >" + type + "</option>");
						}
						%>
						</select>
					</td>
					<td>
						<label for="blocking_interval">Blocking intervals</label><br />
						<input type="text" value="<%=1%>" name="blocking_interval"/>
					</td>
				</tr>
				<tr>
					<td>
						<label for="cost">Cost function</label><br/>
						<select name="cost" id="cost">
						<%
						for (CostTypes type: CostTypes.values()) {
							out.println("<option value=\"" + type + "\" >" + type + "</option>");
						}
						%>
						</select>
					</td>
					<td>
						<label for="query_match_interval">Matching intervals</label><br />
						<input type="text" value="<%=1%>" name="query_match_interval"/>
					</td>
				</tr>
			</table>
		</fieldset>
		</form>
		</div>
		<iframe name="results" scrolling="auto" seamless="true" frameborder="0" ></iframe>
	    <%@include file="footer.jsp" %>
	</div>
 </body>
</html>
