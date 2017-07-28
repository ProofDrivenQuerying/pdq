package uk.ac.ox.cs.pdq.runtime.exec;

import java.util.Properties;

import uk.ac.ox.cs.pdq.InconsistentParametersException;
import uk.ac.ox.cs.pdq.Parameters.EnumParameterValue;
import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters.ExecutorTypes;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Go to class for executing plan.
 * This class can be seen as a PlanExecutor factory, i.e. it will choose the
 * right PlanExecutor implementation to use for a given plan and query, and 
 * properly instantiate it.
 * 
 * @author Efi Tsamoura
 * @author Julien Leblay
 */
public final class Middleware {

	/**
	 *  Types of control flows supported by the plans.
	 */
	public static enum ControlFlows {
		
		/** The pull. */
		@EnumParameterValue(description="Control flow where parent request tuples from their children.")
		PULL,

		/** The push. */
		@EnumParameterValue(description="Control flow where children inform their parent when tuple are available")
		PUSH
	}

	/**
	 * New executor.
	 *
	 * @param params RuntimeParameters
	 * @param p the p
	 * @param q the q
	 * @return a plan execution executor that is appropriate for the given plan
	 * type.
	 * @throws MiddlewareException the middleware exception
	 */
	public static PlanExecutor newExecutor(RuntimeParameters params, RelationalTerm p, ConjunctiveQuery q) throws MiddlewareException {
		Preconditions.checkArgument(p != null, "Cannot execute null plan");
		switch(params.getExecutorType()) {
		default: 
			return new VolcanoPlanExecutor(p, q, params.getSemantics(), params.getTimeout().longValue());
		}
	}
}
