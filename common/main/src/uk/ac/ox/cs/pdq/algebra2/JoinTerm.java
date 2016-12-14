package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.db.Attribute;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class JoinTerm extends RelationalTerm {

	private final List<RelationalTerm> children;

	/** The predicate associated with this selection. */
	private final Predicate predicate;

	/**  Cashed string representation. */
	private String toString = null;

	/** The hash. */
	private Integer hash = null;

	public JoinTerm(Predicate predicate, RelationalTerm child1, RelationalTerm child2) {
		super(getInputAttributes(child1, child2), getOutputAttributes(child1, child2));
		Preconditions.checkNotNull(predicate);
		Preconditions.checkNotNull(child1);
		Preconditions.checkNotNull(child2);
		Preconditions.checkArgument(!CollectionUtils.containsAny(getProperOutputAttributes(child1), child2.getInputAttributes()));
		for(Predicate p:predicate.getEqualityPredicates()) {
			Preconditions.checkArgument(p instanceof AttributeEqualityPredicate);
			Preconditions.checkArgument(((AttributeEqualityPredicate)p).getPosition() < child1.getOutputAttributes().size());
			Preconditions.checkArgument(((AttributeEqualityPredicate)p).getOther() < child2.getOutputAttributes().size());
		}
		this.predicate = predicate;
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
	
	private static List<Attribute> getProperOutputAttributes(RelationalTerm child) {
		Preconditions.checkNotNull(child);
		List<Attribute> output = Lists.newArrayList();
		output.addAll(child.getOutputAttributes());
		output.removeAll(child.getInputAttributes());
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

	/**
	 * Gets the predicate associated with this selection. .
	 *
	 * @return the predicate of this selection
	 * @see uk.ac.ox.cs.pdq.algebra.PredicateBasedOperator#getPredicate()
	 */
	public Predicate getPredicate() {
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
				&& this.predicate.equals(((JoinTerm) o).predicate)
				&& this.children.equals(((JoinTerm) o).children);
	}

	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.predicate, this.children);
		}
		return this.hash;
	}

	@Override
	public List<RelationalTerm> getChildren() {
		return this.children;
	}
}
