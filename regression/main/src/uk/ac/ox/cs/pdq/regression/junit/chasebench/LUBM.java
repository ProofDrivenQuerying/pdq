// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.regression.junit.chasebench;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import uk.ac.ox.cs.pdq.exceptions.DatabaseException;

/**
 * The test case called "LUBM" from the chasebench project.
 * 
 * <pre>
 * New test results (Stefano 2022):
 *  - 001
 *    + Internal : 14s
 *    + External : 15s
 *    + Logical  : 40s
 *  - 010
 *    + Internal : `java.lang.ArrayIndexOutOfBoundsException` after 1110s
 *    + External : 124s
 *    + Logical  : 
 *  - 100
 *    + Internal : 
 *    + External : 
 *    + Logical  : 
 *  - 01k
 *    + Internal : 
 *    + External : 
 *    + Logical  : 
 * Current test result (on a laptop):
 *   - case 001:  timeout
 *   - case 010:  timeout
 *   - case 100:  timeout
 *   - case 01k:  timeout
 * Old PDQ results were: 
 *   - case 001:  timeout
 *   - case 010:  timeout
 *   - case 100:  timeout
 *   - case 01k:  timeout
 * </pre>
 * 
 * @author Gabor
 * @contributor Brandon Moore
 * @contributor Stefano
 */
public class LUBM extends ChaseBenchAbstract {

	@Test
	public void test001InternalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "001");
		super.testInternalDB();
	}

	@Test
	public void test001ExternalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "001");
		super.testExternalDB();
	}

	@Test
	public void test001LogicalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "001");
		super.testLogicalDB();
	}

	@Test
	public void test010InternalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "010");
		super.testInternalDB();
	}

	@Test
	public void test010ExternalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "010");
		super.testExternalDB();
	}

	@Test
	public void test010LogicalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "010");
		super.testLogicalDB();
	}

	@Test
	public void test100InternalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "100");
		super.testInternalDB();
	}

	@Test
	public void test100ExternalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "100");
		super.testExternalDB();
	}

	@Test
	public void test100LogicalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "100");
		super.testLogicalDB();
	}

	@Test
	public void test01kInternalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "01k");
		super.testInternalDB();
	}

	@Test
	public void test01kExternalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "01k");
		super.testExternalDB();
	}

	@Test
	public void test01kLogicalDB() throws DatabaseException, SQLException, IOException {
		init("LUBM", "01k");
		super.testLogicalDB();
	}

}
