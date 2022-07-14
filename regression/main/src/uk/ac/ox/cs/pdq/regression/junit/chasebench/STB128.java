// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.regression.junit.chasebench;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import uk.ac.ox.cs.pdq.exceptions.DatabaseException;

/**
 * The test case called "STB-128" from the chasebench project.
 * <pre>
 * New test results (Stefano 2022):
 *  - 
 *    + Internal : `java.lang.ArrayIndexOutOfBoundsException` after 70s
 *    + External : 178s
 * Current test result (on a laptop):
 *   - case : runs out of memory in both internal and external cases.
 * Old PDQ results were: 
 *   - case :  timeout
 * </pre>
 * @author Gabor
 * @contributor Brandon Moore
 * @contributor Stefano
 */
public class STB128 extends ChaseBenchAbstract {
	
	@Test
	public void testInternalDB() throws DatabaseException, SQLException, IOException {
		init("STB-128", "");
		super.testInternalDB();
	}

	@Test
	public void testExternalDB() throws DatabaseException, SQLException, IOException {
		init("STB-128", "");
		super.testExternalDB();
	}

	@Test
	public void testLogicalDB() throws DatabaseException, SQLException, IOException {
		init("STB-128", "");
		super.testLogicalDB();
	}

}
