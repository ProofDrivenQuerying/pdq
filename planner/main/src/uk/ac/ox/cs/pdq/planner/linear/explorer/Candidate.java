// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear.explorer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;


/**
 * Informally, a fact is candidate for exposure if all its input chase constants are already accessible. 
 * The formal definition of candidate facts is given below:
 * A fact R(c1, ..., cm) in a linear configuration v is a candidate for exposure in v 
 * if AccessedR(c1, ..., cm) is not yet in v and and there is an access method mt on R with input positions j1, ..., jm such that
 * accessible(c_{j_1}), ..., accessible(c_{j_m}) all hold in v.
 * @author Efthymia Tsamoura
 *
 */
public class Candidate implements Cloneable{

	/** Keeps information relevant to the exposed fact,
	 *  e.g., the grounding of its variables to constants */
	private final Match match;

	/**  The axiom that will be fired given this candidate fact*. */
	private final AccessibilityAxiom rule;

	/**  The fact itself. */
	private final Atom fact;

	/**  Input constants. */
	private final List<Constant> input;

	/**  Output constants. */
	private final List<Constant> output;

	/**  Proper output constants. */
	private final List<Constant> properOutput;


	/**
	 * Constructor for Candidate.
	 * @param accessibleSchema AccessibleSchema
	 * 		The accessible counterpart of the input schema
	 * @param rule AccessibilityAxiom
	 * 		The axiom that will be fired given this candidate fact
	 * @param fact Atom
	 * 		The fact itself
	 * @param match Matching
	 * 		Keeps information relevant to the exposed fact
	 */
	public Candidate(AccessibilityAxiom rule, Atom fact, Match match) {
		this.rule = rule;
		this.fact = fact;
		this.match = match;
		List<Constant> allConstants  = fact.getTypedAndUntypedConstants(rule.getAccessMethod().getInputs());
		this.input = Lists.newArrayList(new LinkedHashSet<>(allConstants));
		this.output = Lists.newArrayList(fact.getTypedAndUntypedConstants());
		this.properOutput = Lists.newArrayList(this.output);
		this.properOutput.removeAll(this.input);
	}

	public List<Constant> getInput() {
		return this.input;
	}

	public Collection<Constant> getOutput() {
		return this.output;
	}

	public Collection<Constant> getProperOutput() {
		return this.properOutput;
	}
	
	public Atom getFact() {
		return this.fact;
	}

	public AccessibilityAxiom getRule() {
		return this.rule;
	}

	/**
	 *
	 * @return the constants in the output positions of the fact
	 */
	public LinkedHashMap<Integer, Term> getOutputChaseConstants() {
		LinkedHashMap<Integer, Term> map = new LinkedHashMap<>();
		Integer i = 0;
		for (Term t: this.fact.getTerms()) {
			if (t.isUntypedConstant() && this.output.contains(t)) {
				map.put(i, t);
			}
			++i;
		}
		return map;
	}
	
	public Match getMatch() {
		return this.match;
	}
	
	public Atom getInferredAccessibleFact() {
		Atom accessed = (Atom) Formula.applySubstitution(this.rule.getGuard(),this.match.getMapping());
		Relation relation = this.rule.getBaseRelation();
		return Atom.create(Relation.create(AccessibleSchema.inferredAccessiblePrefix + relation.getName(), relation.getAttributes(), new AccessMethodDescriptor[]{}, relation.isEquality()), accessed.getTerms());
	}

	public Relation getRelation() {
		return this.rule.getBaseRelation();
	}

	public AccessMethodDescriptor getAccessMethod() {
		return this.rule.getAccessMethod();
	}

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
	@Override
	public int hashCode() {
		return Objects.hash(this.rule, this.fact);
	}
	
	@Override
	public String toString() {
		if (this.rule != null) {
			return this.rule.getBaseRelation() + " " +
					this.rule.getAccessMethod() + "\n" +
					this.fact;
		}
		return "EMPTY CANDIDATE";
	}
	
	public boolean isEqualAxiom(Candidate c) {
		return this.rule.equals(c.rule) && this.input.equals(c.input);
	}
}
