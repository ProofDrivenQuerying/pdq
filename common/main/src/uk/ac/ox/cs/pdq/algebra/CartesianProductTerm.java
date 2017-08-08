package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;


/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class CartesianProductTerm extends RelationalTerm {
	protected static final long serialVersionUID = -8806125496554968085L;

	protected final RelationalTerm[] children = new RelationalTerm[2];

	/**  Cashed string representation. */
	protected String toString = null;

	private CartesianProductTerm(RelationalTerm child1, RelationalTerm child2) {
		super(AlgebraUtilities.computeInputAttributes(child1, child2), AlgebraUtilities.computeOutputAttributes(child1, child2));
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		this.children[0] = child1;
		this.children[1] = child2;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("CartesianProduct");
			result.append('{');
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
	
    public static CartesianProductTerm create(RelationalTerm child1, RelationalTerm child2) {
        return Cache.cartesianProductTerm.retrieve(new CartesianProductTerm(child1, child2));
    }

	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0 || childIndex == 1);
		return this.children[0];
	}
}
