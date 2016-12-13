package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.base.Preconditions;

/**
 * RelationalOperator defines a top-class for all logical relational operators.
 *
 * @author Julien Leblay
 */
public abstract class RelationalTerm {
	
	/** The operator's type. */
	protected final TupleType outputType;
	
	protected final List<Attribute> outputAttributes;
	
	protected final RelationalOperator operator;
	
	/**
	 * Instantiates a new operator.
	 * @param input TupleType
	 * @param output TupleType
	 */
	public RelationalTerm(RelationalOperator operator, List<Attribute> outputAttributes) {
		Preconditions.checkArgument(operator != null);
		Preconditions.checkArgument(outputAttributes != null && !outputAttributes.isEmpty());
		this.operator = operator;
		this.outputAttributes = outputAttributes;
		this.outputType = null;
	}

	/**
	 * Gets the output tuple type.
	 *
	 * @return the tuple Type of this operator
	 */
	public TupleType getOutputType() {
		return this.outputType;
	}
	
	public List<Attribute> getOutputAttributes() {
		return this.outputAttributes;
	}
	
	public abstract List<RelationalTerm> getChildren();

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.outputType == ((RelationalTerm) o).outputType
				&& this.outputAttributes == ((RelationalTerm) o).outputAttributes;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.outputType, this.outputAttributes);
	}
	
}
