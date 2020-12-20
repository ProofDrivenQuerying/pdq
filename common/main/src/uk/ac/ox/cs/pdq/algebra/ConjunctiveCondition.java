// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import uk.ac.ox.cs.pdq.db.tuple.Tuple;

/**
 * Represents a condition, that can be used to create -for example- a
 * SelectionTerm, or a dependent join term.
 * 
 * @author Unknown
 * @author Gabor
 * @author Stefano
 */
public class ConjunctiveCondition extends Condition {
	private static final long serialVersionUID = 3482096951862132845L;

	protected final SimpleCondition[] predicates;

	private ConjunctiveCondition(SimpleCondition[] predicates) {
		assert (predicates != null);
		this.predicates = predicates.clone();
	}

	public SimpleCondition[] getSimpleConditions() {
		return this.predicates.clone();
	}

	public int getNumberOfConjuncts() {
		return this.predicates.length;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String sep = "(";
		if (this.predicates.length > 0) {
			for (Condition p : this.predicates) {
				result.append(sep).append(p);
				sep = "&";
			}
			result.append(')');
		}
		return result.toString();
	}

	public static ConjunctiveCondition create(SimpleCondition[] predicates) {
		return Cache.conjunctiveCondition.retrieve(new ConjunctiveCondition(predicates));
	}

	public boolean isSatisfied(Tuple tuple) {
		for (SimpleCondition simpleCondition : this.getSimpleConditions()) {
			if (!simpleCondition.isSatisfied(tuple))
				return false;
		}
		return true;
	}
}
