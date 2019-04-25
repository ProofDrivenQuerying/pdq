package uk.ac.ox.cs.pdq.test.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import uk.ac.ox.cs.pdq.runtime.Runtime;
import java.io.File;

import org.junit.Test;

/**
 *  Tests the Runtime main entry point by executing a plan and checking the output.
 * @author gabor
 *
 */
public class RuntimeTest {

	@Test
	public void test() {
		File testFolder = new File("../regression/test/planner/linear/fast/tpch/simple/case_001");
		File schema = new File(testFolder,"schema.xml");
		File plan = new File(testFolder,"expected-plan.xml");
		File access = new File(testFolder,"accessesMem");
		try {
			Runtime r = new Runtime(new String[] {"-s",schema.getAbsolutePath(),"-p", plan.getAbsolutePath(), "-a", access.getAbsolutePath()});
			assertEquals(200000, r.getTupleCount());
		} catch (Exception e) {
			e.printStackTrace();
			fail("error " + e.getMessage() );
		}
		
	}

}
