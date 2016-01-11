package uk.ac.ox.cs.pdq.planner.dag.cost;

/**
	@author Efthymia Tsamoura

	This package contains classes to evaluate the cost of DAG configurations directly without 
	having first to transform a configuration to a plan. 
	The cost evaluation is delegated to Postgres: the DAG configuration is transformed to an SQL query 
	which is then analysed by the Postgres cost estimation functions.    
	 
**/