package uk.ac.ox.cs.pdq.algebra;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.util.Tuple;


/**
 * Compares the values at two given positions in a tuple.
 *
 * @author Julien Leblay
 */
public class AttributeEqualityCondition extends SimpleCondition {
	private static final long serialVersionUID = 590156716681307220L;

	/**  The second position in the equality comparison. */
	protected final Integer other;

	private AttributeEqualityCondition(Integer position, Integer other) {
		super(position);
		Assert.assertTrue(position >= 0 && other >= 0);
		this.other = other;
	}

	public int getPosition() {
		return this.position;
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
	
    public static AttributeEqualityCondition create(int position, int other) {
        return Cache.attributeEqualityCondition.retrieve(new AttributeEqualityCondition(position, other));
    }

 // Return true iff the tuple value at the 'position' index is less than that
 	// of the constant value in this condition, if the lessThan field is true.
 	// Otherwise do the same but test for a tuple value that is greater than the
 	// constant.
 	@Override
 	public boolean isSatisfied(Tuple tuple) {
 		Preconditions.checkArgument(tuple.size() > this.position);
 		Preconditions.checkArgument(tuple.size() > this.other);

 		Object value = tuple.getValue(this.position);
 		Object targetValue = tuple.getValue(this.other);
 		if (value == null || targetValue == null)
 			return false;

 		if (!(value instanceof Comparable<?> && targetValue instanceof Comparable<?>)) {
 			throw new RuntimeException("Incomparable values:" + value + " and " + targetValue);
 		}
 		int comparison;
 		try {
 			Method m = Comparable.class.getMethod("compareTo", Object.class);
 			comparison = (int) m.invoke(value, targetValue);
 		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
 			throw new RuntimeException("Failed to check isSatisfied on " + tuple,e);
 		}
 		return comparison == 0;
 	}
}
