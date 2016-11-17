package uk.ac.ox.cs.pdq.runtime.exec;

import java.util.Properties;

import uk.ac.ox.cs.pdq.InconsistentParametersException;
import uk.ac.ox.cs.pdq.Parameters.EnumParameterValue;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.plan.Plan;
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
	public static PlanExecutor newExecutor(RuntimeParameters params, Plan p, ConjunctiveQuery q) throws MiddlewareException {
		Preconditions.checkArgument(p != null, "Cannot execute null plan");
		Properties properties = findRelationalProperties(p);
		switch(params.getExecutorType()) {
		case SQL_TREE: 
			return new SQLPlanExecutor(p, q, params.getSemantics(), properties);
		case SQL_STEP: 
			if (!(p instanceof LeftDeepPlan)) {
				throw new InconsistentParametersException("Executor type " + ExecutorTypes.SQL_STEP + " can only be used in conjunction with linear plans.");
			}
			return new SQLStepPlanExecutor((LeftDeepPlan) p, q, params.getSemantics(), properties);
		case SQL_WITH: 
			return new SQLWithPlanExecutor(p, q, params.getSemantics(), properties);
		default: 
			return new VolcanoPlanExecutor(p, q, params.getSemantics(), params.getTimeout().longValue());
		}
	}

	/**
	 * Find relational properties.
	 *
	 * @param plan Plan
	 * @return the schema underlying relational's properties if all of the
	 * relations in the schema have the same properties, null otherwise.
	 */
	private static Properties findRelationalProperties(Plan plan) {
		Properties result = null;
		for (AccessOperator access: plan.getAccesses()) {
			Properties properties = new Properties();
			properties.putAll(access.getRelation().getProperties());
			if (result == null) {
				result = properties;
			} else if (!(properties.equals(result))) {
				return null;
			}
		}
		return result;
	}
}
