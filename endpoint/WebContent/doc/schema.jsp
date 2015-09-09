<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@include file="../head.jsp" %>
<body>
    <div id="content">
        <%@include file="../header.jsp" %>
        <%@include file="../menu.jsp" %>
        <div id="main">
            <div id="example">
                <h2>How to create a schema file</h2>
                <h3>Overview</h3>
                <p>
                	This page describes how to create your own XML schema
                	definition to file to be imported into PDQ.
                	<br/>
                	Essentially, a <span class="code">schema</span> comprises two part:
                	<ul>
                		<li>a list of <span class="code">relations</span>, where each <span class="code">relation</span> featuring <span class="code">attributes</span> and <span class="code">access restrictions</span>,</li>
                		<li>a list of <span class="code">dependencies</span>, where each <span class="code">dependency</span> is defined as a logical rule.</li>
                	</ul>
                </p>
                <p>
                	The planner may be used in a stand-alone fashion. This means
                	relations defined in a schema do not have to refer to 
                	any existing table is an RDBMS or web services.
                	However, as you can imagine, plans created over such relations cannot be executed, 
                	and may online be used, for example, to compare plans with one another or their costs. 
                </p>
                <p>
                	Here is an mininal example of such a schema file.
                	The following section gives more details on how to create
                	schemas over actual relations and fine-tuned parameters,
                	such as costs and cardinalities.  
                </p>
                <h4>Minimal example</h4>

                <pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;schema&gt;
    &lt;!-- Relations are declared here --&gt;
    &lt;relations&gt;
        &lt;!-- This relation has 3 attributes and 2 access methods &gt;
        &lt;relation name="R"&gt;
            &lt;attribute name="a" type="java.lang.Integer" /&gt;
            &lt;attribute name="b" type="java.lang.String" /&gt;
            &lt;attribute name="c" type="java.lang.Double" /&gt;
            &lt;access-method name="m1" type="FREE" cost="100" /&gt;
            &lt;access-method name="m2" type="LIMITED" inputs="1,3" "cost="5" /&gt;
        &lt;/relation&gt;
        &lt;!-- This relation has 2 attributes and is inaccessible &gt;
        &lt;relation name="S"&gt;
            &lt;attribute name="d" type="java.lang.Double" /&gt;
            &lt;attribute name="e" type="java.lang.Integer" /&gt;
        &lt;/relation&gt;
    &lt;/relations&gt;
    &lt;!-- Dependencies are declared here &gt;
    &lt;dependencies&gt;
        &lt;!-- This rule defines a foreign key dependency between R(c) and S(d) --&gt;
        &lt;dependency&gt;
            &lt;body&gt;
                &lt;atom name="R"&gt;
                    &lt;variable name="x" /&gt;
                    &lt;variable name="y" /&gt;
                    &lt;variable name="z" /&gt;
                &lt;/atom&gt;
            &lt;/body&gt;
            &lt;head&gt;
                &lt;atom name="S"&gt;
                    &lt;variable name="z" /&gt;
                    &lt;variable name="w" /&gt;
                &lt;/atom&gt;
            &lt;/head&gt;
        &lt;/dependency&gt;
    &lt;/dependencies&gt;
&lt;/schema&gt;
                    
                </pre>
                <h3>Advanced schemas</h3>
                <h4>File grammar</h4>
                	A grammar is worth a thousand word, so let us start with 
                	this informal one:
                	<pre>
schema ::= relations dependencies

relations ::= relation*

dependencies ::= dependency*

relation ::=
    @name           // Character string, the name of a relation must be unique across a given schema.
    @source?        // Optional. If the relation refer to an actual table/service. See <a href="doc/schema.jsp#sources">details</a>.
    @size?          // Optional. Defines a fixed cardinality for the relations. See <a href="doc/schema.jsp#cost">details</a>.
    attribute*      // This list of attribute is only required if the relation is virtual,
                    // i.e. if attribute @source is not present.
    access-method*  // If no access-method list is defined, the relation is considered inaccessible.

attribute ::=
    @name           // Character string, the name of an attribute must be unique across a given relation.
    @type           // Attribute types are defined as a java canonical class name.
                    // Currently, the system has been tested for java.lang.String
                    // and classes implementing the java.lang.Number interface.

