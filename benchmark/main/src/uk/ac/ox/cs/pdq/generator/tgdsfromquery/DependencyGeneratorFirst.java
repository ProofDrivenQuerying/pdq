package uk.ac.ox.cs.pdq.generator.tgdsfromquery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters.QueryTypes;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.generator.DependencyGenerator;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * Creates tuple generating dependencies given a set of relations and an input query.
 *
 * @author Efthymia Tsamoura
 */
public class DependencyGeneratorFirst extends AbstractDependencyGenerator implements DependencyGenerator{

	/** Logger. */
	private static Logger log = Logger.getLogger(DependencyGeneratorFirst.class);
	
	/**  The query that will guide the creation of the dependencies. */
	private final ConjunctiveQuery query;
	
	/**
	 * Instantiates a new dependency generator first.
	 *
	 * @param schema the schema
	 * @param query the query
	 * @param params the params
	 */
	public DependencyGeneratorFirst(Schema schema, ConjunctiveQuery query, BenchmarkParameters params) {
		super(schema, params);
		this.query = query; 
	}
	
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.generator.DependencyGenerator#generate()
	 */
	@Override
	public Schema generate() {
		SchemaBuilder sb = Schema.builder(this.schema);
		if(this.params.getQueryType().equals(QueryTypes.ACYCLIC)) {
			return sb.addDependencies(this.generateTGDsForAcyclicQueries(this.query)).build();
		} else {
			return sb.addDependencies(this.generateTGDsForNonAcyclicQueries(this.query)).build();
		}
	}

	
	/**
	 * Generates dependencies from an input guarded query. 
	 * The left-hand side of each dependency is a randomly chosen conjunction of the query's body, 
	 * while its right-hand side is a randomly created conjunction.
	 * If the number of requested dependencies is higher than the dependencies that we can create using the conjunctions 
	 * from the query's body, then we randomly create additional dependencies 
	 *
	 * @param query the query
	 * @return 		a list of dependencies
	 */
	private List<Dependency> generateTGDsForNonAcyclicQueries(ConjunctiveQuery query) {
		List<Dependency> dependencies = new ArrayList<>();
		List<Atom> queryBodyAtoms = query.getAtoms();
		Atom guard = queryBodyAtoms.get(queryBodyAtoms.size() - 1);
		List<Set<Atom>> powerSet = Lists.newArrayList(Sets.powerSet(new LinkedHashSet<>(queryBodyAtoms)));
		
		while (!powerSet.isEmpty()) {
			//Select a random subset of conjunctions of the input query's body
			int selection = this.random.nextInt(powerSet.size());
			List<Atom> leftConjuncts = Lists.newArrayList(powerSet.get(selection));
			//Add the guard of the query in the left-hand side of the dependency
			if(!leftConjuncts.isEmpty()) {
				if (leftConjuncts.size() > 1 && !leftConjuncts.contains(guard)) {
					leftConjuncts.add(guard);
				}
				//Create the variables V that will appear in the right-hand side of the dependency  
				List<Variable> existential = this.createVariables(
						this.params.getQueryConjuncts() * this.params.getArity(),
						Utility.getVariables(leftConjuncts));

				//Create the conjunction in the right-hand side of the dependency populating its atoms
				//with variables from V
				ConjunctionInfo rightSide = this.createUnGuardedConjunction(existential,
						this.params.getDependencyConjuncts(), leftConjuncts, this.params.getRepeatedRelations());

				TGD mytgd = new TGD(
						Conjunction.of(leftConjuncts),
						Conjunction.of(rightSide.getConjuncts()));

				if (this.random.nextDouble() > 0.75 && !this.sameBody(dependencies, mytgd)) {
					dependencies.add(mytgd);
				}
			}
			powerSet.remove(selection);
		}

		dependencies.addAll(this.generateGuardedTGDs(dependencies, this.params.getNumberOfConstraints()-dependencies.size()));
		return dependencies;
	}
	
	/**
	 * Generates dependencies from the input acyclic query.
	 * The left-hand side of each  dependency is a randomly chosen conjunction of the query's body, 
	 * while its right-hand side is a randomly created conjunction.
	 * If the number of requested dependencies is higher than the dependencies that we can create using the conjunctions 
	 * from the query's body, then we randomly create additional dependencies 
	 *
	 * @param query the query
	 * @return 		a list of dependencies
	 * @TODO the method to generate dependencies from a query must be put back
	 * @TODO create the class AcyclicQuery
	 */
	private List<Dependency> generateTGDsForAcyclicQueries(ConjunctiveQuery query) {
		List<Dependency> ret = new ArrayList<>();
		List<Atom> queryBodyAtoms = query.getAtoms();
		int dependencies = 0;
		while (dependencies < this.params.getNumberOfConstraints() && dependencies < queryBodyAtoms.size()) {
			List<Atom> leftConjuncts = Lists.newArrayList(queryBodyAtoms.get(dependencies));
			List<Variable> existential = this.createVariables(
					this.params.getQueryConjuncts() * this.params.getArity(),
					Utility.getVariables(leftConjuncts));

			ConjunctionInfo rightSide = this.createUnGuardedConjunction(existential,
					this.params.getDependencyConjuncts(), leftConjuncts, this.params.getRepeatedRelations());

			TGD mytgd = new TGD(
					Conjunction.of(leftConjuncts),
					Conjunction.of(rightSide.getConjuncts()));

			if (!ret.contains(mytgd)) {
				ret.add(mytgd);
				dependencies++;
			}
		}
		ret.addAll(this.generateGuardedTGDs(ret, this.params.getNumberOfConstraints()-ret.size()));
		return ret;
	}
	
	/**
	 * Generate guarded tg ds.
	 *
	 * @param input the input
	 * @param dependencies 		The number of dependencies to create
	 * @return 		randomly created guarded dependencies. None of the output dependencies must already exist in the input collection
	 */
	private List<Dependency> generateGuardedTGDs(Collection<Dependency> input, int dependencies) {
		List<Dependency> ret = new ArrayList<>();
		while (ret.size() < dependencies) {
			List<Variable> universal = this.createVariables(this.params.getQueryConjuncts() * this.params.getArity());
			ConjunctionInfo leftSide = this.createGuardedConjunction(universal, 
					this.random.nextInt(this.params.getDependencyConjuncts()) + 1,
					this.params.getRepeatedRelations());

			List<Variable> existential = this.createVariables(
					this.params.getQueryConjuncts() * this.params.getArity(),
					leftSide.getVariables());

			ConjunctionInfo rightSide = this.createUnGuardedConjunction(existential,
					this.random.nextInt(this.params.getDependencyConjuncts()) + 1, 
					leftSide.getConjuncts(), this.params.getRepeatedRelations());
			
			TGD mytgd = new TGD(
					Conjunction.of(leftSide.getConjuncts()),
					Conjunction.of(rightSide.getConjuncts()));

			if (!input.contains(mytgd) && !ret.contains(mytgd)) {
				ret.add(mytgd);
			}
		}
		return ret;
	}
	
	
}
