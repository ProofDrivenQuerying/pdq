package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;


/**
 * Logical operator representation of an access.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Access implements RelationalOperator, AccessOperator {

	/** The input table of the access. */
	private final Relation relation;

	/** The access method to use. */
	private final AccessMethod accessMethod;

	private final List<Attribute> inputAttributes;

	private final List<Attribute> outputAttributes;

	public Access(Relation relation, AccessMethod accessMethod, List<Attribute> outputAttributes) {
		Preconditions.checkArgument(relation != null);
		Preconditions.checkArgument(accessMethod != null);
		Preconditions.checkArgument(accessMethod.getInputs().isEmpty());
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.inputAttributes = null;
		this.outputAttributes = outputAttributes;
	}

	public Access(Relation relation, AccessMethod accessMethod, List<Attribute> inputAttributes, List<Attribute> outputAttributes) {
		Preconditions.checkArgument(relation != null);
		Preconditions.checkArgument(accessMethod != null);
		Preconditions.checkArgument(inputAttributes.size() == accessMethod.getInputs().size());
		Preconditions.checkArgument(outputAttributes.size() == relation.getArity());
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.inputAttributes = inputAttributes;
		this.outputAttributes = outputAttributes;
	}

	/**
	 * Gets the relation being accessed
	 *
	 * @return the accessed relation
	 * @see uk.ac.ox.cs.pdq.plan.AccessOperator#getRelation()
	 */
	@Override
	public Relation getRelation() {
		return this.relation;
	}

	/**
	 * Gets the access method.
	 *
	 * @return the access method used
	 * @see uk.ac.ox.cs.pdq.plan.AccessOperator#getAccessMethod()
	 */
	@Override
	public AccessMethod getAccessMethod() {
		return this.accessMethod;
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
		result.append(']').append('}');
		return result.toString();
	}

	/**
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
				&& this.inputAttributes.equals(((Access) o).inputAttributes)
				&& this.outputAttributes.equals(((Access) o).outputAttributes);
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.relation, this.accessMethod, this.inputAttributes, this.outputAttributes);
	}

	@Override
	public int getArity() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @return the inputAttributes
	 */
	public List<Attribute> getInputAttributes() {
		return inputAttributes;
	}

	/**
	 * @return the outputAttributes
	 */
	public List<Attribute> getOutputAttributes() {
		return outputAttributes;
	}

}
