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
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * An equality generating dependency
 *
 * @author Efthymia Tsamoura
 */
public class EGD
		extends Implication<Conjunction<Predicate>, Conjunction<Equality>>
		implements Constraint<Conjunction<Predicate>, Conjunction<Equality>> {

	/** The dependency's universally quantified variables */
	protected List<Variable> universal;
	
	/** The dependency's constants */
	protected Collection<TypedConstant<?>> constants = new LinkedHashSet<>();
	
	/**
	 * @param left The left-hand side conjunction of the dependency
	 * @param right The right-hand side conjunction of the dependency
	 */
	public EGD(Conjunction<Predicate> left, Conjunction<Equality> right) {
		super(left, right);
		this.universal = Utility.getVariables(left.getPredicates());
		for (Term term:right.getTerms()) {
			if (!term.isVariable() && !term.isSkolem()) {
				this.constants.add(((TypedConstant) term));
			}
		}
	}
	
	/**
	 * @return List<Variable>
	 */
	public List<Variable> getUniversal() {
		return this.universal;
	}

	/**
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Evaluatable#getFree()
	 */
	@Override
	public List<Term> getFree() {
		return Lists.<Term>newArrayList(this.universal);
	}

	/**
	 * @return L
	 * @see uk.ac.ox.cs.pdq.db.Constraint#getLeft()
	 */
	@Override
	public Conjunction<Predicate> getLeft() {
		return this.left;
	}

	/**
	 * @return R
	 * @see uk.ac.ox.cs.pdq.db.Constraint#getRight()
	 */
	@Override
	public Conjunction<Equality> getRight() {
		return this.right;
	}

	/**
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
	 * @return Collection<TypedConstant<?>>
	 * @see uk.ac.ox.cs.pdq.db.Constraint#getSchemaConstants()
	 */
	@Override
	public Collection<TypedConstant<?>> getSchemaConstants() {
		return this.constants;
	}

	/**
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
	 * @param mapping
	 * @return
	 * 		If canonicalNames is TRUE returns a copy of the input mapping
	 * 		augmented such that Skolem constants are produced for
	 *      the existentially quantified variables
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
	 * @param mapping
	 * @param canonicalNames
	 * 		True if we assign Skolem constants to the existentially quantified variables
	 * @return
	 * 		the grounded dependency using the input mapping.
	 *      If canonicalNames is TRUE then skolem constants are produced for
	 *      the existentially quantified variables
	 */
	public EGD ground(Map<Variable, Constant> mapping, boolean canonicalNames) {
		return canonicalNames == true ? this.ground(this.skolemizeMapping(mapping)): this.ground(mapping);
	}

	/**
	 * @param mapping Map<Variable,Term>
	 * @return TGD<L,R>
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Term>)
	 */
	@Override
	public EGD ground(Map<Variable, Constant> mapping) {
		return new EGD(
				this.left.ground(mapping),
				this.right.ground(mapping));
	}

	/**
	 * @return Set<Variable>
	 * @see uk.ac.ox.cs.pdq.db.Constraint#getBothSideVariables()
	 */
	@Override
	public Set<Variable> getBothSideVariables() {
		Set<Variable> variables = Sets.newHashSet(Utility.getVariables(this.left.getPredicates()));
		variables.retainAll(Utility.getVariables(this.right.getPredicates()));
		return variables;
	}
	
	/**
	 * Let R be a relation of arity n and x_k be its key.
	 * The EGD that captures the EGD dependency is given by
	 * R(x_1,...,x_k,...x_n) ^ R(x_1',...,x_k,...x_n') --> \Wedge_{i \neq k} x_i=x_i'
	 * @param signature
	 * @param attributes
	 * @param keys
	 * @return
	 * 		a collection of EGDs for the input relation and keys
	 */
	public static EGD getEGDs(Signature signature, List<Attribute> attributes, Collection<Attribute> keys) {
		List<Term> leftTerms = Utility.typedToTerms(attributes);
		List<Term> copiedTerms = Lists.newArrayList(leftTerms);
		//Keeps the terms that should be equal
		Map<Term,Term> tobeEqual = com.google.common.collect.Maps.newHashMap();
		int i = 0;
		for(Attribute typed:attributes) {
			if(!keys.contains(typed)) {
				Term term = new Variable(String.valueOf("?" + typed));
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

		Conjunction<Predicate> head =
				Conjunction.of(new Predicate(new Signature(signature.getName(), leftTerms.size()), leftTerms), 
						new Predicate(new Signature(signature.getName(), copiedTerms.size()), copiedTerms));
		
		return new EGD(head, Conjunction.of(equalityPredicates));
	}
	
	public static EGD getEGDs(Relation relation, Collection<Attribute> keys) {
		return getEGDs(new Signature(relation.getName(), relation.getArity()), relation.getAttributes(), keys);
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
				&& this.left.equals(((EGD) o).left)
				&& this.right.equals(((EGD) o).right)
				&& this.universal.equals(((EGD) o).universal);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.universal, this.left, this.right);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		String f = "";
//		if(!this.universal.isEmpty()) {
//			f = this.universal.toString();
//		}
		return f + this.left + LogicalSymbols.IMPLIES + this.right;
	}
}
