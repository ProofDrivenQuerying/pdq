package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.List;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.logging.SimpleStatisticsCollector;
import uk.ac.ox.cs.pdq.logging.SimpleStatisticsCollector.StatisticsRecord;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.TriggerProperty;
import uk.ac.ox.cs.pdq.reasoning.utility.DefaultTGDDependencyAssessor;
import uk.ac.ox.cs.pdq.reasoning.utility.TGDDependencyAssessor;


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
	public RestrictedChaser(StatisticsCollector statistics) {
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
	public <S extends ChaseInstance> void reasonUntilTermination(S instance,  Dependency[] dependencies) {
		Preconditions.checkArgument(instance instanceof ChaseInstance);
		TGDDependencyAssessor accessor = new DefaultTGDDependencyAssessor(dependencies);
		boolean appliedStep = false;
		Dependency[] d = dependencies;
		do {
			appliedStep = false;
			for(Dependency dependency:d) {
				StatisticsRecord r = (statistics instanceof SimpleStatisticsCollector) ?((SimpleStatisticsCollector)statistics).addNewRecord("getTriggers"):null;
				List<Match> matches = null;
				try {
					matches = instance.getTriggers(new Dependency[]{dependency}, TriggerProperty.ACTIVE);
				} finally {
					if (r!=null) r.setEndTime();
				}
				if(!matches.isEmpty()) {
					appliedStep = true;
					StatisticsRecord r1 = (statistics instanceof SimpleStatisticsCollector) ?((SimpleStatisticsCollector)statistics).addNewRecord("chaseStep"):null;
					try {
						instance.chaseStep(matches);
					} finally {
						if (r1!=null) r1.setEndTime();
					}
				}
			}
			d = accessor.getDependencies(instance);	
		} while (appliedStep);
	}


	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.Chaser#clone()
	 */
	@Override
	public RestrictedChaser clone() {
		return new RestrictedChaser(this.statistics);
	}
}
