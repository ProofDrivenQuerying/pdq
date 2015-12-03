package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Constant;
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
	
	/** The constants of this annotated plan that appear on the facts used to build up the constituting unary annotated plans.
	 */
	private final Collection<Constant> exportedConstants;
	
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
		this.exportedConstants = Utility.getConstants(fact);
	}

	/**
	 * 
	 * @return
	 * 		the fact of this configuration
	 */
	public Predicate getFact() {
		return this.fact;
	}
	
	/**
	 * 
	 * @return the base relation associated with the fact that is exposed    
	 */
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
	 * @return UnaryAnnotatedPlan
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#clone()
	 */
	@Override
	public UnaryAnnotatedPlan clone() {
		UnaryAnnotatedPlan clone = new UnaryAnnotatedPlan(
				this.getState().clone(),
				this.fact);
		
		clone.setSize(this.getSize());
		clone.setCardinality(this.getCardinality());
		clone.setQuality(this.getQuality());
		clone.setAdjustedQuality(this.getAdjustedQuality());
		return clone;
	}

	@Override
	public Collection<UnaryAnnotatedPlan> getUnaryAnnotatedPlans() {
		return Sets.newHashSet(this);
	}

	@Override
	public List<UnaryAnnotatedPlan> getUnaryAnnotatedPlansList() {
		return Lists.newArrayList(this);
	}

	/**
	 * 
	 * @return the constants of this annotated plan that appear on the facts 
	 * used to build up the constituting unary annotated plans.
	 * The new chase constants that are produced during chasing are not returned.  
	 */
	@Override
	public Collection<Constant> getExportedConstants() {
		return this.exportedConstants;
	}
	
	
}
