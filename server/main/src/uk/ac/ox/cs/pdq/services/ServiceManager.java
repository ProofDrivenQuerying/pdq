package uk.ac.ox.cs.pdq.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.internal.Lists;

/**
 * Top-level service that provides the means of managing other services
 * (starting, stop, status, etc.)
 * 
 * @author Julien Leblay
 *
 */
public class ServiceManager implements Service {

	/** Logger. */
	private static Logger log = Logger.getLogger(ServiceManager.class); 

	/** External parameters */
	private final ServiceParameters params;

	/** The services currently running, indexed by name. */
	public final Map<String, Service> runningServices = new LinkedHashMap<>();
	
	/** The executors for sub-services. */
	public final ExecutorService execService = Executors.newCachedThreadPool();
	
	/***/
	private ServerSocket serverSocket;
	
	/** Verbose mode. False by default. */
	private boolean verbose = false;
	
	private boolean isInterrupted = false;

	private File configDir = new File(".");
	private final String host;
	private final Integer portNumber;
	private Command[] delayedCommands;
	
	/**
	 * Instantiates a ServiceManager, with an array of sub-services to launch
	 * along the way. Verbose mode is false.
	 * @param services
	 * @param commands
	 */
	public ServiceManager(File paramDir, Command... commands) {
		this(new ServiceParameters(paramDir), false, commands);
		this.configDir = paramDir;
	}
	
	/**
	 * Instantiates a ServiceManager, with an array of sub-services to launch
	 * along the way, with external parameters.
	 * @param params
	 * @param verbose
	 * @param services
	 */
	public ServiceManager(ServiceParameters params, boolean verbose, Command... commands) {
		this.params = params;
		this.verbose = verbose;
		this.host = "localhost";
		this.portNumber = params.getPort();
		this.delayedCommands = commands;
	}
	
	/**
	 * Execute the given commands on the service manager.
	 * @param commands
	 */
	private void execute(Command... commands) {
		for (Command command: commands) {
			log.trace("Executing " + command);
			command.execute(this);
		}
	}
	
	/**
	 * Execute the given commands on the service manager.
	 * @param commands
	 */
	private void execute(PrintStream out, Command... commands) {
		for (Command command: commands) {
			log.trace("Executing " + command);
			command.execute(this, out);
		}
	}
	
	/**
	 * @return the host on which the service manager is running.
	 */
	public String getHost() {
		return this.host;
	}
	
	/**
	 * @return the port number on which the service manager is running.
	 */
	public Integer getPortNumber() {
		return this.portNumber;
	}
	
	/**
	 * Print this service's status to the given PrintStream
	 * @param out PrintStream
	 * @see uk.ac.ox.cs.pdq.services.Service#status(PrintStream)
	 */
	@Override
	public void status(PrintStream out) {
		synchronized (this.serverSocket) {
			if (this.isInterrupted) {
				out.println(this.params.getServiceName() + " is been interrupted");
			} else {
				out.println(this.params.getServiceName() + " is listening on port " + this.serverSocket);
			}
		}
	}
	
