package uk.ac.ox.cs.pdq.planner.dag.priority;

import java.util.List;

import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dag.potential.PotentialAssessor;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;

/**
 * Creates priority assessors based on the input arguments
 *
 * @author Efthymia Tsamoura
 */
public class PriorityAssessorFactory {
	/**
	 * @param parameters PlannerParameters
	 * @param validators List<Validator>
	 * @param potential PotentialAssessor
	 * @param query Query
	 * @param state S
	 * @param seed Integer
	 * @return PriorityAssessor
	 * @throws Exception
	 */
	public static <S extends AccessibleChaseState> PriorityAssessor createPriorityAssessor (
			PlannerParameters parameters,
			List<Validator> validators,
			PotentialAssessor potential,
			Query<?> query,
			S state,
			Integer seed) throws Exception{
		switch(parameters.getPriorityAssessorType()) {
		case DEFAULT:
			return new DefaultPriorityAssessor(validators, potential);
		case DISTANCE2CORETOPK:
			return new Distance2CoreTopK(validators, potential, parameters.getTopConfigurations(), query, (FiringGraph) state.getFiringGraph(), seed);
		case DISTANCE2CORERANGE:
			return new Distance2CoreRange(validators, potential, parameters.getRange(), query, (FiringGraph) state.getFiringGraph(), seed);
		default:
			throw new java.lang.IllegalArgumentException();
		}
	}
}
