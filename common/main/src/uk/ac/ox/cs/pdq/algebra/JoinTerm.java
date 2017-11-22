package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class JoinTerm extends RelationalTerm {
	protected static final long serialVersionUID = -2424275295263353630L;

	protected final RelationalTerm[] children = new RelationalTerm[2];

	/** The join conditions. */
	protected final Condition joinConditions;

	/**  Cached string representation. */
	protected String toString = null;
	
	private JoinTerm(RelationalTerm child1, RelationalTerm child2) {
		super(AlgebraUtilities.computeInputAttributes(child1, child2), 
				AlgebraUtilities.computeOutputAttributes(child1, child2));
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		this.children[0] = child1;
		this.children[1] = child2;
		this.joinConditions = AlgebraUtilities.computeJoinConditions(this.children);
	}


	public Condition getJoinConditions() {
		return this.joinConditions;
	}

	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Join");
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
    
    public static JoinTerm create(RelationalTerm child1, RelationalTerm child2) {
        return Cache.joinTerm.retrieve(new JoinTerm(child1, child2));
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
