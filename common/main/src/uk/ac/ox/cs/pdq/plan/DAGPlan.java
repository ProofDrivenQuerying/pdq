package uk.ac.ox.cs.pdq.plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.rewrite.Rewritable;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * A dag-structured plan .
 *
 * @author Efthymia Tsamoura
 */
public class DAGPlan extends Plan implements Rewritable {

	/**  The top-level operator of the plan. */
	protected final RelationalOperator operator;

	/**  The parent plans *. */
	protected final List<DAGPlan> parents;

	/**  The child plans *. */
	protected final List<DAGPlan> children;

	/**  The plan's leaves. */
	protected final Collection<AccessOperator> leaves;

	/**  The plan's accesses. */
	protected final Collection<AccessOperator> accesses;

	/**
	 * Creates a dag plan with no parent or child subplans.
	 *
	 * @param operator The input top-level logical operator
	 */
	public DAGPlan(RelationalOperator operator) {
		this(Lists.<Term>newArrayList(), operator, Lists.<DAGPlan>newArrayList(), Lists.<DAGPlan>newArrayList());
	}


	/**
	 * Creates a dag plan with no parent or child subplans.
	 *
	 * @param inputs  	The plan's inputs
	 * @param operator 		The input top-level logical operator
	 */
	public DAGPlan(Collection<? extends Term> inputs, RelationalOperator operator) {
		this(inputs, operator, Lists.<DAGPlan>newArrayList(), Lists.<DAGPlan>newArrayList());
	}

	/**
	 * Creates a dag plan.
	 *
	 * @param inputs  	The plan's inputs
	 * @param operator 		The input top-level logical operator
	 * @param parents 		The input parent subplans
	 * @param children 		The input child subplans
	 */
	public DAGPlan(Collection<? extends Term> inputs, RelationalOperator operator, List<DAGPlan> parents, List<DAGPlan> children) {
		super(inferInputTerms(inputs, operator));
		this.operator = operator;
		this.leaves = RelationalOperator.getLeaves(this.operator);
		this.accesses = RelationalOperator.getAccesses(this.operator);
		this.parents = new LinkedList<>();
		for (DAGPlan parent: parents) {
			this.addParent(parent);
		}
		this.children = new LinkedList<>();
		for (DAGPlan child: children) {
			this.addChild(child);
		}
	}

	/**
	 * Returns the input terms of the input operator .
	 *
	 * @param inputs the inputs
	 * @param operator the operator
	 * @return the list
	 */
	private static List<Term> inferInputTerms(Collection<? extends Term> inputs, RelationalOperator operator) {
		List<Term> result = new ArrayList<>();
		for (Term t: operator.getInputTerms()) {
			if (t.isUntypedConstant()) {
				result.add(t);
			}
		}
		return result;
	}

	/**
	 * Adds the input plan in the list of this plan's children.
	 *
	 * @param child the child
	 */
	public void addChild(DAGPlan child) {
		this.children.add(child);
		Preconditions.checkState(this.leaves != null, "LeafOperators BEFORE: " + this.leaves);
		Collection<AccessOperator> newLeaves = RelationalOperator.getLeaves(this.operator);
		Preconditions.checkState(newLeaves != null, "LeafOperators AFTER : " + newLeaves);
		this.leaves.addAll(newLeaves);
		this.accesses.addAll(RelationalOperator.getAccesses(this.operator));
	}

	/**
	 * Adds the input plan in the list of this plan's parents.
	 *
	 * @param plan the plan
	 */
	public void addParent(DAGPlan plan) {
		this.parents.add(plan);
	}

	/**
	 * Gets the ancestors. TOCOMMENT: WHAT DOES THIS MEAN!!!
	 *
	 * @return the ancestor plans
	 */
	public Collection<DAGPlan> getAncestors() {
		Set<DAGPlan> result = Sets.newLinkedHashSet();
		for (DAGPlan parent: this.parents) {
			result.add(parent);
			result.addAll(parent.getAncestors());
		}
		return result;
	}

	/**
	 * Gets the descendants.
	 *
	 * @return the descendant plans
	 */
	public Collection<DAGPlan> getDescendants() {
		Set<DAGPlan> result = Sets.newLinkedHashSet();
		for (DAGPlan child: this.children) {
			result.add(child);
			result.addAll(child.getDescendants());
		}
		return result;
	}


	/**
	 * Gets the parents.
	 *
	 * @return the parents
	 */
	public List<DAGPlan> getParents() {
		return this.parents;
	}


	/**
	 * Gets the children.
	 *
	 * @return the children
	 */
	public List<DAGPlan> getChildren() {
		return this.children;
	}


	/**
	 * The top-level operator of this plan.
	 *
	 * @return the operator
	 */
	@Override
	public RelationalOperator getOperator() {
		return this.operator;
	}

	/**
	 * Gets the effective operator. TCOMMENT: WHAT THE ???? DOES THIS MEAN?
	 *
	 * @return LogicalOperator
	 */
	@Override
	public RelationalOperator getEffectiveOperator() {
		return this.operator;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DAGPlan clone() {
		// TODO: Consider a deep copy for the operator.
		DAGPlan plan = new DAGPlan(this.inputs, this.operator, 
				Lists.newArrayList(this.parents), 
				Lists.newArrayList(this.children));
		plan.setCost(this.cost.clone());
		return plan;
	}

	/**
	 * Gets the leaves.
	 *
	 * @return the leaf operators of this plan
	 */
	public Collection<AccessOperator> getLeaves() {
		return this.leaves;
	}

	/**
	 * Gets the accesses.
	 *
	 * @return the plan's accesses
	 */
	@Override
	public Collection<AccessOperator> getAccesses() {
		return this.accesses;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.operator.toString() + ":" + this.cost;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.Plan#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.operator == null;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.Plan#size()
	 */
	@Override
	public Integer size() {
		return size(this);
	}

	/**
	 * Size.
	 *
	 * @param plan DAGPlan
	 * @return the number of subplans
	 */
	private static Integer size(DAGPlan plan) {
		if(plan.isEmpty()) {
			return 0;
		} else if(plan.getChildren() == null) {
			return 1;
		}
		else {
			Integer leafOperators = 0;
			for(DAGPlan child:plan.getChildren()) {
				leafOperators += size(child);
			}
			return leafOperators + 1;
		}
	}

	/**
	 * Gets the output.
	 *
	 * @return the output terms of this plan
	 */
	@Override
	public List<? extends Term> getOutput() {
		return this.operator.getColumns();
	}

	/**
	 * Gets the output attributes.
	 *
	 * @return the output attributes of this plan
	 */
	@Override
	public List<Typed> getOutputAttributes() {
		return Utility.termsToTyped(this.operator.getColumns(), this.operator.getType());
	}

	/**
	 * Checks if the plan is ``closed'', meaning that there are no inputs
	 *
	 * @return if the plan has no input
	 */
	@Override
	public boolean isClosed() {
		return this.inputs.isEmpty(); 
	}

	/**
	 * Compare to.
	 *
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
