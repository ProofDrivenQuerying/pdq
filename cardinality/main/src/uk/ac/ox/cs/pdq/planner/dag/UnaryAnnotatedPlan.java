package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * An ApplyRule configuration. Corresponds to an access
 * @author Efthymia Tsamoura
 *
 */
public class UnaryAnnotatedPlan extends DAGAnnotatedPlan {
	
	/** The facts of this configuration. These must share the same constants for the input positions of the accessibility axiom */
	private final Predicate fact;

	/** The string representation of this configuration*/
	private String toString;
	
	/**
	 * 
	 * @param state
	 * 		The state of this configuration.
	 * @param fact
	 * 		Input facts. These must share the same constants for the input positions of the input accessibility axiom
	 */
	public UnaryAnnotatedPlan(
			ChaseState state,
			Predicate fact
			) {		
		super(state, 
				Utility.getConstants(fact),
				1,
				0);
		Preconditions.checkNotNull(fact);
		this.fact = fact;
	}


	/**
	 * 
	 * @return
	 * 		the facts of this configuration
	 */
	public Predicate getFact() {
		return this.fact;
	}
	
	public Relation getRelation() {
		return (Relation)this.fact.getSignature();
	}
	
	/**
	 * @return String
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			this.toString = "Unary" + "(" + this.fact.toString() + ")";
		}
		return this.toString;
	}

	/**
	 * @return ApplyRule<S>
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#clone()
	 */
	@Override
	public UnaryAnnotatedPlan clone() {
		return new UnaryAnnotatedPlan(
				this.getState().clone(),
				this.fact);
	}


	@Override
	public Collection<UnaryAnnotatedPlan> getUnaryAnnotatedPlans() {
		return Sets.newHashSet(this);
	}


	@Override
	public List<UnaryAnnotatedPlan> getUnaryAnnotatedPlansList() {
		return Lists.newArrayList(this);
	}
}
