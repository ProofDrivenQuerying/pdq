package uk.ac.ox.cs.pdq.materialize.chase.state;

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
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.materialize.factmanager.FactManager;
import uk.ac.ox.cs.pdq.materialize.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.materialize.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.materialize.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.materialize.utility.Match;

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
public class DatabaseDiskState implements ListState {

	/**  Queries and updates the database of facts *. */
	protected FactManager manager;
	
	protected HomomorphismDetector detector;
	
	/** The _is failed. */
	private boolean _isFailed = false;

	/**  Keeps the classes of equal constants *. */
	protected EqualConstantsClasses constantClasses;
	
	/** The canonical names. */
	protected final boolean canonicalNames = true;
	
	/** Number of parallel threads. **/
	protected final int parallelThreads = 2;

	protected final long timeout = 3600000;

	protected final TimeUnit unit = TimeUnit.MILLISECONDS;
	
	protected List<FactManager> managers;
	
	protected Collection<Predicate> recentAtoms = Sets.newHashSet();

	/**
	 * Instantiates a new database list state.
	 *
	 * @param manager the manager
	 * @param facts the facts
	 * @param graph the graph
	 * @param constantClasses the constant classes
	 */
	public DatabaseDiskState(
			FactManager manager,
			HomomorphismDetector detector
			) {
		Preconditions.checkNotNull(manager);
		Preconditions.checkNotNull(detector);
		this.manager = manager;
		this.detector = detector;
		this.constantClasses = new EqualConstantsClasses();
		this.managers = Lists.newArrayList();
		for(int i = 0; i < this.parallelThreads; ++i) {
			this.managers.add(manager.clone());
		}
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
				threads.add(new ExecuteTGDChaseStepThread((Queue<Match>) matches, this.managers.get(j)));
			}
			long start = System.currentTimeMillis();
			try {
				for(Future<Boolean> output:executorService.invokeAll(threads, this.timeout, this.unit)){
					output.get();
				}
				for(ExecuteTGDChaseStepThread thread:threads) {
					this.recentAtoms.addAll(thread.getObservedPredicates());
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
	public boolean isSuccessful(ConjunctiveQuery query) {
		return !this.getMatches(query).isEmpty();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFacts()
	 */
	@Override
	public Collection<Atom> getFacts() {
		throw new java.lang.UnsupportedOperationException();
	}


	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ListState#addFacts(java.util.Collection)
	 */
	@Override
	public void addFacts(Collection<Atom> facts) {
		this.manager.addFactsSynchronously(facts);
	}	


	/**
	 * Calls the manager to detect homomorphisms of the input query to facts in this state.
	 * The manager detects homomorphisms using a database backend.
	 * @param query Query
	 * @return List<Match>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#getMatches(Query)
	 */
	@Override
	public List<Match> getMatches(ConjunctiveQuery query) {
		return this.detector.getMatches(
				Lists.<Query<?>>newArrayList(query),
				HomomorphismProperty.createMapProperty(query.getGroundingsProjectionOnFreeVars()));
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
	public List<Match> getMatches(ConjunctiveQuery query, HomomorphismProperty... constraints) {
		HomomorphismProperty[] c = new HomomorphismProperty[constraints.length+1];
		System.arraycopy(constraints, 0, c, 0, constraints.length);
		return this.detector.getMatches(Lists.<Query<?>>newArrayList(query), constraints);
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
		return this.detector.getMatches(dependencies, constraints);
	}

	
	public Collection<Predicate> getRecentAtoms() {
		return this.recentAtoms;
	}
	
	/**
	 * Gets the manager.
	 *
	 * @return DBHomomorphismManager
	 */
	public FactManager getManager() {
		return this.manager;
	}

	/**
	 * Sets the manager.
	 *
	 * @param manager DBHomomorphismManager
	 */
	public void setManager(FactManager manager) {
		this.manager = manager;
	}
}
