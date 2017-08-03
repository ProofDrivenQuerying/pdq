package uk.ac.ox.cs.pdq.algebra;

import java.util.Arrays;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;
import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class ProjectionTerm extends RelationalTerm {
	protected static final long serialVersionUID = -1073141016751509636L;

	protected final RelationalTerm child;

	protected final Attribute[] projections;

	protected String toString = null;
	
	private ProjectionTerm(Attribute[] projections, RelationalTerm child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
		Assert.assertNotNull(projections);
		Assert.assertNotNull(child);
		for(int outputAttributeIndex = 0; outputAttributeIndex < child.getNumberOfOutputAttributes(); ++outputAttributeIndex) 
			Assert.assertTrue(Arrays.asList(projections).contains(child.getOutputAttributes()[outputAttributeIndex]));

		this.projections = projections.clone();
		this.child = child;
	}

	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Project");
			result.append('{');
			result.append('[');
			for(int index = 0; index < this.projections.length; ++index) {
				result.append(this.projections[index]);
				if(index < this.projections.length - 1)
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

	public Attribute[] getProjections() {
		return this.projections.clone();
	}
	
    protected static final InterningManager<ProjectionTerm> s_interningManager = new InterningManager<ProjectionTerm>() {
        protected boolean equal(ProjectionTerm object1, ProjectionTerm object2) {
            if (!object1.child.equals(object2.child) || object1.projections.length != object2.projections.length)
                return false;
            for (int index = object1.projections.length - 1; index >= 0; --index)
                if (!object1.projections[index].equals(object2.projections[index]))
                    return false;
            return true;
        }

        protected int getHashCode(ProjectionTerm object) {
            int hashCode = object.child.hashCode();
            for (int index = object.projections.length - 1; index >= 0; --index)
                hashCode = hashCode * 7 + object.projections[index].hashCode();
            return hashCode;
        }
    };
    
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    public static ProjectionTerm create(Attribute[] projections, RelationalTerm child) {
        return s_interningManager.intern(new ProjectionTerm(projections, child));
    }
    
	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}
}
