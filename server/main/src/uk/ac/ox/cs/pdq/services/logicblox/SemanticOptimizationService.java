package uk.ac.ox.cs.pdq.services.logicblox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.services.Service;
import uk.ac.ox.cs.pdq.services.ServiceCall;
import uk.ac.ox.cs.pdq.services.ServiceException;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.logicblox.common.Option;
import com.logicblox.connect.BloxCommand.Command;
import com.logicblox.connect.BloxCommand.CommandResponse;
import com.logicblox.connect.BloxCommand.LogMessageResponse;
import com.logicblox.connect.ProtoBufException.ExceptionContainer;

/**
 * A semantic-optimization service for the LogixBlox database.
 * 
 * @author Julien Leblay
 *
 */
public class SemanticOptimizationService implements Service {

	/** Logger. */
	static final Logger log = Logger.getLogger(SemanticOptimizationService.class);
	
	/**  Thread execution pool. */
	public final ExecutorService execService = Executors.newCachedThreadPool();
	
	/**  Parameters for the logicblox service. */
	private final LogicBloxParameters params;
	
	/**  Server socket. */
	private final ServerSocket serverSocket;
	
	/**  The context repository. */
	private final ContextRepository contextRepo = new ContextRepository();
	
	/** True if the service has been interrupted. */
	private boolean isInterrupted = false;

	/**
	 * Default constructor.
	 *
	 * @param configDir the directory containing the configuration file
	 */
	public SemanticOptimizationService(File configDir) {
		this(new LogicBloxParameters(configDir));
	}

	/**
	 * Constructor for SemanticOptimizationService.
	 * @param params LogicBloxParameters
	 */
	public SemanticOptimizationService(LogicBloxParameters params) {
		this.params = params;
		try {
			this.serverSocket = new ServerSocket(params.getPort());
		} catch (IOException e) {
			throw new ServiceException("Could not start " +
					params.getServiceName() + " on port " + params.getPort());
		}
	}

	/**
	 * Gets the context repository.
	 *
	 * @return ContextRepository
	 */
	public ContextRepository getContextRepository() {
		return this.contextRepo;
	}

	/**
	 * Resolves the workspace name getting the corresponding context.
	 *
	 * @param path the path
	 * @return Context
	 */
	public Context resolve(String path) {
	    return this.contextRepo.resolve(path);
	}
	
	/**
	 * Stop the service.
	 *
	 * @see uk.ac.ox.cs.pdq.services.Service#stop()
	 */
	@Override
	public void stop() {
		log.info("Stopping " + this.params.getServiceName());
		this.isInterrupted = true;
		try {
			this.serverSocket.close();
		} catch (Exception e) {
			log.trace(e);
		}
	}

	/**
	 * Status of the services.
	 *
	 * @param out PrintStream
	 * @see uk.ac.ox.cs.pdq.services.Service#status(PrintStream)
	 */
	@Override
	public void status(PrintStream out) {
		synchronized (this.serverSocket) {
			if (this.isInterrupted) {
				out.println(this.params.getServiceName() + " has been interrupted");
			} else {
				out.println(this.params.getServiceName() + " is listening on port " + this.serverSocket);
			}
		}
	}

	/**
	 * Gets the name.
	 *
	 * @return the service's name
	 * @see uk.ac.ox.cs.pdq.services.Service#getName()
	 */
	@Override
	public String getName() {
		return this.params.getServiceName();
	}

	/**
	 * Run.
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		log.info("Starting " + this.params.getServiceName() + " on port " + this.params.getPort());
		while (!this.isInterrupted) {
			try {
				this.execService.submit(
					new DefaultServiceCall(this, this.serverSocket.accept()));
			} catch (IOException e) {
				log.error(e.getMessage(),e);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
		}
		log.trace(this.params.getServiceName() + " stopped");
	}

	/**
	 * Service call that receives incoming update information from a LogicBlox
	 * instance, and handles them. Messages might be for synchronization or
	 * optimization purposes.
	 * 
	 * @author Julien Leblay
	 */
	public static class DefaultServiceCall implements ServiceCall<GeneratedMessage> {

