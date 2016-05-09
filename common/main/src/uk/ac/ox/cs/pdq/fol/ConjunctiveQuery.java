package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.CanonicalNameGenerator;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * TOCOMMENT find a pretty way to write formulas in javadoc
 * A conjunctive query (CQ) is a first order formula of the form \exists x_1, \ldots, x_n \Wedge A_i,
 * where A_i are atoms with arguments that are either variables or constants.

 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class ConjunctiveQuery extends AbstractFormula implements Query<Conjunction<Atom>> {

	/**  The query's head part. */
	protected final Atom head;

	/**  
	 * The query's body part. */
	protected final Conjunction<Atom> body;

	/**
	 * TOCOMMENT what is the difference of the following two fields? Dow e keep a copy of the free vars both
	 * variables and as terms? Why? a variable object is a Term anyway.
	 * 
	 * The terms in the head of the query. */
	protected final List<Term> freeTerms;

	/** The query's free variables i.e. head variables */
	protected final List<Variable> free;

	/**  
	 * TOCOMMENT is bound the same as existentially quantified? I don't think so
	 * The query's bound variables. */
	protected final List<Variable> bound;
	
	/**  
	 * TOCOMMENT we should get rid of this when we fix #42, together with the grounding field a few lines below, they are very confusing.
	 * 
	 * Map of query's free variables to chase constants. */
	protected Map<Variable, Constant> freeToCanonical;

	/**  The constants that appear in the query's body. */
	protected final Collection<TypedConstant<?>> constants;

	/** 
	 * TOCOMMENT we should get rid of this when we fix #42
	 * 
	 * The grounding. */
	protected Map<Variable, Constant> grounding;

	/**
	 * Builds a conjunctive query given the input head and body.
	 * The query is grounded by assigning fresh chase constants to its variables 
	 * @param head
	 * 		The query's head
	 * @param body
	 * 		The query's body
	 */
	public ConjunctiveQuery(Atom head, Conjunction<Atom> body) {
		this(head, body, generateCanonicalMapping(body));
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
	public ConjunctiveQuery(String name, List<Term> headTerms, Conjunction<Atom> body) {
		this(new Atom(new Predicate(name, headTerms.size()), headTerms), body);
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
	public ConjunctiveQuery(Atom head, Conjunction<Atom> body, Map<Variable, Constant> grounding) {
		super();
		List<Variable> free = head.getVariables();
		List<Variable> bound = Utility.getVariables(body.getAtoms());
		bound.removeAll(free);
		assert Collections.disjoint(free, bound): "Free and bound variables overlap.";
		assert Sets.difference(
				Sets.union(
						Sets.newLinkedHashSet(Utility.getVariables(body.getAtoms())),
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
	}

	/**
	 * TOCOMMENT why is this method here? Shouldn't it be in utility?
	 * 
	 * Gets the schema constants.
	 *
	 * @param right the right
	 * @return 		returns the schema constants of the input conjunction of atoms
	 */
	private static Collection<TypedConstant<?>> getSchemaConstants(Conjunction<Atom> right) {
		Collection<TypedConstant<?>> constants = new LinkedHashSet<>();
		for(Atom atom: right.getChildren()) {
			for (Term term: atom.getTerms()) {
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
	 * TOCOMMENT the next 3 methods are discussed in #42
	 * 
	 * Generate canonical mapping.
	 *
	 * @param body the body
	 * @return 		a mapping of variables of the input conjunction to constants. 
	 * 		A fresh constant is created for each variable of the conjunction. 
	 * 		This method is invoked by the conjunctive query constructor when the constructor is called with empty input canonical mapping.
	 */
	public static Map<Variable, Constant> generateCanonicalMapping(Conjunction<Atom> body) {
		Map<Variable, Constant> canonicalMapping = new LinkedHashMap<>();
			for (Atom p: body) {
				for (Term t: p.getTerms()) {
					if (t.isVariable()) {
						Constant c = canonicalMapping.get(t);
						if (c == null) {
							c = new Skolem(CanonicalNameGenerator.getName());
							canonicalMapping.put((Variable) t, c);
						}
					}
				}
			}
		return canonicalMapping;
	}
	
	/**
	 * Gets the free to canonical.
	 *
	 * @param head the head
	 * @param canonical the canonical
	 * @return 		a mapping of query free variables to canonical constants
	 */
	private static Map<Variable, Constant> getFreeToCanonical(Atom head, Map<Variable, Constant> canonical) {
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
	
	/**
	 * Retruns the grounded body atoms of this query.
	 *
	 * @param mapping Map<Variable,Constant>
	 * @return a copy of the query grounded using the given mapping
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Constant>)
//	/* (non-Javadoc)
//	 * @see uk.ac.ox.cs.pdq.fol.Query#setGrounding(java.util.Map)
//	 */
	@Override
	public Conjunction<Atom> ground(Map<Variable, Constant> mapping) {
		List<Atom> bodyAtoms = new ArrayList<>();
		for (Atom atom: this.body.getChildren()) {
			bodyAtoms.add(atom.ground(mapping));
		}
		return Conjunction.of(bodyAtoms);
	}

	@Override
	public List<Atom> getAtoms() {
		List<Atom> result = new ArrayList<>();
		result.add(this.head);
		result.addAll(this.body.getAtoms());
		return result;
	}


	@Override
	public List<Term> getTerms() {
		Set<Term> terms = new LinkedHashSet<>();
		terms.addAll(this.head.getTerms());
		terms.addAll(this.body.getTerms());
		return new ArrayList<>(terms);
	}

	/**
	 * Checks if the query is boolean boolean.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Query#isBoolean()
	 */
	@Override
	public boolean isBoolean() {
		return this.free.isEmpty();
	}

	/**
	 * Gets the body of the query.
	 *
	 * @return Conjunction<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Query#getBody()
	 */
	@Override
	public Conjunction<Atom> getBody() {
		return this.body;
	}

	/**
	 * Gets the head of the query.
	 *
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.fol.Query#getHead()
	 */
	@Override
	public Atom getHead() {
		return this.head;
	}

	/**
	 * Gets the bound variables of the query.
	 *
	 * @return List<Variable>
	 */
	public List<Variable> getBound() {
		return this.bound;
	}

	/**
	 * Gets the free variables of the query.
	 *
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Evaluatable#getFree()
	 */
	@Override
	public List<Term> getFree() {
		return this.freeTerms;
	}

	/**
	 * TOCOMMENT? which schema? We propably mean "Gets query's constants".
	 * 
	 * Gets the schema constants.
	 *
	 * @return Collection<TypedConstant<?>>
	 * @see uk.ac.ox.cs.pdq.fol.Query#getSchemaConstants()
	 */
	@Override
	public Collection<TypedConstant<?>> getSchemaConstants() {
		return this.constants;
	}
	
	/**
	 * Gets the mapping of the free query variables to canonical constants.
	 *
	 * @return a map of query's free variables to its canonical constants.
	 * Given a CQ Q, the canonical database of Q is the instance which has for each atom R(\vec{v}) 
	 * in Q a corresponding fact for relation R with \vec{v} as a tuple. The canonical constants are the constants of the canonical database of Q
	 */
	public Map<Variable, Constant> getGroundingsProjectionOnFreeVars() {
		return this.freeToCanonical;
	}
	
	public Map<Variable, Constant> getGrounding() {
		return this.grounding;
	}
	
	/**
	 * TOCOMMENT this is exact identity why not use toString()?
	 * Two queries are equal if their heads, bodies, free terms, and bound terms, are all equal (using their corresponding equals).
	 *
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


	@Override
	public int hashCode() {
		return Objects.hash(this.free, this.bound, this.head, this.body);
	}


	@Override
	public String toString() {
		return this.getHead() + " <- " + /*this.bound +*/ this.getBody();
	}

	@Override
	public Collection<Atom> getChildren() {
		return this.getBody().getChildren();
	}

	@Override
	public boolean contains(Predicate s) {
		for (Atom atom: this.getAtoms()) {
			if (atom.getPredicate().equals(s)) {
				return true;
			}
		}
		return false;
	}
}
