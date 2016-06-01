package uk.ac.ox.cs.pdq.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * A dummy test service which simply echoes whatever incoming messages it gets.
 */
public class TestService implements Service {

	/** Logger. */
	static Logger log = Logger.getLogger(TestService.class);

	/** The default port for a test service */
	public static final int DEFAULT_PORT = 9998;
	
	/** The default name for the test service is "test". */
	public static final String SERVICE_NAME = "test";

	/** A flag modeling whether the service has been interrupted. */
	private boolean isInterrupted = false;

	/** The current port of the service if changed from the default. */
	private final int port;

	/**
	 * Instantiates a new test service.
	 */
	public TestService() {
		this(DEFAULT_PORT);
	}

	/**
	 * Constructor for TestService.
	 * @param port int
	 */
	public TestService(int port) {
		this.port = port;
	}

	/**
	 * Stop.
	 *
	 * @see uk.ac.ox.cs.pdq.services.Service#stop()
	 */
	@Override
	public void stop() {
		this.isInterrupted = true;
	}

	/**
	 * Prints the status of the service.
	 *
	 * @param out PrintStream
	 * @see uk.ac.ox.cs.pdq.services.Service#status(PrintStream)
	 */
	@Override
	public void status(PrintStream out) {
		if (this.isInterrupted) {
			out.print(SERVICE_NAME + " is NOT running");
		} else {
			out.print(SERVICE_NAME + " is running.");
			out.print("Listening on port: " + this.port);
		}
	}

	/**
	 * Gets the name of the service.
	 *
	 * @return String
	 * @see uk.ac.ox.cs.pdq.services.Service#getName()
	 */
	@Override
	public String getName() {
		return SERVICE_NAME;
	}

	/**
	 * Runs the test service.
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try(Socket socket = new Socket("localhost", this.port);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
			System.out.println("Starting " + SERVICE_NAME + " on " + socket);
			String line;
			while((line = in.readLine()) != null) {
				out.print("Echo: " + line);
				System.out.println("Test echoed '" + line + "'");
			}
			System.out.println("Done.");
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}
}
