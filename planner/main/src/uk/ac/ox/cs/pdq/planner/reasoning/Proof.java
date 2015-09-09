package uk.ac.ox.cs.pdq.planner.reasoning;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibilityAxiom;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

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
	private final List<ProofState> states = new ArrayList<>();

	/** The goal of the proof. */
	private final Map<Variable, Constant> queryMatch;

	/**
	 * @param queryMatch
	 */
	public Proof(Map<Variable, Constant> queryMatch) {
		this.queryMatch = queryMatch;
	}

	/**
	 * @param match
	 * @param states
	 */
	private Proof(Map<Variable, Constant> match, List<ProofState> states) {
		Preconditions.checkArgument(states != null);
		this.queryMatch = match;
		for (ProofState s : states) {
			this.addState(s);
		}
	}

	/**
	 * @param state
	 */
	private void addState(ProofState state) {
		this.states.add(state);
	}

	/**
	 * @return the list of states that led to a query match
	 */
	public List<ProofState> getStates() {
		return this.states;
	}

	/**
	 * @return the query match of this proof
	 */
	public Map<Variable, Constant> getQueryMatch() {
		return this.queryMatch;
	}

	/**
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
		return Joiner.on("\n").join(this.states) + "\nQUERY MATCH: " + this.queryMatch.toString();
	}
	
	/**
	 * 
	 * @param head
	 * @param queryMatch
	 * @return
	 * 		a proof composed by the input states and the input query match
	 */
	public static Proof createProof(List<ProofState> head, Map<Variable, Constant> queryMatch) {
		Preconditions.checkArgument(head != null);
		Builder builder = new Builder();
		for (ProofState state:head) {
			builder.addAxiom(state.getAxiom());
			for (Map<Variable, Constant> match : state.getMatches()) {
				builder.addMatch(match);
			}
		}
		builder.setQueryMatch(queryMatch);
		return builder.build();
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
				&& this.queryMatch.equals(((Proof) o).queryMatch)
				&& this.states.equals(((Proof) o).states);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.queryMatch, this.states);
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
	public static final class ProofState {

		/** The accessibility axiom that was fired to create this state */
		private final AccessibilityAxiom axiom;

		/** The set of matched entailed by the axiom firing. */
		private final Set<Map<Variable, Constant>> matches = new LinkedHashSet<>();

		/**
		 *
		 * @param axiom
		 */
		public ProofState(AccessibilityAxiom axiom) {
			this.axiom = axiom;
		}

		/**
		 * Adds a single match to the state
		 * @param match
		 */
		public void addMatch(Map<Variable, Constant> match) {
			this.matches.add(match);
		}

		/**
		 * @return the accessibility axiom of the state.
		 */
		public AccessibilityAxiom getAxiom() {
			return this.axiom;
		}

		/**
		 * @return the full set of candidate matches for this state.
		 */
		public Set<Map<Variable, Constant>> getMatches() {
			return this.matches;
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
					&& this.axiom.equals(((Proof.ProofState) o).axiom)
					&& this.matches.equals(((Proof.ProofState) o).matches);
		}

		/**
		 * @return int
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(this.axiom, this.matches);
		}

		/**
		 * @return String
		 */
		@Override
		public String toString() {
			return "ACCESSIBILITY AXIOM: " + this.axiom + "\n\tGROUNDING: "
						+ Joiner.on("\n\tGROUNDING: ").join(this.matches);
		}

		/**
		 * Creates a fresh state from the given list of candidates.
		 * @param candidates
		 * @return a fresh state created from the given list of candidates.
		 */
		public static ProofState createState(AccessibilityAxiom axiom, Set<Map<Variable, Constant>> matches) {
			ProofState state = new ProofState(axiom);
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

		private Map<Variable, Constant> queryMatch = null;
		private List<Proof.ProofState> states = new LinkedList<>();
		private Proof.ProofState currentState = null;

		Builder() {
		}

		/**
		 * @return Builder
		 */
		public Builder clearStates() {
			this.states.clear();
			return this;
		}

		/**
		 * @param match Map<Variable,Term>
		 * @return Builder
		 */
		public Builder setQueryMatch(Map<Variable, Constant> match) {
			this.queryMatch = match;
			return this;
		}

		/**
		 * @param axiom AccessibilityAxiom
		 * @return Builder
		 */
		public Builder addAxiom(AccessibilityAxiom axiom) {
			this.addState();
			this.currentState = new Proof.ProofState(axiom);
			return this;
		}

		/**
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
		 * @return Builder
		 */
		private Builder addState() {
			if (this.currentState != null) {
				this.states.add(this.currentState);
			}
			return this;
		}

		/**
		 * @return Proof
		 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
		 */
		@Override
		public Proof build() {
			this.addState();
			return new Proof(this.queryMatch, this.states);
		}
	}
}
