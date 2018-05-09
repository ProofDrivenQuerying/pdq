package uk.ac.ox.cs.pdq.runtime.conditions;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

public class TypeEqualityCondition extends SimpleCondition {

	private static final long serialVersionUID = 2836867834529357L;

	/**  The second position in the equality type comparison. */
	protected final Integer other;
	
	protected TypeEqualityCondition(Integer position, Integer other) {
		super(position);
		Preconditions.checkArgument(other >= 0);
		this.other = other;
	}

	public int getOther() {
		return this.other;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('#').append(this.position).append('=').append('#').append(this.other);
		return result.toString();
	}
	
    public static TypeEqualityCondition create(int position, int other) {
    	//throw new RuntimeException("NotImplemented");
        return new TypeEqualityCondition(position, other);
    }

	@Override
	public boolean isSatisfied(Tuple tuple) {
		Preconditions.checkArgument(tuple.size() > Math.max(this.position, this.other));
		
		// Return true iff the types at indices 'position' and 'other' are equal, ignoring the values.
		TupleType tupleType = tuple.getType();
		return this.compareTypes(tupleType.getType(this.other), tupleType.getType(this.position));
	}
}