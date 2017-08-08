package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;


/**
 * Compares the values at two given positions in a tuple.
 *
 * @author Julien Leblay
 */
public class AttributeEqualityCondition extends SimpleCondition {
	private static final long serialVersionUID = 590156716681307220L;

	/**  The first of the two positions to be compared for equality. */
	protected final Integer position;

	/**  The other position to which position must be equals for a given tuple. */
	protected final Integer other;

	private AttributeEqualityCondition(Integer position, Integer other) {
		Assert.assertTrue(position >= 0 && other >= 0);
		this.position = position;
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
 
}