	/**
	 * @see uk.ac.ox.cs.pdq.services.Service#stop()
	 */
	@Override
	public void stop() {
		log.info(this.params.getServiceName() + " is shutting down.");
		for (Service service: this.runningServices.values()) {
			service.stop();
		}
		this.isInterrupted = true;
		try {
			this.serverSocket.close();
		} catch (Exception e) {
			log.trace(e);
		}
		this.execService.shutdownNow();
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (this.serverSocket != null && !this.isInterrupted) {
			log.warn(this.params.getServiceName() + "-" + 
					Bootstrap.getVersion() + " is running.");
			return;
		}
		log.info("Starting " + this.params.getServiceName() + "-"
				+ Bootstrap.getVersion() + " on port " + this.params.getPort());
		try {
			this.serverSocket = new ServerSocket(this.params.getPort());
		} catch (IOException e) {
			throw new ServiceException("Could not start " +
					this.params.getServiceName() + " on port " + this.params.getPort());
		}
		this.execute(this.delayedCommands);
		while (!this.isInterrupted) {
			try {
				this.execService.submit(
					new ExecuteCommandCall(this, this.serverSocket.accept()));
			} catch (IOException e) {
				log.error(e.getMessage(),e);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
		}
		log.trace(this.params.getServiceName() + " stopped");
	}

	/**
	 * @return this service's name (as defined in external parameters).
	 * @see uk.ac.ox.cs.pdq.services.Service#getName()
	 */
	@Override
	public String getName() {
		return this.params.getServiceName();
	}
	
	public enum Actions { START, STOP, STATUS }

	/**
	 * A command to the service manager. Consists of an action and a list of
	 * modules to act on. If the module list is empty, the action is performed
	 * on the service manager itself.
	 * 
	 * @author Julien Leblay
	 *
	 */
	@Parameters(separators = ",", commandDescription = "Applies action to a list of modules.")
	public static class Command {
		public final Actions action;

		@Parameter(
//				validateWith = ModuleValidator.class,
				description = "The list of modules to act on.")
		private List<String> modules;

		public Command(Actions action, String... modules) {
			this(action, Lists.newArrayList(modules));
		}
		
		public Command(Actions action, List<String> modules) {
			this.action = action;
			this.modules = modules;
		}
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(String.valueOf(this.action).toLowerCase());
			for (String module: this.modules) {
				result.append(' ').append(module);
			}
			return result.toString();
					
		}
		
		/**
		 * Executes the command action on the list of modules. If the module 
		 * list is empty, the action is performed on the service manager itself.
		 * @param sm
		 */
		public void execute(ServiceManager sm) {
			this.execute(sm, System.out);
		}
		
		/**
		 * Executes the command action on the list of modules. If the module 
		 * list is empty, the action is performed on the service manager itself.
		 * @param sm
		 * @param out
		 */
		public void execute(ServiceManager sm, PrintStream out) {
			switch (this.action) {
			case START:
				if (this.modules.isEmpty()) {
					sm.run();
					return;
				}
				for (String name: this.modules) {
					Service service = sm.runningServices.get(name);
					if (service != null) {
						out.println("Service " +  name + " is already running.");
						continue;
					}
					service = ServiceFactory.create(sm.configDir, name);
					sm.runningServices.put(name, service);
					sm.execService.submit(service);
					out.println("Starting " +  name);
				}
				break;
			case STOP:
				if (this.modules.isEmpty()) {
					sm.stop();
					return;
				}
				for (String name: this.modules) {
					Service service = sm.runningServices.get(name);
					if (service == null) {
						out.println("No service running on " +  name + "'s port.");
						continue;
					}
					sm.runningServices.remove(name);
					service.stop();
					out.println("Stopping " +  name);
				}
				break;

			case STATUS:
				if (this.modules.isEmpty()) {
					sm.status(out);
					for (Service service: sm.runningServices.values()) {
						service.status(out);
					}
					return;
				}
				for (String name: this.modules) {
					Service service = sm.runningServices.get(name);
					if (service == null) {
						out.println("No service running on " +  name + "'s port.");
						continue;
					}
					service.status(out);
				}
				break;
			}
		}
		
		public static class ModuleValidator implements IParameterValidator {
			@Override
			public void validate(String name, String value) throws ParameterException {
				if (!ServiceFactory.isRegistered(value)) {
					throw new ParameterException("No such module '" + value + "'.\n"
							+ "Each module must have a corresponding configuration file.");
				}
			}
		}
	}
	
	public static class ExecuteCommandCall implements ServiceCall<Integer> {

		public static final Integer SUCCESS = 0;
		public static final Integer FAILURE = -1;
		
		private final Socket socket;
		private final BufferedReader in;
		private final OutputStream out;
		private final ServiceManager master;
		
		/**
		 * Constructor for ExecuteCommandCall.
		 * @param master ServiceManager
		 * @param socket Socket
		 */
		public ExecuteCommandCall(ServiceManager master, Socket socket) {
			assert !socket.isClosed();
			log.trace("Starting ExecuteCommandCall on client socket " + socket);
			this.socket = socket;
			this.master = master;
			try {
				this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				this.out = socket.getOutputStream();
			} catch (IOException e) {
				throw new ServiceException(e);
			}
		}

		private Command parse(String s) throws ParameterException {
			assert s != null;
			String[] array = s.split(" ");
			if (array == null || array.length == 0) {
				throw new ParameterException("Invalid command " + s);
			}
			try {
				Actions a = Actions.valueOf(String.valueOf(array[0]).toUpperCase());
				List<String> modules = new LinkedList<>();
				for (int i = 1; i < array.length; i++) {
					if (!ServiceFactory.isRegistered(array[i])) {
						log.warn("Unknown service " + array[i]);
						continue;
					}
					modules.add(array[i]);
				}
				return new Command(a, modules);
			} catch (Exception e) {
				//$FALL-THROUGH$
			}
			throw new ParameterException("Invalid command " + s);
		}
		
		/**
		 * @return GeneratedMessage
		 * @throws ServiceException
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public Integer call() throws ServiceException {
			assert this.in != null;

			log.trace("Starting admin call...");
			try {
			    Command command = this.parse(this.in.readLine());
			    this.master.execute(new PrintStream(this.out), command);
				log.trace("Admin call complete.");
				return SUCCESS;
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				return FAILURE;
			} finally {
				try {
					this.out.close();
				} catch (IOException e) {
					log.error(e);
				}
				try {
					this.in.close();
				} catch (IOException e) {
					log.error(e);
				}
				try {
					this.socket.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		}
	}
}
