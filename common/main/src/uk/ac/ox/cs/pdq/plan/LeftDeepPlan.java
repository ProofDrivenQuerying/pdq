package uk.ac.ox.cs.pdq.plan;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.rewrite.Rewritable;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;

/**
 * A sequence of access subplans, where each subplan consists of a single access operator and (optionally) selections and projections. 
 *
 * @author Efthymia Tsamoura
 */
public final class LeftDeepPlan extends Plan implements Iterable<LeftDeepPlan>, Rewritable {

	/** The top-most access */
	protected final AccessOperator access;

	/** The top-level operator of the plan*/
	protected final RelationalOperator operator;

	/** The prefix sub-plan */
	protected LeftDeepPlan prefix;

	/** The suffix sub-plan */
	protected LeftDeepPlan suffix;

	/** The first linear subplan **/
	protected LeftDeepPlan last;

	/** The last linear subplan **/
	protected LeftDeepPlan first;

	/**
	 * Creates a linear plan having the input relational operator at its top level.
	 * @param operator
	 * 		The top-level operator of the plan
	 */
	public LeftDeepPlan(RelationalOperator operator) {
		this(operator, null, null);
	}

	/**
	 * Creates a linear plan that is suffixed and prefixed by the input subplans.
	 * The output linear plan looks like <prefix,LeftDeepPlan(operator), suffix> 
	 * @param operator
	 * 		The top-level operator of the plan
	 * @param prefix
	 * 		The prefix sub-plan
	 * @param suffix
	 * 		The suffix sub-plan
	 */
	public LeftDeepPlan(RelationalOperator operator, LeftDeepPlan prefix, LeftDeepPlan suffix) {
		super();
		Preconditions.checkArgument(operator != null);
		Preconditions.checkArgument(RelationalOperator.getAccesses(operator) != null);
		Preconditions.checkArgument(RelationalOperator.getAccesses(operator).size() == 1);
		this.operator = operator;
		this.access = RelationalOperator.getAccesses(operator).iterator().next();
		if (prefix == null) {
			this.first = this;
		}
		if (suffix == null) {
			this.last = this;
		}
		if (prefix != null) {
			LeftDeepPlan newSuff = suffix;
			for (Iterator<LeftDeepPlan> pi = prefix.descendingIterator(); pi.hasNext(); ) {
				LeftDeepPlan pref = pi.next();
				if (newSuff != null) {
					pref.addSuffix(newSuff);
				}
				newSuff = pref;
			}
			this.addPrefix(prefix);
		}
		if (suffix != null) {
			LeftDeepPlan newPref = prefix;
			for (Iterator<LeftDeepPlan> si = suffix.iterator(); si.hasNext(); ) {
				LeftDeepPlan suff = si.next();
				if (newPref != null) {
					suff.addPrefix(newPref);
				}
				newPref = suff;
			}
			this.addSuffix(suffix);
		}
	}

	/**
	 * @param proj Projection
	 * @return LeftDeepPlan
	 */
	public LeftDeepPlan projectLast(Projection proj) {
		Preconditions.checkArgument(proj.getChild() == this.last.operator);
		return new LeftDeepPlan(proj, this.last.prefix, null);
	}

	/**
	 * @param suff LeftDeepPlan
	 */
	public void addSuffix(LeftDeepPlan suff) {
		Preconditions.checkState(suff != null);
		this.suffix = suff;
		this.suffix.prefix = this;
		this.last = suff.getLast();
	}

	/**
	 * @param pref LeftDeepPlan
	 */
	public void addPrefix(LeftDeepPlan pref) {
		Preconditions.checkState(pref != null);
		this.prefix = pref;
		this.prefix.suffix = this;
		this.first = pref.getFirst();
	}

	/**
	 * @return List<AccessOperator>
	 */
	public List<AccessOperator> getLeafOperators() {
		return RelationalOperator.getLeaves(this.operator);
	}

	/**
	 * @return the local logical operator for this plan
	 */
	@Override
	public RelationalOperator getOperator() {
		return this.operator;
	}

	/**
	 * @return the top level logical operator for this plan
	 */
	@Override
	public RelationalOperator getEffectiveOperator() {
		return getLast().getOperator();
	}

	/**
	 * @return AccessOperator
	 */
	public AccessOperator getAccess() {
		return this.access;
	}

	/**
	 * @return LeftDeepPlan
	 */
	public LeftDeepPlan getPrefix() {
		return this.prefix;
	}

	/**
	 * @return LeftDeepPlan
	 */
	public LeftDeepPlan getSuffix() {
		return this.suffix;
	}

	/**
	 * @return LeftDeepPlan
	 */
	public LeftDeepPlan getLast() {
		return this.last;
	}

