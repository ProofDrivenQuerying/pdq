package uk.ac.ox.cs.pdq.runtime.conditions;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Tuple;

public class ConditionUtils {
	

	/**
	 * Returns the tuple-dependent join condition for this plan. This is the generic 
	 * join condition, which tests only type equality (and is returned by the no-argument 
	 * version of this method), specialised to value(s) in the given tuple.
	 * 
	 * For each TypeEqualityCondition c in the generic join condition, the return value
	 * contains a corresponding ConstantEqualityCondition, which is satisfied on any
	 * tuple whose value at index c.other is equal to the value of the given tuple at 
	 * index c.position.
	 * 
	 * In a JoinTerm, these indices correspond to the concatenated attributes from the 
	 * left+right children. In a DependentJoinTerm the indices correspond to attributes 
	 * in the right child only 
	 * 
	 * @param tuple A Tuple 
	 * @return A ConstantEqualityCondition object or conjunction of such.
	 */
	public static Condition getJoinCondition(JoinTerm join, Tuple tuple) {

		if (join.getJoinConditions() instanceof TypeEqualityCondition)
			return ConditionUtils.getJoinCondition(tuple, (TypeEqualityCondition) join.getJoinConditions());

		// If the join condition is not a TypeEqualityCondition then it must
		// be a conjunction of multiple TypeEqualityConditions. In that case, handle them 
		// one-by-one and return a conjunction of ConstantEqualityConditions.
		Condition[] simpleConditions = ((ConjunctiveCondition) join.getJoinConditions()).getSimpleConditions();
		SimpleCondition[] predicates = new SimpleCondition[simpleConditions.length];
		for (int i = 0; i != simpleConditions.length; i++) {
			AttributeEqualityCondition ac = (AttributeEqualityCondition) simpleConditions[i];
			predicates[i] = ConditionUtils.getJoinCondition(tuple, TypeEqualityCondition.create(ac.getPosition(),ac.getOther()) );
		}

		return ConjunctiveCondition.create(predicates);
	}
	/*
	 * Every simple condition in the joinCondition is a TypeEqualityCondition. This method
	 * converts such a condition into a ConstantEqualityCondition, based on the value at a
	 * particular index in a given tuple.
	 */
	protected static ConstantEqualityCondition getJoinCondition(Tuple tuple,
			TypeEqualityCondition condition) {

		int position = condition.getPosition();
		Preconditions.checkArgument(tuple.size() > position);

		/* Return a ConstantEqualityCondition derived from the 
		 * TypeEqualityCondition join condition & the tuple. The returned 
		 * condition will be satisfied on any tuple whose value at index 
		 * 'other' is equal to the value of the given tuple at index 'position'.
		 */
		TypedConstant constant = TypedConstant.create(tuple.getValues()[position]);
		int other = condition.getOther();

		return ConstantEqualityCondition.create(other, constant); 
	}
}
