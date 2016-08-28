package uk.ac.ox.cs.pdq.algebra;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Logical operator representation of an access.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */

@Deprecated
public class Access extends UnaryOperator implements AccessOperator {

	/** The input table of the access. */
	private final Relation relation;

	/** The access method to use. */
	private final AccessMethod accessMethod;
	
	/**  The output terms of this access. */
	protected final List<Term> outputTerms;

	/**
	 * Instantiates a new access.
	 *
	 * @param relation Relation
	 * @param accessMethod AccessMethod
	 */
	public Access(Relation relation, AccessMethod accessMethod) {
		this(relation, accessMethod, getOutputTerms(relation), null);
	}

	/**
	 * Instantiates a new access.
	 *
	 * @param relation Relation
	 * @param accessMethod AccessMethod
	 * @param child LogicalOperator
	 */
	public Access(Relation relation, AccessMethod accessMethod, RelationalOperator child) {
		this(relation, accessMethod, getOutputTerms(relation), child);
	}
	
	/**
	 * Instantiates a new access.
	 *
	 * @param relation Relation
	 * @param accessMethod AccessMethod
	 * @param outputTerms the output terms
	 * @param child LogicalOperator
	 */
	public Access(Relation relation, AccessMethod accessMethod, List<Term> outputTerms, RelationalOperator child) {
		super(	(child == null || child instanceof SubPlanAlias) ? TupleType.EmptyTupleType : child.getInputType(),
				(child == null || child instanceof SubPlanAlias) ? Lists.<Term>newArrayList() : child.getInputTerms(),
						TupleType.DefaultFactory.createFromTyped(attributesOf(relation)),
						Utility.typedToTerms(attributesOf(relation)),
						child);
		Preconditions.checkArgument(relation != null);
		Preconditions.checkArgument(accessMethod != null);
		Preconditions.checkArgument(relation.getAccessMethods().contains(accessMethod));
		Preconditions.checkArgument(child == null ?
				(accessMethod.getType() == Types.FREE ? this.inputType.equals(TupleType.EmptyTupleType) : true) :
				(accessMethod.getType() != Types.FREE && accessMethod.getInputs().size() >= child.getInputType().size()));
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.outputTerms = Lists.newArrayList(outputTerms);
	}
	
	/**
	 * Attributes of.
	 *
	 * @param relation the relation
	 * @return the list
	 */
	private static List<Attribute> attributesOf(Relation relation) {
		Preconditions.checkArgument(relation != null);
		return relation.getAttributes();
	}
	
	/**
	 * Gets the output terms.
	 *
	 * @param relation the relation
	 * @return the output terms
	 */
	private static List<Term> getOutputTerms(Relation relation) {
		Preconditions.checkArgument(relation != null);
		return Utility.typedToTerms(relation.getAttributes());
	}

	/**
	 * Gets the relation.
	 *
	 * @return the relation accessed by this operator.
	 * @see uk.ac.ox.cs.pdq.plan.AccessOperator#getRelation()
	 */
	@Override
	public Relation getRelation() {
		return this.relation;
	}

	/**
	 * Gets the access method.
	 *
	 * @return the access method used by this operator.
	 * @see uk.ac.ox.cs.pdq.plan.AccessOperator#getAccessMethod()
	 */
	@Override
	public AccessMethod getAccessMethod() {
		return this.accessMethod;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#deepCopy()
	 */
	@Override
	public Access deepCopy() throws RelationalOperatorException {
		return new Access(this.relation, this.accessMethod, this.outputTerms, this.child);
	}

	/**
	 * Sets the child.
	 *
	 * @param c LogicalOperator
	 */
	public void setChild(RelationalOperator c) {
		Preconditions.checkArgument(c != null);
		Preconditions.checkArgument(c.getType().size() == this.accessMethod.getInputs().size(), "Inputs: " + this.accessMethod + ", Child operator: " + c.getColumns());
		this.child = c;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName()).append('{');
		result.append(this.relation.getName()).append('[');
		result.append(Joiner.on(",").join(this.accessMethod.getInputs()));
		result.append(']').append('}').append('(');
		result.append(this.child).append(')');
		return result.toString();
	}

	/**
	 * Equals.
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		return super.equals(o)
				&& this.getClass().isInstance(o)
				&& this.relation.equals(((Access) o).relation)
				&& this.accessMethod.equals(((Access) o).accessMethod)
				;

	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.outputType, this.inputType, this.child,
				this.columns, this.inputTerms, this.relation, this.accessMethod,
				this.metadata);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.UnaryOperator#getDepth()
	 */
	@Override
	public Integer getDepth() {
		Integer result = (this.child == null ? 0 : this.child.getDepth());
		assert result >= 0;
		return result + 1;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.UnaryOperator#isClosed()
	 */
	@Override
	public boolean isClosed() {
		return this.child == null || this.child.isClosed();
	}

	/*
	 * 
	 */
	@Override
	public boolean isJoinFree() {
		return this.child == null || this.child.isJoinFree();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.UnaryOperator#isLeftDeep()
	 */
	@Override
	public boolean isLeftDeep() {
		return this.child == null || this.child.isLeftDeep();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.UnaryOperator#isRightDeep()
	 */
	@Override
	public boolean isRightDeep() {
		return this.child == null || this.child.isRightDeep();
	}
}
