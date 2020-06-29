// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.reasoning;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.reasoning.Reason;

/**
 * Tests the main interface for reasoning.
 * 
 * @author gabor
 *
 */
public class ReasonTest {

	public ReasonTest() {
	}

	/**
	 * This test will run the Reason the java on the
	 * linear/fast/tpch/simple/case_002 test case from the regression folder twice.
	 * First requesting certain answers only, secondly all answers.
	 */
	@Test
	public void testReason() {
		File testFolder = new File("../regression/test/runtime/DatabaseExamples/case_002");
		File schema = new File(testFolder, "schema.xml");
		File config = new File(testFolder, "case.properties");
		File query = new File(testFolder, "query.xml");
		File output = new File(testFolder, "results//");
		if (output.exists()) {
			for (File child : output.listFiles())
				child.delete();
		} else {
			output.mkdirs();
		}
		try {
			Reason r = new Reason(new String[] { "-s", schema.getAbsolutePath(), "-q", query.getAbsolutePath(), "-c",
					config.getAbsolutePath(), "-o", output.getAbsolutePath() });
			Assert.assertFalse(r.isVerbose());
			testAndDelete(new File(output, "part.csv"));
			testAndDelete(new File(output, "partsupp.csv"));
			testAndDelete(new File(output, "nation.csv"));
			testAndDelete(new File(output, "region.csv"));
			testAndDelete(new File(output, "region_nation.csv"));
			testAndDelete(new File(output, "supplier.csv"));

			Assert.assertTrue(output.list().length == 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("error " + e.getMessage());
		} finally {
			for (File child : output.listFiles())
				child.delete();
			output.delete();
		}

	}
	@Test
	public void testUserQueries() {
		File testFolder = new File("../regression/test/runtime/DatabaseExamples/case_002");
		File schema = new File(testFolder, "schema.xml");
		File config = new File(testFolder, "case.properties");
		File query = new File(testFolder, "query.xml");
		File output = new File(testFolder, "results//");
		if (output.exists()) {
			for (File child : output.listFiles())
				child.delete();
		} else {
			output.mkdirs();
		}
		try {
			Reason r = new Reason(new String[] { "-s", schema.getAbsolutePath(), "-q", query.getAbsolutePath(), "-c",
					config.getAbsolutePath(), "-o", output.getAbsolutePath(),"-uq", query.getAbsolutePath() });
			Assert.assertFalse(r.isVerbose());
			testAndDelete(new File(output, "part.csv"));
			testAndDelete(new File(output, "partsupp.csv"));
			testAndDelete(new File(output, "nation.csv"));
			testAndDelete(new File(output, "region.csv"));
			testAndDelete(new File(output, "region_nation.csv"));
			testAndDelete(new File(output, "supplier.csv"));
			new File(testFolder, "UserQueryResults/query.csv").delete();
			new File(testFolder, "UserQueryResults").delete();
			Assert.assertTrue(output.list().length == 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("error " + e.getMessage());
		} finally {
			for (File child : output.listFiles())
				child.delete();
			output.delete();
		}

	}

	private void testAndDelete(File file) {
		Assert.assertTrue(file.exists());
		Assert.assertTrue(file.length() > 1);
		Assert.assertTrue(file.delete());
	}

}
