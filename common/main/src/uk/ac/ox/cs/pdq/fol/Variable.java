package uk.ac.ox.cs.pdq.fol;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.io.jaxb.adapters.VariableAdapter;

// TODO: Auto-generated Javadoc
/**
 * A variable
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
@XmlJavaTypeAdapter(VariableAdapter.class)
public class Variable extends Term {
	private static final long serialVersionUID = 6326879040237354094L;

	/**  The variable's name. */
	protected final String symbol;
	/**
	 * Instantiates a new variable.
	 *
	 * @param name The name of this variable
	 */
	private Variable(String name) {
		Preconditions.checkArgument(name!=null);
		Preconditions.checkArgument(!name.isEmpty());
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
	
    public static Variable create(String symbol) {
        return Cache.variable.retrieve(new Variable(symbol));
    }
    
    /** 
     * Needed for the xml import/export
     * TOCOMMENT: DO WE STILL NEED THIS?
     */
    public static Variable createFromXml(String symbol) {
        return new Variable(symbol);
    }
    
    /**
     * Needed for the xml import/export
     */
    @Override
    public boolean equals(Object obj) {
    	if (obj == null || !(obj instanceof Variable))
    		return false;
    	if (((Variable)obj).isVariable() != isVariable())
    		return false;
    	if (((Variable)obj).isUntypedConstant() != isUntypedConstant())
    		return false;
    	if (((Variable)obj).getSymbol() == null)
    		return false;
    	if (((Variable)obj).getSymbol().equals(getSymbol()))
    		return true;
    	return false;
    }
}
