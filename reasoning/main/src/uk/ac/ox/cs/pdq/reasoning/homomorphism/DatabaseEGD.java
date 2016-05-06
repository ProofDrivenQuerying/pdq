package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * A dependency of the form \delta = \forall \vec{x} \rho(\vec{x}) --> x_i = x_j where \rho is a conjunction of atoms.
 *
 * @author Efthymia Tsamoura
 */
public class DatabaseEGD
		extends Implication<Conjunction<Atom>, Conjunction<DatabaseEquality>>
		implements Dependency<Conjunction<Atom>, Conjunction<DatabaseEquality>> {
	
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
	public DatabaseEGD(Conjunction<Atom> left, Conjunction<DatabaseEquality> right) {
		super(left, right);
		this.universal = Utility.getVariables(left.getAtoms());
		for (Term term:right.getTerms()) {
			if (!term.isVariable() && !term.isSkolem()) {
				this.constants.add(((TypedConstant) term));
			}
		}
	}
	
	/**
	 * Gets the universal.
	 *
	 * @return List<Variable>
	 */
	public List<Variable> getUniversal() {
		return this.universal;
	}

	/**
	 * 
	 * Gets the free.
	 *
	 * @return List<Term>
	 * @see uk.ac.ox.cs.pdq.fol.Evaluatable#getFree()
	 */
	@Override
	public List<Term> getFree() {
		return Lists.<Term>newArrayList(this.universal);
	}

	/**
	 * Gets the left.
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
	public Conjunction<DatabaseEquality> getRight() {
		return this.right;
	}

	/**
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
	public DatabaseEGD fire(Map<Variable, Constant> mapping, boolean canonicalNames) {
		throw new java.lang.UnsupportedOperationException();
	}

	/**
	 * Ground.
	 *
	 * @param mapping Map<Variable,Term>
	 * @return TGD<L,R>
	 * @see uk.ac.ox.cs.pdq.formula.Formula#ground(Map<Variable,Term>)
	 */
	@Override
	public DatabaseEGD ground(Map<Variable, Constant> mapping) {
		throw new java.lang.UnsupportedOperationException();
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
				&& this.left.equals(((DatabaseEGD) o).left)
				&& this.right.equals(((DatabaseEGD) o).right)
				&& this.universal.equals(((DatabaseEGD) o).universal);
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.universal, this.left, this.right);
	}

	/**
	 * To string.
	 *
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
	
	@Override
	public DatabaseEGD clone() {
		return new DatabaseEGD(this.getBody(), 
				Conjunction.of(Lists.newArrayList(this.getHead().getChildren())));
	}

	@Override
	public Set<Variable> getAllVariables() {
		Set<Variable> variables = Sets.newHashSet(Utility.getVariables(this.left.getAtoms()));
		variables.retainAll(Utility.getVariables(this.right.getAtoms()));
		return variables;
	}
}
