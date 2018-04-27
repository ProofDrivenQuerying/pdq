package uk.ac.ox.cs.pdq.algebra;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Typed;

public abstract class ConstantComparisonCondition extends SimpleCondition {

	private static final long serialVersionUID = 6311780941534105378L;
	
	/**  The value to which the tuple must equal at the given position. */
	protected final TypedConstant constant;

	protected ConstantComparisonCondition(int position, TypedConstant constant) {
		super(position);
		Preconditions.checkNotNull(constant);
		this.constant = constant;
	}

	public TypedConstant getConstant() {
		return this.constant;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('#').append(this.position).append('=');
		result.append(this.constant);
		return result.toString();
	}

	@Override
	public boolean isSatisfied(Typed[] typeds) {
		Preconditions.checkElementIndex(this.position, typeds.length, "Condition position index");

		// Return true iff the type of the TypedConstant in this object is equal
		// to that in the given array at index this.position.
		return this.compareTypes(typeds[this.position].getType(), this.getConstant().getType());
	}
}
