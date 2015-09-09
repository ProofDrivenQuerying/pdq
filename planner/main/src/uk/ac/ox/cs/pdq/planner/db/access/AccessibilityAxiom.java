package uk.ac.ox.cs.pdq.planner.db.access;

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
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.InferredAccessibleRelation;

/**
 * An accessibility axiom
 *
 * @author Efthymia Tsamoura
 */
public class AccessibilityAxiom extends TGD implements GuardedDependency {

	private final InferredAccessibleRelation infAccRelation;
	private final AccessMethod binding;

	/**
	 * @param infAccRel
	 * @param binding
	 */
	public AccessibilityAxiom(InferredAccessibleRelation infAccRel, AccessMethod binding) {
		super(createLeft(infAccRel.getBaseRelation(), binding), createRight(infAccRel, binding));
		this.binding = binding;
		this.infAccRelation = infAccRel;
	}

	/**
	 * @param relation
	 * @param binding
	 * @return Conjunction<PredicateFormula>
	 */
	private static Conjunction<Predicate> createLeft(Relation relation, AccessMethod binding) {
		List<Predicate> leftAtoms = new ArrayList<>();
		List<Integer> bindingPositions = binding.getInputs();
		Relation r = AccessibleRelation.getInstance();
		Predicate f = relation.createAtoms();
		List<Term> terms = f.getTerms();
		for (int bindingPos: bindingPositions) {
			if (binding.getType() != Types.FREE) {
				leftAtoms.add(new Predicate(r, terms.get(bindingPos - 1)));
			}
		}
		leftAtoms.add(f);
		return Conjunction.of(leftAtoms);
	}

	/**
	 * @param infAccRel
	 * @param binding
	 * @return Conjunction<PredicateFormula>
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
	 * @return the inferred accessible relation of the accessibility axiom.
	 */
	public InferredAccessibleRelation getInferredAccessibleRelation() {
		return this.infAccRelation;
	}

	/**
	 * @return the base relation of the accessibility axiom.
	 */
	public Relation getBaseRelation() {
		return this.infAccRelation.getBaseRelation();
	}

	/**
	 * @return the access method of the accessibility axiom.
	 */
	public AccessMethod getAccessMethod() {
		return this.binding;
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
	public TGD ground(Map<Variable, Constant> mapping, boolean canonicalNames) {
		TGD grounded = (canonicalNames == true ? this.ground(this.skolemizeMapping(mapping)): this.ground(mapping));
		List<Predicate> right = grounded.getRight().getPredicates();
		right.removeAll(grounded.getLeft().getPredicates());
		return new TGD(Conjunction.of(grounded.getLeft().getPredicates()), Conjunction.of(right));
	}

	/**
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.db.GuardedDependency#getGuard()
	 */
	@Override
	public Predicate getGuard() {
		return this.left.getPredicates().get(this.left.size()-1);
	}
}