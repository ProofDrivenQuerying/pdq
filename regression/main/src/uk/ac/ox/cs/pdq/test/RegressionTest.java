package uk.ac.ox.cs.pdq.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

import jersey.repackaged.com.google.common.base.Preconditions;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.Parameters;

/**
 * Runs regression tests.
 * 
 * @author Julien Leblay
 */
public abstract class RegressionTest {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(RegressionTest.class);

	/** */
	enum Types {
		planner, kstepblocking, runtime, proof, plangen, user_driven,
		optimizations, cost, jungvis, prefusevis, dag_explorers}

	protected PrintStream out = System.out;

	/**
	 * Runs all the test case in the given directory
	 * @param directory
	 * @return boolean
	 * @throws RegressionTestException
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	protected boolean recursiveRun(File directory) throws RegressionTestException, IOException, ReflectiveOperationException {
		boolean result = true;
		boolean isLeaf = true;
		File[] files = directory.listFiles();
		Arrays.sort(files);
		for (File f: files) {
			if (!f.equals(directory) && f.isDirectory()) {
				result &= this.recursiveRun(f);
				isLeaf = false;
			}
		}
		if (isLeaf) {
			result &= this.run(directory);
		}
		return result;
	}
	
	/**
	 * Overrides the given params with the given maps entries.
	 * @param params
	 * @param overrides
	 */
	protected static void override(Parameters params, Map<String, String> overrides) {
		Preconditions.checkArgument(params != null);
		Preconditions.checkArgument(overrides != null);
		for (Map.Entry<String, String> entry: overrides.entrySet()) {
			params.set(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * @param directory File
	 * @return boolean
	 * @throws RegressionTestException
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	protected abstract boolean run(File directory) throws RegressionTestException, IOException, ReflectiveOperationException ; 
}
