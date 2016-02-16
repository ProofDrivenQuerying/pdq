package uk.ac.ox.cs.pdq.generator.tgdsfromquery;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.LinearGuarded;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.db.metadata.StaticMetadata;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.generator.ViewGenerator;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * Generates views given a set of relations and an input query.
 *
 * @author Efthymia Tsamoura
 * @author Julien LEBLAY
 */
public class ViewGeneratorFirst extends AbstractDependencyGenerator implements ViewGenerator{

	/** The global view id. */
	protected static int globalViewId = 0;
	
	/**  The query that will guide the creation of the dependencies. */
	private final ConjunctiveQuery query;

	/**
	 * Instantiates a new view generator first.
	 *
	 * @param schema the schema
	 * @param query the query
	 * @param params the params
	 */
	public ViewGeneratorFirst(Schema schema, ConjunctiveQuery query, BenchmarkParameters params) {
		super(schema, params);
		this.query = query;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.generator.ViewGenerator#generate()
	 */
	@Override
	public Schema generate() {
		SchemaBuilder sb = Schema.builder(this.schema);
		return sb.addRelations(this.generateViews()).build();
	}
	
	/**
	 * Generates views from the given query q.
	 * The view's body is a randomly chosen subset of conjunctions of the input query's body, 
	 * while its right-hand is a randomly created conjunction of atoms. 
	 * For each 
	 *
	 * @return 		a list of dependencies defining views
	 */
	public List<View> generateViews() {
		List<View> result = new ArrayList<>();
		List<Predicate> queryBodyAtoms = this.query.getBody().getPredicates();
		Predicate guard = queryBodyAtoms.get(queryBodyAtoms.size() - 1);
		List<Set<Predicate>> powerSet = Lists.newArrayList(Sets.powerSet(new LinkedHashSet<>(queryBodyAtoms)));

		if (this.params.getNumberOfConstraints() > powerSet.size() - 1) {
			throw new java.lang.IllegalArgumentException("Attempting to create " + 
					this.params.getNumberOfViews() + " views while the query body atom powerset is " + 
					powerSet.size());
		}
		
		List<View> views = new ArrayList<>();
		while (!powerSet.isEmpty()) {
			int selection = this.random.nextInt(powerSet.size());
			Set<Predicate> conjuncts = Sets.newLinkedHashSet(powerSet.get(selection));
			conjuncts.add(guard);
			List<Set<Variable>> termPowerSet = Lists.newArrayList(Sets.powerSet(new LinkedHashSet<>(Utility.getVariables(conjuncts))));
			for (Set<Variable> terms:termPowerSet) {
				if (!terms.isEmpty()) {
					View myview = this.createViewInstance(conjuncts, terms);
					if (!this.sameView(views, myview)) {
						result.add(myview);
					}
				}
			}
			powerSet.remove(selection);
		}
		return result;
	}
	
	
	
	/**
	 * Creates the view instance.
	 *
	 * @param conjunction the conjunction
	 * @param viewArguments the view arguments
	 * @return 		a view definition given the input conjunction and the variables to expose
	 */
	private View createViewInstance(Set<Predicate> conjunction, Set<Variable> viewArguments) {
		List<Predicate> conjuncts = Lists.newArrayList(conjunction);
		Predicate headAtom =
				new Predicate(
						new Signature("V" + (globalViewId ++), viewArguments.size()), viewArguments);
		List<Variable> free = Lists.newArrayList(viewArguments);
		List<Variable> bound = Utility.getVariables(conjuncts);
		bound.removeAll(free);
		LinearGuarded linear =
				new LinearGuarded(
						headAtom, 
						Conjunction.of(conjuncts));
		Double cost = null;
		switch (this.params.getCostType()) {
		case SIMPLE_CONSTANT:
			cost = this.params.getMeanCost();
			break;
		case SIMPLE_RANDOM:
		case BLACKBOX:
			cost = Utility.meanDist(this.random, this.params.getMeanCost(), 0.0, this.params.getMaxCost());
			break;
		default:
			throw new IllegalArgumentException(this.params.getCostType() + " not yet supported for generated views.");
		}
		AccessMethod b = new AccessMethod(Types.FREE, Lists.<Integer>newArrayList());
		View result = new View(linear, b);
		StaticMetadata metadata = new StaticMetadata(
				(long) this.random.nextInt(this.params.getRelationSize()));
		metadata.setPerInputTupleCost(b, new DoubleCost(cost));
		result.setMetadata(metadata);
		return result; 

	}
}
