package uk.ac.ox.cs.pdq.databasemanagement.execution;

import java.util.List;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Command;
import uk.ac.ox.cs.pdq.db.Match;

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
	 * Constructor.
	 * 
	 * @param command
	 */
	public Task(Command command) {
		this.command = command;
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

	/**
	 * This is a blocking call, after the thread is registered, you can call it to
	 * wait for the results.
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	protected List<Match> getReturnValues() throws DatabaseException {
		try {
			return thread.getResultsAndReset();
		} catch (Throwable e) {
			throw new DatabaseException("Error while executing command: " + command, e);
		}
	}
}
