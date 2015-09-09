package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;
import uk.ac.ox.cs.pdq.reasoning.chase.MapFiringGraph;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;
import uk.ac.ox.cs.pdq.util.Utility;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 *
 *	The facts of this chase state are organised into a list of facts. This type of
 *  state is used in non-blocking chase implementations.
 * 	It keeps the facts of this state in a database.
 * 
 *  @author Efthymia Tsamoura
 */
public class DatabaseListState extends DatabaseChaseState implements ListState{

	protected static Logger log = Logger.getLogger(DatabaseListState.class);
		
	protected final Query<?> query;
	
	/** True if Skolem terms are assigned to existentially quantified variables*/
	protected final boolean canonicalNames = true;
	
	/** The state's facts*/
	protected Collection<Predicate> facts;
	
	/** The firings that took place in this state*/
	protected FiringGraph graph;
	
	/**
	 * 
	 * @param query
	 * @param manager
	 */
	public DatabaseListState(Query<?> query, DBHomomorphismManager manager) {
		this(query, manager, query.getCanonical().getPredicates(), new MapFiringGraph());
		this.manager.addFacts(this.facts);
	}
	
	/**
	 * 
	 * @param query
	 * @param manager
	 * @param facts
	 * @param graph
	 */
	protected DatabaseListState(Query<?> query,
			DBHomomorphismManager manager,
			Collection<Predicate> facts,
			FiringGraph graph) {
		super(manager);
		Preconditions.checkNotNull(facts);
		Preconditions.checkNotNull(graph);
		this.query = query;
		this.facts = facts;
		this.graph = graph;
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
	
	/**
	 * Updates that state given the input matches
	 * @param match
	 * @return
	 */
	@Override
	public boolean chaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Predicate> created = new LinkedHashSet<>();
		for(Match match:matches) {
			//The dependency to fire
			Constraint constraint = (Constraint) match.getQuery();
			Map<Variable, Constant> mapping = match.getMapping();
			Constraint grounded = constraint.fire(mapping, this.canonicalNames);
			//The grounded left-hand side of the input dependency
			Formula left = grounded.getLeft();
			//The grounded right-hand side of the input dependency
			Formula right = grounded.getRight();
			this.facts.addAll(right.getPredicates());
			this.graph.put(constraint, Sets.newHashSet(left.getPredicates()), Sets.newHashSet(right.getPredicates()));
			created.addAll(right.getPredicates());
		}
		this.manager.addFacts(created);
		return true;
	}
	
	/**
	 * @return
	 * 		true if there is an homomorphism that extends the input match
	 */
	@Override
	public boolean isSatisfied(Match match) {
		Preconditions.checkNotNull(match);
		Map<Variable, Constant> mapping = match.getMapping();
		Constraint constraint = ((Constraint)match.getQuery());
		Map<Variable, ? extends Term> input = Utility.retain(mapping, constraint.getBothSideVariables());
		Conjunction.Builder cb = Conjunction.builder();
		for (Predicate p: constraint.getLeft().getPredicates()) {
			cb.and(p);
		}
		for (Predicate p: constraint.getRight().getPredicates()) {
			cb.and(p);
		}
		TGD tgd = new TGD((Conjunction<Predicate>) cb.build(), Conjunction.<Predicate>of());
		List<Match> matches = this.getMaches(tgd);
		Set<Variable> variables = constraint.getBothSideVariables();
		for(Match m:matches) {
			Map<Variable, Constant> map = Utility.retain(m.getMapping(), variables);
			if (map.equals(input)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return Collection<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFacts()
	 */
	@Override
	public Collection<Predicate> getFacts() {
		return this.facts;
	}

	/**
	 * @return FiringGraph<ChaseState>
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFiringGraph()
	 */
	@Override
	public FiringGraph getFiringGraph() {
		return this.graph;
	}
	
	/**
	 * @param facts Collection<? extends PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#addFacts(Collection<? extends PredicateFormula>)
	 */
	@Override
	public void addFacts(Collection<Predicate> facts) {
		this.manager.addFacts(facts);
		this.facts.addAll(facts);
	}
	
	/**
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.getFacts().equals(((DatabaseListState) o).getFacts());
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.getFacts());
	}
	
	/**
	 * @return DatabaseListState
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#clone()
	 */
	@Override
	public DatabaseListState clone() {
		return new DatabaseListState(this.query, this.manager, Sets.newHashSet(this.facts), this.graph.clone());
	}

	@Override
	public boolean isSuccessful() {
		return !this.getMatches(this.query).isEmpty();
	}

	@Override
	public boolean isFailed() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