access-method ::=
    @name           // Character string, the name of an access-method must be unique across a given relation.
    @type           // Must be one of FREE, LIMITED, BOOLEAN
    @inputs?        // Required if type is different from FREE.
                    // Must be specified as a comma-separated list of integers,
                    // referring to input position among the attributes list,  starting from 1.
                    // For instance, in the above example, access method "m2" defines the first
                    // and third attributes as required inputs.
    @cost?          // The cost of the access-method. The semantics of the cost depends on the cost function used.
                    // See <a href="doc/schema.jsp#cost">details</a>.

dependency ::= body head

body ::= atom+

head ::= atom+

atom ::=
    @name           // Character string, the name of an atom must refer to a relation defined in the schema.
    (variable | constant)*
                    // The number of variables and constants have to match the relation's arity.
    
variable ::=
    @name           // Character string, repeated variable indicate joins.
    
constant ::=
    @value          // Character string, the value has to be consistent with the corresponding attribute's type.
                	</pre>

				<a id="sources"></a>				
				<h4>External data source</h4>
				<p>
					PDQ allows defining schemas over relations that refer to 
					actual tables in an RDBMS or online services.
				</p>
				<p>
					This is achieved by specified a "source" attributes for a relation along with its names.
					Currently, all available sources are described <a href="doc/sources.jsp">here</a>.
					If you would like to add more sources, feel free to tell <a href="mailto:pdq@cs.ox.ac.uk">us</a>.
				</p>
				<p>
					When a relation is defined over an existing source,
					there is no need to redefined its attributes in the schema file.
					If a relation refers to a database table or has at least one free access method,
					you may redefine any access method of the relation,
					for example if you want to experiments various scenarios. 
					If the relation has no free access, you only redefined in
					the schema file a subset of the available access methods,
					which are going to be used in effect by the planner. 
				</p>
				<p>
					Example
					... 
				</p>
				

				<a id="cost"></a>				
				<h4>Cost and cardinalities</h4>
				<p>
					When a relation is defined over an existing data source,
					PDQ will attempt to gather some metadata from it
					(such as cardinality, latency, etc.) mainly for cost computation.
					This is a rather simply process if the relations correspond
					to a table in a local databases, but it might be very costly
					if they refer to only services.
				</p>
				<p>
					To overcome this, you must specify explicitly a cardinality for some relation,
					or a cost for some access-method, typically when the relation is not local.
					If no information is available and no explicit value is defined for	either of these,
					PDQ will fall back to some predefined constant values.
					In practice, this means that undetermined cardinalities and
					access costs are given the same "weight" during the planning phase.
				</p>
				<p>
					The semantics of access costs depends on the cost function used.
					For SIMPLE_CONSTANT, the cost is taken a constant value that
					does not depends on any external or internal factors.
					In particular, no assumption is made on the size of the
					inputs and outputs of the access.
					For the default BLACKBOX cost function, the value is 
					interpreted as an per-input-tuple access cost.
					PDQ also allows to fully delegate the cost computation to an
					external system. This is possible for instance when all relations
					are located in a PostgreSQL database and BLACKBOX_DB is used.
					In such a case, any access cost defined in the schema file is
					disregarded. 
				</p>

				<a id="dependencies"></a>				
				<h4>Dependencies</h4>
				<p>
					Dependencies (or integrity constraints) are expressed as rules in First-Order Logic (FOL).
					For instance, the rule of the minimal example above, would typically be written in FOL as:
					<center>
						&forall; z R(x, y, z) &rArr; &exist;w S(z, w) 
					</center>
				</p>
				<p>
					PDQ supports a very large class of constraints,
					known as Tuple-Generating Dependencies (TDG),
					which can be expressed using such rules.
				</p>
				<p>
					Inclusion dependencies (a.k.a foreign key constraints),
					of which the rule above is an example, correspond to
					rules with a single atoms on each side.
				</p>
				<p>
					You may also defined views in the schema, by means of two (symmetric) rules.
					For instance, assuming a new relation <span class="code">T</span> is defined in the schema, 
					the following rules defined it as a view of a join between <span class="code">R</span> and <span class="code">S</span>. 
					<center>
						&forall; x, y, w T(x, y, w)  &rArr; R(x, y, z) &wedge; S(z, w) 
					</center>
					<center>
						&forall; x, y, w R(x, y, z) &wedge; S(z, w) &rArr; T(x, y, w)  
					</center>
				</p>
            </div>
        </div>
        <%@include file="../footer.jsp" %>
    </div>
 </body>
</html>
