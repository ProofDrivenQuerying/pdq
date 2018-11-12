package uk.ac.ox.cs.pdq.planner.dag.explorer.filters;

/**
 * @author Efthymia Tsamoura
 * This package contains classes that given a set of DAG configurations 
 * they filter out the configurations that do not satisfy certain criteria. 
 * The DominationFilter class filters out the fact dominated configurations.
 * A configuration c and c' is fact dominated by another configuration c' 
 * if there exists an homomorphism from the facts of c to the facts of c' and the input constants are preserved.
 * 
 */
