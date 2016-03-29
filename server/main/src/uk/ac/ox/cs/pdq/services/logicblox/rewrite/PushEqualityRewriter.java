package uk.ac.ox.cs.pdq.services.logicblox.rewrite;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.builder.BuilderException;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.builder.DependencyBuilder;
import uk.ac.ox.cs.pdq.db.builder.QueryBuilder;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.NaryFormula;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.rewrite.Rewriter;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

// TODO: Auto-generated Javadoc
/**
 * Rewriter that produces an output formula equivalent to the input one,
 * containing no equality predicates.
 *  
 *
 * @author Julien Leblay
 * @param <F> the generic type
 */
public class PushEqualityRewriter<F extends Formula> implements Rewriter<F, F> {

	/**
	 * Mapping from a term to the cluster is belongs to. A cluster
	 * represented a group of terms that are meant to be equal
	 */
	private final Multimap<Term, Term> clusters = LinkedHashMultimap.create();

	/** Mapping from clusters to the term chosen as its representative. */
	private final Map<Collection<Term>, Term> representatives = new LinkedHashMap<>();

	/**
	 * Rewrites the input formula so as to produce an equivalent one in which
	 * without any equality predicates. This is done in two passes:
	 *  - In the first phase, we group terms into equality clusters
	 *  - In the second phase, we traverse to formula, and replace each variable
	 *  belong to a non-empty clusters by the representative term of that 
	 *  cluster.
	 *
	 * @param input Formula
	 * @return a rewriting of the input formula where equality predicates have
	 * been removed.
	 * @throws RewriterException the rewriter exception
	 */
	@Override
	public F rewrite(F input) throws RewriterException {
		// Pass 1: find all equalities and create corresponding term clusters,
		this.buildClusters(input);
		// Pass 2: replace
		return (F) this.propagate(input);
	}
	
	/**
	 * Apply the propagation to a top-level formula.
	 *
	 * @param f the f
	 * @return the rewritten formula
	 * @throws RewriterException the rewriter exception
	 */
	private Formula propagate(Formula f) throws RewriterException {
		if (f instanceof NaryFormula) {
			return this.propagate((NaryFormula) f);
		}
		if (f instanceof Negation) {
			return this.propagate((Negation) f);
		}
		if (f instanceof Atom) {
			return this.propagate((Atom) f);
		}
		if (f instanceof ConjunctiveQuery) {
			return this.propagate((ConjunctiveQuery) f);
		}
		if (f instanceof Implication) {
			return this.propagate((Implication) f); 
		}
		throw new RewriterException(f + " not supported for push equality rewriting.");
	}

	/**
	 * Apply the propagation to a query.
	 *
	 * @param query NaryFormula<Formula>
	 * @return the rewritten formula
	 * @throws RewriterException the rewriter exception
	 */
	private ConjunctiveQuery propagate(ConjunctiveQuery query) throws RewriterException {
		QueryBuilder result = new QueryBuilder();
		Formula newBody = this.propagate(query.getBody());
		if (newBody == null) {
			throw new RewriterException(query + " not supported for push equality rewriting.");
		}
		try {
			if (newBody instanceof Conjunction) {
				for (Atom p: (Conjunction<Atom>) newBody) {
					result.addBodyAtom(p);
				}
			} else if (newBody instanceof Atom) {
				result.addBodyAtom((Atom) newBody);
			}
			result.setName(query.getHead().getName());
			for (Term t: this.propagate(query.getHead().getTerms())) {
				result.addHeadTerm(t);
			}
			return result.build();
		} catch (BuilderException e) {
			throw new RewriterException(e.getMessage(), e);
		}
	}

