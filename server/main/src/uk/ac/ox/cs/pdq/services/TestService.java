package uk.ac.ox.cs.pdq.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * A dummy test service.
 */
public class TestService implements Service {

	/** Logger. */
	static Logger log = Logger.getLogger(TestService.class);

	/** The Constant DEFAULT_PORT. */
	public static final int DEFAULT_PORT = 9998;
	
	/** The Constant SERVICE_NAME. */
	public static final String SERVICE_NAME = "test";

	/** The is interrupted. */
	private boolean isInterrupted = false;

	/** The port. */
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
	 * Status.
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
	 * Gets the name.
	 *
	 * @return String
	 * @see uk.ac.ox.cs.pdq.services.Service#getName()
	 */
	@Override
	public String getName() {
		return SERVICE_NAME;
	}

	/**
	 * Run.
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
