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

	/**
	 * @return the suffix queries
	 */
	public List<AcyclicQuery> getSuffixQueries() {
		List<AcyclicQuery> ret = new ArrayList<>();
		List<Predicate> atoms = new ArrayList<>(this.body.getChildren());
		for (int i = atoms.size() - 2; i >= 0; --i) {
			List<Predicate> subset = atoms.subList(i, atoms.size());
			List<Variable> f = subset.get(0).getVariables();
			AcyclicQuery cq = new AcyclicQuery(
					new Predicate(new Signature("Q", f.size()), f),
					Conjunction.of(subset));
			ret.add(cq);
		}
		return ret;
	}

	/**
	 * @return the last suffix query
	 */
	public AcyclicQuery getLastQuery() {
		List<Predicate> atoms = new ArrayList<>(this.body.getChildren());
		Predicate atom = atoms.get(atoms.size() - 1);
		AcyclicQuery cq = new AcyclicQuery(
				new Predicate(new Signature("Q", 0),
						new ArrayList<Term>()),
						Conjunction.of(atom));
		return cq;
	}
}
