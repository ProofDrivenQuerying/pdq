package uk.ac.ox.cs.pdq.fol;

/**
 * Logical symbols
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public enum LogicalSymbols {
	AND("\u2227", 2), OR("\u2228", 2), IMPLIES("\u2192", 2),
	EQUIVALENCE("\u2194", 2), BOTTOM("\u22A5", 0), TOP("\u22A4", 0),
	NEGATION("\u00AC", 1), EXISTENTIAL("\u2203", 1), UNIVERSAL("\u2200", 1);

	private final String representation;
	private final int arity;

	/**
	 * Constructor for LogicalSymbols.
	 * @param s String
	 */
	private LogicalSymbols(String s, int a) {
		this.representation = s;
		this.arity = a;
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.representation;
	}

	/**
	 * @return int the arity associated with this symbol
	 */
	public int getArity() {
		return this.arity;
	}
}
