package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.PlannerParameters.ValidatorTypes;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;

/**
 * Creates validators (shape restrictions) based on the input arguments.
 *
 * @author Efthymia Tsamoura
 *
 */
public class ValidatorFactory<S extends AccessibleChaseState> {

	private final ValidatorTypes type;

	private final int depthThreshold;

	/**
	 * Constructor for ValidatorFactory.
	 * @param type ValidatorTypes
	 */
	public ValidatorFactory(ValidatorTypes type) {
		this.type = type;
		this.depthThreshold = 3;
	}

	/**
	 * Constructor for ValidatorFactory.
	 * @param type ValidatorTypes
	 * @param depthThreshold int
	 */
	public ValidatorFactory(ValidatorTypes type, int depthThreshold) {
		this.type = type;
		this.depthThreshold = depthThreshold;
	}

	/**
	 * @return Validator
	 */
	public Validator getInstance() {
		switch(this.type) {
		case DEFAULT_VALIDATOR:
			return new DefaultValidator();
		case APPLYRULE_VALIDATOR:
			return new ApplyRuleValidator();
		case DEPTH_VALIDATOR:
			return new DepthValidator(this.depthThreshold);
		case RIGHT_DEPTH_VALIDATOR:
			return new RightDepthValidator(this.depthThreshold);
		case APPLYRULE_DEPTH_VALIDATOR:
			return new ApplyRuleDepthValidator(this.depthThreshold);
		case LINEAR_VALIDATOR:
			return new LinearValidator();
		default:
			break;
		}
		return null;
	}
}
