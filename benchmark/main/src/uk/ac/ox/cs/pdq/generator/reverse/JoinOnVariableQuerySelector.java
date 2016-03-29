package uk.ac.ox.cs.pdq.generator.reverse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.builder.QueryBuilder;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * A QuerySelector that accept only conjunctive queries without cross products.
 * 
 * @author Julien Leblay
 */
public class JoinOnVariableQuerySelector implements QuerySelector {

	/** Logger. */
	private static Logger log = Logger.getLogger(JoinOnVariableQuerySelector.class);

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.fol.Query)
	 */
	@Override
	public boolean accept(Query<?> q) {
		for (Conjunction<Atom> body: this.enumerateConjunctions(q.getBody())) {
			if (body.size() > 1) {
				Multimap<Term, Atom> clusters = LinkedHashMultimap.create();
				for (Atom pred: body) {
					for (Term t: pred.getTerms()) {
						clusters.put(t, pred);
					}
				}
				Set<Atom> unassigned = Sets.newHashSet(body);
				for (Term t: clusters.keySet()) {
					Collection<Atom> cluster = clusters.get(t);
					if (cluster.size() > 1 && (t.isSkolem() || t.isVariable())) {
						unassigned.removeAll(cluster);
					}
				}
				if (!unassigned.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Enumerate conjunctions.
	 *
	 * @param formula the formula
	 * @return the collection
	 */
	private Collection<Conjunction<Atom>> enumerateConjunctions(Formula formula) {
		Preconditions.checkArgument(formula != null);
		if (formula instanceof Conjunction) {
			List<Conjunction<Atom>> result = new LinkedList<>();
			List<Atom> localConj = new LinkedList<>();
			for (Formula subFormula: ((Conjunction<Formula>) formula)) {
				if (subFormula instanceof Atom) {
					localConj.add((Atom) subFormula);
				} else {
					result.addAll(this.enumerateConjunctions(subFormula));
				}
			}
			result.add(Conjunction.of(localConj));
			return result;
		}
		throw new IllegalStateException();
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String... args) {
		QueryBuilder qb = new QueryBuilder();
		qb.setName("Q");
		qb.addBodyAtom(new Atom(new Predicate("A", 2), Lists.newArrayList(new TypedConstant<>("Continent"), new Variable("x"))));
		qb.addBodyAtom(new Atom(new Predicate("B", 2), Lists.newArrayList(new TypedConstant<>("Continent"), new Variable("x"))));
		log.trace(new JoinOnVariableQuerySelector().accept(qb.build()));
	}
}
