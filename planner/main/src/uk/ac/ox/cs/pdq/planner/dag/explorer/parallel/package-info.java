package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

/**
 * @author Efthymia Tsamoura
 * This package contains classes to support reasoning and proof exploration in parallel.
 * The IterativeExecutor interface is entry point of this package and defines methods that set up the environment to run the reasoning and exploration operations in parallel.   
 * The MultiThreadedExecutor class implements the IterativeExecutor interface using multi-threading.
 * 
 * The ReasoningThread classes and ExplorationThread classes, implement the threads that run in parallel and do parallel reasoning and proof exploration.      
 *    
 * The ReasoningThread creates new binary configurations.
 * Given two lists of configurations L and R, the algorithm creates a new binary configuration c=Binary(l,r) by 
 * taking one configuration l from L and one configuration r from R.
 * First, the thread estimates the cost of c, without saturating it.
 * If the cost of c is lower than the cost of the best plan found so far, c is saturated using the chase algorithm. 
 * Otherwise, c is dropped.
 * 
 * The ExplorationThread iterates over the input collection of configurations to identify the minimum-cost one.
 * Given a set if input configuration C it removes from C the dominated and success dominated configurations 
 * and returns the minimum cost configurations.
 */
	