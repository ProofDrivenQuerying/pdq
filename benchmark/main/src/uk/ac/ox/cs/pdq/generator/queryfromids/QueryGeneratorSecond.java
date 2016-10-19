package uk.ac.ox.cs.pdq.generator.queryfromids;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.generator.tgdsfromquery.QueryGeneratorFirst;
import uk.ac.ox.cs.pdq.generator.utils.InclusionDependencyGraphNode;

import com.beust.jcommander.internal.Lists;

// TODO: Auto-generated Javadoc
/**
 * Creates queries given a set of inclusion dependencies
 * 
 * 
 * 	Input parameters
 * 	NumAtoms
 * 	Dist= max distance to a free access
 * 	JoinTest= probability of joining
 * 	
 * 	Given this we generate a query as follows:
 * 	
 * 	For each i in NumAtoms
 * 	        Choose j randomly in [1,Dist]
 * 	        Let F=relations with a free access
 * 	        Let R_j= relations that have a path in the dependency graph
 * 	        to a relation in F of distance at most j, and also a path from
 * 	        a relation in F of distance at most j
 * 	        Choose relation R randomly from R_j
 * 	        For each position of R,
 * 	               with probability join, choose an existing variable (uniformly at random)
 * 	               otherwise choose a fresh variable.
 *
 * @author Efthymia Tsamoura
 */
public class QueryGeneratorSecond extends QueryGeneratorFirst{


	/**
	 * Default constructor.
	 *
	 * @param schema the schema
	 * @param params the params
	 */
	public QueryGeneratorSecond(Schema schema, BenchmarkParameters params) {
		super(schema, params);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.generator.tgdsfromquery.QueryGeneratorFirst#generate()
	 */
	@Override
	public ConjunctiveQuery generate() {
		return this.generateQueryFromInclusionDependencies();
	}

	/**
	 * Generate query from inclusion dependencies.
	 *
	 * @return the conjunctive query
	 */
	private ConjunctiveQuery generateQueryFromInclusionDependencies() {
		List<LinearGuarded> guardedDependencies = new ArrayList<>();
		// Filter out non-inclusion dependencies
		// TODO: get rid of this when inclusion dependencies have there own class.
		for (Dependency ic: this.schema.getDependencies()) {
			if (ic instanceof LinearGuarded
					&& ((LinearGuarded) ic).getRight().getAtoms().size() == 1) {
				guardedDependencies.add((LinearGuarded) ic);
			}
		}

		if (guardedDependencies.isEmpty()) {
			assert false : "Input schema has no inclusion dependency.";
		}

		//Relations with a free access
		Set<InclusionDependencyGraphNode> freeAccessNodes = new LinkedHashSet<>();
		//Create an inclusion dependency graph
		//The vertices of this graph are the atom predicates
		//There is an edge from P_i to P_j
		//if there is an inclusion dependency P_i(.) --> P_j(.) 
		Map<String, InclusionDependencyGraphNode> nodes = new TreeMap<>();
		for (LinearGuarded guardedDependency:guardedDependencies) {
			Atom l = guardedDependency.getLeft().getAtoms().get(0);
			Predicate s = l.getPredicate();
			InclusionDependencyGraphNode ln = nodes.get(s.getName());
			if (ln == null) {
				ln = new InclusionDependencyGraphNode((Relation) s);
				nodes.put(s.getName(), ln);
			}
			Atom r = guardedDependency.getRight().getAtoms().get(0);
			s = r.getPredicate();
			InclusionDependencyGraphNode rn = nodes.get(s.getName());
			if (rn == null) {
				rn = new InclusionDependencyGraphNode((Relation) s);
				nodes.put(s.getName(), rn);				
			}

			if(((Relation) s).hasFreeAccess()) {
				freeAccessNodes.add(rn);
			}
			rn.addBackNeighbor(ln);
			ln.addNeighbor(rn);
		}

		// Exploit the dependency graph
		if (!nodes.isEmpty()) {
			List<Variable> variablesPool = this.createVariables(1000);
			List<Variable> queryVariables = new ArrayList<>();
			List<Atom> queryAtoms = new ArrayList<>();
			List<Variable> freshVariables = new ArrayList<>();
			for(int atom = 0; atom < this.params.getQueryConjuncts(); ++atom) {
				List<Variable> nextfreshVariables = new ArrayList<>();
				int j = 1 + this.random.nextInt(this.params.getMaxDistanceToFree());
				List<InclusionDependencyGraphNode> n = new ArrayList<>();
				for(InclusionDependencyGraphNode node:freeAccessNodes) {
					n.addAll(node.traverseBackwards(j));
				}
				n = Lists.newArrayList(new LinkedHashSet<>(n));
				List<Variable> terms = new ArrayList<>();
				InclusionDependencyGraphNode queryNode = n.get(this.random.nextInt(n.size()));
				for(int position = 0; position < queryNode.getRelation().getArity(); ++position) {
					if(!this.schema.isCyclic()) {
						if(!queryVariables.isEmpty() && this.params.getJoin() > this.random.nextDouble()) {
							terms.add(queryVariables.get(this.random.nextInt(queryVariables.size())));
						}
						else {
							Variable freshVariable = variablesPool.remove(this.random.nextInt(variablesPool.size()));
							terms.add(freshVariable);
							nextfreshVariables.add(freshVariable);
						}
					}
					else {
						if(!freshVariables.isEmpty() && this.params.getJoin() > this.random.nextDouble()) {
							terms.add(freshVariables.get(this.random.nextInt(freshVariables.size())));
						}
						else {
							Variable freshVariable = variablesPool.remove(this.random.nextInt(variablesPool.size()));
							terms.add(freshVariable);
							nextfreshVariables.add(freshVariable);
						}
					}
				}
				freshVariables.clear();
				freshVariables.addAll(nextfreshVariables);
				queryAtoms.add(new Atom(queryNode.getRelation(), terms));
				queryVariables.addAll(terms);
			}

			// Create free variables
			List<Variable> freeVars = this.pickFreeVariables(queryAtoms);
			return new ConjunctiveQuery(
					new Atom(new Predicate("Q", freeVars.size()), freeVars),
					Conjunction.of(queryAtoms));
		}
		throw new IllegalStateException("Could not generate query. Dependency graph is empty");
	}

}
