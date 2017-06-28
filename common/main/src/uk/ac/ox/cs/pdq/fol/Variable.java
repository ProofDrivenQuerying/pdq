package uk.ac.ox.cs.pdq.fol;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.InterningManager;

// TODO: Auto-generated Javadoc
/**
 * A variable
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Variable implements Term {
	private static final long serialVersionUID = 6326879040237354094L;

	/**  The variable's name. */
	private final String symbol;

	/**
	 * Instantiates a new variable.
	 *
	 * @param name The name of this variable
	 */
	private Variable(String name) {
		Assert.assertNotNull(name);
		Assert.assertTrue(!name.isEmpty());
		this.symbol = name;
	}

	@Override
	public boolean isVariable() {
		return true;
	}

	@Override
	public boolean isUntypedConstant() {
		return false;
	}

	@Override
	public String toString() {
		return this.symbol;
	}
	
	public String getSymbol() {
		return this.symbol;
	}
	
	/**  The default prefix of the variable terms. */
	public static final String DEFAULT_VARIABLE_PREFIX = "_";

	/**  A counter used to create new variable terms. */
	private static int freshVariableCounter = 0;

	/**
	 * Reset counter.
	 */
	public static void resetCounter() {
		Variable.freshVariableCounter = 0;
	}

	/**
	 * Gets the fresh variable.
	 *
	 * @return a new variable using the default variable prefix an integer
	 */
	public static Variable getFreshVariable() {
		return new Variable(DEFAULT_VARIABLE_PREFIX + (freshVariableCounter++));
	}
	
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static final InterningManager<Variable> s_interningManager = new InterningManager<Variable>() {
        protected boolean equal(Variable object1, Variable object2) {
            return object1.symbol.equals(object2.symbol);
        }

        protected int getHashCode(Variable object) {
            return object.symbol.hashCode() * 7;
        }
    };

    public static Variable create(String symbol) {
        return s_interningManager.intern(new Variable(symbol));
    }
}