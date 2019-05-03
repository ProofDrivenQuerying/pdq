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
		File testFolder = new File("../regression/test/planner/linear/fast/tpch/simple/case_002");
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
					config.getAbsolutePath(), "-o", output.getAbsolutePath(), "-ca" });
			Assert.assertTrue(r.isCaOnly());
			Assert.assertFalse(r.isVerbose());
			File generatedOutput = new File(output, "part.csv");
			Assert.assertTrue(generatedOutput.exists());
			Assert.assertTrue(generatedOutput.length() > 1);
			generatedOutput.delete();
			File generatedOutput2 = new File(output, "partsupp.csv");
			Assert.assertTrue(generatedOutput2.exists());
			generatedOutput2.delete();
			Assert.assertTrue(output.list().length == 0);

			Reason r2 = new Reason(new String[] { "-s", schema.getAbsolutePath(), "-q", query.getAbsolutePath(), "-c",
					config.getAbsolutePath(), "-o", output.getAbsolutePath() });
			Assert.assertFalse(r2.isCaOnly());
			Assert.assertTrue(output.list().length == 6);
			for (File child : output.listFiles())
				child.delete();
			output.delete();
		} catch (Exception e) {
			e.printStackTrace();
			fail("error " + e.getMessage());
		}

	}

}
