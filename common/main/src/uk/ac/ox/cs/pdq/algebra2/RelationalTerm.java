package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.Attribute;

import com.google.common.base.Preconditions;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public abstract class RelationalTerm {
		
	protected final List<Attribute> inputAttributes;
	
	protected final List<Attribute> outputAttributes;

	public RelationalTerm(List<Attribute> inputAttributes, List<Attribute> outputAttributes) {
		Preconditions.checkArgument(outputAttributes != null && !outputAttributes.isEmpty());
		this.inputAttributes = inputAttributes;
		this.outputAttributes = outputAttributes;
	}
	
	public List<Attribute> getOutputAttributes() {
		return this.outputAttributes;
	}
	
	public List<Attribute> getInputAttributes() {
		return this.inputAttributes;
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
				&& this.inputAttributes == ((RelationalTerm) o).inputAttributes
				&& this.outputAttributes == ((RelationalTerm) o).outputAttributes;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.inputAttributes, this.outputAttributes);
	}
	
}
