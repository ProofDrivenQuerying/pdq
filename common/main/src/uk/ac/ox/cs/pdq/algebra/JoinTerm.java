package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;

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

	/**  Cashed string representation. */
	protected String toString = null;
	
	private JoinTerm(RelationalTerm child1, RelationalTerm child2) {
		super(AlgebraUtilities.computeInputAttributes(child1, child2), AlgebraUtilities.computeOutputAttributes(child1, child2));
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
	
    protected static final InterningManager<JoinTerm> s_interningManager = new InterningManager<JoinTerm>() {
        protected boolean equal(JoinTerm object1, JoinTerm object2) {
            for (int index = 1; index >= 0; --index)
                if (!object1.children[index].equals(object2.children[index]))
                    return false;
            return true;
        }

        protected int getHashCode(JoinTerm object) {
            int hashCode = 0;
            for (int index = 1; index >= 0; --index)
                hashCode = hashCode * 7 + object.children[index].hashCode();
            return hashCode;
        }
    };
    
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }
    
    public static JoinTerm create(RelationalTerm child1, RelationalTerm child2) {
        return s_interningManager.intern(new JoinTerm(child1, child2));
    }
}
