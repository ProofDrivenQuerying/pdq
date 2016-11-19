package uk.ac.ox.cs.pdq.planner.accessibleschema;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;


// TODO: Auto-generated Javadoc
/**
 * Copy of each of the original integrity constraints, with each relation R replaced by InfAccR.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class InferredAccessibleAxiom extends TGD {

	/**
	 * Creates an inferred accessible axiom.
	 *
	 * @param dependency the dependency
	 * @param predToInfAcc the pred to inf acc
	 */
	public InferredAccessibleAxiom(TGD dependency, Map<Atom, InferredAccessibleRelation> predToInfAcc) {
		super(substitute(dependency.getBody(), predToInfAcc),
				substitute(dependency.getHead(), predToInfAcc));
	}

	/**
	 * Creates the left-hand side of an inferred accessible axiom for the input dependency.
	 *
	 * @param <T> the generic type
	 * @param f Formula
	 * @param predToInfAcc Map<PredicateFormula,InferredAccessibleRelation>
	 * @return a conjunction of predicates corresponding to the LHS of an
	 * inferred accessible axiom
	 */
	private static <T extends Formula> T substitute(T f, Map<Atom, InferredAccessibleRelation> predToInfAcc) {
		return new InferredAccessibleRelationSubstituter<T>(predToInfAcc).rewrite(f);
	}


	/**
	 * The Class InferredAccessibleRelationSubstituter.
	 *
	 * @param <T> the generic type
	 */
	private static class InferredAccessibleRelationSubstituter<T extends Formula> implements Rewriter<T, T> {
		
		/** The pred to inf acc. */
		final Map<Atom, InferredAccessibleRelation> predToInfAcc;

		/**
		 * Constructor for InferredAccessibleRelationSubstituter.
		 * @param predToInfAcc Map<PredicateFormula,InferredAccessibleRelation>
		 */
		public InferredAccessibleRelationSubstituter(Map<Atom, InferredAccessibleRelation> predToInfAcc) {
			this.predToInfAcc = predToInfAcc;
		}

		/**
		 * Rewrite.
		 *
		 * @param input T
		 * @return T
		 */
		@Override
		public T rewrite(T input) {
			return (T) this.substitute(input);
		}

		/**
		 * Substitute.
		 *
		 * @param f Formula
		 * @return Formula
		 */
		private Formula substitute(Formula f) {
			if (f instanceof Conjunction) {
				return this.substitute((Conjunction) f);
			}
			if (f instanceof Disjunction) {
				return this.substitute((Disjunction) f);
			}
			if (f instanceof Negation) {
				return this.substitute((Negation) f);
			}
			if (f instanceof Atom) {
				return this.substitute((Atom) f);
			}
			return f;
		}

		/**
		 * Substitute.
		 *
		 * @param conjunction Conjunction<Formula>
		 * @return Conjunction<Formula>
		 */
		private Formula substitute(Conjunction conjunction) {
			List<Formula> result = new LinkedList<>();
			for (Formula f:conjunction.getChildren()) {
				result.add(this.substitute(f));
			}
			return Conjunction.of(result);
		}

		/**
		 * Substitute.
		 *
		 * @param disjunction Disjunction<Formula>
		 * @return Disjunction<Formula>
		 */
		private Formula substitute(Disjunction disjunction) {
			List<Formula> result = new LinkedList<>();
			for (Formula f: disjunction.getChildren()) {
				result.add(this.substitute(f));
			}
			return Disjunction.of(result);
		}

		/**
		 * Substitute.
		 *
		 * @param neg Negation<Formula>
		 * @return Negation<Formula>
		 */
		private Negation substitute(Negation neg) {
			return Negation.of(this.substitute(neg.getChildren().get(0)));
		}

		/**
		 * Substitute.
		 *
		 * @param pred PredicateFormula
		 * @return PredicateFormula
		 */
		private Atom substitute(Atom pred) {
			return new Atom(this.predToInfAcc.get(pred), pred.getTerms());
		}
	}
}
