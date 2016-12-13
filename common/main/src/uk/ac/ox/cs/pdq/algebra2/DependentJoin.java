package uk.ac.ox.cs.pdq.algebra2;

import java.util.Map;

import uk.ac.ox.cs.pdq.db.TypedConstant;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


/**
 * A dependent join operator.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class DependentJoin implements RelationalOperator {

	private final Predicate predicate;

	private final Map<Integer,TypedConstant<?>> inputConstants;
	
	/**
	 * Instantiates a new join.
	 *
	 * @param pred Atom
	 * @param children the children
	 */
	public DependentJoin(Predicate predicate) {
		Preconditions.checkNotNull(predicate);
		this.predicate = predicate;
		this.inputConstants = null;
	}
	
	/**
	 * Instantiates a new join.
	 *
	 * @param pred Atom
	 * @param children the children
	 */
	public DependentJoin(Predicate predicate, Map<Integer,TypedConstant<?>> inputConstants) {
		Preconditions.checkNotNull(predicate);
		Preconditions.checkNotNull(inputConstants);
		Preconditions.checkArgument(!inputConstants.isEmpty());
		this.predicate = predicate;
		this.inputConstants = inputConstants;
	}

	/**
	 * Gets the predicate associated with this join. If the join is natural this returns null.
	 *
	 * @return Atom
	 * @see uk.ac.ox.cs.pdq.algebra.PredicateBasedOperator#getPredicate()
	 */
	public Predicate getPredicate() {
		return this.predicate;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		if (this.predicate != null) {
			result.append(this.predicate);
		}
		return result.toString();
	}
	

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
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
				&& this.predicate == ((DependentJoin) o).predicate
				&& this.inputConstants == ((DependentJoin) o).inputConstants;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.predicate, this.inputConstants);
	}

	@Override
	public int getArity() {
		// TODO Auto-generated method stub
		return 2;
	}
}
