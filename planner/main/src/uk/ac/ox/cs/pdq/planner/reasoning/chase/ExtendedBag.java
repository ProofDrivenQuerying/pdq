package uk.ac.ox.cs.pdq.planner.reasoning.chase;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.planner.db.access.InferredAccessibleAxiom;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.Bag;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;

/**
 * Extends Bag by maintaining the sub-queries and inferred accessible dependencies satisfied in the current bag. 
 *
 * @author Efthymia Tsamoura
 *
 */
public class ExtendedBag extends Bag {

	/**
	 * A map of inferred accessible dependencies to boolean values
	 * with semantics similar to this.dependencies
	 */
	private final Map<InferredAccessibleAxiom, Boolean> accessibleDependencies;

	/**
	 * A map of the accessible sub-queries to boolean values with
	 * semantics similar to this.dependencies
	 */
	private final Map<Query<?>, Boolean> queries;

	/** 
	 * True if we still chase with the rules of the input schema
	 */
	private final boolean initialState;
	/**
	 * Constructor for ExtendedBag.
	 * @param facts Collection<PredicateFormula>
	 * @param dep Collection<Constraint>
	 * @param accessibleDep Collection<InferredAccessibleAxiom>
	 * @param queries Collection<Query<?>>
	 */
	public ExtendedBag(
			Collection<Predicate> facts, 
			Collection<Constraint> dep,
			Collection<InferredAccessibleAxiom> accessibleDep, 
			Collection<Query<?>> queries) {
		super(facts, dep);
		boolean initialState = true;
		for(Predicate fact:facts) {
			if(fact.getSignature() instanceof InferredAccessibleRelation || 
					fact.getSignature() instanceof AccessibleRelation) {
				initialState = false;
			}
		}
		this.initialState = initialState;
		this.accessibleDependencies = new LinkedHashMap<>();
		for (InferredAccessibleAxiom ic: accessibleDep) {
			this.accessibleDependencies.put(ic, false);
		}
		this.queries = new LinkedHashMap<>();
		for (Query<?> query: queries) {
			this.queries.put(query, false);
		}
	}

	/**
	 * Constructor for ExtendedBag.
	 * @param bag ExtendedBag
	 */
	public ExtendedBag(ExtendedBag bag) {
		super(bag);
		this.queries = new LinkedHashMap<>(bag.getQueries());
		this.accessibleDependencies = new LinkedHashMap<>(bag.getAccessibleDependencies());
		this.initialState = bag.initialState;
		this.isUpdated = bag.isUpdated;
	}


	/**
	 * @param s ExtendedBag
	 * @param t ExtendedBag
	 * @param detector HomomorphismDetector
	 * @return ExtendedBag
	 */
	public ExtendedBag merge(ExtendedBag t, HomomorphismDetector detector) {
		ExtendedBag bag = new ExtendedBag(t);
		for(Constraint dependency:this.getSatisfiedDependencies()) {
			bag.getDependencies().put(dependency, true);
		}
		for(InferredAccessibleAxiom dependency:this.getSatisfiedAccessibleDependencies()) {
			bag.getAccessibleDependencies().put(dependency, true);
		}
		for(Query<?> query:this.getSatisfiedQueries()) {
			bag.getQueries().put(query, true);
		}
		bag.update(detector);
		return bag;
	}

	/**
	 * It iterates over the map of inferred accessible dependencies. If a dependency's left-hand side conjunction
	 * is not already satisfied, then we search for an homomorphism of the
	 * latter to the current bag's facts.
	 * @param detector HomomorphismDetector
	 */
	@Override
	public void update(HomomorphismDetector detector) {
		if(this.initialState) {
			super.update(detector);
		}
		else {
			Iterator<Entry<InferredAccessibleAxiom, Boolean>> it = this.accessibleDependencies.entrySet().iterator();
			while (it.hasNext()) {
				Entry<InferredAccessibleAxiom, Boolean> entry = it.next();
				if (!entry.getValue()) {
					List<Match> matchings = detector.getMatches(
							entry.getKey(),
							HomomorphismConstraint.topK(1),
							HomomorphismConstraint.factScope(Conjunction.of(this.facts)),
							HomomorphismConstraint.bagScope(true, this));
					this.accessibleDependencies.put(entry.getKey(), !matchings.isEmpty());
				}
			}

			/**
			 * It iterates over the map of the accessible sub-queries. If a sub-query is not
			 * already satisfied, then we search for an homomorphism of the latter to
			 * the current bag's facts.
			 * @param detector HomomorphismDetector
			 */
			Iterator<Entry<Query<?>, Boolean>> it2 = this.queries.entrySet().iterator();
			while (it2.hasNext()) {
				Entry<Query<?>, Boolean> entry = it2.next();
				if (!entry.getValue()) {
					List<Match> matchings = detector.getMatches(
							entry.getKey(),
							HomomorphismConstraint.topK(1),
							HomomorphismConstraint.factScope(Conjunction.of(this.facts)),
							HomomorphismConstraint.bagScope(true, this));
					this.queries.put(entry.getKey(), !matchings.isEmpty());
				}
			}
		}
		this.isUpdated = true;
	}

