// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.proof;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Variable;
//import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;

// TODO: Auto-generated Javadoc
/**
 * A proof consisting of a list of proof states, and a query match.
 * Each state represent a step in the reasoning process, that led to the query
 * match.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class Proof {

	/** List of state that lead to a query match (full proof). */
	private final List<State> states = new ArrayList<>();

	private Proof(List<State> states) {
		Preconditions.checkArgument(states != null);
		for (State s : states) {
			this.addState(s);
		}
	}

	/**
	 * Adds the state.
	 *
	 * @param state the state
	 */
	private void addState(State state) {
		this.states.add(state);
	}

	/**
	 * Gets the states.
	 *
	 * @return the list of states that led to a query match
	 */
	public List<State> getStates() {
		return this.states;
	}

	/**
 * Builder.
 *
 * @return a fresh proof builder.
 */
	public static Builder builder() {
		return new Builder();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
//		return Joiner.on("\n").join(this.states) + "\nQUERY MATCH: " + this.queryMatch.toString();
		return Joiner.on("\n").join(this.states);
	}

	/**
	 * Creates the proof.
	 *
	 * @param head the head
	 * @param queryMatch the query match
	 * @return 		a proof composed by the input states and the input query match
	 */
	public static Proof createProof(List<State> head, Map<Variable, Constant> queryMatch) {
		Preconditions.checkArgument(head != null);
		Builder builder = new Builder();
		for (State state:head) {
			builder.addAxiom(state.getAxiom());
			for (Map<Variable, Constant> match : state.getMatches()) {
				builder.addMatch(match);
			}
		}
		builder.setQueryMatch(queryMatch);
		return builder.build();
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
				&& this.states.equals(((Proof) o).states);
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.states);
	}

	/**
	 * A proof state is a single step in the reasoning process that led to
	 * a full proof.
	 * It consists of the accessibility axiom that was fired, and all the
	 * candidate matches it entailed.
	 *
	 * @author Julien Leblay
	 *
	 */
	public static final class State {

		/**  The accessibility axiom that was fired to create this state. */
		private final AccessibilityAxiom axiom;

		/** The set of matched entailed by the axiom firing. */
		private final Set<Map<Variable, Constant>> matches = new LinkedHashSet<>();

		/**
		 * Instantiates a new state.
		 *
		 * @param axiom the axiom
		 */
		public State(AccessibilityAxiom axiom) {
			this.axiom = axiom;
		}

		/**
		 * Adds a single match to the state.
		 *
		 * @param match the match
		 */
		public void addMatch(Map<Variable, Constant> match) {
			this.matches.add(match);
		}

		/**
		 * Gets the axiom.
		 *
		 * @return the accessibility axiom of the state.
		 */
		public AccessibilityAxiom getAxiom() {
			return this.axiom;
		}

		/**
		 * Gets the matches.
		 *
		 * @return the full set of candidate matches for this state.
		 */
		public Set<Map<Variable, Constant>> getMatches() {
			return this.matches;
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
					&& this.axiom.equals(((Proof.State) o).axiom)
					&& this.matches.equals(((Proof.State) o).matches);
		}

		/**
		 * Hash code.
		 *
		 * @return int
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(this.axiom, this.matches);
		}

		/**
		 * To string.
		 *
		 * @return String
		 */
		@Override
		public String toString() {
			return "ACCESSIBILITY AXIOM: " + this.axiom + "\n\tGROUNDING: "
					+ Joiner.on("\n\tGROUNDING: ").join(this.matches);
		}

		/**
		 * Creates a fresh state from the given list of candidates.
		 *
		 * @param axiom the axiom
		 * @param matches the matches
		 * @return a fresh state created from the given list of candidates.
		 */
		public static State createState(AccessibilityAxiom axiom, Set<Map<Variable, Constant>> matches) {
			State state = new State(axiom);
			Preconditions.checkState(state != null);
			for(Map<Variable, Constant> match:matches) {
				state.addMatch(match);
			}
			return state;
		}
	}

	/**
	 * A builder of proofs.
	 *
	 * @author Julien Leblay
	 */
	public static class Builder implements uk.ac.ox.cs.pdq.builder.Builder<Proof> {

		/** The query match. */
		private Map<Variable, Constant> queryMatch = null;
		
		/** The states. */
		private List<Proof.State> states = new LinkedList<>();
		
		/** The current state. */
		private Proof.State currentState = null;

		/**
		 * Instantiates a new builder.
		 */
		Builder() {
		}

		/**
		 * Clear states.
		 *
		 * @return Builder
		 */
		public Builder clearStates() {
			this.states.clear();
			return this;
		}

		/**
		 * Sets the query match.
		 *
		 * @param match Map<Variable,Term>
		 * @return Builder
		 */
		public Builder setQueryMatch(Map<Variable, Constant> match) {
			this.queryMatch = match;
			return this;
		}

		/**
		 * Adds the axiom.
		 *
		 * @param axiom AccessibilityAxiom
		 * @return Builder
		 */
		public Builder addAxiom(AccessibilityAxiom axiom) {
			this.addState();
			this.currentState = new Proof.State(axiom);
			return this;
		}

		/**
		 * Adds the match.
		 *
		 * @param match Map<Variable,Term>
		 * @return Builder
		 */
		public Builder addMatch(Map<Variable, Constant> match) {
			if (this.currentState == null) {
				throw new IllegalStateException(
						"Attempting to add a candidate to proof, before defined an axiom for it");
			}
			this.currentState.addMatch(match);
			return this;
		}

		/**
		 * Adds the state.
		 *
		 * @return Builder
		 */
		private Builder addState() {
			if (this.currentState != null) {
				this.states.add(this.currentState);
			}
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return Proof
		 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
		 */
 		@Override
		public Proof build() {
			this.addState();
			return new Proof(this.states);
		}
	}

	/**
	 * Creates a proof from a list of candidates and a query match.
	 *
	 * @param configurations the configurations
	 * @return the proof
	 */
	public static Proof toProof(List<LinearChaseConfiguration> configurations) {
		Proof.Builder builder = Proof.builder();
		for (LinearChaseConfiguration configuration:configurations) {
			Collection<Candidate> exposedCandidates = configuration.getExposedCandidates();
			if (exposedCandidates != null) {
				Candidate first = exposedCandidates.iterator().next();
				builder.addAxiom(first.getRule());
				for (Candidate candidate: exposedCandidates) {
					builder.addMatch(candidate.getMatch().getMapping());
				}
			}
		}
		return builder.build();
	}

}
