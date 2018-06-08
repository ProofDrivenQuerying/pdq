package uk.ac.ox.cs.pdq.runtime;

import java.util.Collection;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.datasources.utility.Result;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;

/**
 *  Decorates a plan, and executes queries or the plan itself. 
 * @author gabor
 *
 */
public class Runtime {

	/** Runtime's parameters. */
	private RuntimeParameters params;

	/** Runtime's internal schema. */
	private Schema schema;

	/** In-memory facts. */
	private Collection<Atom> facts;

	/**
	 * Constructor for Runtime.
	 * 
	 * @param params
	 *            RuntimeParameters
	 * @param schema
	 *            Schema
	 * @param facts
	 *            List<PredicateFormula>
	 */
	public Runtime(RuntimeParameters params, Schema schema, Collection<Atom> facts) {
		super();
		this.params = params;
		this.schema = schema;
		this.facts = facts;
	}

	/**
	 * Constructor for Runtime.
	 * 
	 * @param params
	 *            RuntimeParameters
	 * @param schema
	 *            Schema
	 */
	public Runtime(RuntimeParameters params, Schema schema) {
		this(params, schema, null);
	}

	/**
	 * Evaluates the given plan and returns its result.
	 *
	 * @param p
	 *            Plan
	 * @param query
	 *            Query
	 * @return the result of the plan evaluation.
	 * @throws EvaluationException
	 *             the evaluation exception
	 */
	public Result evaluatePlan(RelationalTerm p) throws Exception {
		AccessRepository repo = AccessRepository.getRepository();
		try {
			ExecutablePlan executable = new PlanDecorator(repo,schema).decorate(p);
			Table res = executable.execute();
			return res;
		}catch(Throwable t) {
			t.printStackTrace();
		}
		
		throw new Exception("Plan evaluation is not implemented yet.");
	}

	/**
	 * Evaluates the given query, and returns its result.
	 *
	 * @param query
	 *            the query
	 * @return the result of the query evaluation.
	 * @throws EvaluationException
	 *             the evaluation exception
	 */
	public Result evaluateQuery(ConjunctiveQuery query) throws Exception {
		throw new Exception("Query evaluation is not implemented yet.");
	}

	public RuntimeParameters getParams() {
		return params;
	}

	public Collection<Atom> getFacts() {
		return facts;
	}
	
}
