package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * 
 * The blocking chase algorithm organises the facts into trees of bags.
 * This class provides a bag implementation. A bag can be either blocked or non-blocked.
 * Only non-blocked bags are expanded during chasing. Expansion consists of, first, 
 * finding an homomorphism of the left-hand side of an input dependency to a set of facts inside the bag and, second,
 * grounding the corresponding dependency based on the facts found and creating a new child bag using the newly created facts
 * (the consequence ones).
 * This class keeps the schema dependencies satisfied by the facts in the current bag. 
 *
 * @author Efthymia Tsamoura
 *
 */
public class Bag {
	
	/** Status of a Bag. */
	public static enum BagStatus {BLOCKED, NONBLOCKED}

	/** The status of this bag. */
	protected BagStatus type = BagStatus.NONBLOCKED;

	/** Bag's id. */
	protected int id;

	/** Global counter for assigning unique bag IDs. */
	protected static int globalId = 0;

	/** The facts of this bag. */
	protected final Collection<Predicate> facts;

	/** The constants of this bag. */
	protected final Collection<Term> constants;
	
	/**
	 * A map of schema dependencies to boolean values. If the corresponding boolean value
	 * is true, then the left-hand side of the corresponding dependency is satisfied by 
	 * the facts of this bag.
	 */
	private final Map<Constraint, Boolean> dependencies;
	
	/** True if the bag is updated after adding new facts. */
	protected boolean isUpdated = false;

	/**
	 * Constructor for Bag
	 * @param facts
	 * @param dep
	 */
	public Bag(Collection<Predicate> facts, Collection<Constraint> dep) {
		this.facts = new LinkedHashSet<>(facts);
		this.id = globalId++;
		this.constants = Utility.getTerms(facts);
		this.type = BagStatus.NONBLOCKED;
		this.dependencies = new LinkedHashMap<>();
		for (Constraint ic: dep) {
			this.dependencies.put(ic, false);
		}
	}


	/**
	 * Copy constructor
	 * @param bag
	 */
	public Bag(Bag bag) {
		this.facts = new LinkedHashSet<>(bag.getFacts());
		this.id = bag.id;
		this.constants = Utility.getTerms(this.facts);
		this.type = bag.getType();
		this.dependencies = new LinkedHashMap<>(bag.getDependencies());
		this.isUpdated = bag.isUpdated;
	}