	/**
	 * @return LeftDeepPlan
	 */
	public LeftDeepPlan getFirst() {
		return this.first;
	}

	/**
	 * @return LeftDeepPlan
	 */
	@Override
	public LeftDeepPlan clone() {
		return new LeftDeepPlan(this.operator, this.prefix, this.suffix);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.Plan#setCost(uk.ac.ox.cs.pdq.costs.Cost)
	 */
	@Override
	public void setCost(Cost cost) {
		if (!cost.getClass().equals(DoubleCost.class)) {
			throw new IllegalStateException();
		}
		this.cost = cost;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.Plan#getCost()
	 */
	@Override
	public DoubleCost getCost() {
		return (DoubleCost) this.cost;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String sep = "";
		for (LeftDeepPlan step: this) {
			result.append(sep).append(step.getOperator().toString());
			sep = ";";
		}
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.Plan#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.operator == null;
	}

	/**
	 * @return the size of the size, in number of commands.
	 */
	@Override
	public Integer size() {
		return size(this);
	}

	/**
	 * @param plan LeftDeepPlan
	 * @return Integer
	 */
	private static Integer size(LeftDeepPlan plan) {
		if(plan.isEmpty()) {
			return 0;
		} else if(plan.getSuffix() == null) {
			return 1;
		}
		else {
			Integer leafOperators = 0;
			leafOperators += size(plan.getSuffix());
			return leafOperators + 1;
		}
	}

	/**
	 * @return Collection<AccessOperator>
	 */
	@Override
	public Collection<AccessOperator> getAccesses() {
		Collection<AccessOperator> result = new ArrayList<>();
		for (LeftDeepPlan pred: this) {
			result.add(pred.getAccess());
		}
		return result;
	}

	/**
	 * @return List<? extends Term>
	 */
	@Override
	public List<? extends Term> getOutput() {
		return this.operator.getColumns();
	}

	/**
	 * @return List<Typed>
	 */
	@Override
	public List<Typed> getOutputAttributes() {
		RelationalOperator op = this.getLast().getOperator();
		return Utility.termsToTyped(op.getColumns(), op.getType());
	}

	/**
	 * @return true, as linear plans are always closed.
	 */
	@Override
	public boolean isClosed() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<LeftDeepPlan> iterator() {
		return new LinearPlanIterator(this);
	}

	/**
	 * @return Iterator<LeftDeepPlan>
	 */
	public Iterator<LeftDeepPlan> descendingIterator() {
		return new DescendingLinearPlanIterator(this);
	}

	/**
	 * Ascending iterator for Linear plans, i.e. from first to last.
	 * @author Julien Leblay
	 */
	private static class LinearPlanIterator implements Iterator<LeftDeepPlan> {

		private LeftDeepPlan next = null;

		/**
		 * Constructor for LinearPlanIterator.
		 * @param p LeftDeepPlan
		 */
		public LinearPlanIterator(LeftDeepPlan p) {
			Preconditions.checkArgument(p != null);
			this.next = p.getFirst();
		}

		/**
		 * @return boolean
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return this.next != null;
		}

		/**
		 * @return LeftDeepPlan
		 * @see java.util.Iterator#next()
		 */
		@Override
		public LeftDeepPlan next() {
			if (this.next == null) {
				throw new NoSuchElementException();
			}
			LeftDeepPlan result = this.next;
			this.next = this.next.getSuffix();
			return result;
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Descending iterator for Linear plans, i.e. from last to first.
	 * @author Julien Leblay
	 */
	private static class DescendingLinearPlanIterator implements Iterator<LeftDeepPlan> {

		private LeftDeepPlan prev = null;

		/**
		 * Constructor for DescendingLinearPlanIterator.
		 * @param p LeftDeepPlan
		 */
		public DescendingLinearPlanIterator(LeftDeepPlan p) {
			Preconditions.checkArgument(p != null);
			this.prev = p.getLast();
		}

		/**
		 * @return boolean
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return this.prev != null;
		}

		/**
		 * @return LeftDeepPlan
		 * @see java.util.Iterator#next()
		 */
		@Override
		public LeftDeepPlan next() {
			if (this.prev == null) {
				throw new NoSuchElementException();
			}
			LeftDeepPlan result = this.prev;
			this.prev = this.prev.getPrefix();
			return result;
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * @param plan Plan
	 * @return int
	 */
	@Override
	public int compareTo(Plan plan) {
		return this.cost.compareTo(plan.getCost());
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.rewrite.Rewritable#rewrite(uk.ac.ox.cs.pdq.rewrite.Rewriter)
	 */
	@Override
	public <I extends Rewritable, O> O rewrite(Rewriter<I, O> rewriter) throws RewriterException {
		return rewriter.rewrite((I) this);
	}
}
