package uk.ac.ox.cs.pdq.datasources.utility;

/**
 * Representation of a Boolean query/plan result.
 *
 * @author Julien Leblay
 */
public final class BooleanResult implements Result {

	/**  The internal value of the boolean result. */
	private boolean value;

	/**
	 * Default constructor.
	 *
	 * @param value the value
	 */
	public BooleanResult(boolean value) {
		this.value = value;
	}

	@Override
	public boolean isEmpty() {
		return !this.value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Boolean.valueOf(this.value).toString();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.structures.Result#size()
	 */
	@Override
	public int size() {
		return this.isEmpty() ? 0 : 1;
	}

	/**
	 * Gets the result value.
	 *
	 * @return the internal value of the BooleanResult
	 */
	public Boolean getValue() {
		return this.value;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.structures.Result#howDifferent(uk.ac.ox.cs.pdq.structures.Result)
	 */
	@Override
	public Levels howDifferent(Result o) {
		if (this == o) {
			return Levels.IDENTICAL;
		}
		return BooleanResult.class.isInstance(o)
				&& this.getValue().equals(((BooleanResult) o).getValue())
				? Levels.IDENTICAL : Levels.DIFFERENT;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.structures.Result#diff(uk.ac.ox.cs.pdq.structures.Result)
	 */
	@Override
	public String diff(Result o) {
		if (this.getClass().isInstance(o)
				&& this.getValue().equals(((BooleanResult) o).getValue())) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		result.append(this.value).append(" <> ").append(((BooleanResult) o).getValue());
		return result.toString();
	}
}
