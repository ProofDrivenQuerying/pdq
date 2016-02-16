package uk.ac.ox.cs.pdq.planner.accessibleschema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.GuardedDependency;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.InferredAccessibleRelation;

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
public class AccessibilityAxiom extends TGD implements GuardedDependency {

	/**  The inferred accessible relation of the axiom *. */
	private final InferredAccessibleRelation infAccRelation;
	
	/**  The access method that this axiom maps to *. */
	private final AccessMethod method;

	/**
	 * Instantiates a new accessibility axiom.
	 *
	 * @param infAccRel 		An inferred accessible relation
	 * @param method 		A method to access this relation
	 */
	public AccessibilityAxiom(InferredAccessibleRelation infAccRel, AccessMethod method) {
		super(createLeft(infAccRel.getBaseRelation(), method), createRight(infAccRel, method));
		this.method = method;
		this.infAccRelation = infAccRel;
	}

	/**
	 * Creates the left.
	 *
	 * @param relation 		A schema relation 
	 * @param method 		A method to access this relation
	 * @return 		the atoms of the left-hand side of the accessibility axiom that corresponds to the input relation and the input access method
	 */
	private static Conjunction<Predicate> createLeft(Relation relation, AccessMethod method) {
		List<Predicate> leftAtoms = new ArrayList<>();
		List<Integer> bindingPositions = method.getInputs();
		Relation r = AccessibleRelation.getInstance();
		Predicate f = relation.createAtoms();
		List<Term> terms = f.getTerms();
		for (int bindingPos: bindingPositions) {
			if (method.getType() != Types.FREE) {
				leftAtoms.add(new Predicate(r, terms.get(bindingPos - 1)));
			}
		}
		leftAtoms.add(f);
		return Conjunction.of(leftAtoms);
	}

	/**
	 * Creates the right.
	 *
	 * @param infAccRel the inf acc rel
	 * @param binding the binding
	 * @return 		the atoms of the right-hand side of the accessibility axiom that corresponds to the input relation and the input access method
	 */
	private static Conjunction<Predicate> createRight(InferredAccessibleRelation infAccRel, AccessMethod binding) {
		Relation relation = infAccRel.getBaseRelation();
		List<Predicate> rightAtoms = new ArrayList<>();
		List<Integer> bindingPositions = binding.getInputs();
		Relation accessible = AccessibleRelation.getInstance();
		Predicate f = infAccRel.createAtoms();
		List<Term> terms = f.getTerms();
		for (int i = 1; i <= relation.getArity(); ++i) {
			if (!bindingPositions.contains(i)) {
				rightAtoms.add(new Predicate(accessible, terms.get(i - 1)));
			}
		}
		rightAtoms.add(infAccRel.createAtoms());
		return Conjunction.of(rightAtoms);
	}

	/**
	 * Gets the inferred accessible relation.
	 *
	 * @return the inferred accessible relation of the accessibility axiom.
	 */
	public InferredAccessibleRelation getInferredAccessibleRelation() {
		return this.infAccRelation;
	}

	/**
	 * Gets the base relation.
	 *
	 * @return the base relation of the accessibility axiom.
	 */
	public Relation getBaseRelation() {
		return this.infAccRelation.getBaseRelation();
	}

	/**
	 * Gets the access method.
	 *
	 * @return the access method of the accessibility axiom.
	 */
	public AccessMethod getAccessMethod() {
		return this.method;
	}
	
	/**
	 * Ground.
	 *
	 * @param mapping the mapping
	 * @param canonicalNames 		True if we assign Skolem constants to the existentially quantified variables
	 * @return 		the grounded dependency using the input mapping.
	 *      If canonicalNames is TRUE then skolem constants are produced for
	 *      the existentially quantified variables
	 */
	public TGD ground(Map<Variable, Constant> mapping, boolean canonicalNames) {
		TGD grounded = (canonicalNames == true ? this.ground(this.skolemizeMapping(mapping)): this.ground(mapping));
		List<Predicate> right = grounded.getRight().getPredicates();
		right.removeAll(grounded.getLeft().getPredicates());
		return new TGD(Conjunction.of(grounded.getLeft().getPredicates()), Conjunction.of(right));
	}

	/**
	 * Gets the guard.
	 *
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.db.GuardedDependency#getGuard()
	 */
	@Override
	public Predicate getGuard() {
		return this.left.getPredicates().get(this.left.size()-1);
	}
}