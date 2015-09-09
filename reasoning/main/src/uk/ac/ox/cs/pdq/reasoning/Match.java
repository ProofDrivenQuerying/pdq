package uk.ac.ox.cs.pdq.reasoning;

import java.util.Map;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.base.Preconditions;


/**
 * Keeps information related to homomorphisms
 * It maintains the Evaluatable object that was matched and
 * a mapping of the query's variables to constants.
 *
 * It is created when detecting homomorphisms during chasing.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class Match {

	protected final Evaluatable query;

	/** Mapping of the query variables to chase constants */
	protected final Map<Variable, Constant> mapping;

	/**
	 * Constructor for Match.
	 * @param q Evaluatable
	 * @param mapping Map<Variable,Constant>
	 */
	public Match(Evaluatable q, Map<Variable, Constant> mapping) {
		Preconditions.checkArgument(mapping != null);
		this.mapping = mapping;
		this.query = q;
	}

	/**
	 * @return Map<Variable,Constant>
	 */
	public Map<Variable, Constant> getMapping() {
		return this.mapping;
	}


	/**
	 * @return Evaluatable
	 */
	public Evaluatable getQuery() {
		return this.query;
	}

	/**
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

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.query, this.mapping);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		if (this.mapping != null) {
			return this.mapping.toString();
		}
		return "Matching?";
	}
}
