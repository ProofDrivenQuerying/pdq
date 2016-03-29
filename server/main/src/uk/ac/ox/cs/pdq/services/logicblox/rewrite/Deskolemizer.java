package uk.ac.ox.cs.pdq.services.logicblox.rewrite;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.builder.QueryBuilder;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;
import uk.ac.ox.cs.pdq.services.logicblox.cost.LogicBloxDelegateCostEstimator;

// TODO: Auto-generated Javadoc
/**
 * Deskolemizes an input formula, by replacing any Skolem term into a fresh
 * variables.
 *
 * @author Julien LEBLAY
 * @param <F> the generic type
 */
public class Deskolemizer<F extends Formula> implements Rewriter<F, F> {

	/** Logger. */
	static final Logger log = Logger.getLogger(LogicBloxDelegateCostEstimator.class);

	/**  Locally stores all know implementation. */
	private static final Map<Class<?>, Class<?>> repository = new LinkedHashMap<>();
	static {
		for (Class<?> c: Deskolemizer.class.getDeclaredClasses()) {
			try {
				c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/** The mapping. */
	protected Map<Skolem, Variable> mapping = new LinkedHashMap<>();

	/**
	 * Rewrite.
	 *
	 * @param input F
	 * @return a deskolemize for formula
	 */
	@Override
	public F rewrite(F input) {
		return resolve(input, this.mapping).rewrite(input);
	}
	
	/**
	 * If term is a Skolem, maps it to a fresh variable and returns that 
	 * variable. Otherwise, the method has no effect, in the input term is 
	 * returned.
	 * @param term Term
	 * @return Term
	 */
	protected Term map(Term term) {
		Term result = term;
		if (term.isSkolem()) {
			result = this.mapping.get(term);
			if (result == null) {
				result = Variable.getFreshVariable();
				this.mapping.put((Skolem) term, (Variable) result);
			}
		}
		return result;
	}
	
	/**
	 * Find the proper Deskolimizer implementation to the given input.
	 *
	 * @param <F> the generic type
	 * @param input F
	 * @param mapping Map<Skolem,Variable>
	 * @return a specialized Desolemizer implementation, suited for the input
	 * formula.
	 */
	private static <F extends Formula> Deskolemizer<F> resolve(F input, Map<Skolem, Variable> mapping) {
		assert input != null;
		Class<Deskolemizer<?>>  result = null;
		Class<?> paramType = input.getClass();
		while (result == null && !paramType.equals(Object.class)) {
			result = (Class<Deskolemizer<?>>) repository.get(paramType);
			paramType = paramType.getSuperclass();
		}
		assert result != null;
		try {
			Constructor<Deskolemizer<?>> constructor = result.getConstructor();
			Deskolemizer<F> res = (Deskolemizer<F>) constructor.newInstance();
			res.mapping = mapping;
			return res;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Deskolemizer implementation for ConjunctiveQuery.
	 */
	public static class QueryDeskolemizer extends Deskolemizer<ConjunctiveQuery> {
		static { repository.put(ConjunctiveQuery.class, QueryDeskolemizer.class); }

		/**
		 * Rewrite.
		 *
		 * @param input ConjunctiveQuery
		 * @return ConjunctiveQuery
		 */
		@Override
		public ConjunctiveQuery rewrite(ConjunctiveQuery input) {
			QueryBuilder builder = new QueryBuilder();
			builder.setName(input.getHead().getName());
			for (Atom subFormula: input.getBody()) {
				builder.addBodyAtom(
						Deskolemizer.resolve(subFormula, this.mapping)
								.rewrite(subFormula));
			}
			for (Term t: input.getHead().getTerms()) {
				builder.addHeadTerm(this.map(t));
			}
			return builder.build();
		}
	}
	
	/**
	 * Deskolemizer implementation for Implication.
	 */
	public static class ImplicationDeskolemizer extends Deskolemizer<Implication<?, ?>> {
		static { repository.put(Implication.class, ImplicationDeskolemizer.class); }

		/**
		 * Rewrite.
		 *
		 * @param input Implication<?,?>
		 * @return Implication<?,?>
		 */
		@Override
		public Implication<?, ?> rewrite(Implication<?, ?> input) {
			Formula left = input.getLeft();
			Formula right = input.getRight();
			return Implication.of(
					Deskolemizer.resolve(left, this.mapping).rewrite(left),
					Deskolemizer.resolve(right, this.mapping).rewrite(right));
		}
	}
	
	/**
	 * Deskolemizer implementation for Conjunction.
	 */
	public static class ConjunctionDeskolemizer extends Deskolemizer<Conjunction<?>> {
		static { repository.put(Conjunction.class, ConjunctionDeskolemizer.class); }

		/**
		 * Rewrite.
		 *
		 * @param input Conjunction<?>
		 * @return Conjunction<?>
		 */
		@Override
		public Conjunction<?> rewrite(Conjunction<?> input) {
			Conjunction.Builder builder = Conjunction.builder();
			for (Formula subFormula: input) {
				builder.and(Deskolemizer.resolve(subFormula, this.mapping)
						.rewrite(subFormula));
			}
			return builder.build();
		}
	}
	
	/**
	 * Deskolemizer implementation for Disjunction.
	 */
	public static class DisjunctionDeskolemizer extends Deskolemizer<Disjunction<?>> {
		static { repository.put(Disjunction.class, DisjunctionDeskolemizer.class); }

		/**
		 * Rewrite.
		 *
		 * @param input Disjunction<?>
		 * @return Disjunction<?>
		 */
		@Override
		public Disjunction<?> rewrite(Disjunction<?> input) {
			Disjunction.Builder builder = Disjunction.builder();
			for (Formula subFormula: input) {
				builder.or(Deskolemizer.resolve(subFormula, this.mapping)
						.rewrite(subFormula));
			}
			return builder.build();
		}
	}
	
	/**
	 * Deskolemizer implementation for Negation.
	 */
	public static class NegationDeskolemizer extends Deskolemizer<Negation<?>> {
		static { repository.put(Negation.class, NegationDeskolemizer.class); }

		/**
		 * Rewrite.
		 *
		 * @param input Negation<?>
		 * @return Negation<?>
		 */
		@Override
		public Negation<?> rewrite(Negation<?> input) {
			Formula subFormula = input.getChild();
			return Negation.of(Deskolemizer.resolve(subFormula, this.mapping)
					.rewrite(subFormula));
		}
	}
	
	/**
	 * Deskolemizer implementation for Atoms.
	 */
	public static class AtomDeskolemizer extends Deskolemizer<Atom> {
		static { repository.put(Atom.class, AtomDeskolemizer.class); }

		/**
		 * Rewrite.
		 *
		 * @param input PredicateFormula
		 * @return PredicateFormula
		 */
		@Override
		public Atom rewrite(Atom input) {
			Atom.Builder builder = Atom.builder();
			builder.setSignature(input.getSignature());
			for (Term term: input.getTerms()) {
				builder.addTerm(this.map(term));
			}
			return builder.build();
		}
	}
}
