package uk.ac.ox.cs.pdq.materialize.chase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.materialize.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.materialize.chase.state.DatabaseDiskState;
import uk.ac.ox.cs.pdq.materialize.chase.state.ListState;
import uk.ac.ox.cs.pdq.materialize.factmanager.FactManager;
import uk.ac.ox.cs.pdq.materialize.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.materialize.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.materialize.utility.DefaultTGDDependencyAssessor;
import uk.ac.ox.cs.pdq.materialize.utility.Match;
import uk.ac.ox.cs.pdq.materialize.utility.ReasonerUtility;
import uk.ac.ox.cs.pdq.materialize.utility.TGDDependencyAssessor;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;


// TODO: Auto-generated Javadoc
/**
 * (From modern dependency theory notes)
 * Runs the chase algorithm applying only active triggers. 
 * Consider an instance I, a set Base of values, and a TGD
 * \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
 * a trigger for \delta in I is a homomorphism h of \sigma into I. A trigger is active if it
 * does not extend to a homomorphism h0 into I. Informally, a trigger is a tuple \vec{c}
 * satisfying \sigma, and it is active if there is no witness \vec{y} that makes \tau holds.
 * A chase step appends to I additional facts that were produced during grounding \delta. 
 * The output of the chase step is a new instance in which h is no longer an active trigger.
 * 
 * The facts that are generated during chasing are stored in a list.
 *
 * @author Efthymia Tsamoura
 *
 */
public class RestrictedChaser extends Chaser {

	/**
	 * Constructor for RestrictedChaser.
	 *
	 * @param statistics the statistics
	 */
	public RestrictedChaser(
			StatisticsCollector statistics) {
		super(statistics);
	}

	/**
	 * Chases the input state until termination.
	 *
	 * @param <S> the generic type
	 * @param instance the instance
	 * @param dependencies the dependencies
	 */
	@Override
	public <S extends ChaseState> void reasonUntilTermination(S instance,  Collection<? extends Constraint> dependencies) {
		Preconditions.checkArgument(instance instanceof ListState);
		TGDDependencyAssessor accessor = new DefaultTGDDependencyAssessor(dependencies);
		boolean appliedStep = false;
		Collection<? extends Constraint> d = dependencies;
		long start = System.currentTimeMillis();
		do {
			
			long start0 = System.currentTimeMillis();
			appliedStep = false;
			for(Constraint dependency:d) {
				List<Match> matches = instance.getMatches(Lists.newArrayList(dependency), HomomorphismProperty.createActiveTriggerProperty());	
				if(!matches.isEmpty()) {
					appliedStep = true;
				}
				Queue<Match> queue = new ConcurrentLinkedQueue<>(matches);
				matches.clear();
				instance.chaseStep(queue);
				queue.clear();
				d = accessor.getDependencies(instance);	
			}
			long end0 = System.currentTimeMillis();
			System.out.println("---Finish chase round " + " " + (end0-start0));
			
		} while (appliedStep);
		long end = System.currentTimeMillis();
		System.out.println("---Time to reason until termination " + " " + (end-start));
	}

	/**
	 * Entails.
	 *
	 * @param <S> the generic type
	 * @param instance the instance
	 * @param free 		Mapping of query's free variables to constants
	 * @param target the target
	 * @param constraints the constraints
	 * @return 		true if the input instance with the given set of free variables and constraints implies the target query.
	 */
	@Override
	public <S extends ChaseState> boolean entails(S instance, Map<Variable, Constant> free, Query<?> target,
			Collection<? extends Constraint<?,?>> constraints) {
		Collection<? extends Constraint<?, ?>> relevantDependencies = new ReasonerUtility().findRelevant(target, constraints);
		this.reasonUntilTermination(instance, relevantDependencies);
		if(!instance.isFailed()) {
			HomomorphismProperty[] c = {
					HomomorphismProperty.createTopKProperty(1),
					HomomorphismProperty.createMapProperty(free)};
			return !instance.getMatches(target,c).isEmpty(); 
		}
		return false;
	}

	/**
	 * Entails.
	 *
	 * @param <S> the generic type
	 * @param source the source
	 * @param target the target
	 * @param constraints the constraints
	 * @return 		true if the source query entails the target query
	 */
	@Override
	public boolean entails(Query<?> source, Query<?> target,
			Collection<? extends Constraint<?,?>> constraints, HomomorphismDetector detector, FactManager manager) {	
		Collection<? extends Constraint<?, ?>> relevantDependencies = new ReasonerUtility().findRelevant(target, constraints);
		manager.addFactsSynchronously(source.getCanonical().getAtoms());
		DatabaseDiskState instance = new DatabaseDiskState(manager, detector);
		this.reasonUntilTermination(instance, relevantDependencies);
		if(!instance.isFailed()) {
			HomomorphismProperty[] c = {
					HomomorphismProperty.createTopKProperty(1)};
			return !instance.getMatches(target,c).isEmpty(); 
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.Chaser#clone()
	 */
	@Override
	public RestrictedChaser clone() {
		return new RestrictedChaser(this.statistics);
	}
}
