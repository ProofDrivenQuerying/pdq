package uk.ac.ox.cs.pdq.algebra;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.TypedConstant;

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

}
