package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import uk.ac.ox.cs.pdq.runtime.Bootstrap;

/** Starts the bootstrap with pre configured parameters to run the examples.
 * @author gabor
 *
 */
public class ExamplesExecutor {
	
	public static void main(String[] args) {
		
		String root = "examples/";
		
		String current = root + "example_01/";
		String myArgs[] = new String[]{
				"-d", current + "data.txt",
				"-p", current + "expected-plan.xml",
				"-q", current + "query.xml",
				"-s", current + "schema.xml",
				"-v"
		};
		Bootstrap.main(myArgs);
	}
}
