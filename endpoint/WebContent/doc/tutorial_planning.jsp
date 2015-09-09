<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@include file="../head.jsp" %>
<body>
    <div id="content">
        <%@include file="../header.jsp" %>
        <%@include file="../menu.jsp" %>
        <div id="main">
            <div id="example">
                <h2>Plan creation with PDQ</h2>
                <p>
					In order to run PDQ’s planner you have to create a configuration folder with the three following files: 
					schema.xml, query.xml and case.properties. The first two files describe the input schema and the input query, 
					while the third one specifies planning and logging properties. 
					You can find examples of configurations folders <a href="examples/examples_all.zip">here</a> 
					and inside the pdq-regression project, under directory "test".     
                	<br/>
                	The syntax to run the PDQ planner is 
                	<br/>
                	java –jar pdq-benchmark.jar planner –i configuration/folder/path [logging options] [planning options/cost options]
                	<h3>Logging options</h3>
                	<ul>
                		<li><b>-w</b> the best plan found is saved under the input configuration folder.</li>
                		<li><b>-W</b> all the plans that are built during planning are saved under the input configuration folder.</li>
                	</ul>
                	
                	<h3>Planning options</h3>
                	
                	<p>
                		The planning options are prefixed by –D and specify the properties of the planner that will eventually search the space of proofs/plans. 
                		Some of the most important options are listed below: 
                		<br/>
                		<b>-Dplanner_type=[LINEAR_GENERIC, LINEAR_OPTIMIZED, LINEAR_KCHASE, DAG_GENERIC, DAG_OPTIMIZED]</b>
                		
                	
                	    <ul>
	                		<li><b>LINEAR_GENERIC</b>: Linear planning algorithm that searches the space of plans/proofs exhaustively 
	                		and performs reasoning every planning step. This algorithm, as well as, the <b>LINEAR_OPTIMIZED</b> 
	                		algorithm is described in 
	                		
	                		<p>
	                		Michael Benedikt, Balder ten Cate, and Efthymia Tsamoura. "Generating low-cost plans from proofs", 
	                		In Proceedings of the 33rd ACM SIGMOD-SIGACT-SIGART symposium on Principles of database systems (PODS '14). 
	                		NY, USA, pp. 200-211, 2014. 
	                		</p>
	                		</li>
	                		
	                		<li><b>LINEAR_OPTIMIZED</b>: Linear planning algorithm that prunes the search space without compromising optimality. 
	                		It employs the "dominance", "success dominance", "equivalence" and "path post-pruning" optimisations. 
	                		Similar to <b>LINEAR_GENERIC</b> it performs reasoning every planning step. 
	                		You can turn off/on path post-pruning using the option <b>-Dpost_pruning_type=REMOVE_ACCESSES</b>. 
	                		By default, "path post-pruning" is turned on.
	                		</li>
	                		
	                		
	                		<li><b>LINEAR_KCHASE</b>: Linear planning algorithm that does reasoning every k planning steps. 
	                		It employs the "dominance" and "success dominance" optimisations also applied in the <b>LINEAR_OPTIMIZED</b> explorer 
	                		but not the "path post-pruning" optimisation. 
	                		</li>
	                		
	                		
	                		<li><b>DAG_GENERIC</b>: Bushy planning algorithm that is based on the dynamic programming paradigm and prunes down the search space
	                		employing the "dominance" and "success dominance" optimisations. This algorithm, as well as, the <b>DAG_OPTIMIZED</b> algorithm 
	                		is detailed described in 
	                		
	                		<p>
	                		Michael Benedikt, Julien Leblay, and Efthymia Tsamoura. "Querying with access patterns and integrity constraints", 
	                		Proceedings of the VLDB Endowment 8,6 pp. 690-701, 2015.
	                		</p>
	                		</li>
	                		
	                		
	                		<li><b>DAG_OPTIMIZED</b>: Bushy planning algorithm that is based on the dynamic programming paradigm and prunes down 
	                		the search space employing "dominance" and "success dominance" optimisations. The difference with the <b>DAG_GENERIC</b> 
	                		algorithms is that it performs reasoning in parallel. 
	                		</li>
	                	
                		</ul>
                		
                		<p>
                		
	                		When using the <b>DAG*</b> explorers you can specify the "dominance" (<b>-Ddominance_type</b>) and "success dominance" 
	                		(<b>-Dsuccess_dominance_type</b>) types to <b>OPEN</b> or <b>CLOSED</b>. <b>CLOSED</b> domination requires both input 
	                		configurations/plans that are compared to be closed (i.e., have no input parameters). <b>OPEN</b> does not impose this requirement 
	                		but it is not guaranteed to return the optimal bushy plan.  
							You can further restrict the type and shape of configurations/plans that the planner visits. 
							This is done through the <b>–Dvalidator_type</b> and <b>–Dfilter_type</b> options.
                			<br/>
                			<b>–Dvalidator_type=[DEFAULT_VALIDATOR,APPLYRULE_VALIDATOR, DEPTH_VALIDATOR, RIGHT_DEPTH_VALIDATOR, APPLYRULE_DEPTH_VALIDATOR, LINEAR_VALIDATOR]</b>
                		
	                		<ul>
		                		<li><b>DEFAULT_VALIDATOR</b>: No shape or type restriction.
		                		</li>
		                		
		                		<li><b>APPLYRULE_VALIDATOR</b>: Requires at least one of the input configurations to be an ApplyRule one.
		                		</li>
		                		
		                		<li><b>DEPTH_VALIDATOR</b>: Restricts the depth of the plans visited.
		                		</li>
		                		
		                		<li><b>RIGHT_DEPTH_VALIDATOR</b>: Restricts the depth of the RHS plans used.
		                		</li>
		                		
		                		<li><b>APPLYRULE_DEPTH_VALIDATOR</b>: Combination of APPLYRULE_VALIDATOR and DEPTH_VALIDATOR.	
		                		</li>
		                		
		                		<li><b>LINEAR_VALIDATOR</b>: Restricts the shape of plans to left-deep ones.
		                		</li>
		                	
	                		</ul>
	                		<br/>
	                		<b>–Dfilter_type=[FACT_DOMINATED_FILTER, NUMERICALLY_DOMINATED_FILTER]</b>
	                		
	                		<ul>
		                		<li><b>FACT_DOMINATED_FILTER</b>: Removes the fact dominated configurations after each exploration step.
		                		</li>
		                		
		                		<li><b>NUMERICALLY_FACT_DOMINATED_FILTER</b>: Removes the numerically fact dominated configurations after each exploration step.
		                		</li>
	                		</ul>
	                		
	                		
	                		<br/>
	                		<b>-Dcontrol_flow=[BOTTOM_UP, TOP_DOWN]</b>
	                		
	                		<ul>
		                		<li><b>BOTTOM_UP</b>: Pull control flow allowing tuples to move bottom-up only during execution.
		                		</li>
		                		
		                		<li><b>TOP_DOWN</b>: Pull control flow allowing tuples to move top-down and bottom-up during plan execution.	
		                		</li>
	                		</ul>
	                		
	                		
	                		<br/>
	                		<b>-Dreasoning_type=[BLOCKING_CHASE/ RESTRICTED_CHASE/ KTERMINATION_CHASE/ BOUNDED_CHASE] </b>
	                		
	                		<ul>
		                		<li><b>BLOCKING_CHASE</b>: Chase algorithm with blocking. It is used in cases where the chase algorithm does not terminate.
		                		</li>
		                		
		                		<li><b>RESTRICTED_CHASE</b>: Restricted chase algorithm as defined in 
		                		
		                		<p>
		                		Andrea Cali, Georg Gottlob, and Michael Kifer. “Taming the infinite chase: Query answering under expressive relational constraints”, In KR, pp.70-80, 2008.
		                		</p>
		                		</li>
		                		
		                		<li><b>KTERMINATION_CHASE</b>: Restricted chase algorithm, where the number of rule firing rounds is bounded by a constant K.	
		                		</li>
	                		</ul>
	                		
	                		<br/>
	                		We use a database to detect homomorphisms during chasing. By default, we use the Apache Derby database which is embedded within the pdq-benchmark.jar. 
	                		Users can also use  the MySQL database for this purpose; 
	                		however, in this case, they have to install MySQL in advance and specify the following parameters in the case.properties file       

                			<p>
	                			<br/>
	                			<b>-Dconnection_url=jdbc:mysql://localhost/</b>
	                			<br/>
								<b>-Ddatabase_name=[name of the chase database]</b>
								<br/>
								<b>-Ddatabase_user=[username]</b>
								<br/>
								<b>-Ddatabase_password=[password]</b>
                			</p>
                		</p>
                		
                		<p>
						The complete set of planning options can be found in the PlannerParameters.java file under the pdq-planner project.
                		</p>
                	
                	</p>
                	
                	<h3>Cost options</h3>
                	<p>
	                	The cost options are also prefixed by –D and specify the cost function that will be used to evaluate the cost of each plan found. 
	                	Some of the most important options are listed below.
	                	
	                	<br/>
	                	<b>-Dcost_type=[SIMPLE_CONSTANT, SIMPLE_RANDOM, SIMPLE_GIVEN, SIMPLE_COUNT, BLACKBOX, BLACKBOX_DB, INVERSE_LENGTH, SIMPLE_ERSPI] </b>
	                		
	                	<ul>
		                	<li><b>SIMPLE_CONSTANT</b>: Estimates the cost as the sum of the cost of all accesses in a plan, where access costs are provided externally.
		                	</li>
		                		
		                	<li><b>SIMPLE_RANDOM</b>: Estimates the cost as the sum of the cost of all accesses in a plan, where access costs are assigned randomly.
		                	</li>
		                	
		                	<li><b>SIMPLE_GIVEN</b>: Estimates the cost as the sum of the cost of all accesses in a plan, where access costs are measured automatically from the underlying datasources.
		                	</li>
		                	
		                	
		                	<li><b>SIMPLE_COUNT</b>: Estimates the cost as the sum of all accesses in the plan.
		                	</li>
		                		
		                	<li><b>BLACKBOX</b>: Estimates the cost using the function described in "Database Management System" by Ramakrishnan and Gehrke.
		                	</li>
		                	
		                	<li><b>INVERSE_LENGTH</b>: Estimates the cost as the number of atoms in a plan.
		                	</li>
		                	
		                	<li><b>SIMPLE_ERSPI</b>: Estimates the cost as the sum of the estimated result size per invocation associated to each access method used in a plan. 
		                	When using the <b>SIMPLE_ERSPI</b> cost function you must provide a file containing the relation/column cardinality statistics. 
		                	Examples using this cost function can be found <a href="examples/bio.zip">here</a> and under pdq-regression/test/dag/bio/SIMPLE_ERSPI. 
		                	In the other cost functions, the metadata can be embedded in the schema.xml files.
		                	</li>
		                	
		                	<li><b>BLACKBOX_DB</b>: When the data reside on PostgreSQL users do also have the option to estimate the cost by translating the query to SQL and asking its cost to PostgreSQL database. 
		                	When using this option, users should also specify the following parameters
		                	
                			<p>
	                			<br/>
	                			<b>-Dblack_box_connection_url=jdbc:postgresql://localhost/</b>
	                			<br/>
								<b>-Dblack_box_database_name=[name of the database where data reside]</b>
								<br/>
								<b>-Dblack_box_database_user=[username]</b>
								<br/>
								<b>-Dblack_box_database_password=[password]</b>
                			</p>
		                	</li>
		                	<br/>
		                	Examples using this cost function can be found <a href="examples/examples_blackbox_db_with.zip">here</a>.
	                	</ul>
                	</p>
                </p>
                
                  
              
            </div>
        </div>
        <%@include file="../footer.jsp" %>
    </div>
 </body>
</html>
