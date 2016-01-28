package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;

/**
 * Utility class for common stateless logical operator operators.
 *
 * @author Julien Leblay
 */
public class Operators {

	/**
	 *
	 * @param terms List<Term>
	 * @return
	 * 	 	a conjunction of select conditions that the output values of a source must satisfy
	 * 		based on the exposed fact's terms.
	 * 		The select conditions enforce value equality when two terms are equal
	 * 		and equality to a constant when an exposed fact's term is mapped to a schema constant.
	 * 		The returned list is null if there does not exist any select condition
	 */
	public static Predicate createSelectPredicates(List<Term> terms) {
		Set<Predicate> result = new LinkedHashSet<>();
		Integer termIndex = 0;
		for (Term term : terms) {

			if (term instanceof TypedConstant) {
				result.add(new ConstantEqualityPredicate(
						termIndex, (TypedConstant) term));
			} else {
				List<Integer> appearances = Utility.search(terms, term);
				if (appearances.size() > 1) {
					for (int i = 0; i < appearances.size() - 1; ++i) {
						Integer indexI = appearances.get(i);
						for (int j = i + 1; j < appearances.size(); ++j) {
							Integer indexJ = appearances.get(j);
							result.add(new AttributeEqualityPredicate(indexI, indexJ));
						}
					}
				}
			}
			++termIndex;
		}
		return result.isEmpty() ? null : new ConjunctivePredicate<>(result);
	}

	/**
	 * @param query Query
	 * @param childOp LogicalOperator
	 * @return Projection
	 */
	public static Projection createFinalProjection(Query<?> query, RelationalOperator childOp) {
		List<Term> freeTerms = query.getFree();
		List<Term> toProject = new ArrayList<>();
		for (Term term: freeTerms) {
			if (term.isVariable()) {
				Constant constant = query.getFreeToCanonical().get(term);
				Preconditions.checkState(childOp.getColumns().contains(constant), constant + " not in " + childOp.getColumns() + "\nQuery: " + query + "\nCanonical Mapping: " + query.getFreeToCanonical() + "\nSubplan: " + childOp);
				toProject.add(constant);
			} else {
				toProject.add(term);
			}
		}
		if (!childOp.getInputTerms().isEmpty()) {
			List<TypedConstant<?>> constants = new ArrayList<>(childOp.getInputTerms().size());
			for (Term t: childOp.getInputTerms()) {
				Preconditions.checkState(!t.isVariable() && !t.isSkolem(), "Successful plan cannot be open.");
				constants.add((TypedConstant) t);
			}
			return new Projection(new DependentJoin(new StaticInput(constants), childOp),
					toProject);
		}
		return new Projection(childOp, toProject);
	}

}
