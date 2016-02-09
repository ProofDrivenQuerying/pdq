package uk.ac.ox.cs.pdq.fol;

import java.util.List;


/**
 * A conjunctive query Q is cyclic (acyclic) if its associated hypergraph H(Q) is cyclic (acyclic).
 *
 * @author Efthymia Tsamoura
 */
public class AcyclicQuery extends ConjunctiveQuery {

	/**
	 * @param name The query's name
	 * @param head The query's head terms
	 * @param right The query's body
	 */
	public AcyclicQuery(String name, List<Term> head, Conjunction<Predicate> right) {
		super(name, head, right);
	}

	/**
	 * @param left The query's head
	 * @param right The query's body
	 */
	public AcyclicQuery(Predicate left, Conjunction<Predicate> right) {
		super(left, right);
	}
}