	/**
	 * Finds the dependencies which are satisfied. If a dependency's left-hand side subformula is not
	 * already satisfied, then we search for homomorphisms of this dependency to the facts of this bag.
	 * @param detector HomomorphismDetector
	 */
	public void update(HomomorphismDetector detector) {
		Iterator<Entry<Constraint, Boolean>> it = this.dependencies.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Constraint, Boolean> entry = it.next();
			if (!entry.getValue()) {
				List<Match> matchings = detector.getMatches(
						entry.getKey(),
						HomomorphismConstraint.topK(1),
						HomomorphismConstraint.factScope(Conjunction.of(this.facts)),
						HomomorphismConstraint.bagScope(true, this));
				this.dependencies.put(entry.getKey(), !matchings.isEmpty());
			}
		}
		this.isUpdated = true;
	}

	/**
	 * @return the satisfied dependencies
	 */
	public Collection<Constraint> getSatisfiedDependencies() {
		Collection<Constraint> satisfiedSchemaICs = new LinkedHashSet<>();
		Iterator<Entry<Constraint, Boolean>> it = this.dependencies.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Constraint, Boolean> entry = it.next();
			if (entry.getValue()) {
				satisfiedSchemaICs.add(entry.getKey());
			}
		}
		return satisfiedSchemaICs;
	}


	/**
	 * 
	 * @return
	 */
	public Map<Constraint, Boolean> getDependencies() {
		return this.dependencies;
	}
	

	/**
	 * 
	 * @return
	 * 		the facts of this bag
	 */
	public Collection<Predicate> getFacts() {
		return this.facts;
	}

	/**
	 * 
	 * @return
	 * 		the bag's id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * 
	 * @return
	 * 		the bag's status
	 */
	public BagStatus getType() {
		return this.type;
	}

	/**
	 * Sets the type of the bag
	 * @param type
	 */
	public void setType(BagStatus type) {
		this.type = type;
	}


	/**
	 * 
	 * @return
	 * 		the constants of this bag
	 */
	public Collection<Term> getConstants() {
		return this.constants;
	}
	
	/**
	 * 
	 * @return
	 * 		true if the bag is updated
	 */
	public boolean isUpdated() {
		return isUpdated;
	}

	/**
	 * Copies the facts of the input bag that have constants that also appear in this bag
	 * @param bag
	 * 		Input bag
	 * @return
	 * 		The facts that will be copied to this bag
	 */
	public Collection<BagBoundPredicate> copyFacts(Bag bag) {
		Collection<BagBoundPredicate> copiedFacts = new LinkedHashSet<>();
		List<Term> c = Lists.newArrayList(this.constants);
		c.retainAll(bag.getConstants());
		if(!c.isEmpty()) {
			for(Predicate fact:bag.getFacts()) {
				if(c.containsAll(fact.getTerms())) {
					this.addFacts(fact);
					copiedFacts.add(new BagBoundPredicate(fact, this.id));
				}
			}
		}
		this.isUpdated = false;
		return copiedFacts;
	}

	/**
	 * This method is called during fact propagation
	 * (a fact F is propagated to a bag B if B contains all of the constants of F)
	 * @param constants
	 * 		Input constants
	 * @return
	 * 		true if all of the input constants appear inside this bag.
	 */
	public boolean containsTerms(Collection<Term> constants) {
		return this.constants.containsAll(constants);
	}

	/**
	 *
	 * @param fact
	 * 		fact to be added
	 */
	public void addFacts(Predicate fact) {
		this.facts.add(fact);
		this.isUpdated = false;
	}

	/**
	 * 
	 * @param facts
	 * 		facts to be added
	 */
	public void addFacts(Collection<Predicate> facts) {
		this.facts.addAll(facts);
		this.isUpdated = false;
	}

	@Override
	public String toString() {
		return '{' + Joiner.on(",").join(this.facts) + ',' + this.type.toString() + ',' + new Integer(this.id) + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.id == ((Bag) o).id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	public Bag clone() {
		return new Bag(this);
	}

	/**
	 * 
	 * @return
	 * 		a replica bag. A bag with the same contents but with different id.
	 */
	public Bag replicate() {
		Bag replica = new Bag(this);
		replica.id = globalId++;
		return replica;
	}
	
	/**
	 * Blocking implementation which updates the status of a bag every time new
	 * facts are propagated upwards the tree.
	 * A bag B' blocks another bag B if
	 *  -in the initial configuration, B' satisfies as many dependencies as B does
	 *  -in the next configurations, B' satisfies as many inferred accessible dependencies as B does
	 *  plus as many inferred accessible sub-queries of the original query as B does.
	 *
	 */
	public boolean isBlocked(Bag bag) {
		Preconditions.checkArgument(bag.isUpdated);
		Preconditions.checkArgument(this.isUpdated);
		Collection<Constraint> parentICs = this.getSatisfiedDependencies();
		Collection<Constraint> childICs = bag.getSatisfiedDependencies();
		//Find the schema dependencies that are satisfied in the parent and child bags, respectively.
		if (parentICs.isEmpty() && childICs.isEmpty()) {
			return false;
		} else {
			//If both bags satisfy the same dependencies, then both of them are blocked
			if (parentICs.equals(childICs)) {
				this.setType(BagStatus.BLOCKED);
				bag.setType(BagStatus.BLOCKED);
				return true;
				//If the parent bag satisfies any dependency that the child bag also satisfies, then the child is blocked
			} else if (parentICs.containsAll(childICs)) {
				bag.setType(BagStatus.BLOCKED);
				return false;
				//If the child bag satisfies any dependency that the parent bag also satisfies, then the parent is blocked
			} else if (childICs.containsAll(parentICs)) {
				this.setType(BagStatus.BLOCKED);
				return true;
			}
		}
		return false;
	}
}
