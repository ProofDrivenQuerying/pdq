package uk.ac.ox.cs.pdq.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.ParametersException;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.services.ServiceManager.Actions;
import uk.ac.ox.cs.pdq.services.ServiceManager.Command;
import uk.ac.ox.cs.pdq.services.ServiceManager.ExecuteCommandCall;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

// TODO: Auto-generated Javadoc
/**
 * Bootstrapping class for start PDQ services from command line. 
 * 
 * @author Julien Leblay
 */
public class Bootstrap {

	/** Logger. */
	private static Logger log = Logger.getLogger(Bootstrap.class); 
	
	/** The Constant PROGRAM_NAME. */
	private static final String PROGRAM_NAME = "pdq-server.jar";
	
	/** The Constant THREAD_GROUP. */
	private static final String THREAD_GROUP = "PDQ-services";
	
	/** The Constant THREAD_NAME. */
	private static final String THREAD_NAME = "PDQ-server-master";
	
	/** The help. */
	@Parameter(names = { "-h", "--help" }, help = true, 
			description = "Displays this help message.")
	private boolean help;
	
	/**
	 * Checks if is help.
	 *
	 * @return true, if is help
	 */
	public boolean isHelp() {
		return this.help;
	}
	
	/** The config dir. */
	@Parameter(names = { "-c", "--config" },
			validateWith=DirectoryValidator.class,
			description =
			"Directory where to look for service configuration files. "
			+ "Default is the current diredctory.")
	private File configDir = new File(".");
	
	/**
	 * Gets the config dir.
	 *
	 * @return the config dir
	 */
	public File getConfigDir() {
		return this.configDir;
	}
	
	/**
	 * Constructor for Bootstrap.
	 * @param args String[]
	 */
	private Bootstrap(String... args) {
		JCommander jc = new JCommander(this);
		Map<String, Command> commands = new LinkedHashMap<>();
		for (Actions a: Actions.values()) {
			Command c = new Command(a);
			commands.put(a.name().toLowerCase(), c);
			jc.addCommand(a.name().toLowerCase(), c);
		}
		jc.setProgramName(Bootstrap.PROGRAM_NAME);
		try {
			jc.parse(args);
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			jc.usage();
			return;
		}
		this.loadServiceParameters(this.getConfigDir());
		Command o = commands.get(jc.getParsedCommand());
		if (o == null || this.isHelp()) {
			jc.usage();
			return;
		}
		try {
			if (o.action == Actions.START && !this.isServiceManagerUp()) {
				this.launchServiceManager(o);
			} else {
				this.sendCommands(o);
			}
		} catch (ParametersException e) {
			System.err.println(e.getMessage());
			jc.usage();
			return;
		}
	}
	
	/**
	 * Checks if is service manager up.
	 *
	 * @return true, if the 'status' command on the service manager was
	 * successful
	 */
	private boolean isServiceManagerUp() {
		return this.sendCommands(new Command(Actions.STATUS));
	}

	/**
	 * Connects to the service manager and attempts to execute the given 
	 * commands.
	 *
	 * @param commands the commands
	 * @return true if all the commands were executed successfully
	 */
	private boolean sendCommands(Command... commands) {
		ServiceManager sm = new ServiceManager(this.getConfigDir());
		boolean result = true;
		try(Socket clientSocket = new Socket(sm.getHost(), sm.getPortNumber());
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
			for (Command c : commands) {
				out.println(c);
			}
			String response = "";
			String line = in.readLine();
			while (line != null) {
				response += line + "\n";
				line = in.readLine();
			}
			try {
				int errorCode = Integer.parseInt(response);
				result &= (errorCode != ExecuteCommandCall.FAILURE);
			} catch (NumberFormatException e) {
				log.trace(response);
				result &= true;
			}
		} catch (IOException e) {
			log.trace(e);
			return false;
		}
		return result;
	}
	
	/**
	 * Launch the service manager daemon.
	 *
	 * @param commands the commands
	 */
	private void launchServiceManager(Command... commands) {
		Thread t = new Thread(
				new ThreadGroup(THREAD_GROUP),
				new ServiceManager(this.getConfigDir(), commands),
				THREAD_NAME);
		t.setDaemon(true);
		t.run();
	}
	
	/**
	 * Loads parameters files, and registered every services associated with 
	 * them.
	 *
	 * @param configDir the config dir
	 */
	private void loadServiceParameters(File configDir) {
		for (File propertiesFile: configDir.listFiles(new FileFilter() {
			@Override public boolean accept(File f) {
				return f.getName().startsWith("pdq-")
						&& f.getName().endsWith("-service.properties");
			}
		})) {
			try {
				Properties properties = new Properties();
				properties.load(new FileInputStream(propertiesFile));
				ServiceFactory.register(properties.getProperty("service_name"),
						properties.getProperty("service_class"));
			} catch (IOException e) {
				log.error("Unabled to load properties " + propertiesFile.getName(), e);
			}
		}
	}
	
	/**
	 * The Class DirectoryValidator.
	 */
	public static class DirectoryValidator implements IParameterValidator {
		
		/* (non-Javadoc)
		 * @see com.beust.jcommander.IParameterValidator#validate(java.lang.String, java.lang.String)
		 */
		@Override
		public void validate(String name, String value) throws ParameterException {
			try {
				File f = new File(value);
				if (!(f.exists() && f.isDirectory())) {
					throw new ParameterException(name + " must be a valid directory.");
				}
			} catch (Exception e) {
				throw new ParameterException(name + " must be a valid directory.");
			}
		}
		
	}
	
	/**
	 * Instantiates the bootstrap.
	 *
	 * @param args String[]
	 */
	public static void main(String... args) {
		new Bootstrap(args);
	}

	/**
	 * Gets the version.
	 *
	 * @return the version of the builders code, as given by Maven
	 */
	public static String getVersion() {
		String path = "/server.version";
		try (InputStream stream = ExplorationSetUp.class.getResourceAsStream(path)) {
			if (stream == null) {
				return "UNKNOWN";
			}
			Properties props = new Properties();
			props.load(stream);
			stream.close();
			return (String) props.get("version");
		} catch (IOException e) {
			log.warn(e);
			return "UNKNOWN";
		}
	}
}
