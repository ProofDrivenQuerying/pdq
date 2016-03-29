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
import uk.ac.ox.cs.pdq.util.CanonicalNameGenerator;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * A conjunctive query (CQ) is a first order formula of the form \exists x_1, \ldots, x_n \Wedge A_i,
 * where A_i are atoms with arguments that are either variables or constants.

 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class ConjunctiveQuery extends AbstractFormula implements Query<Conjunction<Atom>> {

	/**  The query's head part. */
	protected final Atom head;

	/**  The query's body part. */
	protected final Conjunction<Atom> body;

	/**  The terms in the head of the query. */
	protected final List<Term> freeTerms;

	/** The query's free variables i.e. head variables */
	protected final List<Variable> free;

	/**  The query's bound variables. */
	protected final List<Variable> bound;

	/**  Map of query's free variables to chase constants. */
	protected final Map<Variable, Constant> freeToCanonical;

	/**  The constants that appear in the query's body. */
	protected final Collection<TypedConstant<?>> constants;

	/**  The canonical database of the query. */
	protected Conjunction<Atom> canonical;

	/** The grounding. */
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
		this.canonical = this.ground(grounding);
	}

	/**
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
	 * Generate canonical mapping.
	 *
	 * @param body the body
	 * @return 		a mapping of variables of the input conjunction to constants. 
	 * 		A fresh constant is created for each variable of the conjunction. 
	 * 		This method is invoked by the conjunctive query constructor when the constructor is called with empty input canonical mapping.
	 */
	private static Map<Variable, Constant> generateCanonicalMapping(Conjunction<Atom> body) {
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
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.fol.Query#setGrounding(java.util.Map)
	 */
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
	public Conjunction<Atom> ground(Map<Variable, Constant> mapping) {
		List<Atom> bodyAtoms = new ArrayList<>();
		for (Atom atom: this.body.getChildren()) {
			bodyAtoms.add(atom.ground(mapping));
		}
		return Conjunction.of(bodyAtoms);
	}


	/**
	 * Gets the canonical.
	 *
	 * @return Conjunction<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Query#getCanonical()
	 */
	@Override
	public Conjunction<Atom> getCanonical() {
		return this.canonical;
	}

	/**
	 * Gets the predicates.
	 *
	 * @return List<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getAtoms()
	 */
	@Override
	public List<Atom> getAtoms() {
		List<Atom> result = new ArrayList<>();
		result.add(this.head);
		result.addAll(this.body.getAtoms());
		return result;
	}

	/**
	 * Gets the terms.
	 *
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
	 * Checks if is boolean.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Query#isBoolean()
	 */
	@Override
	public boolean isBoolean() {
		return this.free.isEmpty();
	}

	/**
	 * Gets the body.
	 *
	 * @return Conjunction<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Query#getBody()
	 */
	@Override
	public Conjunction<Atom> getBody() {
		return this.body;
	}

	/**
	 * Gets the head.
	 *
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.fol.Query#getHead()
	 */
	@Override
	public Atom getHead() {
		return this.head;
	}

	/**
	 * Gets the bound.
	 *
	 * @return List<Variable>
	 */
	public List<Variable> getBound() {
		return this.bound;
	}

	/**
	 * Gets the free.
	 *
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Evaluatable#getFree()
	 */
	@Override
	public List<Term> getFree() {
		return this.freeTerms;
	}

	/**
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
	 * Gets the free to canonical.
	 *
	 * @return Map<Variable,Term>
	 * @see uk.ac.ox.cs.pdq.fol.Query#getFreeToCanonical()
	 */
	@Override
	public Map<Variable, Constant> getFreeToCanonical() {
		return this.freeToCanonical;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.fol.Query#getVariablesToCanonical()
	 */
	@Override
	public Map<Variable, Constant> getVariablesToCanonical() {
		return this.grounding;
	}

	/**
	 * Equals.
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

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.free, this.bound, this.head, this.body);
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return this.getHead() + " <- " + /*this.bound +*/ this.getBody();
	}

	/**
	 * Gets the children.
	 *
	 * @return Collection<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getSubFormulas()
	 */
	@Override
	public Collection<Atom> getChildren() {
		return this.getBody().getChildren();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.fol.Rule#contains(uk.ac.ox.cs.pdq.fol.Predicate)
	 */
	@Override
	public boolean contains(Predicate s) {
		for (Atom atom: this.getAtoms()) {
			if (atom.getSignature().equals(s)) {
				return true;
			}
		}
		return false;
	}
}
