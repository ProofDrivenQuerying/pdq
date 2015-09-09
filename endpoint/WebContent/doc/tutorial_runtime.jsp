<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@include file="../head.jsp" %>
<body>
    <div id="content">
        <%@include file="../header.jsp" %>
        <%@include file="../menu.jsp" %>
        <div id="main">
            <div id="example">
                <h2>Plan execution with PDQ</h2>
                <p>
					Similar to planning in order to run PDQ’s runtime you have to create a configuration
					folder with the following files: schema.xml, query.xml, plan.xml and case.properties. 
					The plan.xml contains the plan found during planning, while the case.properties file 
					specifies the runtime parameters.  
                	<br/>
                	The syntax to run the PDQ runtime is 
                	<br/>
                	java –jar pdq-benchmark.jar runtime –i configuration/folder/path [logging options] [runtime options]
                	
                	
                	<h3>Runtime options</h3>
                	
                	<p>
                		<b>-Dexecutor_type=[PIPELINED, SQL_TREE, SQL_STEP, SQL_WITH]</b>
                		
                	
                	    <ul>
	                		<li><b>PIPELINED</b>: Volcano-style pipelining iterator execution, where all operators are implemented in the middleware. 
	                		When using the <b>PIPELINED</b> plan executor, users do also have the option to specify the number of output tuples 
	                		using the <b>-Dtuples_limit option</b>. 
	                		</li>
	                		
	                		<li><b>SQL_TREE</b>: Executes a query by translating it to a nested SQL query and delegating its execution to PostgreSQL.
	                		</li>
	                		
	                		
	                		<li><b>SQL_STEP</b>: Executes a query by translating it to a sequence of SQL queries and delegating its execution to PostgreSQL. 
	                		Each query is materialized and possibly relies on a previously materialized one.
	                		</li>
	                		
	                		
	                		<li><b>SQL_WITH</b>: Executes a query by translating it to a SQL WITH query and delegating its execution to PostgreSQL. 
	                		</li>
	                	
                		</ul>
                		
                	</p>
                	
                </p>
                
                  
              
            </div>
        </div>
        <%@include file="../footer.jsp" %>
    </div>
 </body>
</html>
