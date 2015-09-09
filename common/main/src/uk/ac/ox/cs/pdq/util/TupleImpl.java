package uk.ac.ox.cs.pdq.util;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 *
 * A tuple implementation
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
class TupleImpl implements Tuple {

	/** The type of the tuple */
	private final TupleType type;

	/** The underlying object of the tuple */
	private final Object[] values;

	/**
	 * Default constructor.
	 * @param type
	 * @param values
	 */
	TupleImpl(TupleType type, Object... values) {
		this.type = type;
		if (values == null || values.length == 0) {
			this.values = new Object[0];
		} else {
			this.values = new Object[values.length];
			System.arraycopy(values, 0, this.values, 0, values.length);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.structures.Tuple#appendTuple(uk.ac.ox.cs.pdq.structures.Tuple)
	 */
	@Override
	public Tuple appendTuple(Tuple t) {
		Type[] compoundType = new Class<?>[this.type.size() + t.getType().size()];
		Object[] result = new Object[compoundType.length];
		for (int i = 0, l = compoundType.length; i < l; i++) {
			if (i < this.size()) {
				compoundType[i] = this.type.getType(i);
				result[i] = this.values[i];
			} else {
				compoundType[i] = t.getType().getType(i - this.size());
				result[i] = t.getValue(i - this.size());
			}
		}
		return TupleType.DefaultFactory.create(compoundType).createTuple(result);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.structures.Tuple#getType()
	 */
	@Override
	public TupleType getType() {
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.structures.Tuple#size()
	 */
	@Override
	public int size() {
		return this.values.length;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.structures.Tuple#getValue(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(int i) {
		return (T) this.values[i];
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.structures.Tuple#getValues()
	 */
	@Override
	public Object[] getValues() {
		return this.values;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (null == object) {
			return false;
		}
		if (!(object instanceof Tuple)) {
			return false;
		}
		final Tuple other = (Tuple) object;
		if (other.size() != this.size()) {
			return false;
		}

		final int size = this.size();
		for (int i = 0; i < size; i++) {
			final Object thisNthValue = this.getValue(i);
			final Object otherNthValue = other.getValue(i);
			if ((thisNthValue == null && otherNthValue != null)
					|| (thisNthValue != null && !thisNthValue.equals(otherNthValue))) {
				return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 17;
		for (Object value : this.values) {
			if (value != null) {
				hash = hash * 37 + value.hashCode();
			}
		}
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Arrays.toString(this.values);
	}
}
