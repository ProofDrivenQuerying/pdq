package uk.ac.ox.cs.pdq.fol;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.io.jaxb.adapters.VariableAdapter;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * A variable
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
@XmlJavaTypeAdapter(VariableAdapter.class)
public class Variable extends Term {
	private static final long serialVersionUID = 6326879040237354094L;

	/** The variable's name. */
	protected final String symbol;

	/**
	 * Instantiates a new variable.
	 *
	 * @param name
	 *            The name of this variable
	 */
	private Variable(String name) {
		Preconditions.checkArgument(name != null);
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

	/** The default prefix of the variable terms. */
	public static final String DEFAULT_VARIABLE_PREFIX = "_";

	/**
	 * Gets the fresh variable.
	 *
	 * @return a new variable using the default variable prefix an integer
	 */
	public static Variable getFreshVariable() {
		return new Variable(DEFAULT_VARIABLE_PREFIX + GlobalCounterProvider.getNext("VariableName"));
	}

	public static Variable create(String symbol) {
		return Cache.variable.retrieve(new Variable(symbol));
	}

	/**
	 * This is a necessary work around. The normal way to create a Variable is to
	 * use the Variable.create function, however that will return a cached object,
	 * and JaxB will not work when we have multiple Variables with the same name in
	 * an xml. We need them to be new Objects containing the same data, therefore we
	 * bypass the caching of the Variable.create function.
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
		if (((Variable) obj).isVariable() != isVariable())
			return false;
		if (((Variable) obj).isUntypedConstant() != isUntypedConstant())
			return false;
		if (((Variable) obj).getSymbol() == null)
			return false;
		if (((Variable) obj).getSymbol().equals(getSymbol()))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		if (isVariable())
			return getSymbol().hashCode();
		return super.hashCode();
	}
}
