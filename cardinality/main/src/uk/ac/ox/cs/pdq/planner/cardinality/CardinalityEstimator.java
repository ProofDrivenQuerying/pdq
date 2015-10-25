package uk.ac.ox.cs.pdq.planner.cardinality;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.UnaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;


/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public interface CardinalityEstimator {

	Pair<Integer,Double> sizeQualityOf(UnaryAnnotatedPlan configuration);
	
	Pair<Integer,Double> sizeQualityOf(DAGAnnotatedPlan left, DAGAnnotatedPlan right, Chaser egd, HomomorphismDetector detector, Collection<? extends Constraint> dependencies);
	
	double adjustedQualityOf(UnaryAnnotatedPlan configuration);
	
	double adjustedQualityOf(DAGAnnotatedPlan left, DAGAnnotatedPlan right);
	
	CardinalityEstimator clone();
	
}
