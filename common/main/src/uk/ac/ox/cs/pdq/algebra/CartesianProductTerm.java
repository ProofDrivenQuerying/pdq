package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;


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
	
    protected static final InterningManager<CartesianProductTerm> s_interningManager = new InterningManager<CartesianProductTerm>() {
        protected boolean equal(CartesianProductTerm object1, CartesianProductTerm object2) {
            for (int index = 1; index >= 0; --index)
                if (!object1.children[index].equals(object2.children[index]))
                    return false;
            return true;
        }

        protected int getHashCode(CartesianProductTerm object) {
            int hashCode = 0;
            for (int index = 1; index >= 0; --index)
                hashCode = hashCode * 7 + object.children[index].hashCode();
            return hashCode;
        }
    };
    
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    public static CartesianProductTerm create(RelationalTerm child1, RelationalTerm child2) {
        return s_interningManager.intern(new CartesianProductTerm(child1, child2));
    }

	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0 || childIndex == 1);
		return this.children[0];
	}
}
