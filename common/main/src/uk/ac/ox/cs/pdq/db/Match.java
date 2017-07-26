package uk.ac.ox.cs.pdq.db;

import java.util.Map;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.base.Preconditions;

//TODO add cache manager 
/**
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class Match {

	/**  The formula or query that will be grounded using an homomorphism*. */
	protected final Formula formula;

	/** The mapping of query's variables to constants.*/
	protected final Map<Variable, Constant> mapping;

	public Match(Formula formula, Map<Variable, Constant> mapping) {
		Preconditions.checkArgument(formula instanceof ConjunctiveQuery || formula instanceof Dependency);
		Preconditions.checkArgument(mapping != null);
		this.mapping = mapping;
		this.formula = formula;
	}

	public Map<Variable, Constant> getMapping() {
		return this.mapping;
	}

	public Formula getQuery() {
		return this.formula;
	}

	/**
	 * Two matches are equal if the queries and the mappings they contain are equal (using equals accordingly).
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.formula.equals(((Match) o).formula)
				&& this.mapping.equals(((Match) o).mapping);
	}


	@Override
	public int hashCode() {
		return Objects.hash(this.formula, this.mapping);
	}

	@Override
	public String toString() {
		return this.mapping.toString() + "\n" + this.formula.toString(); 
	}
}
