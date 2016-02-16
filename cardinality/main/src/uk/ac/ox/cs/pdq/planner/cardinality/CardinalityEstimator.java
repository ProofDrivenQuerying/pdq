/*
 * 
 */
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


// TODO: Auto-generated Javadoc
/**
 * The Interface CardinalityEstimator.
 *
 * @author Efthymia Tsamoura
 */
public interface CardinalityEstimator {

	/**
	 * Size quality of.
	 *
	 * @param configuration the configuration
	 * @return 		the size and the quality of the input annotated plan
	 */
	Pair<BigInteger,Double> sizeQualityOf(UnaryAnnotatedPlan configuration);
	
	/**
	 * Size quality of.
	 *
	 * @param left the left
	 * @param right the right
	 * @param egd 		Runs the EGD chase algorithm 
	 * @param detector 		Detects homomorphisms during chasing
	 * @param dependencies 		The dependencies to take into account during chasing
	 * @return 		the size and the quality of the annotated plan that is composed by the input annotated plans
	 */
	Pair<BigInteger,Double> sizeQualityOf(DAGAnnotatedPlan left, DAGAnnotatedPlan right, Chaser egd, HomomorphismDetector detector, Collection<? extends Constraint> dependencies);
	
	/**
	 * Adjusted quality of.
	 *
	 * @param configuration the configuration
	 * @param query the query
	 * @param matchesQuery 		true if the annotated plan matches the input query.
	 * @return 		the adjusted quality of the input annotated plan. 
	 * 		The adjusted quality of the input annotated plan equals its quality plus other penalty measures introduced by projection.
	 */
	double adjustedQualityOf(DAGAnnotatedPlan configuration, Query<?> query, boolean matchesQuery);
	
	/**
	 * Cardinality of.
	 *
	 * @param configuration the configuration
	 * @param query the query
	 * @return 		the cardinality of the input annotated plan after applying the projections of the input
	 */
	BigInteger cardinalityOf(DAGAnnotatedPlan configuration, Query<?> query);
	
	/**
	 * Clone.
	 *
	 * @return the cardinality estimator
	 */
	CardinalityEstimator clone();

}
