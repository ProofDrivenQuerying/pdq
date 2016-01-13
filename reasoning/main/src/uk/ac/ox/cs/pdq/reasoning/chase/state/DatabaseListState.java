package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClass;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph;
import uk.ac.ox.cs.pdq.reasoning.utility.MapFiringGraph;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 *
 * 	Organises the facts during chasing into a list. 
 *	This type of state is used in terminating chase implementations.
 *	It also maintains the classes of equal chase constants that are derived after chasing with EGDs.
 *	This implementation does not store equality facts into the database, but when a class of equal constants is created
 *	the database facts are updated; update includes replacing every chase constant c, with a constant c' that is equal to c
 *	under the constraints and c' is a representative.
 *	The database is cleared from the obsolete facts after a chase step is applied.
 *	 
 *
 * @author Efthymia Tsamoura
 *
 */
public class DatabaseListState extends DatabaseChaseState implements ListState {


	private boolean _isFailed = false;

	/** The state's facts*/
	protected Collection<Predicate> facts;

	/** The firings that took place in this state*/
	protected FiringGraph graph;

	/** Keeps the classes of equal constants **/
	protected EqualConstantsClasses constantClasses;
	
	protected final boolean canonicalNames = true;

	/**
	 * 
	 * @param query
	 * @param manager
	 */
	public DatabaseListState(Query<?> query, DBHomomorphismManager manager) {
		this(manager, Sets.newHashSet(query.getCanonical().getPredicates()), new MapFiringGraph(), inferEqualConstantsClasses(query.getCanonical().getPredicates()));
		this.manager.addFacts(this.facts);
	}

	/**
	 * 
	 * @param manager
	 * @param facts
	 */
	public DatabaseListState(
			DBHomomorphismManager manager,
			Collection<Predicate> facts) {
		this(manager, facts, new MapFiringGraph(), inferEqualConstantsClasses(facts));
		this.manager.addFacts(this.facts);
	}

	/**
	 * 
	 * @param manager
	 * @param facts
	 * @param graph
	 * @param constantClasses
	 */
	protected DatabaseListState(
			DBHomomorphismManager manager,
			Collection<Predicate> facts,
			FiringGraph graph, 
			EqualConstantsClasses constantClasses
			) {
		super(manager);
		Preconditions.checkNotNull(facts);
		Preconditions.checkNotNull(graph);
		this.facts = facts;
		this.graph = graph;
		this.constantClasses = constantClasses;
	}

	public static EqualConstantsClasses inferEqualConstantsClasses(Collection<Predicate> facts) {
		EqualConstantsClasses constantClasses = new EqualConstantsClasses();
		for(Predicate fact:facts) {
			if(fact instanceof Equality) {
				constantClasses.add((Equality) fact);
			}
		}
		return constantClasses;
	}

	/**
	 * Updates that state given the input match. 
	 * @param match
	 * @return
	 */
	@Override
	public boolean chaseStep(Match match) {	
		return this.chaseStep(Sets.newHashSet(match));
	}

	@Override
	public boolean chaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Predicate> created = new LinkedHashSet<>();
		//For each fired EGD create classes of equivalent constants
		for(Match match:matches) {
			Constraint dependency = (Constraint) match.getQuery();
			Map<Variable, Constant> mapping = match.getMapping();
			Constraint grounded = dependency.fire(mapping, true);
			Formula left = grounded.getLeft();
			Formula right = grounded.getRight();
			if(dependency instanceof EGD) {
				for(Predicate equality:right.getPredicates()) {
					boolean successfull = this.constantClasses.add((Equality)equality);
					if(!successfull) {
						this._isFailed = true;
						break;
					}
				}
			}	
			this.graph.put(dependency, Sets.newHashSet(left.getPredicates()), Sets.newHashSet(right.getPredicates()));
		}

		//Iterate over all the database facts and replace their chase constants based on the classes of equal constants 
		//Delete the old facts from this state
		Collection<Predicate> obsoleteFacts = Sets.newHashSet();
		for(Match match:matches) {
			if(match.getQuery() instanceof EGD) {
				for(Predicate fact:this.facts) {
					List<Term> newTerms = Lists.newArrayList();
					for(Term term:fact.getTerms()) {
						EqualConstantsClass cls = this.constantClasses.getClass(term);;
						newTerms.add(cls != null ? cls.getRepresentative() : term);
					}
					if(!newTerms.equals(fact.getTerms())) {
						created.add(new Predicate(fact.getSignature(), newTerms));
						obsoleteFacts.add(fact);
					}
				}
			}
		}

		//Do not add the Equalities inside the database
		for(Match match:matches) {
			if(!(match.getQuery() instanceof EGD)) {
				Constraint dependency = (Constraint) match.getQuery();
				Map<Variable, Constant> mapping = match.getMapping();
				Constraint grounded = dependency.fire(mapping, true);
				Formula right = grounded.getRight();
				for(Predicate fact:right.getPredicates()) {
					List<Term> newTerms = Lists.newArrayList();
					for(Term term:fact.getTerms()) {
						EqualConstantsClass cls = this.constantClasses.getClass(term);
						newTerms.add(cls != null ? cls.getRepresentative() : term);
					}
					created.add(new Predicate(fact.getSignature(), newTerms));
				}
			}
		}

		this.facts.removeAll(obsoleteFacts);
		this.manager.deleteFacts(obsoleteFacts);
		this.addFacts(created);
		return !this._isFailed;
	}

	public EqualConstantsClasses getConstantClasses() {
		return this.constantClasses;
	}

	@Override
	public boolean isFailed() {
		return this._isFailed;
	}

	@Override
	public boolean isSuccessful(Query<?> query) {
		return !this.getMatches(query).isEmpty();
	}

	@Override
	public FiringGraph getFiringGraph() {
		return this.graph;
	}

	@Override
	public Collection<Predicate> getFacts() {
		return this.facts;
	}

	@Override
	public ChaseState merge(ChaseState s) {
		Preconditions.checkState(s instanceof DatabaseListState);
		Collection<Predicate> facts =  new LinkedHashSet<>(this.facts);
		facts.addAll(s.getFacts());

		EqualConstantsClasses classes = this.constantClasses.clone();
		if(!classes.merge(((DatabaseListState)s).constantClasses)) {
			return null;
		}
		return new DatabaseListState(
				this.getManager(),
				facts, 
				this.getFiringGraph().merge(s.getFiringGraph()), classes);
	}

	@Override
	public void addFacts(Collection<Predicate> facts) {
		this.manager.addFacts(facts);
		this.facts.addAll(facts);
	}

	@Override
	public DatabaseListState clone() {
		return new DatabaseListState(this.manager, Sets.newHashSet(this.facts), this.graph.clone(), this.constantClasses.clone());
	}	


}
