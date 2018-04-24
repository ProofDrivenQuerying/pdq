/**
 * Contains classes in charges are executing plans, i.e. producing the output 
 * of a plan, given some data instance. This is typically done via the
 * {@code Plan Executor} interface.
 * 
 * Since the data instance be stored in different forms (e.g. in-memory,
 * an underlying RDMS), each implementation of the {@code PlanExecutor} is 
 * dedicated to executing plans with a specific method, and different 
 * assumptions about the data.
 * 
 * For instance, the {@code VolcanoPlanExecutor} will produce a physical plan 
 * as a tree of tuple iterator 
 * (see package {@code uk.ac.ox.cs.pdq.runtime.exec.iterator})
 * and execute it regardless of where/how the data is stored.
 * 
 * SQL-Executors will assume the data is stored in some RDBMS. From there, an
 * executor might either translate a plan directly in SQL, or split the plan
 * into steps, materializing the result of each step and running the next on
 * top of the previous ones.
 */
package uk.ac.ox.cs.pdq.runtime.exec;