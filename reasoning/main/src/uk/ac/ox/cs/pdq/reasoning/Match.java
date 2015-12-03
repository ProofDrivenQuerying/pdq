package uk.ac.ox.cs.pdq.reasoning;

import java.util.Map;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.base.Preconditions;


/**
 * A trigger or query match.
 * 
 * (From modern dependency theory notes)
 * Consider an instance I, a set Base of values, and a TGD
 * \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
 * A trigger for \delta in I is a homomorphism h of \sigma into I.
 * 
 * 
 * (Query match definition) If Q′ is a conjunctive query and v is a chase configuration
	having elements for each free variable of Q′, then a homomorphism of Q′ into v
	mapping each free variable into the corresponding element is called a match for Q′ in
	v.
 *  
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class Match {

	/** The dependency or query that will be grounded using an homomorphism**/
	protected final Evaluatable query;

	/** The mapping of query's variables to chase constants.*/
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
		return this.mapping.toString() + "\n" + this.query.toString(); 
	}
}