	/**
	 * Apply the propagation to a conjunction or disjunction.
	 *
	 * @param nary NaryFormula<Formula>
	 * @return the rewritten formula
	 * @throws RewriterException the rewriter exception
	 */
	private Formula propagate(NaryFormula<Formula> nary) throws RewriterException {
		List<Formula> result = new LinkedList<>();
		for (Formula f: nary) {
			Formula g = this.propagate(f);
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
		throw new RewriterException(nary + " not supported for equality propagation rewriting.");
	}

	/**
	 * Apply the propagation to a conjunction or disjunction.
	 *
	 * @param implication the implication
	 * @return the rewritten formula
	 * @throws RewriterException the rewriter exception
	 */
	private Formula propagate(Implication<?, ?> implication) throws RewriterException {
		if (implication instanceof TGD) {
			DependencyBuilder builder = new DependencyBuilder();
			builder.addLeft(this.propagate(((TGD) implication).getLeft()));
			builder.addRight(this.propagate(((TGD) implication).getRight()));
			return builder.build();
		}
		return Implication.of(
				this.propagate(implication.getLeft()),
				this.propagate(implication.getRight()));
	}

	/**
	 * Apply the propagation to a negated formula.
	 *
	 * @param negation Negation<Formula>
	 * @return the rewritten formula
	 * @throws RewriterException the rewriter exception
	 */
	private Negation<Formula> propagate(Negation<Formula> negation) throws RewriterException {
		Collection<Formula> subFormula = negation.getChildren();
		assert subFormula.size() == 1;
		Formula sub = this.propagate(subFormula.iterator().next());
		if (sub != null) {
			return Negation.of(sub);
		}
		throw new RewriterException(negation + " not supported in equality propagation rewriting.");
	}

	/**
	 * Apply the propagation to a predicate.
	 * @param pred PredicateFormula
	 * @return the rewritten formula
	 */
	private Atom propagate(Atom pred) {
		if (pred.isEquality()) {
			return null;
		}
		return new Atom(pred.getSignature(), this.propagate(pred.getTerms()));
	}

	/**
	 * Apply the propagation to a collection of terms.
	 * @param terms Collection<Term>
	 * @return a new collection in which each term belonging to an equality
	 * cluster has been replaced by that cluster's representative.
	 */
	private Collection<Term> propagate(Collection<Term> terms) {
		Collection<Term> result = new LinkedList<>();
		for (Term t: terms) {
			Collection<Term> cluster = this.clusters.get(t);
			if (!cluster.isEmpty()) {
				result.add(this.representatives.get(cluster));
			} else {
				result.add(t);
			}
		}
		return result;
	}
	
	/**
	 * Find equalities with a formula and build equality clusters accordingly .
	 *
	 * @param f the f
	 * @throws RewriterException the rewriter exception
	 */
	private void buildClusters(Formula f) throws RewriterException {
		for (Atom pred: f.getAtoms()) {
			if (pred.isEquality()) {
				this.cluster(pred.getTerms());
			}
		}
	}
	
	/**
	 * Groups all the input terms into a single clusters. If some terms in terms
	 * already belongs to some distinct clusters, there are merged into a single
	 * one.
	 *
	 * @param terms the terms
	 * @throws RewriterException the rewriter exception
	 */
	private void cluster(Collection<Term> terms) throws RewriterException  {
		Collection<Term> c1;
		Iterator<Term> it = terms.iterator();
		if (it.hasNext()) {
			Term t1 = it.next();
			c1 = this.clusters.get(t1);
			this.addToCluster(t1, c1);
			while(it.hasNext()) {
				Term ti = it.next();
				this.addToCluster(ti, c1);
				Collection<Term> c2 = this.clusters.get(ti);
				if (!c2.isEmpty()) {
					c1.addAll(c2);
					this.clusters.putAll(ti, c1);
				}
			}
		}
	}
	
	/**
	 * Adds a single term t to the given cluster, and update the clusters 
	 * representative.  
	 *
	 * @param t the t
	 * @param cluster the cluster
	 * @throws RewriterException the rewriter exception
	 */
	private void addToCluster(Term t, Collection<Term> cluster) throws RewriterException {
		Term rep = this.representatives.remove(cluster);
		if (rep == null) {
			rep = t;
		}
		cluster.add(t);
		if (!t.isVariable()) {
			if (rep.isVariable()) {
				rep = t;
			} else if (!t.equals(rep)) {
				throw new RewriterException("Unsatisfiable equality: " + t + " <> " + rep);
			}
		}
		this.representatives.put(cluster, rep);
	}
}
