package uk.ac.ox.cs.pdq.test.planner;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.ParametersException;
import uk.ac.ox.cs.pdq.test.planner.PlannerTest.PlannerTestCommand;
import uk.ac.ox.cs.pdq.test.planner.RuntimeTest.RuntimeTestCommand;
import uk.ac.ox.cs.pdq.test.planner.UserPlannerTest.UserPlannerTestCommand;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

/**
 * Runs regression tests.
 * 
 * @author Julien Leblay
 */
public class Bootstrap {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(Bootstrap.class);
	
	/** Program execution command (to appear un usage message). */
	public static final String PROGRAM_NAME = "java -jar pdq-regression.jar";

	/** Default error code. */
	public static final int ERROR_CODE = -1;

	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;
	
	protected PrintStream out;

	/**
	 * Sets up a regression test for the given test case directory.
	 * 
	 * @param args the command line parameters as given by the main method.
	 * @throws ReflectiveOperationException
	 * @throws IOException
	 * @throws RegressionTestException
	 */
	public Bootstrap(String... args) {

		JCommander jc = new JCommander(this);
		Map<String, Command> commands = new LinkedHashMap<>();
		{
			Command c = new PlannerTestCommand();
			commands.put(c.name, c);
			jc.addCommand(c.name, c);

			c = new RuntimeTestCommand();
			commands.put(c.name, c);
			jc.addCommand(c.name, c);
			
			c = new UserPlannerTestCommand();
			commands.put(c.name, c);
			jc.addCommand(c.name, c);
		}
		jc.setProgramName(Bootstrap.PROGRAM_NAME);
		try {
			jc.parse(args);
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			jc.usage();
			return;
		}
		if (this.isHelp()) {
			jc.usage();
			return;
		}
		Command o = commands.get(jc.getParsedCommand());
		if (o == null) {
			jc.usage();
			return;
		}
		try {
			o.execute();
		} catch (RegressionTestException | IOException
				| ReflectiveOperationException |ParametersException e) {
			log.error(e.getMessage());
			return;
		}
	}
	
	/**
	 * A command to the regression test bootstrap. Consists of an action and a 
	 * associated parameters. 
	 * 
	 * @author Julien Leblay
	 *
	 */
	public static abstract class Command {

		public final String name;
		
		@Parameter(names = { "-i", "--input" }, required = true,
				description = "Path to the regression test case directories.",
				validateWith=DirectoryValidator.class)
		private String input;

		@DynamicParameter(names = "-D", required = false, 
				description = "Force the given parameters across all the test in the suite, "
						+ "ignoring those that may be specified in each parameter file. "
						+ "For instance, '-Dtimeout=10000' force a timeout 10s seconds on all tests.")
		private Map<String, String> params = new HashMap<>();
		
		public Command(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(name);
			result.append(' ').append(input);
			return result.toString();
					
		}

		/**
		 * @return the path the initialConfig file to use.
		 */
		public String getInput() {
			return this.input;
		}

		/**
		 * @param input String
		 */
		public Map<String, String> getParameterOverrides() {
			return this.params;
		}

		/**
		 * Executes the command action on the list of modules. If the module 
		 * list is empty, the action is performed on the service manager itself.
		 * @param sm
		 * @throws ReflectiveOperationException 
		 */
		public abstract void execute() throws RegressionTestException, IOException, ReflectiveOperationException;
	}

	public static class DirectoryValidator implements IParameterValidator {
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
	 * @return true if the line command asked for help.
	 */
	public boolean isHelp() {
		return this.help;
	}

	/**
	 * @param help
	 */
	public void setHelp(boolean help) {
		this.help = help;
	}

	/**
	 * Instantiates an experiment and runs it.
	 * @param args
	 */
	public static void main(String... args) {
		new Bootstrap(args);
	}
}
