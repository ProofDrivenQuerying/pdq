package uk.ac.ox.cs.pdq.runtime.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.datasources.utility.BooleanResult;
import uk.ac.ox.cs.pdq.datasources.utility.Result;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.CartesianProduct;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.IsEmpty;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.MemoryScan;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Projection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.SymmetricMemoryHashJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;


// TODO: Auto-generated Javadoc
/**
 * In-memory query evaluator.
 * 
 * @author Julien Leblay
 *
 */
public class InMemoryQueryEvaluator implements QueryEvaluator {

	/**  The logger. */
	public static Logger log = Logger.getLogger(InMemoryQueryEvaluator.class);

	/**  The evaluator's event bus. */
	private EventBus eventBus;
	
	/**  The query to be evaluated. */
	private final ConjunctiveQuery query;

	/**
	 * Constructor for InMemoryQueryEvaluator.
	 * @param q Query
	 */
	public InMemoryQueryEvaluator(ConjunctiveQuery q) {
		this.query = q;
	}
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.query.QueryEvaluator#setEventBus(com.google.common.eventbus.EventBus)
	 */
	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.query.QueryEvaluator#evaluate()
	 */
	@Override
	public Result evaluate() throws EvaluationException {
		try (TupleIterator phyPlan = this.makePhysicalPlan(this.query)) {
			phyPlan.open();
			if (this.query.isBoolean()) {
				boolean result = (boolean) phyPlan.next().getValue(0);
				if (this.eventBus != null) {
					this.eventBus.post(TupleType.DefaultFactory.create(Boolean.class).createTuple(result));
				}
				return new BooleanResult(!result);
			}
			Table result = new Table(RuntimeUtilities.getAttributesCorrespondingToFreeVariables(this.query));
			while(phyPlan.hasNext()) {
				Tuple t = phyPlan.next();
				result.appendRow(t);
				if (this.eventBus != null) {
					this.eventBus.post(t);
				}
			}
			return result;
		}
	}

	/**
	 * Make physical plan.
	 *
	 * @param query the q
	 * @return a physical plan as a tuple iterator.
	 * @throws EvaluationException if the statement could not be generated.
	 */
	private TupleIterator makePhysicalPlan(ConjunctiveQuery query) throws EvaluationException {
		Map<Variable, Set<Atom>> joins = new LinkedHashMap<>();
		Map<Atom, TupleIterator> scans = new LinkedHashMap<>();
		for (Atom p: query.getAtoms()) {
			scans.put(p, this.makeScans(p));
			for (Term t: p.getTerms()) {
				if (t instanceof Variable) {
					Set<Atom> preds = joins.get(t);
					if (preds == null) {
						preds = new LinkedHashSet<>();
						joins.put((Variable) t, preds);
					}
					preds.add(p);
				}
			}
		}
		TupleIterator result = this.makeJoins(scans, new LinkedList<>(joins.values()));
		if (query.isBoolean()) {
			result = new IsEmpty(result);
		} else {
			result = new Projection(RuntimeUtilities.getAttributesCorrespondingToFreeVariables(query), result);
		}
		return result;
	}
	
	/**
	 * Make scans.
	 *
	 * @param atom Atom
	 * @return TupleIterator
	 * @throws EvaluationException the evaluation exception
	 */
	private TupleIterator makeScans(Atom atom) throws EvaluationException {
		if (!(atom.getPredicate() instanceof InMemoryTableWrapper)) {
			throw new EvaluationException(
					atom.getPredicate().getClass().getSimpleName() +
					" relations not supported in In-Mem query evaluator.");
		}
		InMemoryTableWrapper r = (InMemoryTableWrapper) atom.getPredicate();
		SimpleCondition[] conditions = this.computeSelectionConditions(r.getAttributes(), atom.getTerms()); 
		if (conditions.length == 0) 
			return new MemoryScan(r.getAttributes(), r.getData());
		return new Selection(ConjunctiveCondition.create(conditions), new MemoryScan(r.getAttributes(), r.getData()));
	}

	/**
	 * Make selection predicates.
	 *
	 * @param attributes List<Attribute>
	 * @param terms List<Term>
	 * @return List<Atom>
	 */
	private SimpleCondition[] computeSelectionConditions(Attribute[] attributes, Term[] terms) {
		List<SimpleCondition> result = new ArrayList<>();
		Map<Term, Integer> positions = new LinkedHashMap<>();
		int i = 0;
		for (Term t: terms) {
			if (t instanceof TypedConstant) {
				result.add(ConstantEqualityCondition.create(i, (TypedConstant) t));
			} else {
				Integer position = positions.get(t);
				if (position != null) {
					result.add(AttributeEqualityCondition.create(position, i));
				} else {
					positions.put(t, i);
				}
			}
			i++;
		}
		return result.toArray(new SimpleCondition[result.size()]);
	}
	
	/**
	 * Make joins.
	 *
	 * @param scans the scans
	 * @param clusters the clusters
	 * @return a join/cross product relation tree
	 */
	private TupleIterator makeJoins(Map<Atom, TupleIterator> scans, List<Set<Atom>> clusters) {
		TupleIterator outer = null;
		Iterator<Set<Atom>> i = connectedComponents(clusters).iterator();
		if (i.hasNext()) {
			do {
				TupleIterator inner = null;
				Set<Atom> cluster = i.next();
				Iterator<Atom> j = cluster.iterator();
				if (j.hasNext()) {
					do {
						Atom atom = j.next();
						if (inner == null) {
							inner = scans.get(atom);
						} else {
							inner = new SymmetricMemoryHashJoin(inner, scans.get(atom));
						}
					} while (j.hasNext());
				}
				if (outer == null) {
					outer = inner;
				} else {
					outer = new CartesianProduct(outer, inner);
				}
			} while (i.hasNext());
		}
		return outer;
	}
	
	/**
	 * Connected components.
	 *
	 * @param clusters the clusters
	 * @return 		a partition of the given clusters, such that all predicates in the
	 *      each component are connected, and no predicates part of distinct
	 *      component are connected.
	 */
	private List<Set<Atom>> connectedComponents(List<Set<Atom>> clusters) {
		List<Set<Atom>> result = new LinkedList<>();
		if (clusters.isEmpty()) 
			return result;
		Set<Atom> first = clusters.get(0);
		if (clusters.size() > 1) {
			List<Set<Atom>> rest = connectedComponents(clusters.subList(1, clusters.size()));
			for (Set<Atom> s : rest) {
				if (!Collections.disjoint(first, s)) {
					first.addAll(s);
				} else {
					result.add(s);
				}
			}
		}
		result.add(first);
		return result;
	}
}
