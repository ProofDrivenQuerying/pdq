package uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance;

import java.util.ArrayList;

import uk.ac.ox.cs.pdq.cost.estimators.AccessCountCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.DominanceTypes;

/**
 * Creates cost dominance detectors using the input parameters.
 * The available options are:
 * 		CLOSED for closed dominance and
 * 		OPEN for open dominance (an open configuration may dominate a closed one)
 *
 * @author Efthymia Tsamoura
 */
public class DominanceFactory {

	private final DominanceTypes type;

	/**
	 * Constructor for DominanceFactory.
	 * @param type DominanceTypes
	 * @param openToClosedComparison boolean
	 */
	public DominanceFactory(DominanceTypes type) {
		this.type = type;
	}

	/**
	 * @return Dominance[]
	 */
	public Dominance[] getInstance() {
		ArrayList<Dominance> detector = new ArrayList<>();
		switch(this.type) {
		case CLOSED:
			detector.add(new ClosedDominance());
			break;
		case OPEN:
			SimpleCostEstimator<Plan> sc1 = new AccessCountCostEstimator<>();
			detector.add(new StrictOpenDominance(sc1, true));
			break;
		default:
			break;
		}
		Dominance<?>[] array = new Dominance[detector.size()];
		detector.toArray(array);
		return array;
	}
}
