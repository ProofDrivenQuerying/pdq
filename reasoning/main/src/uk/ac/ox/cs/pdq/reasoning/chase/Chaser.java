package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;

/**
 *
 * The chase algorithm
 *
 * @author Efthymia Tsamoura
 */
public abstract class Chaser {

	protected static Logger log = Logger.getLogger(Chaser.class);

	/** Collects statistics related to chasing **/
	protected final StatisticsCollector statistics;

	/**
	 * Constructor for Chaser.
	 * @param statistics 
	 * 		
	 */
	public Chaser(StatisticsCollector statistics) {
		this.statistics = statistics;
	}
	

	/**
	 * Chases the input state until termination
	 * @param s
	 * @param target
	 * @param dependencies
	 */
	public abstract <S extends ChaseState> void reasonUntilTermination(S s, Query<?> target, Collection<? extends Constraint> dependencies);
	
	/**
	 * 
	 * @param instance
	 * @param free
	 * 		Mapping of query's free variables to constants
	 * @param target
	 * @param constraints
	 * @return
	 * 		true if the input instance with the given set of free variables and constraints implies the target query.
	 * 		
	 */
	public abstract <S extends ChaseState> boolean entails(S instance, Map<Variable, Constant> free, Query<?> target, Collection<? extends Constraint> constraints);
	
	
	/**
	 * 
	 * @param source
	 * @param target
	 * @param constraints
	 * @return
	 * 		true if the source query entails the target query
	 */
	public abstract <S extends ChaseState> boolean entails(Query<?> source, Query<?> target, Collection<? extends Constraint> constraints);
	

	public abstract Chaser clone();
}
