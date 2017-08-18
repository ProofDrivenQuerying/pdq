package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class RenameTerm extends RelationalTerm {
	protected static final long serialVersionUID = -5225689808613760428L;

	protected final RelationalTerm child;

	protected final Attribute[] renamings;

	/**  Cashed string representation. */
	private String toString = null;

	private RenameTerm(Attribute[] renamings, RelationalTerm child) {
		super(AlgebraUtilities.computeRenamedInputAttributes(renamings, child), renamings);
		Assert.assertNotNull(renamings);
		Assert.assertNotNull(child);
		Assert.assertTrue(renamings.length == child.getOutputAttributes().length);
		this.renamings = renamings.clone();
		this.child = child;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Rename");
			result.append('{');
			result.append('[');
			for(int index = 0; index < this.renamings.length; ++index) {
				result.append(this.renamings[index]);
				if(index < this.renamings.length - 1)
					result.append(",");
			}
			result.append(']');
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

	public Attribute[] getRenamings() {
		return this.renamings.clone();
	}
	
    public static RenameTerm create(Attribute[] renamings, RelationalTerm child) {
        return Cache.renameTerm.retrieve(new RenameTerm(renamings, child));
    }
    
	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}
}
