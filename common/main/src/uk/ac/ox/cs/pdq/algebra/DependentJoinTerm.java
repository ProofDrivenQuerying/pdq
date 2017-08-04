package uk.ac.ox.cs.pdq.algebra;

import java.util.Arrays;

import org.junit.Assert;


/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class DependentJoinTerm extends RelationalTerm {
	protected static final long serialVersionUID = 3160309108592668317L;

	protected final RelationalTerm[] children = new RelationalTerm[2];

	/** The join conditions. */
	protected final Condition joinConditions;
	
	/** Input positions for the right hand child**/
	protected final Integer[] sidewaysInput;

	/**  Cashed string representation. */
	protected String toString = null;

	private DependentJoinTerm(RelationalTerm child1, RelationalTerm child2) {
		super(AlgebraUtilities.computeInputAttributes(child1, child2, AlgebraUtilities.computePositionsOfInputAttributes(child1, child2)), AlgebraUtilities.computeOutputAttributes(child1, child2));
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		for(int inputAttributeIndex = 0; inputAttributeIndex < child2.getNumberOfInputAttributes(); ++inputAttributeIndex) 
			Assert.assertTrue(Arrays.asList(child1.getOutputAttributes()).contains(child2.getInputAttributes()[inputAttributeIndex]));
		this.children[0] = child1;
		this.children[1] = child2;
		this.sidewaysInput = AlgebraUtilities.computePositionsOfInputAttributes(child1, child2);
		this.joinConditions = AlgebraUtilities.computeJoinConditions(this.children);
	}

	public Condition getPredicate() {
		return this.joinConditions;
	}
	
	public Integer[] getSidewaysInput() {
		return this.sidewaysInput.clone();
	}
	
	public Condition getJoinConditions() {
		return this.joinConditions;
	}

	@Override
	public String toString() {
		if(this.toString == null) {
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
        return Cache.dependentJoinTerm.intern(new DependentJoinTerm(child1, child2));
    }
    
	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0 || childIndex == 1);
		return this.children[0];
	}
}
