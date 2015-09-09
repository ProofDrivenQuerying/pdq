package uk.ac.ox.cs.pdq.generator.third;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.LinearGuarded;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.generator.QueryGenerator;
import uk.ac.ox.cs.pdq.generator.first.AbstractDependencyGenerator;
import uk.ac.ox.cs.pdq.generator.utils.InclusionDependencyGraphNode;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;


/**
 * 
 * @author Julien LEBLAY
 *
 */
public class QueryGeneratorThird extends AbstractDependencyGenerator implements QueryGenerator{
	
	/**
	 * Default constructor.
	 * 
	 * @param schema
	 */
	public QueryGeneratorThird(Schema schema, BenchmarkParameters params) {
		super(schema, params);
	}

	@Override
	public ConjunctiveQuery generate() {
		return this.generateQueryFromInclusionDependencies();
	}

	private ConjunctiveQuery generateQueryFromInclusionDependencies() {
		List<LinearGuarded> guardedDependencies = new ArrayList<>();
		// Filter out non-inclusion dependencies
		// TODO: get rid of this when inclusion dependencies have there own class.
		for (Constraint ic : this.schema.getDependencies()) {
			if (ic instanceof LinearGuarded
					&& ((LinearGuarded) ic).getRight().getPredicates().size() == 1) {
				guardedDependencies.add((LinearGuarded) ic);
			}
		}

		if (guardedDependencies.isEmpty()) {
			assert false : "Input schema has no inclusion dependency.";
		}

		// Create dependency graph
		Map<String, InclusionDependencyGraphNode> nodes = new TreeMap<>();
		for (LinearGuarded guardedDependency:guardedDependencies) {
			Predicate l = guardedDependency.getLeft().getPredicates().get(0);
			Signature s = l.getSignature();
			InclusionDependencyGraphNode ln = nodes.get(s.getName());
			if (ln == null) {
				ln = new InclusionDependencyGraphNode((Relation) s);
				nodes.put(s.getName(), ln);
			}
			Predicate r = guardedDependency.getRight().getPredicates().get(0);
			s = r.getSignature();
			InclusionDependencyGraphNode rn = nodes.get(s.getName());
			if (rn == null) {
				rn = new InclusionDependencyGraphNode((Relation) s);
				nodes.put(s.getName(), rn);
			}
			ln.addNeighbor(rn);
		}

		// Exploit the dependency graph
		if (!nodes.isEmpty()) {
			int length = 0;
			List<Predicate> queryAtoms = new ArrayList<>();
			List<String> ordered = new ArrayList<>(nodes.keySet());
			do {
				InclusionDependencyGraphNode start = nodes.get(ordered.get(this.random.nextInt(ordered.size())));
				List<Predicate> atoms = start.traverseRandom(this.random, this.params.getQueryConjuncts() - length, null, null);
				if (queryAtoms.isEmpty()
						// Ensure the first fragment has more than one atom in the first round
						&& (atoms.size() > 1 || this.params.getQueryConjuncts() <= 1)) {
					queryAtoms.addAll(atoms);
					length = atoms.size();
				} else if (this.random.nextBoolean()) {
					queryAtoms.addAll(atoms);
					length = queryAtoms.size();
				} else {
					this.join(queryAtoms, atoms);
					length = queryAtoms.size();
				}
			} while (length < this.params.getQueryConjuncts());

			// Create free variables
			List<Variable> freeVars = this.pickFreeVariables(queryAtoms);
			return new ConjunctiveQuery(
					new Predicate(new Signature("Q", freeVars.size()), freeVars),
					Conjunction.of(queryAtoms));
		}
		throw new IllegalStateException("Could not generate query. Dependency graph is empty");
	}

	/**
	 * Attempts to join two lists of atoms on predicate/attributes
	 * with common signature and name, such that no pre-existing join variable 
	 * was used in the operation.
	 * 
	 * @param leftAtoms
	 * @param rightAtoms
	 * @return true if the two list of atoms could be joined
	 */
	private Boolean join(List<Predicate> leftAtoms, List<Predicate> rightAtoms) {
		Multimap<Term, Predicate> clusters = LinkedHashMultimap.create();
		for (Predicate a : leftAtoms) {
			for (Term t : a.getTerms()) {
				clusters.put(t, a);
			}
		}
		for (Predicate a : rightAtoms) {
			for (Term t : a.getTerms()) {
				clusters.put(t, a);
			}
		}

		for (Predicate a : leftAtoms) {
			for (Predicate b : rightAtoms) {
				if (a.getSignature().equals(b.getSignature())) {
					for (int i = 0, l = a.getSignature().getArity(); i < l; i++) {
						Term t1 = a.getTerm(i);
						if (clusters.get(t1).size() == 1) {
							Term t2 = b.getTerm(i);
							if (clusters.get(t2).size() == 1) {
								List<Term> terms = Lists.newArrayList(b.getTerms());
								terms.set(i, t1);
								leftAtoms.set(leftAtoms.indexOf(a), new Predicate(a.getSignature(), terms));
								leftAtoms.addAll(rightAtoms);
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
}
