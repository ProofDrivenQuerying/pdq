package uk.ac.ox.cs.pdq.reasoningdatabase.execution;

import java.util.List;

import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.resoningdatabase.sqlcommands.BasicSelect;
import uk.ac.ox.cs.pdq.resoningdatabase.sqlcommands.Command;

/**
 * A Task is a Command that will be executed by an ExecutorThread. This object
 * helps to return values in an asynchronous way.
 * 
 * @author Gabor
 *
 */
public class Task {
	/**
	 * Allocated thread. It will be set by the thread who got this task.
	 */
	private ExecutorThread thread;
	/**
	 * The SQL command that we want to be executed.
	 */
	private Command command;

	/**
	 * Flag to indicate when the current task is completed or failed.
	 */
	private boolean isFinished = false;
	/**
	 * A finished task has either results or resultException. The result could be a number of updated records, but it is not implemented yet, or results of a query.
	 */
	private List<Match> queryResults;
	/**
	 * Same as above but for generic commands, such as ExplainQuery.
	 */
	private List<String> genericResults;
	/**
	 * When the database provider returns an exception instead of data.
	 */
	private Throwable resultException;

	private boolean isQuery = false;

	protected Object RESULTS_LOCK = new Object();
	/**
	 * Specifies if we want List<Match> type results or the generic List<String> type results.
	 */
	private boolean isGeneric=false;

	/**
	 * Constructor.
	 * 
	 * @param command
	 */
	public Task(Command command,boolean isGeneric) {
		this.command = command;
		this.isGeneric = isGeneric;
		isQuery = command instanceof BasicSelect;
	}

	/**
	 * Returns the executor thread
	 * 
	 * @return
	 */
	public ExecutorThread getExecutorThread() {
		return thread;
	}

	/**
	 * The Executor will call this to register itself as the one executing the
	 * command.
	 * 
	 * @param executorThread
	 */
	public void setExecutorThread(ExecutorThread executorThread) {
		this.thread = executorThread;
	}

	/**
	 * The executor thread will receive the task, and need to get the SQL command to
	 * execute.
	 * 
	 * @return
	 */
	public Command getCommand() {
		return command;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	protected void setResults(List<Match> results, Throwable exceptionThrown) {
		synchronized (RESULTS_LOCK) {
			this.queryResults = results;
			this.resultException = exceptionThrown;
			this.isFinished = true;
			// wake up the one waiting for the results.
			RESULTS_LOCK.notify();
		}
	}

	protected void setGenericResults(List<String> results, Throwable exceptionThrown) {
		synchronized (RESULTS_LOCK) {
			this.genericResults = results;
			this.resultException = exceptionThrown;
			this.isFinished = true;
			// wake up the one waiting for the results.
			RESULTS_LOCK.notify();
		}
	}

	public boolean isQuery() {
		return isQuery;
	}

	/**
	 * This is a blocking call, after the thread is registered, you can call it to
	 * wait for the results.
	 * 
	 * Returns the results from the latest execution. Throws exception in case the
	 * last execution was causing an exception.
	 * 
	 * Resets the thread state, so it can start working on the next task.
	 * 
	 * @return
	 * @throws Throwable
	 */
	public List<Match> getReturnValues() throws DatabaseException {
		waitUntilFinished();
		// give results and reset status.
		synchronized (RESULTS_LOCK) {
			try {
				if (resultException != null) {
					throw new DatabaseException("Error while executing command: " + command, resultException);
				}
				List<Match> ret = this.queryResults;
				return ret;
			} finally {
				this.queryResults = null;
				this.isFinished = false;
				RESULTS_LOCK.notify(); // wake up the thread and continue executing tasks.
			}
		}
	}

	private void waitUntilFinished() {
		boolean isFinished = false;
		// wait for results to be ready.
		while (!isFinished) {
			synchronized (RESULTS_LOCK) {
				isFinished = this.isFinished;
				try {
					if (!isFinished)
						RESULTS_LOCK.wait(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public List<String> getGenericReturnValues() throws DatabaseException {
		waitUntilFinished();
		// give results and reset status.
		synchronized (RESULTS_LOCK) {
			try {
				if (resultException != null) {
					throw new DatabaseException("Error while executing command: " + command, resultException);
				}
				List<String> ret = this.genericResults;
				return ret;
			} finally {
				this.queryResults = null;
				this.isFinished = false;
				RESULTS_LOCK.notify(); // wake up the thread and continue executing tasks.
			}
		}
	}

	public boolean isGeneric() {
		return isGeneric;
	}

}
