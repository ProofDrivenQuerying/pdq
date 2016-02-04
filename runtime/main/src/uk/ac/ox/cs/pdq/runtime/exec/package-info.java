/**
 * This package features classed in charges are executing plans, i.e. producing
 * the output of a plan, given some data instance.
 * Since the data instance may take or be stored in different forms (in-memory,
 * an underlying RDMS), each implementation of the PlanExecutor is dedicated
 * to executing plans with a specific method, and different assumptions about
 * the data.
 * 
 * For instance, the VolcanoPlanExecution will produce a physical plan as a
 * tree of tuple iterator (see package uk.ac.ox.cs.pdq.runtime.exec.iterator)
 * and execute it regardless of where/how the data is stored.
 * 
 * SQL*Executor will assume the data is stored in some RDBMS. From there an
 * executor might either translate a plan directly in SQL, or split the plan
 * into steps, materializing the result of each step and running the next on
 * top of the previous ones.
 */
package uk.ac.ox.cs.pdq.runtime.exec;