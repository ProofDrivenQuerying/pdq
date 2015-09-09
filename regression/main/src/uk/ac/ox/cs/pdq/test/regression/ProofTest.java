package uk.ac.ox.cs.pdq.test.regression;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.test.regression.Bootstrap.Command;

/**
 * Runs regression tests for the proof system.
 * 
 * @author Julien Leblay
 */
public class ProofTest extends RegressionTest {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(ProofTest.class);

	/** File name where input parameters must be stored in a test case directory */
	private static final String PARAMETERS_FILE = "case.properties";

	/**
	 *  File name where each key is a query file in the directory,
	 *  and its corresponding value is a boolean that specified whether the 
	 *  query stored in that file is answerable with respect to the schema
	 */
	private static final String ANSWERABILTY_FILE = "expected.properties";

	/** File name where the schema must be stored in a test case directory */
	private static final String SCHEMA_FILE = "schema.xml";

	/** File name prefix for query files in a test case directory */
	private static final String QUERY_FILE_PREFIX = "query";

	/** File name suffix for query files in a test case directory */
	private static final String QUERY_FILE_SUFFIX = ".xml";
	
	public static class ProofCommand extends Command {
		public ProofCommand() {
			super("proof");
		}

		@Override
		public void execute() throws RegressionTestException, IOException, ReflectiveOperationException {
			new ProofTest().recursiveRun(new File(getInput()));
		}
	}

	/**
	 * Runs all the test case in the given directory
	 * @param directory
	 * @return boolean
	 * @throws RegressionTestException
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	@Override
	protected boolean run(File directory) throws RegressionTestException, IOException, ReflectiveOperationException {
		boolean result = true;
		try (
				FileInputStream sis = new FileInputStream(directory.getAbsolutePath() + '/' + SCHEMA_FILE);
				FileInputStream ais = new FileInputStream(directory.getAbsolutePath() + '/' + ANSWERABILTY_FILE)
		) {
			this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");

			// Loading schema
			Schema schema = new SchemaReader().read(sis);
			RegressionParameters localParams = new RegressionParameters(new File(directory.getAbsolutePath() + '/' + PARAMETERS_FILE));

			if (schema == null) {
				this.out.println("\tSKIP: Could not read schema in " + directory.getAbsolutePath());
				return true;
			}
			// Loading queries's expected answerabilities
			Properties expected = new Properties();
			expected.load(ais);


			// Loading queries
			String[] queryFiles = directory.list(new FilenameFilter() {
				@Override public boolean accept(File dir, String name) {
					return name.startsWith(QUERY_FILE_PREFIX)
							&& name.endsWith(QUERY_FILE_SUFFIX);
				}
			});
			if (queryFiles != null && queryFiles.length > 0) {
				for (String d: queryFiles) {
					Query<?> q = null;
					File queryFile = new File(directory.getAbsolutePath() + '/' + d);
					if (queryFile.exists()) {
						try(FileInputStream qis = new FileInputStream(queryFile)) {
							q = new QueryReader(schema).read(qis);
						}
						this.out.println("\tQuery file '" + queryFile.getAbsolutePath() + "'");
						String b = expected.getProperty(d);
						if (b == null) {
							this.out.println("\tSKIP: Answerability of " + d + " not defined in " + ANSWERABILTY_FILE);
							continue;
						}
						// Running the actual test
						result &= this.run(localParams, schema, q, Boolean.valueOf(b));
					}
					
				}
			} else {
				this.out.println("Skipping '" + directory.getAbsolutePath() + "' (no query files found in directory)");
			}

		} catch (FileNotFoundException e) {
			log.debug(e);
			this.out.println("Skipping '" + directory.getAbsolutePath() + "' (not a case directory)");
		} catch (Exception e) {
			this.out.println("\tFAIL: " + directory.getAbsolutePath());
			this.out.println("\texception thrown: " + e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace(this.out);
			return false;
		}
		return result;
	}
	
	/**
	 * Runs a single case
	 * @param params
	 * @param s
	 * @param q
	 * @param expected boolean
	 * @return boolean
	 * @throws ProofException
	 * @throws EvaluationException
	 * @throws AccessException
	 */
	private boolean run(RegressionParameters params, Schema s, Query<?> q, boolean expected) {
//		try (PrintStream nullOut = new PrintStream(ByteStreams.nullOutputStream())) {
//			Reasoner solver = new Reasoner(params, new ChainedStatistics(nullOut), s, q);
//			if (expected == solver.check()) {
//				this.out.println("\tPASS");
//				return true;
//			}
//			this.out.println("\tFAIL");
			return false;
//		}
	}
}
