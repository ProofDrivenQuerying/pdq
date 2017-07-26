package uk.ac.ox.cs.pdq.fol;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;


/**
 * A predicate's signature, associate a symbol with an arity.
 *
 * @author Julien Leblay
 */
public class Predicate {

	/**  Predicate name. */
	protected final String name;

	/**  Predicate arity. */
	protected final Integer arity;

	/**  true, if this is the signature for an equality predicate. */
	protected final Boolean isEquality;

	public Predicate(String name, Integer arity) {
		Assert.assertNotNull(name);
		Assert.assertTrue(!name.isEmpty());
		Assert.assertTrue(arity >= 0);
		this.name = name;
		this.arity = arity;
		this.isEquality = false;
	}
	
	public Predicate(String name, Integer arity, boolean isEquality) {
		Assert.assertNotNull(name);
		Assert.assertTrue(!name.isEmpty());
		Assert.assertTrue(arity >= 0);
		this.name = name;
		this.arity = arity;
		this.isEquality = isEquality;
	}

	/**
	 * Gets the name of the predicate.
	 *
	 * @return the name of the predicate.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the arity of the predicate.
	 *
	 * @return the arity of the predicate.
	 */
	public int getArity() {
		return this.arity;
	}

	/**
	 * Checks if this is an equality predicate.
	 *
	 * @return true if the signature is of an equality predicate,
	 * false otherwise
	 */
	public boolean isEquality() {
		return this.isEquality;
	}

	@Override
	public String toString() {
		return this.name;
	}
	
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static final InterningManager<Predicate> s_interningManager = new InterningManager<Predicate>() {
        protected boolean equal(Predicate object1, Predicate object2) {
            return object1.name.equals(object2.name) && object1.arity == object2.arity && object1.isEquality == object2.isEquality;
        }

        protected int getHashCode(Predicate object) {
            return object.name.hashCode() + object.arity.hashCode() * 7;
        }
    };

    public static Predicate create(String name, Integer arity) {
        return s_interningManager.intern(new Predicate(name, arity));
    }
    
    public static Predicate create(String name, Integer arity, Boolean isEquality) {
        return s_interningManager.intern(new Predicate(name, arity, isEquality));
    }

}
