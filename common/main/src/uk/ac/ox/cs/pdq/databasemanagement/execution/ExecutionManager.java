package uk.ac.ox.cs.pdq.databasemanagement.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseParameters;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Command;
import uk.ac.ox.cs.pdq.db.Match;

/**
 * This class is responsible for executing database requests. The request have
 * to be a Command object. The execution happens in a single or multi-threaded
 * way depending on the configuration. The manager will create as many
 * ExecutorThreads as many connections we allow in the parameters.
 * 
 * @author Gabor
 *
 */
public class ExecutionManager {
	/**
	 * The database parameters, connection credentials, url, number of connections
	 */
	private DatabaseParameters parameters;
	/**
	 * The managed threads
	 */
	private List<ExecutorThread> threads;
	/**
	 * The main queue for tasks. Each Command that we want to execute will be
	 * wrapped in a Task object to be able to manage return values and exceptions.
	 */
	private ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<>();
	/**
	 * Basic lock object for synchronisation.
	 */
	protected Object TASKS_LOCK = new Object();

	/**
	 * Creates the manager. It is possible to create multiple instances, however it
	 * could be a singleton as well, since mostly we do not want to have more then
	 * one instance.
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	public ExecutionManager(DatabaseParameters parameters) throws DatabaseException {
		this.parameters = parameters;
		threads = new ArrayList<>();
		for (int i = 0; i < this.parameters.getNumberOfThreads(); i++) {
			ExecutorThread thread = new ExecutorThread(parameters, this);
			thread.start();
			threads.add(thread);
		}
	}

	/**
	 * Closes connections, and stopps the executor threads.
	 * 
	 * @throws DatabaseException
	 */
	public void shutdown() throws DatabaseException {
		for (int i = 0; i < threads.size(); i++)
			threads.get(i).shutdown();
	}

	/**
	 * Executes a list of commands. In case we have enough threads to run them all
	 * parallel it will do so, but in case the number of commands is higher then the
	 * number of threads, it will group them.
	 * 
	 * Always waits for the results, so this call is a "blocking call"
	 * 
	 * @param commands
	 * @return
	 * @throws DatabaseException
	 */
	public List<Match> execute(List<Command> commands) throws DatabaseException {
		List<Match> returnValues = new ArrayList<>();
		int poolSize = this.parameters.getNumberOfThreads();
		if (poolSize <= 1) {
			// forced synchronous mode
			for (Command command : commands) {
				List<Match> values = startTask(command,false).getReturnValues();
				returnValues.addAll(values);
			}
			return returnValues;
		}

		int executing = 1; // assume not all thread are free;
		Collection<Task> results = new ArrayList<>();
		for (Command command : commands) {
			// start each task on a different thread
			executing++;
			results.add(startTask(command,false));
			if (poolSize <= executing) {
				for (Task result : results)
					returnValues.addAll(result.getReturnValues());
				results.clear();
				executing = 1;
			}
		}
		for (Task result : results) {
			// collect results
			List<Match> values = result.getReturnValues();
			returnValues.addAll(values);
		}
		return returnValues;
	}
	
	public List<String> executeGeneric(Command command) throws DatabaseException {
		return startTask(command, true).getGenericReturnValues();
	}

	/**
	 * Executes a single command and waits for the results.
	 * 
	 * @param command
	 * @throws DatabaseException
	 */
	public void execute(Command command) throws DatabaseException {
		Task ret = startTask(command,false);
		// must read return values even if there is no results to receive exceptions and
		// to reset the thread state to accept more tasks.
		ret.getReturnValues();
		return;
	}

	/**
	 * Submits a new command to the queue, and creates a wrapper object to be able
	 * to retrieve the results later. Non-blocking call, it will return immediately
	 * after the job request is submitted.
	 * 
	 * Call the Task.getReturnValues() function to wait for the results.
	 * 
	 * @param command
	 * @return
	 */
	protected Task startTask(Command command, boolean isGeneric) {
		Task task = new Task(command, isGeneric);
		synchronized (TASKS_LOCK) {
			// adding new task to the queue
			tasks.add(task);
			TASKS_LOCK.notify();
		}

		synchronized (task) {
			// wait until a thread takes ownership, and starts working on the task.
			while (task.getExecutorThread() == null) {
				try {
					task.wait(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		// at this stage the results are not ready yet, but we have a thread that is
		// responsible to populate the results
		return task;
	}

	/**
	 * This is the main function that the executor threads can use to access the task queue.
	 * @return
	 */
	protected ConcurrentLinkedQueue<Task> getTasks() {
		return tasks;
	}

}
