package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * TOCOMMENT this is not well readable in javadoc we need to find out how to write formulas in javadoc (maybe html?)
 * A dependency of the form \delta = \forall \vec{x} \rho(\vec{x}) --> x_i = x_j where \rho is a conjunction of atoms.
 *
 * @author Efthymia Tsamoura
 */
public class EGD
		extends Implication<Conjunction<Atom>, Conjunction<Equality>>
		implements Dependency<Conjunction<Atom>, Conjunction<Equality>> {
	
	/**  The dependency's universally quantified variables. */
	protected List<Variable> universal;
	
	/**  The dependency's constants. */
	protected Collection<TypedConstant<?>> constants = new LinkedHashSet<>();
	
	/**
	 * Instantiates a new egd.
	 *
	 * @param left The left-hand side conjunction of the dependency
	 * @param right The right-hand side conjunction of the dependency
	 */
	public EGD(Conjunction<Atom> left, Conjunction<Equality> right) {
		super(left, right);
		this.universal = Utility.getVariables(left.getAtoms());
		for (Term term:right.getTerms()) {
			if (!term.isVariable() && !term.isSkolem()) {
				this.constants.add(((TypedConstant) term));
			}
		}
	}
	
	/**
	 * Gets the universally quantified variables.
	 *
	 * @return List<Variable>
	 */
	public List<Variable> getUniversal() {
		return this.universal;
	}

	/**
	 * TOCOMMENT why is this a list of Term objects while the method above returns a list of Variable objects?
	 * TOCOMMENT An EGD does not have free variables, it has only universally quantified
	 * TOCOMMENT See #139 for comments for all methods below up to line 143 
	 * Gets the free variables.
	 *
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Evaluatable#getFree()
	 */
	@Override
	public List<Term> getFree() {
		return Lists.<Term>newArrayList(this.universal);
	}

	/**
	 * TOCOMMENT it would be better to say getBody or getHead; it's not always clear what left and right is.
	 * Gets the left-hand side of the constraint.
	 *
	 * @return L
	 * @see uk.ac.ox.cs.pdq.db.Dependency#getLeft()
	 */
	@Override
	public Conjunction<Atom> getLeft() {
		return this.left;
	}

	/**
	 * Gets the right.
	 *
	 * @return R
	 * @see uk.ac.ox.cs.pdq.db.Dependency#getRight()
	 */
	@Override
	public Conjunction<Equality> getRight() {
		return this.right;
	}

	/**
	 * TOCOMMENT all terms?
	 * Gets the terms.
	 *
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Formula#getTerms()
	 */
	@Override
	public List<Term> getTerms() {
		List<Term> terms = new ArrayList<>();
		terms.addAll(this.left.getTerms());
		terms.addAll(this.right.getTerms());
		return terms;
	}


	/**
	 * Gets the schema constants.
	 *
	 * @return Collection<TypedConstant<?>>
	 * @see uk.ac.ox.cs.pdq.db.Dependency#getSchemaConstants()
	 */
	@Override
	public Collection<TypedConstant<?>> getSchemaConstants() {
		return this.constants;
	}

	/**
	 * Fire.
	 *
	 * @param mapping Map<Variable,Term>
	 * @param canonicalNames boolean
	 * @return TGD<L,R>
	 * @see uk.ac.ox.cs.pdq.ics.IC#fire(Map<Variable,Term>, boolean)
	 */
	@Override
	public EGD fire(Map<Variable, Constant> mapping, boolean canonicalNames) {
		return this.ground(mapping, canonicalNames);
	}

	/**
	 * TOCOMMENT this method seems to be checking that all variables in the EGD are in the input mapping, 
	 * copies the input mapping and returns it. Maybe it is a residue of some old meaningful method?
	 * Skolemize mapping.
	 *
	 * @param mapping the mapping
	 * @return 		If canonicalNames is TRUE returns a copy of the input mapping
	 * 		augmented such that Skolem constants are produced for
	 *      the existentially quantified variables
	 *      
	 */
	public Map<Variable, Constant> skolemizeMapping(Map<Variable, Constant> mapping) {
		Map<Variable, Constant> result = new LinkedHashMap<>(mapping);
		for (Variable variable: this.universal) {
			Variable variableTerm = variable;
			Preconditions.checkState(result.get(variableTerm) != null);
		}
		return result;
	}

	/**
	 * TOCOMMENT Since it is not clear what the above method does it is not clear what this or the following method does.
	 * 
	 * Ground.
	 *
	 * @param mapping the mapping
	 * @param canonicalNames 		True if we assign Skolem constants to the existentially quantified variables
	 * @return 		the grounded dependency using the input mapping.
	 *      If canonicalNames is TRUE then skolem constants are produced for
	 *      the existentially quantified variables
	 */
	public EGD ground(Map<Variable, Constant> mapping, boolean canonicalNames) {
		return canonicalNames == true ? this.ground(this.skolemizeMapping(mapping)): this.ground(mapping);
	}

	/**
	 * Ground.
	 *
	 * @param mapping Map<Variable,Term>
	 * @return TGD<L,R>
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Term>)
	 */
	@Override
	public EGD ground(Map<Variable, Constant> mapping) {
		return new EGD(this.left.ground(mapping), this.right.ground(mapping));
	}

	/**
	 * Gets the both side variables.
	 *
	 * @return Set<Variable>
	 * @see uk.ac.ox.cs.pdq.db.Dependency#getAllVariables()
	 */
	@Override
	public Set<Variable> getAllVariables() {
		Set<Variable> variables = Sets.newHashSet(Utility.getVariables(this.left.getAtoms()));
		variables.retainAll(Utility.getVariables(this.right.getAtoms()));
		return variables;
	}
	
	/**
	 * Let R be a relation of arity n and x_k be its key.
	 * The EGD that captures the EGD dependency is given by
	 * R(x_1,...,x_k,...x_n) ^ R(x_1',...,x_k,...x_n') --> \Wedge_{i \neq k} x_i=x_i'
	 *
	 * @param predicate the signature
	 * @param attributes the attributes
	 * @param keys the keys
	 * @return 		a collection of EGDs for the input relation and keys
	 */
	public static EGD getEGDs(Predicate predicate, List<Attribute> attributes, Collection<Attribute> keys) {
		List<Term> leftTerms = Utility.typedToTerms(attributes);
		List<Term> copiedTerms = Lists.newArrayList(leftTerms);
		//Keeps the terms that should be equal
		Map<Term,Term> tobeEqual = com.google.common.collect.Maps.newHashMap();
		int i = 0;
		for(Attribute typed:attributes) {
			if(!keys.contains(typed)) {
				Term term = new Variable(String.valueOf("?" + typed));//TOCOMMENT why are we using a "?" here?
				copiedTerms.set(i, term);
				tobeEqual.put(leftTerms.get(i), term);
			}
			i++;
		}
		//Create the constant equality predicates
		Collection<Equality> equalityPredicates = Sets.newHashSet();
		for(Entry<Term, Term> pair:tobeEqual.entrySet()) {
			equalityPredicates.add(new Equality(pair.getKey(), pair.getValue()));
		}

		Conjunction<Atom> head =
				Conjunction.of(new Atom(new Predicate(predicate.getName(), leftTerms.size()), leftTerms), 
						new Atom(new Predicate(predicate.getName(), copiedTerms.size()), copiedTerms));
		
		//TOCOMMENT here we give "head" as the left part of the EGDs -- wrong choice of local variable name, or a bug?
		return new EGD(head, Conjunction.of(equalityPredicates));
	}
	
	/**
	 * TOCOMMENT why plural in the name of the method?
	 * Constructs an EGD for the given relation and key attibutes.
	 *
	 * @param relation the relation
	 * @param keys the key attirbutes
	 * @return the EGD representing the primary key
	 */
	public static EGD getEGDs(Relation relation, Collection<Attribute> keys) {
		return getEGDs(new Predicate(relation.getName(), relation.getArity()), relation.getAttributes(), keys);
	}

	/**
	 * Two EGDs are equal if (by using the corresponding equals() methods) left and right hand sides and all universally quantified variables are equal.
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
				&& this.left.equals(((EGD) o).left)
				&& this.right.equals(((EGD) o).right)
				&& this.universal.equals(((EGD) o).universal);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.universal, this.left, this.right);
	}

	@Override
	public String toString() {
		String f = "";
//		if(!this.universal.isEmpty()) {
//			f = this.universal.toString();
//		}
		return f + this.left + LogicalSymbols.IMPLIES + this.right;
	}
	
	@Override
	public EGD clone() {
		return new EGD(this.getBody(), 
				Conjunction.of(Lists.newArrayList(this.getHead().getChildren())));
	}
}
