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
		if (parameters.getDatabaseDriver().contains("derby")) {
			this.parameters.setNumberOfThreads(1); // derby can't handle more then one connection.
		}
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
		List<Match> returnValues = new ArrayList<>();
		int poolSize = this.parameters.getNumberOfThreads();
		int executing = 3; // assume not all thread are free;
		if (poolSize > commands.size()) {
			for (Command command : commands) {
				// start each task on a different thread
				executing++;
				results.add(startTask(command));
				if (poolSize <= executing) {
					for (Task result : results) 
						returnValues.addAll(result.getReturnValues());
					results.clear();
					executing = 3;
				}
			}
			for (Task result : results) {
				// collect results
				List<Match> values = result.getReturnValues();
				returnValues.addAll(values);
			}
		} else if (poolSize<=1) {
			// forced synchronous mode
			for (Command command : commands) {
				List<Match> values = startTask(command).getReturnValues();
				returnValues.addAll(values);
			}
		}
		return returnValues;
	}

	public void execute(Command command) throws DatabaseException {
		Task ret = startTask(command);
		// must read return values even if there is no results to receive exceptions and to reset the thread state to accept more tasks.
		ret.getReturnValues();
		return;
	}
	protected Task startTask(Command command) {
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
