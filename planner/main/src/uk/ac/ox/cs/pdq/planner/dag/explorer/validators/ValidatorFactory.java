package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.PlannerParameters.ValidatorTypes;

/**
 * Creates validators (shape restrictions) based on the input arguments.
 * 
 * 	-The DefaultValidator requires the left and right configurations to be non-trivial:
	an ordered pair of configurations (left, right) is non-trivial if the output facts of the right configuration are not included in the output facts of left configuration and vice versa.
	-The ApplyRuleDepthValidator requires the input pair of configurations to be non trivial, their combined depth to be <= the depth threshold
	and at least one of the input configurations to be an ApplyRule.
	-The ApplyRuleValidator requires the input pair of configurations to be non trivial and at least one of the input configurations to be an ApplyRule.
	-The DepthValidator requires the input pair of configurations to be non trivial and their combined depth to be <= the depth threshold.
	-The LinearValidator requires the input pair of configurations to be non trivial and their composition to be a closed left-deep configuration
	-The RightDepthValidator requires the input pair of configurations to be non trivial and the right's depth to be <= the depth threshold
 *
 * @author Efthymia Tsamoura
 *
 */
public class ValidatorFactory {

	/**  The type of the target validator object*. */
	private final ValidatorTypes type;

	/** The depth threshold. */
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
	 * Gets the single instance of ValidatorFactory.
	 *
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
