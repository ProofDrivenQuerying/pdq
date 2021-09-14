// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.google.common.base.Preconditions;
/**
 * Super class of AttributeEquality and Constant Equality condition. Maybe we could rename it to EqualityCondition.
 * 
 * @author Efi - I think
 * @author Gabor
 */
public abstract class SimpleCondition extends Condition {
	private static final long serialVersionUID = -9137009966765258144L;
	/**  The first of the two positions to be compared for equality. */
	protected final Integer position;

	/** The value to be used to override the position index and replace it with
	 * the mappedNamed in the toString method
	 **/
	protected String provenanceName;

	protected String otherProvenance;

	public String getOtherProvenance() {
		return otherProvenance;
	}

	public void setOtherProvenance(String otherProvenance) {
		this.otherProvenance = otherProvenance;
	}

	public String getProvenanceName() {
		return provenanceName;
	}

	public void setProvenanceName(String provenanceName) {
		this.provenanceName = provenanceName;
	}

	protected SimpleCondition(Integer position) {
		Preconditions.checkArgument(position >= 0);
		this.position = position;
	}

	public int getPosition() {
		return this.position;
	}

	public boolean compareTypes(Type type, Type targetType) {
		return type.equals(targetType);
	}
	
	public boolean compareValues(Object value, Object targetValue) {
		if (value instanceof Comparable<?> && targetValue instanceof Comparable<?>) {
			try {
				Method m = Comparable.class.getMethod("compareTo", Object.class);
				return ((int) m.invoke(value, targetValue)) == 0;
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Could not compare " + value + " to "+ targetValue,e);
			}
		}
		return value.equals(targetValue);
	}	
}
