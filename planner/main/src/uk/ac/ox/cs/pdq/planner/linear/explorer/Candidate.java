package uk.ac.ox.cs.pdq.planner.linear.explorer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.Match;

import com.google.common.collect.Lists;


/**
 * A candidate fact
 * @author Efthymia Tsamoura
 *
 */
public class Candidate implements Cloneable{

	/** Keeps information relevant to the exposed fact,
	 *  e.g., the grounding of its variables to constants */
	private final Match match;

	private final AccessibleSchema accessibleSchema;

	private final AccessibilityAxiom rule;

	/** The fact*/
	private final Predicate fact;

	/** Input constants */
	private final List<Constant> input;

	/** Output constants */
	private final List<Constant> output;

	/** Proper output constants */
	private final List<Constant> properOutput;


	/**
	 * Constructor for Candidate.
	 * @param accessibleSchema AccessibleSchema
	 * @param rule AccessibilityAxiom
	 * @param fact PredicateFormula
	 * @param matching Matching
	 */
	public Candidate(AccessibleSchema accessibleSchema, AccessibilityAxiom rule, Predicate fact, Match matching) {
		this.accessibleSchema = accessibleSchema;
		this.rule = rule;
		this.fact = fact;
		this.match = matching;
		this.input = PlannerUtility.getInputConstants(rule.getAccessMethod(), fact);
		this.output = Lists.newArrayList(uk.ac.ox.cs.pdq.util.Utility.getConstants(fact));
		this.properOutput = Lists.newArrayList(this.output);
		this.properOutput.removeAll(this.input);
	}

	/**
	 * @return List<Constant>
	 */
	public List<Constant> getInput() {
		return this.input;
	}

	/**
	 * @return Collection<Constant>
	 */
	public Collection<Constant> getOutput() {
		return this.output;
	}

	/**
	 * @return Collection<Constant>
	 */
	public Collection<Constant> getProperOutput() {
		return this.properOutput;
	}

	/**
	 * @return PredicateFormula
	 */
	public Predicate getFact() {
		return this.fact;
	}

	/**
	 * @return AccessibilityAxiom
	 */
	public AccessibilityAxiom getRule() {
		return this.rule;
	}

	/**
	 * @return the constants in the output positions of the fact
	 */
	public LinkedHashMap<Integer, Term> getOutputChaseConstants() {
		LinkedHashMap<Integer, Term> map = new LinkedHashMap<>();
		Integer i = 0;
		for (Term t: this.fact.getTerms()) {
			if (t.isSkolem() && this.output.contains(t)) {
				map.put(i, t);
			}
			++i;
		}
		return map;
	}

	/**
	 * @return Matching
	 */
	public Match getMatch() {
		return this.match;
	}

	/**
	 * @return the inferred accessible counterpart of the fact
	 */
	public Predicate getInferredAccessibleFact() {
		Predicate accessed = this.rule.getGuard().ground(this.match.getMapping());
		Relation baseRelation = this.rule.getBaseRelation();
		return new Predicate(this.accessibleSchema.getInferredAccessibleRelation(baseRelation), accessed.getTerms() );
	}

	/**
	 * @return Relation
	 */
	public Relation getRelation() {
		return this.rule.getBaseRelation();
	}

	/**
	 * @return AccessMethod
	 */
	public AccessMethod getBinding() {
		return this.rule.getAccessMethod();
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
				&& this.rule.equals(((Candidate) o).rule)
				&& this.fact.equals(((Candidate) o).fact);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.rule, this.fact);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		if (this.rule != null) {
			return this.rule.getBaseRelation() + " " +
					this.rule.getAccessMethod() + "\n" +
					this.fact;
		}
		return "EMPTY CANDIDATE";
	}

	/**
	 * @return Candidate
	 */
	@Override
	public Candidate clone() {
		return new Candidate(this.accessibleSchema, this.rule, this.fact, this.match);
	}
}