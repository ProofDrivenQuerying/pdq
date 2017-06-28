package uk.ac.ox.cs.pdq.algebra;

import java.util.Arrays;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;


/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class DependentJoinTerm extends RelationalTerm {
	protected static final long serialVersionUID = 3160309108592668317L;

	protected final RelationalTerm[] children = new RelationalTerm[2];

	/** The predicate associated with this selection. */
	protected final Condition predicate;

	/**  Cashed string representation. */
	protected String toString = null;

	protected DependentJoinTerm(Condition predicate, RelationalTerm child1, RelationalTerm child2) {
		super(AlgebraUtilities.getInputAttributes(child1, child2), AlgebraUtilities.getOutputAttributes(child1, child2));
		Assert.assertNotNull(predicate);
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		for(int inputAttributeIndex = 0; inputAttributeIndex < child2.getNumberOfInputAttributes(); ++inputAttributeIndex) 
			Assert.assertTrue(Arrays.asList(child1.getOutputAttributes()).contains(child2.getInputAttributes()[inputAttributeIndex]));
		this.predicate = predicate;
		this.children[0] = child1;
		this.children[1] = child2;
	}

	public Condition getPredicate() {
		return this.predicate;
	}

	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("DependentJoin");
			result.append('{');
			result.append('[').append(this.predicate).append(']');
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
	
    protected static final InterningManager<DependentJoinTerm> s_interningManager = new InterningManager<DependentJoinTerm>() {
        protected boolean equal(DependentJoinTerm object1, DependentJoinTerm object2) {
            for (int index = 1; index >= 0; --index)
                if (!object1.children[index].equals(object2.children[index]))
                    return false;
            return true;
        }

        protected int getHashCode(DependentJoinTerm object) {
            int hashCode = 0;
            for (int index = 1; index >= 0; --index)
                hashCode = hashCode * 7 + object.children[index].hashCode();
            return hashCode;
        }
    };
    
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    public static DependentJoinTerm create(Condition predicate, RelationalTerm child1, RelationalTerm child2) {
        return s_interningManager.intern(new DependentJoinTerm(predicate, child1, child2));
    }
}
