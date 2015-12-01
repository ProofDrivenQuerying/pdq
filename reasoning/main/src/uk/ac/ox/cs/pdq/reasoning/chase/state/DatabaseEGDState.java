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
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;
import uk.ac.ox.cs.pdq.reasoning.chase.MapFiringGraph;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClass;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class DatabaseEGDState extends DatabaseChaseState implements ListState {


	private boolean _isFailed = false;

	/** The state's facts*/
	protected Collection<Predicate> facts;

	/** The firings that took place in this state*/
	protected FiringGraph graph;

	/** Keeps the classes of equal constants **/
	protected EqualConstantsClasses constantClasses;

	/**
	 * 
	 * @param query
	 * @param manager
	 */
	public DatabaseEGDState(Query<?> query, DBHomomorphismManager manager) {
		this(manager, Sets.newHashSet(query.getCanonical().getPredicates()), new MapFiringGraph(), inferEqualConstantsClasses(query.getCanonical().getPredicates()));
		this.manager.addFacts(this.facts);
	}

	/**
	 * 
	 * @param manager
	 * @param facts
	 */
	public DatabaseEGDState(
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
	protected DatabaseEGDState(
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

	private static EqualConstantsClasses inferEqualConstantsClasses(Collection<Predicate> facts) {
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
		Preconditions.checkState(s instanceof DatabaseEGDState);
		Collection<Predicate> facts =  new LinkedHashSet<>(this.facts);
		facts.addAll(s.getFacts());

		EqualConstantsClasses classes = this.constantClasses.clone();
		if(!classes.merge(((DatabaseEGDState)s).constantClasses)) {
			return null;
		}
		return new DatabaseEGDState(
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
	public DatabaseEGDState clone() {
		return new DatabaseEGDState(this.manager, Sets.newHashSet(this.facts), this.graph.clone(), this.constantClasses.clone());
	}	


}
