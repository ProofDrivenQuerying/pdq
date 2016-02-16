package uk.ac.ox.cs.pdq.plan;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Costable;
import uk.ac.ox.cs.pdq.util.Differentiable;
import uk.ac.ox.cs.pdq.util.Operator;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.collect.ImmutableList;

// TODO: Auto-generated Javadoc
/**
 * Abstract plan class.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public abstract class Plan implements Costable, Comparable<Plan>, Differentiable<Plan> {
	
	/**  The plan's inputs. */
	protected final List<? extends Term> inputs;

	/**  The plan's cost. */
	protected Cost cost = DoubleCost.UPPER_BOUND;

	/**
	 * Constructor for Plan.
	 */
	public Plan() {
		this(ImmutableList.<Term>of());
	}

	/**
	 * Instantiates a new plan.
	 *
	 * @param inputs 		The plan's inputs
	 */
	public Plan(List<? extends Term> inputs) {
		this.inputs = inputs;
	}

	/**
	 * Sets the plan's cost.
	 *
	 * @param cost the new cost
	 */
	public void setCost(Cost cost) {
		this.cost = cost;
	}

	/**
	 * Gets the cost.
	 *
	 * @return the plan's cost
	 */
	public Cost getCost() {
		return this.cost;
	}

	/**
	 * Gets the inputs.
	 *
	 * @return the plan's input terms
	 */
	public List<? extends Term> getInputs() {
		return this.inputs;
	}

	/**
	 * How different.
	 *
	 * @param o Plan
	 * @return Levels
	 */
	@Override
	public Levels howDifferent(Plan o) {
		if (o == null) {
			return Levels.DIFFERENT;
		}
		if (this.getCost().equals(o.getCost())) {
			if (this.equals(o)) {
				return Levels.IDENTICAL;
			}
			return Levels.EQUIVALENT;
		}
		return Levels.DIFFERENT;
	}

	/**
	 * Diff.
	 *
	 * @param o Plan
	 * @return String
	 */
	@Override
	public String diff(Plan o) {
		StringBuilder result = new StringBuilder();
		result.append("\n\tCosts: ").append(this.getCost()).append(" <-> ").append(o.getCost());
		result.append("\n\tLeaves:\n\t\t");
		result.append(this.getAccesses()).append("\n\t\t");
		result.append(o.getAccesses()).append("\n\t");
		return result.toString();
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true if the current plan is empty
	 */
	public abstract boolean isEmpty();

	/**
	 * Size.
	 *
	 * @return the number of subplans
	 */
	public abstract Integer size();

	/**
	 * Gets the output.
	 *
	 * @return the output terms of this plan
	 */
	public abstract List<? extends Term> getOutput();

	/**
	 * Gets the operator.
	 *
	 * @param <O> the generic type
	 * @return the top-level operator of this plan
	 */
	public abstract <O extends Operator> O getOperator();

	//This function seems confusing
	/**
	 * Gets the effective operator.
	 *
	 * @param <O> the generic type
	 * @return the top level operator of this plan
	 */
	public abstract <O extends Operator> O getEffectiveOperator();

	/**
	 * Gets the output attributes.
	 *
	 * @return the output attributes of this plan
	 */
	public abstract List<Typed> getOutputAttributes();

	/**
	 * Checks if is closed.
	 *
	 * @return true if the plan has no input
	 */
	public abstract boolean isClosed();

	/**
	 * Gets the accesses.
	 *
	 * @return the accesses of this plan
	 */
	public abstract Collection<AccessOperator> getAccesses();
}
