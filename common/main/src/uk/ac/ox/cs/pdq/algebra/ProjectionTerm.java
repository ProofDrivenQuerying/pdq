package uk.ac.ox.cs.pdq.algebra;

import java.util.Arrays;

import org.junit.Assert;

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
	
    public static ProjectionTerm create(Attribute[] projections, RelationalTerm child) {
        return Cache.projectionTerm.intern(new ProjectionTerm(projections, child));
    }
    
	@Override
	public RelationalTerm getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}
}
