package uk.ac.ox.cs.pdq.databasemanagement.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Command;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;

public class ExecutionManager {
	private DatabaseParameters parameters;
	private List<ExecutorThread> threads;
	private ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<>();
	protected Object TASKS_LOCK = new Object();

	public ExecutionManager(DatabaseParameters parameters) throws DatabaseException {
		this.parameters = parameters;
		threads = new ArrayList<>();
		for (int i = 0; i < this.parameters.getNumberOfThreads(); i++) {
			ExecutorThread thread = new ExecutorThread(parameters, this);
			thread.start();
			threads.add(thread);
		}
	}

	public void shutdown() {
		for (int i = 0; i < threads.size(); i++)
			threads.get(i).shutdown();
	}

	public List<Match> execute(List<Command> commands) throws DatabaseException {
		Collection<Task> results = new ArrayList<>();
		for (Command command : commands) {
			results.add(execute(command));
		}
		List<Match> returnValues = new ArrayList<>();
		for (Task result : results) {
			returnValues.addAll(result.getReturnValues());
		}
		return returnValues;
	}

	public Task execute(Command command) {
		Task task = new Task(command);
		synchronized (TASKS_LOCK) {
			// adding new task to the queue
			tasks.add(task);
			TASKS_LOCK.notify();
		}

		synchronized (task) {
			// wait until a thread takes ownership, and starts working on the task.
			if (task.getExecutorThread() == null) {
				try {
					task.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		// at this stage the results are not ready yet, but we have a thread that is responsible to populate the results
		return task;
	}

	protected ConcurrentLinkedQueue<Task> getTasks() {
		return tasks;
	}
}
