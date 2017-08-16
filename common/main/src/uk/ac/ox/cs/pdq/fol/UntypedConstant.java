package uk.ac.ox.cs.pdq.fol;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.TypedConstant;

/**
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public final class UntypedConstant extends Constant implements Comparable<Constant>{
	private static final long serialVersionUID = 7918785072370309908L;

	/**  The constant's name. */
	protected final String symbol;

	private UntypedConstant(String name) {
		Preconditions.checkArgument(name!=null);
		Preconditions.checkArgument(!name.isEmpty());
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
	 * TOCOMMENT: WHAT IS THIS? .
	 *
	 */
	public static UntypedConstant getFreshConstant() {
		return new UntypedConstant(DEFAULT_CONSTANT_PREFIX + (freshConstantCounter++));
	}
	
    public static UntypedConstant create(String symbol) {
        return Cache.untypedConstant.retrieve(new UntypedConstant(symbol));
    }

	@Override
	public int compareTo(Constant o) {
		if (o instanceof UntypedConstant) {
			return this.symbol.compareTo(((UntypedConstant) o).symbol);
		}
		return -1;
	}
}
