package uk.ac.ox.cs.pdq.algebra;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Tuple;

public class ConstantInequalityCondition extends ConstantComparisonCondition {

	private static final long serialVersionUID = 8050441112668868046L;

	/** The log. */
	private static Logger log = Logger.getLogger(ConstantInequalityCondition.class);

	protected boolean lessThan;

	private ConstantInequalityCondition(int position, TypedConstant constant) {
		this(position, constant, true);
	}
	
	private ConstantInequalityCondition(int position, TypedConstant constant, boolean lessThan) {
		super(position, constant);
		this.lessThan = lessThan;
	}

//	public static ConstantInequalityCondition create(int position, TypedConstant constant, boolean lessThan) {
//		return Cache.constantInequalityCondition.retrieve(new ConstantInequalityCondition(position, constant, lessThan));
//	}
//
//	public static ConstantInequalityCondition create(int position, TypedConstant constant) {
//		return Cache.constantInequalityCondition.retrieve(new ConstantInequalityCondition(position, constant));
//	}
	
	// Return true iff the tuple value at the 'position' index is less than that
	// of the constant value in this condition, if the lessThan field is true.
	// Otherwise do the same but test for a tuple value that is greater than the
	// constant.
	@Override
	public boolean isSatisfied(Tuple tuple) {
		Preconditions.checkArgument(tuple.size() > this.position);

		Object value = tuple.getValue(this.position);
		Object targetValue = this.getConstant().getValue();
		if (value == null || targetValue == null)
			return false;

		if (!(value instanceof Comparable<?> && targetValue instanceof Comparable<?>)) {
			log.warn("Incomparable values:" + value + " and " + targetValue);
			return false;
		}

		int comparison;
		try {
			Method m = Comparable.class.getMethod("compareTo", Object.class);
			comparison = (int) m.invoke(value, targetValue);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.warn("Problem comparing " + value + " to " + targetValue + ": " + e);
			return false;
		}
		return lessThan ? comparison < 0: comparison > 0;
	}
}
