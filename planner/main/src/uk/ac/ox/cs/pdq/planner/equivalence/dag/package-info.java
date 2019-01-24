package uk.ac.ox.cs.pdq.planner.equivalence.dag;

/**
 * This package contains classes that store fact-equivalent configurations. 
 * The definition of fact-equivalent configurations is given below: A mapping h from the chase constants of one configuration
 * conf to the chase constants of another configuration conf'
 * is fact-preserving if it preserves inferred accessible output facts
 * in going from conf to conf' and if the h image of every input
 * constant of conf is an input constant of conf'. Configurations conf, conf' are fact-equivalent 
 * if there is a bijective fact-preserving mapping h between them.
 * The classes support operations related to adding and removing fact-equivalent configurations
 * and can work in multi-threaded environments, where different threads are adding/removing configurations belonging to the same class. 
 * 
 * @author Efthymia Tsamoura  
 */
	

