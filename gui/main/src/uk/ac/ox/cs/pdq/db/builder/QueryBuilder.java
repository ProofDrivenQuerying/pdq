package uk.ac.ox.cs.pdq.db.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.builder.Builder;
import uk.ac.ox.cs.pdq.builder.BuilderException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Named;

// TODO: Auto-generated Javadoc
/**
 * Builder for conjunctive queries.
 * 
 * @author Julien Leblay
 */
public class QueryBuilder implements Builder<ConjunctiveQuery> {

	/**  The temporary query name. */
	private String name = null;

	/**  The temporary query type. */
	private String type = null;

	/**  The temporary query head. */
	private List<Term> head = new LinkedList<>();

	/**  The temporary query body. */
	private List<Atom> body = new LinkedList<>();

	/** An index from variable name to their respective instances. */
	private Map<String, Term> termIndex = new LinkedHashMap<>();

	/**
	 * Replaces the variable of a predicate by variable instances present in
	 * the builder's variable index. This method allows several predicates to
	 * share the same variables instance, make comparison more efficient.
	 *
	 * @param p the p
	 * @return a PredicateFormula identical to p, in which variables are those
	 * appearing the instance's variable index.
	 */
	private Atom unifyVariable(Atom p) {
		Collection<Term> uniTerms = new ArrayList<>();
		for (Term t : p.getTerms()) {
			if (t.isVariable() /* MR || t.isSkolem() */) {
				Term v = this.termIndex.get(((Named) t).getName());
				if (v == null) {
					this.termIndex.put(((Named) t).getName(), t);
					uniTerms.add(t);
				} else {
					uniTerms.add(v);
				}
			} else {
				uniTerms.add(t);
			}
		}
		Term[] terms = new Term[uniTerms.size()];
		int i = 0;
		for(Term term : uniTerms) terms[i++] = term;
		return Atom.create(p.getPredicate(), terms);
	}
	
	/**
	 * Adds the body atom.
	 *
	 * @param p PredicateFormula
	 * @return QueryBuilder
	 */
	public QueryBuilder addBodyAtom(Atom p) {
		this.body.add(this.unifyVariable(p));
		return this;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param n String
	 * @return QueryBuilder
	 */
	public QueryBuilder setName(String n) {
		this.name = n;
		return this;
	}
	
	/**
	 * Sets the type.
	 *
	 * @param n String
	 * @return QueryBuilder
	 */
	public QueryBuilder setType(String n) {
		this.type = n;
		return this;
	}
	
	/**
	 * Adds the head term.
	 *
	 * @param term Term
	 * @return QueryBuilder
	 */
	public QueryBuilder addHeadTerm(Term term) {
		if (term.isVariable() /* MR || term.isSkolem() */) {
			Term v = this.termIndex.get(((Named) term).getName());
			if (v != null) {
				this.head.add(v);
				return this;
			}
			throw new BuilderException("Attempting to add variable '" + term + "' to query head while it does not appear in the body");
		}
		this.head.add(term);
		return this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
	 */
	@Override
	public ConjunctiveQuery build() {
		Atom[] atoms = new Atom[this.body.size()];
		int i = 0;
		for(Atom atom : this.body) atoms[i++] = atom;
		return ConjunctiveQuery.create(new Variable[0], atoms);
	}

}
