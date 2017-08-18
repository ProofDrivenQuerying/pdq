package uk.ac.ox.cs.pdq.runtime.exec;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.datasources.utility.BooleanResult;
import uk.ac.ox.cs.pdq.datasources.utility.Result;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters.Semantics;
import uk.ac.ox.cs.pdq.runtime.TimeoutException;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Distinct;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.runtime.util.TupleOutputLimitEnforcer;


// TODO: Auto-generated Javadoc
/**
 * Alternate executor for plans, based on the Volcano iterator model.
 * The input logical plan into a physical operator tree, 
 * and runs it in a pipelined fashion.
 * 
 * @author Julien Leblay
 * 
 */
public class PipelinedPlanExecutor implements PlanExecutor {

	/** Logger. */
	private static Logger log = Logger.getLogger(PipelinedPlanExecutor.class);

	/** The universal table. */
	private Table universalTable = null;
	
	/** The plan. */
	private final RelationalTerm plan;
	
	/** The query. */
	private final ConjunctiveQuery query;
	
	/** The semantics. */
	private final Semantics semantics;
	
	/** The event bus. */
	private EventBus eventBus;
	
	/** The timeout. */
	private Long timeout;
	
	/** The tuple limit. */
	private int tupleLimit = -1;
	
	/**
	 * Default constructor.
	 *
	 * @param plan the plan
	 * @param query the query
	 * @param sem Semantics
	 * @param timeout Long
	 */
	public PipelinedPlanExecutor(RelationalTerm plan, ConjunctiveQuery query, Semantics sem, Long timeout) {
		this.plan = plan;
		this.query = query;
		this.semantics = sem;
		this.timeout = timeout;
	}

	/**
	 * Default constructor.
	 *
	 * @param plan the plan
	 * @param query the query
	 * @param sem Semantics
	 */
	public PipelinedPlanExecutor(RelationalTerm plan, ConjunctiveQuery query, Semantics sem) {
		this(plan, query, sem, Long.MAX_VALUE);
	}

	/**
	 * Sets the event bus.
	 *
	 * @param eventBus EventBus
	 * @see uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor#setEventBus(EventBus)
	 */
	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor#execute()
	 */
	@Override
	public Result execute() throws EvaluationException {
		return this.execute(ExecutionModes.DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor#execute(uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor.ExecutionModes)
	 */
	@Override
	public Result execute(ExecutionModes mode) throws EvaluationException {
		RelationalTerm logOp = this.plan;
		// Boolean query
		if (this.query.isBoolean()) {
//			logOp = new uk.ac.ox.cs.pdq.algebra.IsEmpty(logOp);
			try (TupleIterator phyOp = PlanTranslator.translate(logOp)) {
				phyOp.open();
				return new BooleanResult(!((boolean) phyOp.next().getValue(0)));
			}
		}
		// Non-boolean query
		try (TupleIterator phyOp = PlanTranslator.translate(logOp)) {
			TupleIterator top = (this.semantics == Semantics.SET ? new Distinct(phyOp) : phyOp);
			this.universalTable = new Table(phyOp.getOutputAttributes());

			ExecutorService execService = Executors.newFixedThreadPool(1);
			execService.execute(new TimeoutChecker(this.timeout, top));
			top.setEventBus(this.eventBus);
			if (0 <= this.tupleLimit && this.tupleLimit < Integer.MAX_VALUE) {
				this.eventBus.register(new TupleOutputLimitEnforcer(top, this.tupleLimit));
			}
			top.open();
			while (top.hasNext()) {
				Tuple t = top.next();
				this.universalTable.appendRow(t);
				this.attemptPost(t);
			}
			execService.shutdownNow();
			if (top.isInterrupted()) {
				throw new TimeoutException();
			}
//			this.universalTable.setHeader(RuntimeUtilities.variablesToAttributes(
//					this.query.getFreeVariables(), this.universalTable.getType()));
		}
		return this.universalTable;
	}

	/**
	 * Attempt post.
	 *
	 * @param t Tuple
	 */
	private void attemptPost(Tuple t) {
		assert t != null : "RuntimeParameters of attemptPost cannot be null.";
		if (this.eventBus != null) {
			this.eventBus.post(t);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor#setTuplesLimit(int)
	 */
	@Override
	public void setTuplesLimit(int limit) {
		this.tupleLimit = limit;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor#setCache(boolean)
	 */
	@Override
	public void setCache(boolean doCache) {
		throw new NotImplementedException("PipelinedPlanExecutor.setCache");
	}

	/**
	 * Internal evaluation timeout mechanism.
	 * 
	 * @author Julien Leblay
	 *
	 */
	public class TimeoutChecker extends Thread {

		/** The timeout. */
		private final long timeout;
		
		/** The iterator. */
		private final TupleIterator iterator;

		/**
		 * Constructor for TimeoutChecker.
		 * @param timeout long
		 * @param i TupleIterator
		 */
		public TimeoutChecker(long timeout, TupleIterator i) {
			super("TimeoutChecker");
			this.setDaemon(true);
			this.timeout = timeout;
			this.iterator = i;
		}

		/**
		 * Run.
		 *
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (0L < this.timeout
					&& this.timeout < Long.MAX_VALUE) {
				try {
					Thread.sleep(this.timeout);
					this.iterator.interrupt();
				} catch (InterruptedException e) {
					log.debug(e);
					// Nothing to do.
				}
			}
		}
	}
}
