package uk.ac.ox.cs.pdq.algebra;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;


/**
 * Compares the values at two given positions in a tuple.
 *
 * @author Julien Leblay
 */
public class AttributeEqualityPredicate implements SimplePredicate {
	private static final long serialVersionUID = 590156716681307220L;

	/**  The first of the two positions to be compared for equality. */
	protected final Integer position;

	/**  The other position to which position must be equals for a given tuple. */
	protected final Integer other;

	public AttributeEqualityPredicate(Integer position, Integer other) {
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
	
    protected static final InterningManager<AttributeEqualityPredicate> s_interningManager = new InterningManager<AttributeEqualityPredicate>() {
        protected boolean equal(AttributeEqualityPredicate object1, AttributeEqualityPredicate object2) {
            if (object1.position != object2.position || object1.other != object2.other)  
                return false;
            return true;
        }

        protected int getHashCode(AttributeEqualityPredicate object) {
            int hashCode = object.position.hashCode() + object.other.hashCode() * 7;
            return hashCode;
        }
    };
    
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    public static AttributeEqualityPredicate create(int position, int other) {
        return s_interningManager.intern(new AttributeEqualityPredicate(position, other));
    }
 
}
