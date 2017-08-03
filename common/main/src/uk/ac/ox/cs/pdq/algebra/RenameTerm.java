package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;
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
		super(child.getInputAttributes(), renamings);
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
	
    protected static final InterningManager<RenameTerm> s_interningManager = new InterningManager<RenameTerm>() {
        protected boolean equal(RenameTerm object1, RenameTerm object2) {
            if (!object1.child.equals(object2.child) || object1.renamings.length != object2.renamings.length)
                return false;
            for (int index = object1.renamings.length - 1; index >= 0; --index)
                if (!object1.renamings[index].equals(object2.renamings[index]))
                    return false;
            return true;
        }

        protected int getHashCode(RenameTerm object) {
            int hashCode = object.child.hashCode();
            for (int index = object.renamings.length - 1; index >= 0; --index)
                hashCode = hashCode * 7 + object.renamings[index].hashCode();
            return hashCode;
        }
    };
    
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    public static RenameTerm create(Attribute[] renamings, RelationalTerm child) {
        return s_interningManager.intern(new RenameTerm(renamings, child));
    }
    
	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}
}
