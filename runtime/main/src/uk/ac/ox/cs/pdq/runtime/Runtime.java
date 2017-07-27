package uk.ac.ox.cs.pdq.runtime;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.datasources.Result;
import uk.ac.ox.cs.pdq.datasources.Table;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryRelation;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryViewWrapper;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.runtime.exec.Middleware;
import uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor;
import uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor.ExecutionModes;
import uk.ac.ox.cs.pdq.runtime.query.InMemoryQueryEvaluator;
import uk.ac.ox.cs.pdq.runtime.query.QueryEvaluator;
import uk.ac.ox.cs.pdq.runtime.query.QueryEvaluatorFactory;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * 
 * Top level class for runtime evaluating plans and queries.
 * 
 * Besides provide function the evaluate plan and queries, this class
 * takes care of properly initializing the underlying datasources, e.g.
 * in-memory facts, relation tables, or web services.
 * 
 * @author Julien Leblay
 */
public class Runtime {

	/**  Runtime's parameters. */
	private RuntimeParameters params;
	
	/**  Runtime's internal schema. */
	private Schema schema;

	/**  In-memory facts. */
	private List<Atom> facts;
	
	/** The event bus. */
	private EventBus eventBus;
	
	/**
	 * Constructor for Runtime.
	 * @param params RuntimeParameters
	 * @param schema Schema
	 * @param facts List<PredicateFormula>
	 */
	public Runtime(RuntimeParameters params, Schema schema, List<Atom> facts) {
		super();
		this.eventBus = new EventBus();
		this.params = params;
		this.schema = schema;
		this.facts = facts;
		if (this.facts != null) {
			this.loadFacts(schema);
		}
	}
	
	/**
	 * Constructor for Runtime.
	 * @param params RuntimeParameters
	 * @param schema Schema
	 */
	public Runtime(RuntimeParameters params, Schema schema) {
		this(params, schema, null);
	}
	
	/**
	 * Register the given event handler.
	 *
	 * @param handler the handler
	 */
	public void registerEventHandler(EventHandler handler) {
		this.eventBus.register(handler);
	}
	
	/**
	 * Register the given event handler.
	 *
	 * @param handler the handler
	 */
	public void unregisterEventHandler(EventHandler handler) {
		this.eventBus.unregister(handler);
	}

	/**
	 * Sets up a database from the list of facts, if provided.
	 *
	 * @param s Schema
	 */
	private void loadFacts(Schema s) {
		Set<InMemoryViewWrapper> views = new LinkedHashSet<>();
		Map<String, InMemoryRelation> relations = new LinkedHashMap<>();
		Map<String, Collection<Tuple>> dataDist = new LinkedHashMap<>();
		Map<InMemoryRelation, TupleType> types = new LinkedHashMap<>();
		
		for (Relation r: s.getRelations()) {
			InMemoryRelation w = null;
			if (r instanceof InMemoryTableWrapper) {
				w = (InMemoryTableWrapper) r;
			} else if (r instanceof InMemoryViewWrapper) {
				w = (InMemoryViewWrapper) r;
				views.add((InMemoryViewWrapper) r);
			} else {
				throw new IllegalStateException("Conversion from " + 
						r.getClass().getSimpleName() + " to " + 
						InMemoryRelation.class.getSimpleName() + 
						" is not yet supported.");
			}
			w.clear();
			relations.put(w.getName(), w);
		}

		for (Atom fact: this.facts) {
			InMemoryRelation w = relations.get(fact.getPredicate().getName());
			Attribute[] attributes = ((Relation) fact.getPredicate()).getAttributes();
			TupleType type = types.get(w);
			if (type == null) {
				type = TupleType.DefaultFactory.createFromTyped(attributes);
				types.put(w, type);
			}
			Collection<Tuple> data = dataDist.get(w.getName());
			if (data == null) {
				data = new LinkedList<>();
				dataDist.put(w.getName(), data);
				relations.put(w.getName(), w);
			}
			data.add(toTuple(type, attributes, fact.getTerms()));
		}
		for (String r: dataDist.keySet()) {
			relations.get(r).load(dataDist.get(r));
		}
		if (!views.isEmpty()) {
			this.loadViewFacts(views, relations, dataDist);
		}
	}

	/**
	 * Loads fact in the corresponding views.
	 *
	 * @param views the views
	 * @param relations the relations
	 * @param dataDist the data dist
	 */
	private void loadViewFacts(Set<InMemoryViewWrapper> views,
			Map<String, InMemoryRelation> relations,
			Map<String, Collection<Tuple>> dataDist) {
		for (InMemoryViewWrapper v: views) {
			
			LinearGuarded dependency = v.getDependency();
			ConjunctiveQuery cq = ConjunctiveQuery.create(
					dependency.getFreeVariables(),
					(Conjunction) Conjunction.of(dependency.getHead()));
			Collection<Tuple> data = new LinkedList<>();
			try {
				InMemoryQueryEvaluator eval = new InMemoryQueryEvaluator(cq);
				Result r = eval.evaluate();
				if (r instanceof Table) {
					Table d = (Table) r;
					for (Tuple t: d) {
						data.add(t);
					}
					v.load(data);
				}
			} catch (EvaluationException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
	}

	/**
	 * Evaluates the given plan and returns its result. 
	 *
	 * @param p Plan
	 * @param query Query
	 * @return the result of the plan evaluation.
	 * @throws EvaluationException the evaluation exception
	 */
	public Result evaluatePlan(RelationalTerm p, ConjunctiveQuery query)
			throws EvaluationException {
		return this.evaluatePlan(p, query, ExecutionModes.DEFAULT);
	}

	/**
	 * Evaluates the given plan and returns its result. 
	 *
	 * @param p Plan
	 * @param query Query
	 * @param mode ExecutionModes
	 * @return the result of the plan evaluation.
	 * @throws EvaluationException the evaluation exception
	 */
	public Result evaluatePlan(RelationalTerm p, ConjunctiveQuery query, ExecutionModes mode)
			throws EvaluationException {
		PlanExecutor executor = Middleware.newExecutor(this.params, p, query);
		executor.setTuplesLimit(this.params.getTuplesLimit());
		executor.setCache(this.params.getDoCache());
		executor.setEventBus(this.eventBus);
		return executor.execute(mode);
	}

	/**
	 * Evaluates the given query, and returns its result.
	 *
	 * @param query the query
	 * @return the result of the query evaluation.
	 * @throws EvaluationException the evaluation exception
	 */
	public Result evaluateQuery(ConjunctiveQuery query) throws EvaluationException {
		QueryEvaluator evaluator = QueryEvaluatorFactory.newEvaluator(this.schema, query);
		if (evaluator != null) {
			evaluator.setEventBus(this.eventBus);
			return evaluator.evaluate();
		}
		throw new EvaluationException("Query cannot be directly evaluated.");
	}
	
	/**
	 * To tuple.
	 *
	 * @param type TupleType
	 * @param attributes List<Attribute>
	 * @param values Constant[]
	 * @return a tuple view of the given collection of terms.
	 */
	private static Tuple toTuple(TupleType type, Attribute[] attributes, Constant[] values) {
		Preconditions.checkArgument(attributes.length == values.length);
		Object[] constants = new Object[values.length];
		for (int i = 0, l = values.length; i < l; i++) {
			constants[i] = Utility.cast(attributes[i].getType(), values[i].toString());
		}
		return type.createTuple(constants);
	}
}
