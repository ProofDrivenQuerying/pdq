/**
 * Top-level package for the runtime project.
 * 
 * The point of entry to the package and project is the {@code Runtime} class.
 * A {@code Runtime} is typically initialized with a schema and additional 
 * parameters.
 * Then, external plans (either read from files or coming from a planner) can
 * be executes from the {@code Runtime}. Various execution method may be used 
 * for a given plan, which depends on the plan itself, how and where the data 
 * is stored and external parameters.
 *  
 * {@code Bootstrap} is the default executable class for the whole library.
 */
package uk.ac.ox.cs.pdq.runtime;