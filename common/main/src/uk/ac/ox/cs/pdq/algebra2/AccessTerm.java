package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


/**
 *
 * @author Efthymia Tsamoura
 */
public class AccessTerm extends RelationalTerm {

	/** The accessed relation. */
	private final Relation relation;

	/** The access method to use. */
	private final AccessMethod accessMethod;

	/**  The constants used to call the underlying access method. */
	protected final Map<Integer, TypedConstant<?>> inputConstants;

	/**  Cashed string representation. */
	private String toString = null;

	/** The hash. */
	private Integer hash = null;

	public AccessTerm(Relation relation, AccessMethod accessMethod) {
		super(getInputAttributes(relation, accessMethod), relation.getAttributes());
		Preconditions.checkArgument(relation != null);
		Preconditions.checkArgument(accessMethod != null);
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.inputConstants = null;
	}

	public AccessTerm(Relation relation, AccessMethod accessMethod, Map<Integer, TypedConstant<?>> inputConstants) {
		super(getInputAttributes(relation, accessMethod, inputConstants), relation.getAttributes());
		Preconditions.checkArgument(relation != null);
		Preconditions.checkArgument(accessMethod != null);
		Preconditions.checkArgument(inputConstants != null);
		for(Integer position:inputConstants.keySet()) {
			Preconditions.checkArgument(position < relation.getArity());
			Preconditions.checkArgument(accessMethod.getInputs().contains(position));
		}
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.inputConstants = inputConstants;
	}

	private static List<Attribute> getInputAttributes(Relation relation, AccessMethod accessMethod) {
		Preconditions.checkArgument(relation != null);
		Preconditions.checkArgument(accessMethod != null);
		if(accessMethod.getInputs().isEmpty()) {
			return null;
		}
		List<Attribute> inputs = Lists.newArrayList();
		for(Integer i:accessMethod.getInputs()) {
			inputs.add(relation.getAttribute(i));
		}
		return inputs;
	}

	private static List<Attribute> getInputAttributes(Relation relation, AccessMethod accessMethod, Map<Integer, TypedConstant<?>> inputConstants) {
		Preconditions.checkArgument(relation != null);
		Preconditions.checkArgument(accessMethod != null && !accessMethod.getInputs().isEmpty());
		Preconditions.checkArgument(inputConstants != null);
		for(Integer position:inputConstants.keySet()) {
			Preconditions.checkArgument(position < relation.getArity());
			Preconditions.checkArgument(accessMethod.getInputs().contains(position));
		}
		List<Attribute> inputs = Lists.newArrayList();
		for(Integer i:accessMethod.getInputs()) {
			if(!inputConstants.containsKey(i)) {
				inputs.add(relation.getAttribute(i));
			}
		}
		return inputs;
	}

	/**
	 * Gets the relation being accessed
	 *
	 * @return the accessed relation
	 * @see uk.ac.ox.cs.pdq.plan.AccessOperator#getRelation()
	 */
	public Relation getRelation() {
		return this.relation;
	}

	/**
	 * Gets the access method.
	 *
	 * @return the access method used
	 * @see uk.ac.ox.cs.pdq.plan.AccessOperator#getAccessMethod()
	 */
	public AccessMethod getAccessMethod() {
		return this.accessMethod;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Access").append('{');
			result.append(this.relation.getName());
			result.append('[').append(Joiner.on(",").join(this.accessMethod.getInputs())).append(']');
			result.append('}');
			this.toString = result.toString();
		}
		return this.toString;
	}

	/**
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
				&& this.relation.equals(((AccessTerm) o).relation)
				&& this.accessMethod.equals(((AccessTerm) o).accessMethod);
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.relation, this.accessMethod);
		}
		return this.hash;
	}

	@Override
	public List<RelationalTerm> getChildren() {
		return ImmutableList.of();
	}

}
