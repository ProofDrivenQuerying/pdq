package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class SelectionTerm extends RelationalTerm {

	private static final long serialVersionUID = 3979377421532058418L;

	private final RelationalTerm child;

	/** The predicate associated with this selection. */
	private final Condition predicate;

	/**  Cashed string representation. */
	private String toString = null;

	private SelectionTerm(Condition predicate, RelationalTerm child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
		Assert.assertNotNull(predicate);
		Assert.assertNotNull(child);
		this.predicate = predicate;
		this.child = child;
	}

	public Condition getPredicate() {
		return this.predicate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Select");
			result.append('{');
			result.append('[').append(this.predicate).append(']');
			result.append(this.child.toString());
			result.append('}');
			this.toString = result.toString();
		}
		return this.toString;
	}

	@Override
	public RelationalTerm[] getChildren() {
		RelationalTerm[] children = new RelationalTerm[1];
		children[0] = this.child;
		return children;
	}
	
    protected static final InterningManager<SelectionTerm> s_interningManager = new InterningManager<SelectionTerm>() {
        protected boolean equal(SelectionTerm object1, SelectionTerm object2) {
            if (!object1.child.equals(object2.child) ||!object1.predicate.equals(object2.predicate))
                return false;
            return true;
        }

        protected int getHashCode(SelectionTerm object) {
            int hashCode = object.child.hashCode() + object.predicate.hashCode() * 7;
            return hashCode;
        }
    };
    
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    public static SelectionTerm create(Condition predicate, RelationalTerm child) {
        return s_interningManager.intern(new SelectionTerm(predicate, child));
    }
}
