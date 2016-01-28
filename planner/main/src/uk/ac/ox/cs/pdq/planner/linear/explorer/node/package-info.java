package uk.ac.ox.cs.pdq.planner.linear.explorer.node;

/**
 * 	The linear plans that are visited during exploration are organised into a tree. 
	The nodes of this tree correspond to (partial) linear configurations. 
	This package contain classes to implement tree nodes. We have two different plan node types depending on the cost function
	that is used to evaluate the costs of plans found during exploration:
	-Simple nodes are used when costing plans using a simple cost functions. These nodes keep a single path to success.
	-Blackbox nodes are used when costing plans using a blackbox cost functions. These nodes keep every path to success.
	@author Efthymia Tsamoura
 */
	