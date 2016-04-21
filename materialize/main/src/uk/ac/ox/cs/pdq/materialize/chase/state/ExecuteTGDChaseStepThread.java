package uk.ac.ox.cs.pdq.materialize.chase.state;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.materialize.factmanager.FactManager;
import uk.ac.ox.cs.pdq.materialize.utility.Match;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Sets;

// TODO: Auto-generated Javadoc
/**
 * Executes a TGD chase step
 * @author Efthymia Tsamoura
 */
public class ExecuteTGDChaseStepThread implements Callable<Boolean> {

	protected final Queue<Match> matches;
	
	protected final FactManager manager;
	
	/** Dirty observation of predicates that are created **/
	protected final Collection<Predicate> observedPredicates = Sets.newHashSet();
	
	public ExecuteTGDChaseStepThread(Queue<Match> matches,
			FactManager manager
			) {
		//TODO check input arguments
		this.matches = matches;
		this.manager = manager;
	}
	
	/**
	 * Call.
	 *
	 * @return Boolean
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		Match match;
		while ((match = this.matches.poll()) != null) {
			Constraint dependency = (Constraint) match.getQuery();
			if(dependency instanceof TGD) {
				Map<Variable, Constant> mapping = match.getMapping();
				Constraint grounded = dependency.fire(mapping, true);
				this.manager.addFactsAsynchronously(grounded.getRight().getAtoms());
				for(Atom atom:grounded.getRight().getAtoms()) {
					this.observedPredicates.add(atom.getPredicate());
				}
			}
		}
		this.manager.addFactsAsynchronously(Lists.<Atom>newArrayList());
		return true;
	}
	
	public Collection<Predicate> getObservedPredicates() {
		return this.observedPredicates;
	}

}
