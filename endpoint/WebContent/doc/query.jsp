<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@include file="../head.jsp" %>
<body>
    <div id="content">
        <%@include file="../header.jsp" %>
        <%@include file="../menu.jsp" %>
        <div id="main">
            <div id="example">
                <h2>How to create a query file</h2>
                <h3>Overview</h3>
				<p>
					Queries are expressed as rules in First-Order Logic (FOL).
					For instance, the query of the minimal example below, would be written in FOL as:
					<center>
						Q(y) &larr; S(5, y) 
					</center>
				</p>
				<!-- 
				<p>
					The query's body (right-hand side) must only refer to relations that have
					been defined in the <a href="doc/schema.jsp">schema</a>.
				</p>
				<p>
					The query's head (left-hand side) is made of a single atom,
					whose name is arbitrary and whose variable all appear in the body.
				</p>
				 -->
                <h4>Minimal example</h4>
                <pre>
&lt;?xml version="1.0" encoding="UTF-8"?>
&lt;query type="conjunctive">
    &lt;body>
        &lt;atom name="S">
            &lt;constant value="5" />
            &lt;variable name="y" />
        &lt;/atom>
    &lt;/body>
    &lt;head name="Q">
            &lt;variable name="y" />
    &lt;/head>
&lt;/query>

                </pre>
                <h4>Grammar</h4>
                	Informally, the query grammar goes as follows:
                	<pre>
query ::= body head

body ::= atom+      // Atoms must refer to relations defined in the schema.

head ::= atom

atom ::=
    @name           // Character string, the name of an atom must refer to a relation defined in the schema.
    (variable | constant)*
                    // The number of variables and constants have to match the relation's arity.
    
variable ::=
    @name           // Character string, must refer to variables declared in the body.
    
constant ::=
    @value          // Character string, in the query body, values have to be consistent
                    // with the corresponding attribute's type.
                	</pre>
            </div>
        </div>
        <%@include file="../footer.jsp" %>
    </div>
 </body>
</html>
