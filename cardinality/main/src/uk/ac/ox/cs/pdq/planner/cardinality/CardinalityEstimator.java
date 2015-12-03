package uk.ac.ox.cs.pdq.planner.cardinality;

import java.math.BigInteger;
import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Query;
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

	/**
	 * 
	 * @param configuration
	 * @return
	 * 		the size and the quality of the input annotated plan
	 */
	Pair<BigInteger,Double> sizeQualityOf(UnaryAnnotatedPlan configuration);
	
	/**
	 * 
	 * @param left
	 * @param right
	 * @param egd
	 * 		Runs the EGD chase algorithm 
	 * @param detector
	 * 		Detects homomorphisms during chasing
	 * @param dependencies
	 * 		The dependencies to take into account during chasing
	 * @return
	 * 		the size and the quality of the annotated plan that is composed by the input annotated plans
	 */
	Pair<BigInteger,Double> sizeQualityOf(DAGAnnotatedPlan left, DAGAnnotatedPlan right, Chaser egd, HomomorphismDetector detector, Collection<? extends Constraint> dependencies);
	
	/**
	 * 
	 * @param configuration
	 * @param query
	 * @param matchesQuery
	 * 		true if the annotated plan matches the input query.
	 * @return
	 * 		the adjusted quality of the input annotated plan. 
	 * 		The adjusted quality of the input annotated plan equals its quality plus other penalty measures introduced by projection.  
	 */
	double adjustedQualityOf(DAGAnnotatedPlan configuration, Query<?> query, boolean matchesQuery);
	
	/**
	 * 
	 * @param configuration
	 * @param query
	 * @return
	 * 		the cardinality of the input annotated plan after applying the projections of the input    
	 */
	BigInteger cardinalityOf(DAGAnnotatedPlan configuration, Query<?> query);
	
	CardinalityEstimator clone();

}