		/** The socket. */
		private final Socket socket;
		
		/** The in. */
		private final InputStream in;
		
		/** The out. */
		private final OutputStream out;
		
		/** The master. */
		private final SemanticOptimizationService master;
		
		/**
		 * Constructor for DefaultServiceCall.
		 * @param master SemanticOptimizationService
		 * @param socket Socket
		 */
		public DefaultServiceCall(SemanticOptimizationService master, Socket socket) {
			assert !socket.isClosed();
			log.debug("Opening DefaultServiceCall on client socket " + socket);
			this.socket = socket;
			this.master = master;
			try {
				this.in = new BufferedInputStream(socket.getInputStream());
				this.out = new BufferedOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				throw new ServiceException(e);
			}
		}
		
		/**
		 * Handles the actual service call by receiving the incoming message, 
		 * delegating it to the appropriate handler and return the resulting
		 * response message.
		 *
		 * @return GeneratedMessage
		 * @throws ServiceException the service exception
		 * @throws SQLException 
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public GeneratedMessage call() throws ServiceException, SQLException {
			assert this.in != null;
			assert this.out != null;

			log.debug("Starting service call...");
			try {
				GeneratedMessage response = null;
				boolean done = false;
				while (!done) {
					response = null;
					Option<? extends GeneratedMessage> optRequest = Option.none();

					try {
						optRequest = DelimitedMessageProtocol.receive(this.in);
					} catch (final InvalidProtocolBufferException exc) {
						// format error: send message error back
						final CommandResponse.Builder builder = CommandResponse.newBuilder();
						builder.setException(ExceptionContainer.newBuilder()
								.setMessage(exc.getMessage()).build());
						response = builder.build();
						break;
					}

					if (optRequest.isSome()) {
						final GeneratedMessage req = optRequest.unwrap();
						if (!(req instanceof Command)) {
							throw new ServiceException("Unexpected message from client: " + req);
						}
						Command command = (Command) req;
						if (command.hasSynchronizeWorkspace()) {
						    log.debug("Synchronization Command: " +
						    		command.getSynchronizeWorkspace().getName());
							response = new SynchronizationHandler(this.master)
									.handle(command.getSynchronizeWorkspace());
						} else if (command.hasTopoOrderUpdate()) {
						    log.debug("TopoUpdate Command ");
							response = new TopoOrderUpdateHandler(this.master)
									.handle(command.getTopoOrderUpdate());
						} else if (command.hasRuleToOptimize()) {
						    log.debug("Optimization Command ");
							response = new OptimizationHandler(this.master, this.socket)
									.handle(command.getRuleToOptimize());
						} else if (command.hasLogMessage()) {
							response = CommandResponse.newBuilder()
									.setLogMessage(LogMessageResponse.newBuilder()
										       .setLogFilePath("STDOUT")
										       .build()).build();
						} else {
							throw new ServiceException("Unexpected message from client: " + req);
						}
					}

					if (response != null) {
						// send the response
						DelimitedMessageProtocol.send(this.out, response);
					} else {
						// null response means we're done
						done = true;
					}
				}
				log.debug("Service call complete.");
				return response;
			} catch (IOException e) {
				log.error("Communication error with client.", e);
				throw new ServiceException("Communication error with client.", e);
			} finally {
				try { this.out.close();    } catch (IOException e) { log.error(e); }
				try { this.in.close();     } catch (IOException e) { log.error(e); }
				try { this.socket.close(); } catch (IOException e) { log.error(e); }
			}
		}
	}
	
	public static boolean filterLBname(String s)
	{
		if(s.endsWith(":cond") || s.endsWith(":convert") || s.endsWith(":eq_2") || s.endsWith(":ne_2"))
			return true;
		return false;
	}
}
