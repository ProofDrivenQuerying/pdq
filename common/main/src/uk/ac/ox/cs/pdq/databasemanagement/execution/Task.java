package uk.ac.ox.cs.pdq.databasemanagement.execution;

import java.util.List;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Command;
import uk.ac.ox.cs.pdq.db.Match;

/**
 * A Task is a Command that will be executed by a thread. This object helps to return values in an asynchronous way.
 * @author Gabor
 *
 */
public class Task {
	private ExecutorThread thread;
	private Command command;

	public Task(Command command) {
		this.command = command;
	}

	public ExecutorThread getExecutorThread() {
		return thread;
	}

	public void setExecutorThread(ExecutorThread executorThread) {
		this.thread = executorThread;
	}

	public Command getCommand() {
		return command;
	}

	protected List<Match> getReturnValues() throws DatabaseException {
		try {
			return thread.getResultsAndReset();
		} catch (Throwable e) {
			throw new DatabaseException("Error while executing command: " + command,e);
		}
	}
}
