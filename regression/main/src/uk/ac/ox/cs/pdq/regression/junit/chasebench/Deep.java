// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.regression.junit.chasebench;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import uk.ac.ox.cs.pdq.exceptions.DatabaseException;

/**
 * The test case called "Deep" from the chasebench project.
 * 
 * <pre>
 * New test results (Stefano 2022):
 *  - 100
 *    + Internal : 43s
 *    + External : 19s
 *    + Logical  : 61s
 *  - 200
 *    + Internal : 
 *    + External : 
 *    + Logical  : 
 *  - 300
 *    + Internal : 
 *    + External : 
 *    + Logical  : 
 * Current test result (on a laptop):
 *   - case 100:  27 seconds.
 *   - case 200:  timeout
 *   - case 300:  timeout
 * Old PDQ results were: 
 *   - case 100:  118 seconds (on test hardware).
 *   - case 200:  timeout
 *   - case 300:  timeout
 * </pre>
 * 
 * @author Gabor
 * @contributor Brandon Moore
 * @contributor Stefano
 */
public class Deep extends ChaseBenchAbstract {

	@Test
	public void test100InternalDB() throws DatabaseException, SQLException, IOException {
		init("deep", "100");
		super.testInternalDB();
	}

	@Test
	public void test100ExternalDB() throws DatabaseException, SQLException, IOException {
		init("deep", "100");
		super.testExternalDB();
	}

	@Test
	public void test100LogicalDB() throws DatabaseException, SQLException, IOException {
		init("deep", "100");
		super.testLogicalDB();
	}

	@Test
	public void test200InternalDB() throws DatabaseException, SQLException, IOException {
		init("deep", "200");
		super.testInternalDB();
	}

	@Test
	public void test200ExternalDB() throws DatabaseException, SQLException, IOException {
		init("deep", "200");
		super.testExternalDB();
	}

	@Test
	public void test200LogicalDB() throws DatabaseException, SQLException, IOException {
		init("deep", "200");
		super.testLogicalDB();
	}

	@Test
	public void test300InternalDB() throws DatabaseException, SQLException, IOException {
		init("deep", "300");
		super.testInternalDB();
	}

	@Test
	public void test300ExternalDB() throws DatabaseException, SQLException, IOException {
		init("deep", "300");
		super.testExternalDB();
	}

	@Test
	public void test300LogicalDB() throws DatabaseException, SQLException, IOException {
		init("deep", "300");
		super.testLogicalDB();
	}

}
