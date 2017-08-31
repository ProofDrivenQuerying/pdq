package uk.ac.ox.cs.pdq.fol;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public final class UntypedConstant extends Constant implements Comparable<Constant>{
	private static final long serialVersionUID = 7918785072370309908L;

	/**  The constant's name. */
	protected final String symbol;

	/**  The default prefix of the constant terms. */
	public static final String DEFAULT_CONSTANT_PREFIX = "c";
	
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


	/**
	 * Creates a new Constant that was never used or existed before.
	 */
	public static UntypedConstant getFreshConstant() {
		return new UntypedConstant(DEFAULT_CONSTANT_PREFIX + GlobalCounterProvider.getNext("ConstantName"));
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
