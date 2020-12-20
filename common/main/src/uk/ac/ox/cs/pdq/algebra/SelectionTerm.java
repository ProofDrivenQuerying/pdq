// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.algebra;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;

/**
 * 
 * @author Efthymia Tsamoura
 * @author Stefano
 *
 */
public class SelectionTerm extends RelationalTerm {

	private static final long serialVersionUID = 3979377421532058418L;

	protected final RelationalTerm child;

	/** The predicate associated with this selection. */
	protected final Condition selectionCondition;

	/** Cached string representation. */
	private String toString = null;

	private SelectionTerm(Condition selectionCondition, RelationalTerm child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
		Preconditions.checkNotNull(selectionCondition);
		Preconditions.checkNotNull(child);
		try {
			Preconditions.checkArgument(SelectionTerm.assertSelectionCondition(selectionCondition, child.getOutputAttributes()));
		}catch(Exception e) {
			throw e;
		}
		this.selectionCondition = selectionCondition;
		this.child = child;
	}

	public Condition getSelectionCondition() {
		return this.selectionCondition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Select");
			result.append('{');
			result.append('[').append(this.selectionCondition).append(']');
			result.append(this.child.toString());
			result.append('}');
			this.toString = result.toString();
		}
		return this.toString;
	}

	@Override
	public RelationalTerm[] getChildren() {
		RelationalTerm[] children = new RelationalTerm[1];
		children[0] = this.child;
		return children;
	}

	public static SelectionTerm create(Condition predicate, RelationalTerm child) {
		return Cache.selectionTerm.retrieve(new SelectionTerm(predicate, child));
	}

	@Override
	public RelationalTerm getChild(int childIndex) {
		assert (childIndex == 0);
		return this.child;
	}

	@Override
	public Integer getNumberOfChildren() {
		return 1;
	}

	/**
	 * 3) Inductive case for constant selection:
	 * 
	 * input T is a selection term sigma_{posi=c} T_0 where posi is an attribute and
	 * c is a constant. Again we let a be the attribute corresponding to posi.
	 * 
	 * let (phi_0, M_0)=T_0.toLogic
	 * 
	 * return (phi'_0, M'_0) where
	 * 
	 * phi'_0 is formed from phi_0 by substituting the variable M'_0(a) with c
	 * 
	 * M'_0 agrees with M_0 except M'_0(a) is set to c
	 */
	@Override
	public RelationalTermAsLogic toLogic() {
		RelationalTermAsLogic T1logic = getChildren()[0].toLogic();
		if (!this.getConditions().isEmpty()) {
			// this case deals with different joins and selectionTerm case.
			RelationalTermAsLogic TNewlogic = T1logic; 
			Formula phiNew = TNewlogic.getFormula();
			Map<Attribute, Term> mapNew = TNewlogic.getMapping();
			List<SimpleCondition> conditions = this.getConditions();
			// Apply conditions
			for (SimpleCondition s:conditions) {
				if (s instanceof ConstantEqualityCondition) {
					TypedConstant constant = ((ConstantEqualityCondition)s).getConstant();
					int position = ((ConstantEqualityCondition)s).getPosition();
					Attribute a = this.getOutputAttribute(position);
					if (T1logic.getMapping().get(a)!=null)
						phiNew = SelectionTerm.replaceTerm(phiNew,T1logic.getMapping().get(a),constant);
					mapNew.put(a, constant);
				}
			}
			return new RelationalTermAsLogic(phiNew, mapNew);
		} else {
			return T1logic;
		}
		
	}

	/**
	 * Asserts that the given condition can be applied to the given attribute types.
	 * Returns false when there are mismatching types.
	 * 
	 * @param selectionCondition
	 * @param outputAttributes
	 * @return
	 */
	public static boolean assertSelectionCondition(Condition selectionCondition, Attribute[] outputAttributes) {
		if (selectionCondition instanceof ConjunctiveCondition) {
			for (SimpleCondition conjunct : ((ConjunctiveCondition) selectionCondition).getSimpleConditions()) {
				if (assertSelectionCondition(conjunct, outputAttributes) == false)
					return false;
			}
			return true;
		} else
			return assertSelectionCondition((SimpleCondition) selectionCondition, outputAttributes);
	}

	/**
	 * Asserts that the given condition can be applied to the given attribute types.
	 * Returns false when there are mismatching types.
	 * 
	 * @param selectionCondition
	 * @param outputAttributes
	 * @return
	 */
	public static boolean assertSelectionCondition(SimpleCondition selectionCondition, Attribute[] outputAttributes) {
		if (selectionCondition instanceof ConstantInequalityCondition) {
			int position = ((ConstantInequalityCondition) selectionCondition).getPosition();
			if (position > outputAttributes.length || !((ConstantInequalityCondition) selectionCondition).getConstant()
					.getType().equals(outputAttributes[position].getType()))
				return false;
			else
				return true;
		} else if (selectionCondition instanceof ConstantEqualityCondition) {
			int position = ((ConstantEqualityCondition) selectionCondition).getPosition();
			if (position > outputAttributes.length || !((ConstantEqualityCondition) selectionCondition).getConstant()
					.getType().equals(outputAttributes[position].getType()))
				return false;
			else
				return true;
		} else if (selectionCondition instanceof AttributeEqualityCondition) {
			int position = ((AttributeEqualityCondition) selectionCondition).getPosition();
			int other = ((AttributeEqualityCondition) selectionCondition).getOther();
			if (position > outputAttributes.length || other > outputAttributes.length)
				return false;
			else
				return true;
		} else
			throw new RuntimeException("Unknown operator type");
	}


	/**
	 * Replaces a term (Variable or constant) with a new term in a formula.
	 * 
	 * @param phiNew
	 * @param term
	 * @param constant
	 * @return
	 */
	public static Formula replaceTerm(Formula phi, Term old, Term newTerm) {
		if (phi instanceof Atom) {
			Term[] terms = phi.getTerms();
			for (int i = 0; i < terms.length; i++) {
				if (terms[i].equals(old)) {
					terms[i] = newTerm;
				}
			}
			return Atom.create(((Atom) phi).getPredicate(), terms);
		} else {
			Atom[] atoms = ((Conjunction) phi).getAtoms();
			Atom[] newAtoms = new Atom[atoms.length];
			for (int i = 0; i < atoms.length; i++) {
				newAtoms[i] = (Atom) replaceTerm(atoms[i], old, newTerm);
			}
			return Conjunction.create(newAtoms);
		}
	}

}
