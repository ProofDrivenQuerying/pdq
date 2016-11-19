package uk.ac.ox.cs.pdq.db;

import java.util.Map;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.base.Preconditions;


/**
 * A query match.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class Match {

	/**  The dependency or query that will be grounded using an homomorphism*. */
	protected final Formula query;

	/** The mapping of query's variables to constants.*/
	protected final Map<Variable, Constant> mapping;

	/**
	 * Constructor for Match.
	 * @param q Evaluatable
	 * @param mapping Map<Variable,Constant>
	 */
	public Match(Formula q, Map<Variable, Constant> mapping) {
		Preconditions.checkArgument(q instanceof ConjunctiveQuery || q instanceof Dependency);
		Preconditions.checkArgument(mapping != null);
		this.mapping = mapping;
		this.query = q;
	}

	/**
	 * Gets the mapping.
	 *
	 * @return Map<Variable,Constant>
	 */
	public Map<Variable, Constant> getMapping() {
		return this.mapping;
	}


	/**
	 * Gets the query.
	 *
	 * @return Evaluatable
	 */
	public Formula getQuery() {
		return this.query;
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
				&& this.query.equals(((Match) o).query)
				&& this.mapping.equals(((Match) o).mapping);
	}


	@Override
	public int hashCode() {
		return Objects.hash(this.query, this.mapping);
	}

	@Override
	public String toString() {
		return this.mapping.toString() + "\n" + this.query.toString(); 
	}
}
