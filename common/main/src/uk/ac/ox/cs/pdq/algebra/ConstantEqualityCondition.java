package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.TypedConstant;

/**
 * Represents a constant equality condition, that can be used to create -for example- a SelectionTerm.
 *  
 * @author Unknown
 * @author Gabor
 */
public class ConstantEqualityCondition extends SimpleCondition {
	private static final long serialVersionUID = 1040523371703814834L;

	/**  The position of the tuple, whose value we will compare. */
	protected final Integer position;

	/**  The value to which the tuple must equal at the given position. */
	protected final TypedConstant constant;

	private ConstantEqualityCondition(int position, TypedConstant constant) {
		Assert.assertTrue(position >= 0 && constant != null);
		this.position = position;
		this.constant = constant;
	}

	public int getPosition() {
		return this.position;
	}

	public TypedConstant getConstant() {
		return this.constant;
	}
	
    public static ConstantEqualityCondition create(int position, TypedConstant constant) {
        return Cache.constantEqualityCondition.retrieve(new ConstantEqualityCondition(position, constant));
    }

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('#').append(this.position).append('=');
		result.append(this.constant);
		return result.toString();
	}
}
