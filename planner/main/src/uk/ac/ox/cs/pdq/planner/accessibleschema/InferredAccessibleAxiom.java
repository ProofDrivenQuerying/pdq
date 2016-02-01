package uk.ac.ox.cs.pdq.planner.accessibleschema;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;


/**
 * Copy of each of the original integrity constraints, with each relation R replaced by InfAccR.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class InferredAccessibleAxiom extends TGD {

	/**
	 * Creates an inferred accessible axiom
	 * @param dependency
	 * @param predToInfAcc
	 */
	public InferredAccessibleAxiom(TGD dependency, Map<Predicate, InferredAccessibleRelation> predToInfAcc) {
		super(substitute(dependency.getLeft(), predToInfAcc),
				substitute(dependency.getRight(), predToInfAcc));
	}

	/**
	 * Creates the left-hand side of an inferred accessible axiom for the input dependency
	 * @param f Formula
	 * @param predToInfAcc Map<PredicateFormula,InferredAccessibleRelation>
	 * @return a conjunction of predicates corresponding to the LHS of an
	 * inferred accessible axiom
	 */
	private static <T extends Formula> T substitute(T f, Map<Predicate, InferredAccessibleRelation> predToInfAcc) {
		return new InferredAccessibleRelationSubstituter<T>(predToInfAcc).rewrite(f);
	}


	private static class InferredAccessibleRelationSubstituter<T extends Formula> implements Rewriter<T, T> {
		final Map<Predicate, InferredAccessibleRelation> predToInfAcc;

		/**
		 * Constructor for InferredAccessibleRelationSubstituter.
		 * @param predToInfAcc Map<PredicateFormula,InferredAccessibleRelation>
		 */
		public InferredAccessibleRelationSubstituter(Map<Predicate, InferredAccessibleRelation> predToInfAcc) {
			this.predToInfAcc = predToInfAcc;
		}

		/**
		 * @param input T
		 * @return T
		 */
		@Override
		public T rewrite(T input) {
			return (T) this.substitute(input);
		}

		/**
		 * @param f Formula
		 * @return Formula
		 */
		private Formula substitute(Formula f) {
			if (f instanceof Conjunction) {
				return this.substitute((Conjunction<Formula>) f);
			}
			if (f instanceof Disjunction) {
				return this.substitute((Disjunction<Formula>) f);
			}
			if (f instanceof Negation) {
				return this.substitute((Negation<Formula>) f);
			}
			if (f instanceof Predicate) {
				return this.substitute((Predicate) f);
			}
			return f;
		}

		/**
		 * @param conjunction Conjunction<Formula>
		 * @return Conjunction<Formula>
		 */
		private Conjunction<Formula> substitute(Conjunction<Formula> conjunction) {
			List<Formula> result = new LinkedList<>();
			for (Formula f: conjunction) {
				result.add(this.substitute(f));
			}
			return Conjunction.of(result);
		}

		/**
		 * @param disjunction Disjunction<Formula>
		 * @return Disjunction<Formula>
		 */
		private Disjunction<Formula> substitute(Disjunction<Formula> disjunction) {
			List<Formula> result = new LinkedList<>();
			for (Formula f: disjunction) {
				result.add(this.substitute(f));
			}
			return Disjunction.of(result);
		}

		/**
		 * @param neg Negation<Formula>
		 * @return Negation<Formula>
		 */
		private Negation<Formula> substitute(Negation<Formula> neg) {
			return Negation.of(this.substitute(neg.getChild()));
		}

		/**
		 * @param pred PredicateFormula
		 * @return PredicateFormula
		 */
		private Predicate substitute(Predicate pred) {
			return new Predicate(this.predToInfAcc.get(pred), pred.getTerms());
		}
	}
}
