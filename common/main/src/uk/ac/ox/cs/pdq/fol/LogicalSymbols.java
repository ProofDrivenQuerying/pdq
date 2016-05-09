package uk.ac.ox.cs.pdq.fol;

/**
 * A enumeration of Logical symbols.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public enum LogicalSymbols {
	
	/** The and. */
	AND("\u2227", 2), 
 /** The or. */
 OR("\u2228", 2), 
 /** The implies. */
 IMPLIES("\u2192", 2),
	
	/** The equivalence. */
	EQUIVALENCE("\u2194", 2), 
 /** The bottom. */
 BOTTOM("\u22A5", 0), 
 /** The top. */
 TOP("\u22A4", 0),
	
	/** The negation. */
	NEGATION("\u00AC", 1), 
 /** The existential. */
 EXISTENTIAL("\u2203", 1), 
 /** The universal. */
 UNIVERSAL("\u2200", 1);

	/** The representation. */
	private final String representation;
	
	/** The arity. */
	private final int arity;

	/**
	 * Constructor for LogicalSymbols.
	 *
	 * @param s String
	 * @param a the a
	 */
	private LogicalSymbols(String s, int a) {
		this.representation = s;
		this.arity = a;
	}

	@Override
	public String toString() {
		return this.representation;
	}

	/**
	 * Gets the arity.
	 *
	 * @return int the arity associated with this symbol
	 */
	public int getArity() {
		return this.arity;
	}
}
