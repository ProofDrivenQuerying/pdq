package uk.ac.ox.cs.pdq.planner.accessibleschema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

// TODO: Auto-generated Javadoc
/**
 * 
 * For an access method mt on relation R of arity n with input positions j1, ..., jm 
 * an accessibility axiom is a rule of the form
 * accessible(x_{j_1}) \wegde ... \wedge accessible(x_{j_m}) \wedge R(x_1, ..., x_n) -->
 * InferredAccessible R(x_1, ..., x_n) \wedge \Wedge_{j} accessible(x_j)
 * 
 *
 * @author Efthymia Tsamoura
 */
public class AccessibilityAxiom extends TGD {	
	private static final long serialVersionUID = 4888518579167846542L;

	/**  The access method that this axiom maps to *. */
	private final AccessMethod method;
	
	private final Relation relation;

	/**
	 * Instantiates a new accessibility axiom.
	 *
	 * @param relation 		An inferred accessible relation
	 * @param method 		A method to access this relation
	 */
	public AccessibilityAxiom(Relation relation, AccessMethod method) {
		super(createLeft(relation, method), createRight(relation, method));
		this.relation = relation;
		this.method = method;
	}

	/**
	 *
	 * @param relation 		A schema relation 
	 * @param method 		A method to access this relation
	 * @return 		the atoms of the left-hand side of the accessibility axiom that corresponds to the input relation and the input access method
	 */
	private static Formula createLeft(Relation relation, AccessMethod method) {
		List<Formula> leftAtoms = new ArrayList<>();
		Integer[] bindingPositions = method.getInputs();
		Atom atom = createAtomsWithoutExtraAttribute(relation);
		Term[] terms = atom.getTerms();
		for (int bindingPos: bindingPositions) 
			leftAtoms.add(Atom.create(AccessibleSchema.accessibleRelation, terms[bindingPos - 1]));
		leftAtoms.add(atom);
		return Conjunction.of(leftAtoms.toArray(new Atom[leftAtoms.size()]));
	}

	private static Atom createAtomsWithoutExtraAttribute(Relation relation) {
		Term[] terms = new Term[relation.getArity()-1];
		Attribute[] attributes = relation.getAttributes();
		for (int index = 0; index < attributes.length; ++index) 
			terms[index] = Variable.create(attributes[index].getName());
		return Atom.create(relation, terms);
	}

	/**
	 *
	 * @param relation the inf acc rel
	 * @param method the binding
	 * @return 		the atoms of the right-hand side of the accessibility axiom that corresponds to the input relation and the input access method
	 */
	private static Formula createRight(Relation relation, AccessMethod method) {
		List<Formula> rightAtoms = new ArrayList<>();
		Integer[] bindingPositions = method.getInputs();
		Atom f = createAtomsWithoutExtraAttribute(Relation.create(AccessibleSchema.inferredAccessiblePrefix + relation.getName(), relation.getAttributes(), new AccessMethod[]{AccessMethod.create(new Integer[]{})}, relation.isEquality()));
		Term[] terms = f.getTerms();
		for (int i = 1; i <= terms.length; ++i) {
			if (!Arrays.asList(bindingPositions).contains(i)) 
				rightAtoms.add(Atom.create(AccessibleSchema.accessibleRelation, terms[i-1]));
		}
		rightAtoms.add(f);
		return Conjunction.of(rightAtoms.toArray(new Atom[rightAtoms.size()]));
	}

	/**
	 *
	 * @return the base relation of the accessibility axiom.
	 */
	public Relation getBaseRelation() {
		return this.relation;
	}

	/**
	 *
	 * @return the access method of the accessibility axiom.
	 */
	public AccessMethod getAccessMethod() {
		return this.method;
	}

	/**
	 *
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.fol.GuardedDependency#getGuard()
	 */
	public Atom getGuard() {
		return this.getBodyAtom(this.getNumberOfBodyAtoms()-1);
	}
}
