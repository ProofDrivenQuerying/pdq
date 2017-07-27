package uk.ac.ox.cs.pdq.planner.dominance;

import java.util.ArrayList;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.DominanceTypes;

// TODO: Auto-generated Javadoc
/**
 * Creates cost dominance detectors using the input parameters.
 * The available options are:
 * 		-CLOSED for closed dominance. A closed configuration c dominates a closed configuration c', if c both cost- and fact- dominates c'.
 * 		-OPEN for open dominance. A configuration c dominates a configuration c',
 * 		if c both cost- and fact- dominates c' with one of the two being strict. When both configurations are open, then a simple plan cost estimator is used
 * 		to assess the configurations' costs; otherwise, the costs of their corresponding (closed) plans are considered.
 * 
 *
 * @author Efthymia Tsamoura
 */
public class DominanceFactory {

	/** The type. */
	private final DominanceTypes type;
	
	/** The cost estimator. */
	private final SimpleCostEstimator costEstimator;
	
	private final FactDominance factDominance;

	/**
	 * Constructor for DominanceFactory.
	 *
	 * @param type DominanceTypes
	 * @param costEstimator the cost estimator
	 */
	public DominanceFactory(DominanceTypes type, FactDominance factDominance, SimpleCostEstimator costEstimator) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(costEstimator);
		this.type = type;
		this.factDominance = factDominance;
		this.costEstimator = costEstimator;
	}

	/**
	 * Gets the single instance of DominanceFactory.
	 *
	 * @return Dominance[]
	 */
	public Dominance[] getInstance() {
		ArrayList<Dominance> detector = new ArrayList<>();
		switch(this.type) {
		case CLOSED:
			detector.add(new CostFactDominance(this.costEstimator, this.factDominance, false));
			break;
		case OPEN:
			detector.add(new CostFactDominance(this.costEstimator, this.factDominance, true));
			break;
		default:
			break;
		}
		Dominance[] array = new Dominance[detector.size()];
		detector.toArray(array);
		return array;
	}
}
