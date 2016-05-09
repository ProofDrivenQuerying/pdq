package uk.ac.ox.cs.pdq.fol;

import java.util.ArrayList;
import java.util.List;

/**
 * A conjunctive query Q is cyclic (acyclic) if its associated hypergraph H(Q) is cyclic (acyclic).
 *
 * @author Efthymia Tsamoura
 */
public class AcyclicQuery extends ConjunctiveQuery {

	/**
	 * TOCOMMENT I commented elsewhere about the left and right terminology. Usually within PDQ right is the "head".
	 * Here it is the body.
	 * 
	 * Instantiates a new acyclic query.
	 *
	 * @param name The query's name
	 * @param head The query's head terms
	 * @param right The query's body
	 */
	public AcyclicQuery(String name, List<Term> head, Conjunction<Atom> right) {
		super(name, head, right);
	}

	/**
	 * Instantiates a new acyclic query.
	 *
	 * @param left The query's head
	 * @param right The query's body
	 */
	public AcyclicQuery(Atom left, Conjunction<Atom> right) {
		super(left, right);
	}
}
