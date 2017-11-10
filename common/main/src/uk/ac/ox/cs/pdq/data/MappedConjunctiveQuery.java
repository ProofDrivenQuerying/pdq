package uk.ac.ox.cs.pdq.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.fol.Conjunction;

/**
 * This class intends to map all the relevant information about conjunctiveQuery
 * to the schema where we intend to execute it.
 * 
 * @author Gabor
 *
 */
public class MappedConjunctiveQuery {

	private ConjunctiveQueryDescriptor leftAtom;
	private MappedConjunctiveQuery rightSideConjunction;
	private ConjunctiveQueryDescriptor rightSideAtom;
	private Conjunction conjunction;

	private List<Pair<Integer, Integer>> matchingColumnIndexes;

	/**
	 * Constructor to represent a conjunction between two atoms.
	 */
	public MappedConjunctiveQuery(ConjunctiveQueryDescriptor leftAtom, ConjunctiveQueryDescriptor rightAtom, Conjunction conjunction) {
		this.leftAtom = leftAtom;
		rightSideAtom = rightAtom;
		this.conjunction = conjunction;
		this.matchingColumnIndexes = new ArrayList<>();
	}

	/**
	 * Constructor to represent a conjunction between an atom and a conjunction.
	 */
	public MappedConjunctiveQuery(ConjunctiveQueryDescriptor leftAtom, MappedConjunctiveQuery rightConjunction, Conjunction conjunction) {
		this.leftAtom = leftAtom;
		rightSideConjunction = rightConjunction;
		this.conjunction = conjunction;
		this.matchingColumnIndexes = new ArrayList<>();
	}

	public List<Pair<Integer, Integer>> getMatchingColumnIndexes() {
		return matchingColumnIndexes;
	}

	public void addMatchingColumnIndexes(Integer left,Integer right) {
		this.matchingColumnIndexes.add(new ImmutablePair<Integer,Integer>(left,right));
	}

	public ConjunctiveQueryDescriptor getLeftAtom() {
		return leftAtom;
	}

	public MappedConjunctiveQuery getRightSideConjunction() {
		return rightSideConjunction;
	}

	public ConjunctiveQueryDescriptor getRightSideAtom() {
		return rightSideAtom;
	}

	public Conjunction getConjunction() {
		return conjunction;
	}

}
