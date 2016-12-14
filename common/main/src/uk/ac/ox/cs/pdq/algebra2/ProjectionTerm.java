package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.Attribute;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class ProjectionTerm extends RelationalTerm {

	private final RelationalTerm child;

	private final List<Attribute> projections;

	/**  Cashed string representation. */
	private String toString = null;

	/** The hash. */
	private Integer hash = null;

	
	public ProjectionTerm(List<Attribute> projections, RelationalTerm child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
		Preconditions.checkNotNull(projections);
		Preconditions.checkNotNull(child);
		Preconditions.checkArgument(child.getOutputAttributes().containsAll(projections));
		this.projections = projections;
		this.child = child;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Project");
			result.append('{');
			result.append('[').append(Joiner.on(",").join(this.projections)).append(']');
			result.append(this.child.toString());
			result.append('}');
			this.toString = result.toString();
		}
		return this.toString;
	}

	/**
	 * Two selection operators are equal if they have the same predicate and the same child operator 
	 * ("same" in both cases tested with the corresponding equals() method).
	 * 
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return super.equals(o)
				&& this.getClass().isInstance(o)
				&& this.projections.equals(((ProjectionTerm) o).projections)
				&& this.child.equals(((ProjectionTerm) o).child);
	}

	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.projections, this.child);
		}
		return this.hash;
	}

	@Override
	public List<RelationalTerm> getChildren() {
		return ImmutableList.of(this.child);
	}

	/**
	 * @return the projections
	 */
	public List<Attribute> getProjections() {
		return projections;
	}
}