	/**
	 * @return the collection of satisfied inferred accessible dependencies
	 */
	public Collection<InferredAccessibleAxiom> getSatisfiedAccessibleDependencies() {
		Collection<InferredAccessibleAxiom> satisfiedAccessibleSchemaICs = new LinkedHashSet<>();
		Iterator<Entry<InferredAccessibleAxiom, Boolean>> it =
				this.accessibleDependencies.entrySet().iterator();
		while (it.hasNext()) {
			Entry<InferredAccessibleAxiom, Boolean> entry = it.next();

			if (entry.getValue()) {
				satisfiedAccessibleSchemaICs.add(entry.getKey());
			}
		}
		return satisfiedAccessibleSchemaICs;
	}

	/**
	 * @return the collection of satisfied accessible sub-queries
	 */
	public Collection<Query<?>> getSatisfiedQueries() {
		Collection<Query<?>> satisfiedQueries = new LinkedHashSet<>();
		Iterator<Entry<Query<?>, Boolean>> it =
				this.queries.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Query<?>, Boolean> entry = it.next();
			if (entry.getValue()) {
				satisfiedQueries.add(entry.getKey());
			}
		}
		return satisfiedQueries;
	}

	/**
	 * @return Collection<InferredAccessibleAxiom>
	 */
	public Map<InferredAccessibleAxiom, Boolean> getAccessibleDependencies() {
		return this.accessibleDependencies;
	}

	/**
	 * @return Collection<Query<?>>
	 */
	public Map<Query<?>, Boolean> getQueries() {
		return this.queries;
	}

	/**
	 * @return ExtendedBag
	 */
	@Override
	public ExtendedBag clone() {
		return new ExtendedBag(this);
	}

	/**
	 * @return ExtendedBag
	 */
	@Override
	public ExtendedBag replicate() {
		ExtendedBag replica = new ExtendedBag(this);
		replica.id = globalId++;
		return replica;
	}

	/**
	 *
	 * @param bag
	 * 		Input bag
	 */
	@Override
	public boolean isBlocked(Bag bag) {
		Preconditions.checkArgument(bag.isUpdated());
		Preconditions.checkArgument(this.isUpdated);
		Preconditions.checkArgument(bag instanceof ExtendedBag);
		//If we are in the initial configuration
		if (this.initialState) {
			return super.isBlocked(bag);
		} else {
			//Same as above; we do consider the accessible sub-queries though.
			Collection<InferredAccessibleAxiom> parentICs = this.getSatisfiedAccessibleDependencies();
			Collection<InferredAccessibleAxiom> childICs = ((ExtendedBag) bag).getSatisfiedAccessibleDependencies();
			Collection<Query<?>> parentQueries = this.getSatisfiedQueries();
			Collection<Query<?>> childQueries = ((ExtendedBag) bag).getSatisfiedQueries();
			if (!(parentICs.isEmpty() && childICs.isEmpty()
					&& parentQueries.isEmpty() && childQueries.isEmpty())) {
				if (parentICs.equals(childICs) && parentQueries.equals(childQueries)) {
					this.setType(BagStatus.BLOCKED);
					bag.setType(BagStatus.BLOCKED);
					return true;
				} else if (parentICs.containsAll(childICs) && parentQueries.containsAll(childQueries)) {
					bag.setType(BagStatus.BLOCKED);
					return false;
				} else if (childICs.containsAll(parentICs) && childQueries.containsAll(parentQueries)) {
					this.setType(BagStatus.BLOCKED);
					return true;
				}
			}
		}
		return false;
	}
	
}
