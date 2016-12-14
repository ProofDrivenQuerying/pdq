package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.Attribute;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class CartesianProductTerm extends RelationalTerm {

	private final List<RelationalTerm> children;

	/**  Cashed string representation. */
	private String toString = null;

	/** The hash. */
	private Integer hash = null;

	public CartesianProductTerm(RelationalTerm child1, RelationalTerm child2) {
		super(getInputAttributes(child1, child2), getOutputAttributes(child1, child2));
		Preconditions.checkNotNull(child1);
		Preconditions.checkNotNull(child2);
		this.children = ImmutableList.of(child1, child2);
	}
	
	private static List<Attribute> getInputAttributes(RelationalTerm child1, RelationalTerm child2) {
		Preconditions.checkNotNull(child1);
		Preconditions.checkNotNull(child2);
		List<Attribute> output = Lists.newArrayList();
		output.addAll(child1.getInputAttributes());
		output.addAll(child2.getInputAttributes());
		return output;
	}
	
	private static List<Attribute> getOutputAttributes(RelationalTerm child1, RelationalTerm child2) {
		Preconditions.checkNotNull(child1);
		Preconditions.checkNotNull(child2);
		List<Attribute> output = Lists.newArrayList();
		output.addAll(child1.getOutputAttributes());
		output.addAll(child2.getOutputAttributes());
		return output;
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
			result.append(this.children.get(0).toString());
			result.append(',');
			result.append(this.children.get(1).toString());
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
				&& this.children.equals(((CartesianProductTerm) o).children);
	}

	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.children);
		}
		return this.hash;
	}

	@Override
	public List<RelationalTerm> getChildren() {
		return this.children;
	}
}
