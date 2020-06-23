// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * Dependent join term represents a dependent join where at least one of the
 * outputs of the left side are used as input for the right side. Both left and
 * right side can have other inputs.
 * 
 * @author Efthymia Tsamoura
 *
 */
public class DependentJoinTerm extends JoinTerm {
	protected static final long serialVersionUID = 3160309108592668317L;

	protected final RelationalTerm[] children = new RelationalTerm[2];

	/** The join conditions. */
	protected final Condition joinConditions;

	/** Input positions for the right hand child **/
	protected final Map<Integer, Integer> positionsInRightChildThatAreBoundFromLeftChild;

	/** Cached string representation. */
	protected String toString = null;

	/**
	 * Will create a dependent join term based on the left and right child's input
	 * and output attributes. A connection between these are made when an output
	 * attribute on the left (child1) has the same name as an input attribute on the
	 * right (child2)
	 * 
	 * @param child1
	 * @param child2
	 */
	private DependentJoinTerm(RelationalTerm child1, RelationalTerm child2) {
		this(child1, child2, JoinTerm.computeJoinConditions(new RelationalTerm[] { child1, child2 }));
	}
	
	/**
	 * This constructor can be used when the left and right side attribute names does not match. The join condition computed externally.
	 *  
	 * @param child1
	 * @param child2
	 * @param joinConditions
	 */
	private DependentJoinTerm(RelationalTerm child1, RelationalTerm child2,Condition joinConditions) {
		super(child1, child2,joinConditions, true);
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		// The first child most have at least one output that can be used as an input
		// for the second.
		if (!CollectionUtils.containsAny(Arrays.asList(child1.getOutputAttributes()),
				Arrays.asList(child2.getInputAttributes()))) {
			Assert.assertTrue(CollectionUtils.containsAny(Arrays.asList(child1.getOutputAttributes()),
					Arrays.asList(child2.getInputAttributes())));
		}
		this.children[0] = child1;
		this.children[1] = child2;
		this.positionsInRightChildThatAreBoundFromLeftChild = JoinTerm
				.computePositionsInRightChildThatAreBoundFromLeftChild(child1, child2);
		this.joinConditions = JoinTerm.computeJoinConditions(this.children);
	}

	public Map<Integer, Integer> getPositionsInLeftChildThatAreInputToRightChild() {
		return this.positionsInRightChildThatAreBoundFromLeftChild;
	}

	public Condition getJoinConditions() {
		return this.joinConditions;
	}

	/**
	 * Returns true iff the given input attribute in the right child is bound from
	 * the left child.
	 * 
	 * @return true iff the given attribute is bound from the left child.
	 * @throws IllegalArgumentException if the given attribute is not an input
	 *                                  attribute in the right child.
	 */
	public boolean isBound(Attribute attribute) {

		Preconditions.checkArgument(Arrays.asList(this.getChild(1).getInputAttributes()).contains(attribute));
		return this.joinMap().containsValue(attribute);
	}

	/**
	 * Returns an array containing those input attribute in the right child that are
	 * bound from the left child.
	 * 
	 * @return An array of attributes.
	 */
	public Attribute[] boundAttributes() {
		return Arrays.stream(this.getChild(1).getInputAttributes()).filter(attr -> this.isBound(attr))
				.toArray(Attribute[]::new);
	}

	@Override
	public String toString() {
		if (this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("DependentJoin");
			result.append('{');
			result.append('[').append(this.joinConditions).append(']');
			result.append(this.children[0].toString());
			result.append(',');
			result.append(this.children[1].toString());
			result.append('}');
			this.toString = result.toString();
		}
		return this.toString;
	}

	@Override
	public RelationalTerm[] getChildren() {
		return this.children.clone();
	}

	public static DependentJoinTerm create(RelationalTerm child1, RelationalTerm child2) {
		return Cache.dependentJoinTerm.retrieve(new DependentJoinTerm(child1, child2));
	}
	
    public static JoinTerm create(RelationalTerm child1, RelationalTerm child2, Condition joinConditions) {
        return Cache.dependentJoinTerm.retrieve(new DependentJoinTerm(child1, child2, joinConditions));
    }

	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0 || childIndex == 1);
		return this.children[childIndex];
	}

	@Override
	public Integer getNumberOfChildren() {
		return this.children.length;
	}
}
