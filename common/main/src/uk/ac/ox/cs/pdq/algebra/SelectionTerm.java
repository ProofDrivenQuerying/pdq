package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class SelectionTerm extends RelationalTerm {

	private static final long serialVersionUID = 3979377421532058418L;

	protected final RelationalTerm child;

	/** The predicate associated with this selection. */
	protected final Condition selectionCondition;

	/**  Cashed string representation. */
	private String toString = null;

	private SelectionTerm(Condition selectionCondition, RelationalTerm child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
		Assert.assertNotNull(selectionCondition);
		Assert.assertNotNull(child);
		Assert.assertTrue(AlgebraUtilities.assertSelectionCondition(selectionCondition, child.getOutputAttributes()));
		this.selectionCondition = selectionCondition;
		this.child = child;
	}

	public Condition getSelectionCondition() {
		return this.selectionCondition;
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
			result.append('[').append(this.selectionCondition).append(']');
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
	
    public static SelectionTerm create(Condition predicate, RelationalTerm child) {
        return Cache.selectionTerm.retrieve(new SelectionTerm(predicate, child));
    }
    
	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}
	
	@Override
	public Integer getNumberOfChildren() {
		return 1;
	}
}
