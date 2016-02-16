package uk.ac.ox.cs.pdq.db.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.builder.Builder;
import uk.ac.ox.cs.pdq.builder.BuilderException;
import uk.ac.ox.cs.pdq.fol.AcyclicQuery;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
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
	private List<Predicate> body = new LinkedList<>();

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
	private Predicate unifyVariable(Predicate p) {
		Collection<Term> uniTerms = new ArrayList<>();
		for (Term t : p.getTerms()) {
			if (t.isVariable() || t.isSkolem()) {
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
		return new Predicate(p.getSignature(), uniTerms);
	}
	
	/**
	 * Adds the body atom.
	 *
	 * @param p PredicateFormula
	 * @return QueryBuilder
	 */
	public QueryBuilder addBodyAtom(Predicate p) {
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
		if (term.isVariable() || term.isSkolem()) {
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
		Conjunction<Predicate> b = Conjunction.of(this.body);
		if (this.type != null && this.type.equals("acyclic")) {
			return new AcyclicQuery(this.name, this.head, b);
		}
		return new ConjunctiveQuery(this.name, this.head, b);
	}

}
