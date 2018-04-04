package uk.ac.ox.cs.pdq.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryRelation;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryViewWrapper;
import uk.ac.ox.cs.pdq.datasources.utility.Result;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQueryWithInequality;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor;
import uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor.ExecutionModes;
import uk.ac.ox.cs.pdq.runtime.exec.SetupPlanExecutor;
import uk.ac.ox.cs.pdq.runtime.query.QueryEvaluator;
import uk.ac.ox.cs.pdq.runtime.query.QueryEvaluatorFactory;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;
import uk.ac.ox.cs.pdq.util.EventHandler;

/**
 * 
 * Top level class for runtime evaluating plans and queries.
 * 
 * Besides provide function the evaluate plan and queries, this class takes care
 * of properly initializing the underlying datasources, e.g. in-memory facts,
 * relation tables, or web services.
 * 
 * @author Julien Leblay
 */
public class Runtime {
	private MultiInstanceFactCache factCache = new MultiInstanceFactCache(); // not connected to any database.

	/** Runtime's parameters. */
	private RuntimeParameters params;

	/** Runtime's internal schema. */
	private Schema schema;

	/** In-memory facts. */
	private List<Atom> facts;

	/** The event bus. */
	private EventBus eventBus;

	/**
	 * Constructor for Runtime.
	 * 
	 * @param params
	 *            RuntimeParameters
	 * @param schema
	 *            Schema
	 * @param facts
	 *            List<PredicateFormula>
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
	 * 
	 * @param params
	 *            RuntimeParameters
	 * @param schema
	 *            Schema
	 */
	public Runtime(RuntimeParameters params, Schema schema) {
		this(params, schema, null);
	}

	/**
	 * Register the given event handler.
	 *
	 * @param handler
	 *            the handler
	 */
	public void registerEventHandler(EventHandler handler) {
		this.eventBus.register(handler);
	}

	/**
	 * TOCOMMENT: DIFFERENTIATE FROM PRIOR
	 *
	 * @param handler
	 *            the handler
	 */
	public void unregisterEventHandler(EventHandler handler) {
		this.eventBus.unregister(handler);
	}

	/**
	 * Sets up a database from the list of facts, if provided.
	 *
	 * @param s
	 *            Schema
	 */
	private void loadFacts(Schema s) {
		Set<InMemoryViewWrapper> views = new LinkedHashSet<>();
		Map<String, InMemoryRelation> relations = new LinkedHashMap<>();
		Map<String, Collection<Tuple>> dataDist = new LinkedHashMap<>();
		Map<InMemoryRelation, TupleType> types = new LinkedHashMap<>();

		for (Relation r : s.getRelations()) {
			InMemoryRelation w = null;
			if (r instanceof InMemoryTableWrapper) {
				w = (InMemoryTableWrapper) r;
			} else if (r instanceof InMemoryViewWrapper) {
				w = (InMemoryViewWrapper) r;
				views.add((InMemoryViewWrapper) r);
			} else {
				if (r instanceof View) {
					w = new InMemoryViewWrapper(r.getName(), r.getAttributes());
					((InMemoryViewWrapper) w).setViewToRelationDependency(((View) r).getViewToRelationDependency());
					views.add((InMemoryViewWrapper) w);

				} else {
					w = new InMemoryTableWrapper(r);
				}

				// throw new IllegalStateException("Conversion from " +
				// r.getClass().getSimpleName() + " to " +
				// InMemoryRelation.class.getSimpleName() +
				// " is not yet supported.");
			}
			w.clear();
			relations.put(w.getName(), w);
		}

		factCache.addFacts(this.facts, -1);
		for (Atom fact : this.facts) {
			InMemoryRelation w = relations.get(fact.getPredicate().getName());
			Attribute[] attributes = this.schema.getRelation(fact.getPredicate().getName()).getAttributes();
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
			data.add(RuntimeUtilities.createTuple(attributes, fact.getTerms()));
		}
		for (String r : dataDist.keySet()) {
			relations.get(r).load(dataDist.get(r));
		}
		if (!views.isEmpty()) {
			this.loadViewFacts(views, relations, dataDist);
		}
	}

	/**
	 * Loads fact in the corresponding views.
	 *
	 * @param views
	 *            the views
	 * @param relations
	 *            the relations
	 * @param dataDist
	 *            the data dist
	 */
	private void loadViewFacts(Set<InMemoryViewWrapper> views, Map<String, InMemoryRelation> relations,
			Map<String, Collection<Tuple>> dataDist) {
		for (InMemoryViewWrapper v : views) {

			LinearGuarded dependency = v.getViewToRelationDependency();
			try {
				// TOCOMMENT this is a hack, since queries are already implemented for memory we
				// can use that implementation by pouring the facts into the internal database
				// manager and execute a CQ over that data. Every other memory query including
				// the caching of data should use the internal database manager.
				InternalDatabaseManager db = new InternalDatabaseManager(factCache, -1);
				Set<Variable> freeVariables = new HashSet<>();
				for (Atom a : dependency.getBodyAtoms()) {
					for (Term t : a.getTerms()) {
						if (t.isVariable())
							freeVariables.add((Variable) t);
					}
				}
				ConjunctiveQuery leftQuery = null;
				List<Pair<Variable, Variable>> inequalities = new ArrayList<>();
				leftQuery = ConjunctiveQueryWithInequality.create(
						freeVariables.toArray(new Variable[freeVariables.size()]), dependency.getHeadAtoms(),
						inequalities);
				List<Match> results = db.answerConjunctiveQuery(leftQuery);
				Collection<Tuple> data = new ArrayList<>();
				for (Match m : results) {
					Term terms[] = new Term[freeVariables.size()];
					int i = 0;
					for (Term a : freeVariables) {
						terms[i] = m.getMapping().get(a);
						i++;
					}
					data.add(RuntimeUtilities.createTuple(v.getAttributes(), terms));
				}
				v.load(data);
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Evaluates the given plan and returns its result.
	 *
	 * @param p
	 *            Plan
	 * @param query
	 *            Query
	 * @return the result of the plan evaluation.
	 * @throws EvaluationException
	 *             the evaluation exception
	 */
	public Result evaluatePlan(RelationalTerm p, ConjunctiveQuery query) throws EvaluationException {
		return this.evaluatePlan(p, query, ExecutionModes.DEFAULT);
	}

	/**
	 * Evaluates the given plan and returns its result.
	 *
	 * @param p
	 *            Plan
	 * @param query
	 *            Query
	 * @param mode
	 *            ExecutionModes
	 * @return the result of the plan evaluation.
	 * @throws EvaluationException
	 *             the evaluation exception
	 */
	public Result evaluatePlan(RelationalTerm p, ConjunctiveQuery query, ExecutionModes mode)
			throws EvaluationException {
		PlanExecutor executor = SetupPlanExecutor.newExecutor(this.params, p, query);
		executor.setTuplesLimit(this.params.getTuplesLimit());
		// executor.setCache(this.params.getDoCache());
		executor.setEventBus(this.eventBus);
		return executor.execute(mode);
	}

	/**
	 * Evaluates the given query, and returns its result.
	 *
	 * @param query
	 *            the query
	 * @return the result of the query evaluation.
	 * @throws EvaluationException
	 *             the evaluation exception
	 */
	public Result evaluateQuery(ConjunctiveQuery query) throws EvaluationException {
		QueryEvaluator evaluator = QueryEvaluatorFactory.newEvaluator(this.schema, query);
		if (evaluator != null) {
			evaluator.setEventBus(this.eventBus);
			return evaluator.evaluate();
		}
		throw new EvaluationException("Query cannot be directly evaluated.");
	}

}
