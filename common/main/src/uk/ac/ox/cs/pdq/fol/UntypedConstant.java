package uk.ac.ox.cs.pdq.fol;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public final class UntypedConstant extends Constant {
	private static final long serialVersionUID = 7918785072370309908L;

	/**  The constant's name. */
	private final String symbol;

	private UntypedConstant(String name) {
		Assert.assertNotNull(name);
		Assert.assertTrue(!name.isEmpty());
		this.symbol = name;
	}

	@Override
	public String toString() {
		return this.symbol;
	}

	public String getSymbol() {
		return this.symbol;
	}

	@Override
	public boolean isUntypedConstant() {
		return true;
	}

	/**
	 * TOCOMMENT I suggest this goes, something is a variable if it is instance of Variable
	 *
	 */
	@Override
	public boolean isVariable() {
		return false;
	}

	/**  The default prefix of the constant terms. */
	public static final String DEFAULT_CONSTANT_PREFIX = "c";

	/**   A counter used to create new constant terms. */
	private static int freshConstantCounter = 0;
	
	/**
	 * Gets the fresh constant.
	 *
	 */
	public static UntypedConstant getFreshConstant() {
		return new UntypedConstant(DEFAULT_CONSTANT_PREFIX + (freshConstantCounter++));
	}
	
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static final InterningManager<UntypedConstant> s_interningManager = new InterningManager<UntypedConstant>() {
        protected boolean equal(UntypedConstant object1, UntypedConstant object2) {
            return object1.symbol.equals(object2.symbol);
        }

        protected int getHashCode(UntypedConstant object) {
            return object.symbol.hashCode() * 7;
        }
    };

    public static UntypedConstant create(String symbol) {
        return s_interningManager.intern(new UntypedConstant(symbol));
    }
}
