package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.LimitReachedException.Reasons;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
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
public class DatabaseRestrictedState extends DatabaseChaseState implements ListState {

	/** The _is failed. */
	private boolean _isFailed = false;

	/**  Keeps the classes of equal constants *. */
	protected EqualConstantsClasses constantClasses;
	
	/** The canonical names. */
	protected final boolean canonicalNames = true;
	
	/** Number of parallel threads. **/
	protected final int parallelThreads = 50;

	protected final long timeout = 3600000;

	protected final TimeUnit unit = TimeUnit.MILLISECONDS;
	
	//TODO Hack think of a better placing of this 
	protected Collection<Predicate> latestPredicates = Sets.newHashSet();
	
	/**
	 * Instantiates a new database list state.
	 *
	 * @param manager the manager
	 * @param facts the facts
	 */
	public DatabaseRestrictedState(
			DBHomomorphismManager manager,
			Collection<Atom> facts) {
		this(manager, facts, inferEqualConstantsClasses(facts));
	}

	/**
	 * Instantiates a new database list state.
	 *
	 * @param manager the manager
	 * @param facts the facts
	 * @param graph the graph
	 * @param constantClasses the constant classes
	 */
	protected DatabaseRestrictedState(
			DBHomomorphismManager manager,
			Collection<Atom> facts,
			EqualConstantsClasses constantClasses
			) {
		super(manager);
		Preconditions.checkNotNull(facts);
		this.constantClasses = constantClasses;
		this.manager.addFactsSynchronously(facts);
	}
	
	/**
	 * Infer equal constants classes.
	 *
	 * @param facts the facts
	 * @return the equal constants classes
	 */
	public static EqualConstantsClasses inferEqualConstantsClasses(Collection<Atom> facts) {
		EqualConstantsClasses constantClasses = new EqualConstantsClasses();
		for(Atom fact:facts) {
			if(fact instanceof Equality) {
				constantClasses.add((Equality) fact);
			}
		}
		return constantClasses;
	}
	
	/**
	 * Updates that state given the input match. 
	 *
	 * @param match the match
	 * @return true, if successful
	 */
	@Override
	public boolean chaseStep(Match match) {	
		return this.chaseStep(Sets.newHashSet(match));
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#chaseStep(java.util.Collection)
	 */
	@Override
	public boolean chaseStep(Collection<Match> matches) {
		
		//Start multiple threads which will consume the input matches
		ExecutorService executorService = null;
		try {
			//Create a pool of threads to run in parallel
			executorService = Executors.newFixedThreadPool(this.parallelThreads);
			List<ExecuteTGDChaseStepThread> threads = new ArrayList<>();
			for(int j = 0; j < this.parallelThreads; ++j) {
				//Create the threads that will run the database update statements
				threads.add(new ExecuteTGDChaseStepThread((Queue<Match>) matches, this.manager.clone()));
				//TODO cleanup the threads after cloning
			}
			long start = System.currentTimeMillis();
			try {
				for(Future<Boolean> output:executorService.invokeAll(threads, this.timeout, this.unit)){
					output.get();
				}
				for(ExecuteTGDChaseStepThread thread:threads) {
					this.latestPredicates.addAll(thread.getObservedPredicates());
				}
			} catch(java.util.concurrent.CancellationException e) {
				executorService.shutdownNow();
				if (this.timeout <= (System.currentTimeMillis() - start)) {
					try {
						throw new LimitReachedException(Reasons.TIMEOUT);
					} catch (LimitReachedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			executorService.shutdown();
		} catch (InterruptedException | ExecutionException e) {
			executorService.shutdownNow();
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Gets the constant classes.
	 *
	 * @return the constant classes
	 */
	public EqualConstantsClasses getConstantClasses() {
		return this.constantClasses;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#isFailed()
	 */
	@Override
	public boolean isFailed() {
		return this._isFailed;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#isSuccessful(uk.ac.ox.cs.pdq.fol.Query)
	 */
	@Override
	public boolean isSuccessful(Query<?> query) {
		return !this.getMatches(query).isEmpty();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFiringGraph()
	 */
	@Override
	public FiringGraph getFiringGraph() {
		throw new java.lang.UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFacts()
	 */
	@Override
	public Collection<Atom> getFacts() {
		throw new java.lang.UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#merge(uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState)
	 */
	@Override
	public ChaseState merge(ChaseState s) {
		throw new java.lang.UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ListState#addFacts(java.util.Collection)
	 */
	@Override
	public void addFacts(Collection<Atom> facts) {
		this.manager.addFactsSynchronously(facts);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState#clone()
	 */
	@Override
	public DatabaseRestrictedState clone() {
//		return new DatabaseRestrictedState(this.manager, Sets.newHashSet(this.facts), this.constantClasses.clone());
		throw new java.lang.UnsupportedOperationException();
	}	


	/**
	 * Calls the manager to detect homomorphisms of the input query to facts in this state.
	 * The manager detects homomorphisms using a database backend.
	 * @param query Query
	 * @return List<Match>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#getMatches(Query)
	 */
	@Override
	public List<Match> getMatches(Query<?> query) {
		return this.manager.getMatches(
				Lists.<Query<?>>newArrayList(query),
//				HomomorphismConstraint.createTopKConstraint(1),
				HomomorphismProperty.createFactProperty(Conjunction.of(this.getFacts())),
				HomomorphismProperty.createMapProperty(query.getFreeToCanonical()));
	}
	
	/**
	 * Calls the manager to detect homomorphisms of the input query to facts in this state.
	 * The manager detects homomorphisms using a database backend.
	 *
	 * @param query Query
	 * @param constraints the constraints
	 * @return List<Match>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#getMatches(Query)
	 */
	@Override
	public List<Match> getMatches(Query<?> query, HomomorphismProperty... constraints) {
		HomomorphismProperty[] c = new HomomorphismProperty[constraints.length+1];
		System.arraycopy(constraints, 0, c, 0, constraints.length);
		c[constraints.length] = HomomorphismProperty.createFactProperty(Conjunction.of(this.getFacts()));
		return this.manager.getMatches(Lists.<Query<?>>newArrayList(query), c);
	}

	/**
	 * Calls the manager to detect homomorphisms of the input dependencies to facts in this state.
	 * The manager detects homomorphisms using a database backend.
	 * @param dependencies Collection<D>
	 * @param constraints HomomorphismConstraint...
	 * @return Map<D,List<Match>>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#getHomomorphisms(Collection<D>)
	 */
	@Override
	public List<Match> getMatches(Collection<? extends Constraint> dependencies, HomomorphismProperty... constraints) {
		return this.manager.getMatches(dependencies, constraints);
	}

	
	public Collection<Predicate> getLatestPredicates() {
		return this.latestPredicates;
	}
}
