package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Skolem.Generator;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A conjunctive query (CQ) is a first order formula of the form
\exists x_1, \ldots, x_n \WEdge A_i,
where A_i are atoms with arguments that are either variables or constants.

 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class ConjunctiveQuery extends AbstractFormula implements Query<Conjunction<Predicate>> {

	/** The query's head part*/
	protected final Predicate head;

	/** The query's body part*/
	protected final Conjunction<Predicate> body;

	/** The terms in the head of the query*/
	protected final List<Term> freeTerms;

	/** The query's free variables i.e. head variables */
	protected final List<Variable> free;

	/** The query's bound variables*/
	protected final List<Variable> bound;

	/** Map of query's free variables to chase constants*/
	protected final Map<Variable, Constant> freeToCanonical;

	/** The constants that appear in the query's body*/
	protected final Collection<TypedConstant<?>> constants;

	/** The canonical database of the query*/
	protected Conjunction<Predicate> canonical;

	protected Map<Variable, Constant> grounding;

	/**
	 * Builds a conjunctive query given the input head and body.
	 * The query is grounded by assigning fresh chase constants to its variables 
	 * @param head
	 * 		The query's head
	 * @param body
	 * 		The query's body
	 */
	public ConjunctiveQuery(Predicate head, Conjunction<Predicate> body) {
		this(head, body, createGrounding(body));
	}
	
	/**
	 * Builds a conjunctive query given the input head variables and body.
	 * The query is grounded by assigning fresh chase constants to its variables 
	 * @param name
	 * 		The query's name
	 * @param headTerms
	 * 		The query's head variables
	 * @param body
	 * 		The query's body
	 */
	public ConjunctiveQuery(String name, List<Term> headTerms, Conjunction<Predicate> body) {
		this(new Predicate(new Signature(name, headTerms.size()), headTerms), body);
	}

	/**
	 * Builds a conjunctive query given the input head variables and body.
	 * The query is grounded using the input mapping of variables to constants.
	 *  
	 * @param head
	 * 		The query's head
	 * @param body
	 * 		The query's body
	 * @param grounding
	 * 		Mapping of query variables to constants  
	 */
	public ConjunctiveQuery(Predicate head, Conjunction<Predicate> body, Map<Variable, Constant> grounding) {
		super();
		List<Variable> free = head.getVariables();
		List<Variable> bound = Utility.getVariables(body.getPredicates());
		bound.removeAll(free);
		assert Collections.disjoint(free, bound): "Free and bound variables overlap.";
		assert Sets.difference(
				Sets.union(
						Sets.newLinkedHashSet(Utility.getVariables(body.getPredicates())),
						Sets.newLinkedHashSet(head.getVariables())),
						Sets.union(
								Sets.newLinkedHashSet(free),
								Sets.newLinkedHashSet(bound))).isEmpty():
									"Some variables are neither free nor bound.";
		this.free = free;
		this.head = head;
		this.bound = bound;
		this.body = body;
		this.freeTerms = head.getTerms();
		this.constants = getSchemaConstants(body);
		this.freeToCanonical = getFreeToCanonical(head,grounding);
		this.grounding = grounding;
		this.canonical = this.ground(grounding);
	}

	/**
	 * 
	 * @param right
	 * @return
	 * 		returns the schema constants of the input conjunction of atoms
	 */
	private static Collection<TypedConstant<?>> getSchemaConstants(Conjunction<Predicate> right) {
		Collection<TypedConstant<?>> constants = new LinkedHashSet<>();
		for(Predicate predicate: right.getChildren()) {
			for (Term term: predicate.getTerms()) {
				if (!term.isSkolem() && !term.isVariable()) {
					TypedConstant<?> schemaConstant = ((TypedConstant) term);
					Preconditions.checkState(schemaConstant != null);
					constants.add(schemaConstant);
				}
			}
		}
		return constants;
	}

	/**
	 * 
	 * @param body
	 * @return
	 * 		a mapping of variables of the input conjunction to constants. 
	 * 		A fresh constant is created for each variable of the conjunction. 
	 * 		This method is invoked by the conjunctive query constructor when the constructor is called with empty input mapping. 
	 */
	private static Map<Variable, Constant> createGrounding(Conjunction<Predicate> body) {
		Map<Variable, Constant> canonicalMapping = new LinkedHashMap<>();
			for (Predicate p: body) {
				for (Term t: p.getTerms()) {
					if (t.isVariable()) {
						Constant c = canonicalMapping.get(t);
						if (c == null) {
							c = new Skolem(Generator.getName());
							canonicalMapping.put((Variable) t, c);
						}
					}
				}
			}
		return canonicalMapping;
	}
	
	/**
	 * 
	 * @param head
	 * @param canonical
	 * @return
	 * 		a mapping of query free variables to canonical constants 
	 */
	private static Map<Variable, Constant> getFreeToCanonical(Predicate head, Map<Variable, Constant> canonical) {
		Map<Variable, Constant> freeToCanonical = new LinkedHashMap<>();
		for(Term headTerm: head.getTerms()) {
			Constant chaseTerm  = canonical.get(headTerm);
			if (chaseTerm != null && !chaseTerm.isSkolem()) {
				throw new java.lang.IllegalStateException("Chase Term " + headTerm + ", " + head.getTerms());
			}
			if (headTerm.isVariable()) {
				freeToCanonical.put((Variable) headTerm, chaseTerm);
			}
		}
		return freeToCanonical;
	}
	
	@Override
	public void setGrounding(Map<Variable, Constant> grounding) {
		this.grounding = grounding;
		this.canonical = this.ground(grounding);
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.formula.Query#ground(java.util.Map)
	 */
	@Override
	public Conjunction<Predicate> ground(Map<Variable, Constant> mapping) {
		List<Predicate> bodyAtoms = new ArrayList<>();
		for (Predicate atom: this.body.getChildren()) {
			bodyAtoms.add(atom.ground(mapping));
		}
		return Conjunction.of(bodyAtoms);
	}


	/**
	 * @return Conjunction<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Query#getCanonical()
	 */
	@Override
	public Conjunction<Predicate> getCanonical() {
		return this.canonical;
	}

	/**
	 * @return List<Query<Conjunction<PredicateFormula>>>
	 * @see uk.ac.ox.cs.pdq.fol.Query#getImportantSubqueries()
	 */
	@Override
	public List<Query<Conjunction<Predicate>>> getImportantSubqueries() {
		List<Query<Conjunction<Predicate>>> queries = new ArrayList<>();
		Set<Predicate> sets = new LinkedHashSet<>();
		sets.addAll(this.body.getChildren());
		Set<Set<Predicate>> subQueries = Sets.powerSet(sets);
		Iterator<Set<Predicate>> subQueryIterator = subQueries.iterator();
		while (subQueryIterator.hasNext()) {
			Set<Predicate> queryConjuncts = subQueryIterator.next();
			if(!queryConjuncts.isEmpty())
			{
				List<Variable> variables = Utility.getVariables(queryConjuncts);
				List<Variable> appearingBound = Lists.newArrayList(this.bound);
				appearingBound.retainAll(variables);

				Set<Set<Variable>> appearingBoundSets = Sets.powerSet(Sets.newLinkedHashSet(appearingBound));
				Iterator<Set<Variable>> appearingBoundIterator = appearingBoundSets.iterator();
				while (appearingBoundIterator.hasNext()) {
					Set<Variable> v = appearingBoundIterator.next();
					List<Variable> myfree = Lists.newArrayList(variables);
					myfree.removeAll(v);

					ConjunctiveQuery cq = new ConjunctiveQuery(
							new Predicate(new Signature("Q", myfree.size()), new ArrayList<>(myfree)),
							Conjunction.of(queryConjuncts));
					queries.add(cq);
				}
			}
		}
		return queries;
	}

	/**
	 * @return List<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getPredicates()
	 */
	@Override
	public List<Predicate> getPredicates() {
		List<Predicate> result = new ArrayList<>();
		result.add(this.head);
		result.addAll(this.body.getPredicates());
		return result;
	}

	/**
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getTerms()
	 */
	@Override
	public List<Term> getTerms() {
		Set<Term> terms = new LinkedHashSet<>();
		terms.addAll(this.head.getTerms());
		terms.addAll(this.body.getTerms());
		return new ArrayList<>(terms);
	}

	/**
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Query#isBoolean()
	 */
	@Override
	public boolean isBoolean() {
		return this.free.isEmpty();
	}

	/**
	 * @return Conjunction<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Query#getBody()
	 */
	@Override
	public Conjunction<Predicate> getBody() {
		return this.body;
	}

	/**
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.fol.Query#getHead()
	 */
	@Override
	public Predicate getHead() {
		return this.head;
	}

	/**
	 * @return List<Variable>
	 */
	public List<Variable> getBound() {
		return this.bound;
	}

	/**
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Evaluatable#getFree()
	 */
	@Override
	public List<Term> getFree() {
		return this.freeTerms;
	}

	/**
	 * @return Collection<TypedConstant<?>>
	 * @see uk.ac.ox.cs.pdq.fol.Query#getSchemaConstants()
	 */
	@Override
	public Collection<TypedConstant<?>> getSchemaConstants() {
		return this.constants;
	}

	/**
	 * @return Map<Variable,Term>
	 * @see uk.ac.ox.cs.pdq.fol.Query#getFreeToCanonical()
	 */
	@Override
	public Map<Variable, Constant> getFreeToCanonical() {
		return this.freeToCanonical;
	}
	
	@Override
	public Map<Variable, Constant> getVariablesToCanonical() {
		return this.grounding;
	}

	/**
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.head.equals(((ConjunctiveQuery) o).head)
				&& this.body.equals(((ConjunctiveQuery) o).body)
				&& this.free.equals(((ConjunctiveQuery) o).free)
				&& this.bound.equals(((ConjunctiveQuery) o).bound);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.free, this.bound, this.head, this.body);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.getHead() + " <- " + /*this.bound +*/ this.getBody();
	}

	/**
	 * @return Collection<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getSubFormulas()
	 */
	@Override
	public Collection<Predicate> getChildren() {
		return this.getBody().getChildren();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.fol.Rule#contains(uk.ac.ox.cs.pdq.fol.Signature)
	 */
	@Override
	public boolean contains(Signature s) {
		for (Predicate atom: this.getPredicates()) {
			if (atom.getSignature().equals(s)) {
				return true;
			}
		}
		return false;
	}
}
