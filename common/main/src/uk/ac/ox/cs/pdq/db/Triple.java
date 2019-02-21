package uk.ac.ox.cs.pdq.db;

/**
 * A triple of elements.
 *
 * @author Efthymia Tsamoura
 * @param <T1> Type of the first element
 * @param <T2> Type of the second element
 * @param <T3> Type of the third element
 */
public class Triple<T1, T2, T3> {

	/** The first. */
	private final T1 first;
	
	/** The second. */
	private final T2 second;
	
	/** The third. */
	private final T3 third;

	/**
	 * Constructor for Triple.
	 * @param first T1
	 * @param second T2
	 * @param third T3
	 */
	public Triple(T1 first, T2 second, T3 third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	/**
	 * Gets the first.
	 *
	 * @return T1
	 */
	public T1 getFirst() {
		return this.first;
	}

	/**
	 * Gets the second.
	 *
	 * @return T2
	 */
	public T2 getSecond() {
		return this.second;
	}

	/**
	 * Gets the third.
	 *
	 * @return T3
	 */
	public T3 getThird() {
		return this.third;
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.first == null) ? 0 : this.first.hashCode());
		result = prime * result + ((this.second == null) ? 0 : this.second.hashCode());
		result = prime * result + ((this.third == null) ? 0 : this.third.hashCode());
		return result;
	}

	/**
	 * Equals.
	 *
	 * @param obj Object
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}

		if (this.getClass() != obj.getClass()) {
			return false;
		}

		Triple<Object, Object, Object> other = (Triple<Object, Object, Object>) obj;

		if (this.first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!this.first.equals(other.first)) {
			return false;
		}
		if (this.second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!this.second.equals(other.second)) {
			return false;
		}
		if (this.third == null) {

			if (other.third != null) {
				return false;
			}
		} else if (!this.third.equals(other.third)) {
			return false;
		}
		return true;
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return this.first.toString() + " " + this.second.toString() + " " + this.third.toString();
	}

}
