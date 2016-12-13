package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.Attribute;

import com.google.common.base.Preconditions;


/**
 * OpenProjection operator.
 *
 * @author Julien Leblay
 */
public class Projection implements RelationalOperator{

	private final List<Attribute> inputAttributes;
	
	private final List<Attribute> outputAttributes;

	public Projection(List<Attribute> inputAttributes, List<Attribute> outputAttributes) {
		Preconditions.checkArgument(inputAttributes != null && !inputAttributes.isEmpty());
		Preconditions.checkArgument(outputAttributes != null && !outputAttributes.isEmpty());
		this.inputAttributes = inputAttributes;
		this.outputAttributes = outputAttributes;
	}
	
	public List<Attribute> getOutputAttributes() {
		return this.getOutputAttributes();
	}
	
	/**
	 * @return the inputAttributes
	 */
	public List<Attribute> getInputAttributes() {
		return inputAttributes;
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
				&& this.inputAttributes.equals(((Projection) o).inputAttributes)
				&& this.outputAttributes.equals(((Projection) o).outputAttributes);

	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.inputAttributes, this.outputAttributes);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return null;
	}

	@Override
	public int getArity() {
		// TODO Auto-generated method stub
		return 1;
	}
}
