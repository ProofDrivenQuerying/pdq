package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Map;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.Match;

/**
 * Extends a Match by maintaining the bag where the
 * homomorphism is found.
 *
 * @author Efthymia Tsamoura
 *
 */
public final class BagMatch extends Match {

	protected Integer bag;
	/**
	 * Constructor for BagMatching.
	 * @param constraint Evaluatable
	 * @param mapping Map<Variable,Constant>
	 * @param bag Integer
	 */
	public BagMatch(Evaluatable constraint, Map<Variable, Constant> mapping, Integer bag) {
		super(constraint, mapping);
		this.bag = bag;
	}

	/**
	 * @return Integer
	 */
	public Integer getBag() {
		return this.bag;
	}

	/**
	 * Method equals.
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
				&& this.query.equals(((BagMatch) o).query)
				&& this.mapping.equals(((BagMatch) o).mapping)
				&& this.bag.equals(((BagMatch) o).bag);

	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.query, this.mapping, this.bag);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		if (this.mapping != null && this.bag != null) {
			return "<" + this.mapping.toString() + ',' + this.bag.toString() + ">";
		}
		return "BagMatch?";
	}

}
