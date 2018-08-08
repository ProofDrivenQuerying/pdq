package uk.ac.ox.cs.pdq.runtime;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;
import uk.ac.ox.cs.pdq.util.Table;

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

	private AccessRepository repository;

	/**
	 * Constructor for Runtime.
	 * 
	 * @param params
	 *            RuntimeParameters
	 * @param schema
	 *            Schema
	 */
	public Runtime(RuntimeParameters params, Schema schema) {
		super();
		this.params = params;
		this.schema = schema;
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
	public Table evaluatePlan(RelationalTerm p) throws Exception {
		AccessRepository repo = this.repository;
		if (repo == null)
				repo = AccessRepository.getRepository();
		try {
			ExecutablePlan executable = new PlanDecorator(repo,schema).decorate(p);
			System.out.println("Executing plan " + p.hashCode());
			Table res = executable.execute();
			System.out.println("plan " + p.hashCode() + " finished.");
			return res;
		}catch(Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	public RuntimeParameters getParams() {
		return params;
	}

	public void setAccessRepository(AccessRepository repository) {
		this.repository = repository;
		
	}
}
