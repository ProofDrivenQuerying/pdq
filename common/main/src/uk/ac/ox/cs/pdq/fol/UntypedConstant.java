// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

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

	/**
	 * The DEFAULT_PREFIX for canonical names. Used for example to create cannonicalQuerries. 
	 */
	public static final String CANONICAL_CONSTANT_PREFIX = "c";

	/** The DEFAULT_PREFIX for non canonical constants (labelled nulls). */
	public static final String NON_CANONICAL_CONSTANT_PREFIX = "k";
	
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

	public boolean isCannonicalConstant() {
		return symbol!=null && symbol.startsWith(CANONICAL_CONSTANT_PREFIX);
	}
	
	public boolean isNonCannonicalConstant() {
		return symbol!=null && symbol.startsWith(NON_CANONICAL_CONSTANT_PREFIX);
	}
	/**
	 * Creates a new Constant that was never used or existed before.
	 */
	public static UntypedConstant getFreshConstant() {
		return new UntypedConstant(CANONICAL_CONSTANT_PREFIX + GlobalCounterProvider.getNext("ConstantName"));
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
