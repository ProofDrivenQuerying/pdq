package uk.ac.ox.cs.pdq.runtime.exec;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters.Semantics;
import uk.ac.ox.cs.pdq.runtime.TimeoutException;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Distinct;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.runtime.util.TupleOutputLimitEnforcer;
import uk.ac.ox.cs.pdq.util.BooleanResult;
import uk.ac.ox.cs.pdq.util.Result;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.eventbus.EventBus;

/**
 * Alternate executor for plans, based on the Volcano iterator model.
 * The input logical plan into a physical operator tree, 
 * and runs it in a pipelined fashion.
 * 
 * @author Julien Leblay
 * 
 */
public class VolcanoPlanExecutor implements PlanExecutor {

	/** Logger. */
	private static Logger log = Logger.getLogger(VolcanoPlanExecutor.class);

	private Table universalTable = null;
	private final Plan plan;
	private final Query<?> query;
	private final Semantics semantics;
	private EventBus eventBus;
	private Long timeout;
	private int tupleLimit = -1;
	private boolean doCache;

	/**
	 * Default constructor
	 * 
	 * @param plan
	 * @param query
	 * @param sem Semantics
	 * @param timeout Long
	 */
	public VolcanoPlanExecutor(Plan plan, Query<?> query, Semantics sem, Long timeout) {
		this.plan = plan;
		this.query = query;
		this.semantics = sem;
		this.timeout = timeout;
	}

	/**
	 * Default constructor
	 * 
	 * @param plan
	 * @param query
	 * @param sem Semantics
	 */
	public VolcanoPlanExecutor(Plan plan, Query<?> query, Semantics sem) {
		this(plan, query, sem, Long.MAX_VALUE);
	}

	/**
	 * @param eventBus EventBus
	 * @see uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor#setEventBus(EventBus)
	 */
	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.runtime.plan.PlanExecutor#execute()
	 */
	@Override
	public Result execute() throws EvaluationException {
		return this.execute(ExecutionModes.DEFAULT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.runtime.plan.PlanExecutor#execute(ExecutionModes mode)
	 */
	@Override
	public Result execute(ExecutionModes mode) throws EvaluationException {
		RelationalOperator logOp = this.plan.getEffectiveOperator();
		// Boolean query
		if (this.query.isBoolean()) {
			logOp = new uk.ac.ox.cs.pdq.algebra.IsEmpty(logOp);
			try (TupleIterator phyOp = PlanTranslator.translate(logOp)) {
				phyOp.open();
				return new BooleanResult(!((boolean) phyOp.next().getValue(0)));
			}
		}
		// Non-boolean query
		try (TupleIterator phyOp = PlanTranslator.translate(logOp)) {
			TupleIterator top = (this.semantics == Semantics.SET ? new Distinct(phyOp) : phyOp);
			this.universalTable = new Table(phyOp.getColumns());

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
			this.universalTable.setHeader(Utility.termsToAttributes(
					this.query.getFree(), this.universalTable.getType()));
		}
		return this.universalTable;
	}

	/**
	 * @param t Tuple
	 */
	private void attemptPost(Tuple t) {
		assert t != null : "RuntimeParameters of attemptPost cannot be null.";
		if (this.eventBus != null) {
			this.eventBus.post(t);
		}
	}

	@Override
	public void setTuplesLimit(int limit) {
		this.tupleLimit = limit;
	}

	@Override
	public void setCache(boolean doCache) {
		this.doCache = doCache; 
	}

	/**
	 * Internal evaluation timeout mechanism.
	 * 
	 * @author Julien Leblay
	 *
	 */
	public class TimeoutChecker extends Thread {

		private final long timeout;
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
