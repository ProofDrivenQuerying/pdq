package uk.ac.ox.cs.pdq.services.logicblox.rewrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.builder.QueryBuilder;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.NaryFormula;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;

import com.beust.jcommander.internal.Lists;

// TODO: Auto-generated Javadoc
/**
 * Rewriter that produces an output formula equivalent to the input one,
 * where constant in predicate have moved as external equality predicates.
 *  
 *
 * @author Julien Leblay
 * @param <F> the generic type
 */
public class PullEqualityRewriter<F extends Formula> implements Rewriter<F, F> {

	/** The bindings. */
	private final Map<Variable, TypedConstant<?>> bindings = new LinkedHashMap<>();
	
	/** The schema. */
	private final Schema schema;
	
	/**
	 * Instantiates a new pull equality rewriter.
	 *
	 * @param schema the schema
	 */
	public PullEqualityRewriter(Schema schema) {
		this.schema = schema;
	}
	
	/**
	 * Rewrite.
	 *
	 * @param input Formula
	 * @return a rewriting of the input formula where equality predicates have
	 * been removed.
	 */
	@Override
	public F rewrite(F input) {
		Formula result =  this.findBindings(input);
		return (F) this.incorporateEqualities(result);
	}
	
	/**
	 * Apply the propagation to a top-level formula.
	 *
	 * @param f the f
	 * @return the rewritten formula
	 */
	private Formula incorporateEqualities(Formula f) {
		if (f instanceof ConjunctiveQuery) {
			QueryBuilder result = new QueryBuilder();
			ConjunctiveQuery query = ((ConjunctiveQuery) f);
			for (Predicate p: query.getBody()) {
				result.addBodyAtom(p);
			}
			for (Predicate p: this.makeEqualities()) {
				result.addBodyAtom(p);
			}
			result.setName(query.getHead().getName());
			for (Term t: query.getHead().getTerms()) {
				result.addHeadTerm(t);
			}
			return result.build();
		}
		Conjunction.Builder result = Conjunction.builder();
		result.and(f);
		for (Formula g: this.makeEqualities()) {
			result.and(g);
		}
		return result.build();
	}
	
	/**
	 * Make equalities.
	 *
	 * @return a conjunction of equality predicates, based on the bindings
	 * recorded so far.
	 */
	private Conjunction<Predicate> makeEqualities() {
		Collection<Predicate> result = new ArrayList<>();
		for (Map.Entry<Variable, TypedConstant<?>> entry: this.bindings.entrySet()) {
			if (String.class.equals(entry.getValue().getType())) {
				result.add(new Predicate(
						this.schema.getRelation("string:eq_2"),
						Lists.newArrayList(entry.getKey(), entry.getValue())));
			}
			// TODO: manage other primitive types
		}
		return Conjunction.of(result);
	}
	
	/**
	 * Apply the propagation to a top-level formula.
	 *
	 * @param f the f
	 * @return the rewritten formula
	 */
	private Formula findBindings(Formula f) {
		if (f instanceof NaryFormula) {
			return this.propagate((NaryFormula) f);
		}
		if (f instanceof Negation) {
			return this.propagate((Negation) f);
		}
		if (f instanceof Predicate) {
			return this.propagate((Predicate) f);
		}
		if (f instanceof ConjunctiveQuery) {
			ConjunctiveQuery query = ((ConjunctiveQuery) f);
			Formula body = this.findBindings(query.getBody());
			if (body instanceof Predicate) {
				return new ConjunctiveQuery(query.getHead(),
						Conjunction.of((Predicate) body));
			}
			return new ConjunctiveQuery(query.getHead(),
						(Conjunction<Predicate>) body);
		}
		throw new UnsupportedOperationException(f + " not supported in equality propagation rewriting.");
	}

	/**
	 * Apply the propagation to a conjunction or disjunction.
	 * @param nary NaryFormula<Formula>
	 * @return the rewritten formula
	 */
	private Formula propagate(NaryFormula<Formula> nary) {
		List<Formula> result = new LinkedList<>();
		for (Formula f: nary) {
			Formula g = this.findBindings(f);
			if (g != null) {
				result.add(g);
			}
		}
		if (result.isEmpty()) {
			return null;
		}
		if (result.size() == 1) {
			return result.get(0);
		}
		if (nary instanceof Conjunction) {
			return Conjunction.of(result);
		}
		if (nary instanceof Disjunction) {
			return Disjunction.of(result);
		}
		throw new UnsupportedOperationException(nary + " not supported in equality propagation rewriting.");
	}


	/**
	 * Apply the propagation to a negated formula.
	 * @param negation Negation<Formula>
	 * @return the rewritten formula
	 */
	private Negation<Formula> propagate(Negation<Formula> negation) {
		Collection<Formula> subFormula = negation.getChildren();
		assert subFormula.size() == 1;
		return Negation.of(this.findBindings(subFormula.iterator().next()));
	}

	/**
	 * Apply the propagation to a predicate.
	 * @param pred PredicateFormula
	 * @return the rewritten formula
	 */
	private Predicate propagate(Predicate pred) {
		List<Term> results = new ArrayList<>();
		for (Term t: pred.getTerms()) {
			if (!t.isVariable() && !t.isSkolem()) {
				Variable v = Variable.getFreshVariable();
				this.bindings.put(v, (TypedConstant) t);
				results.add(v);
			} else {
				results.add(t);
			}
		}
		return new Predicate(pred.getSignature(), results);
	}
}
