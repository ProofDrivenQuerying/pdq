package uk.ac.ox.cs.pdq.plan;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.fol.Term;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Tree-shaped plans
 *
 * @author Efthymia Tsamoura
 */
public final class TreePlan extends DAGPlan {

	/**
	 * 
	 * @param operator The input top-level logical operator
	 * @param parent The parent plan
	 * @param children The input child subplans
	 */
	public TreePlan(RelationalOperator operator, DAGPlan parent, List<DAGPlan> children) {
		this(Lists.<Term>newArrayList(), operator, parent, children);
	}

	/**
	 * 
	 * @param operator The input top-level logical operator
	 * @param parent The parent plan
	 */
	public TreePlan(RelationalOperator operator, DAGPlan parent) {
		this(operator, parent, null);
	}

	/**
	 * Creates a tree plan with no parent or children subplans
	 * @param operator The input top-level logical operator
	 */
	public TreePlan(RelationalOperator operator) {
		this(operator, null, null);
	}

	/**
	 * Creates a tree plan with no parent or child subplans
	 * @param inputs
	 *  	The plan's inputs
	 * @param operator
	 * 		The input top-level logical operator
	 */
	public TreePlan(Collection<? extends Term> inputs, RelationalOperator operator) {
		this(inputs, operator, null, Lists.<DAGPlan>newArrayList());
	}
	
	/**
	 * 
	 * @param inputs The plan's inputs
	 * @param operator
	 * 		The input top-level logical operator
	 * @param parent The parent plan
	 * @param children The input child subplans
	 */
	public TreePlan(Collection<? extends Term> inputs, RelationalOperator operator, DAGPlan parent, List<DAGPlan> children) {
		super(inputs, operator, parent == null ? Lists.<DAGPlan>newArrayList() : Lists.newArrayList(parent), children);
		Preconditions.checkArgument(this.parents.size() <= 1);
	}


	public TreePlan getParent() {
		return (TreePlan) this.parents.get(0);
	}

	/**
	 * 	Adds the input plan in the list of this plan's children
	 */
	@Override
	public void addChild(DAGPlan plan) {
		Preconditions.checkArgument(this.children.size() <= 1);
		this.children.add(plan);
		plan.addParent(this);
		this.leaves.addAll(RelationalOperator.getLeaves(this.operator));
		this.accesses.addAll(RelationalOperator.getAccesses(this.operator));
	}
}
