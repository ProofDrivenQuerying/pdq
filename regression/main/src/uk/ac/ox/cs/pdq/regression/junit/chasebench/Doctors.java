// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.regression.junit.chasebench;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import uk.ac.ox.cs.pdq.exceptions.DatabaseException;

/**
 * The test case called "Doctors" from the chasebench project.
 * 
 * <pre>
 * New test results (Stefano 2022):
 * - 10k
 *   + Internal : 8s
 *   + External : 
 *   + Logical  : 24s
 * - 100k
 *   + Internal : `java.lang.OutOfMemoryError: Required array length 2147483639 + 20 is too large` after 277s
 *   + External : 
 *   + Logical  : 
 * - 500k
 *   + Internal : 
 *   + External : 
 *   + Logical  : 
 * - 1m
 *   + Internal : 
 *   + External : 
 *   + Logical  : 
 * 
 *  Current (2019) test results (on a laptop):
 *   - case 10k mem:  out of memory.
 *   - case 10k ext:  101 sec.
 *   - case 10k log.ext.:  349 sec.
 *   
 *  Old PDQ results were (on a test hardware): 
 *   - case 10k :  timeout, all queries has 1 match except the last two  that has no results.
 *   - case 100k:  timeout
 *   - case 500k:  timeout
 *   - case 1m  :  timeout
 * 
 * </pre>
 * 
 * @author Gabor
 * @contributor Brandon Moore
 * @contributor Stefano
 */
public class Doctors extends ChaseBenchAbstract {
	// int EXPECTED_NUMBER_OF_RESULTS[] = { 73048, 73048, 73048, 73048, 73465, 292,
	// 73048, 73048, 181 };

	@Test
	public void test10kInternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "10k");
		super.testInternalDB();
	}

	@Test
	public void test10kExternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "10k");
		super.testExternalDB();
	}

	@Test
	public void test10kLogicalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "10k");
		super.testLogicalDB();
	}

	@Test
	public void test100kInternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "100k");
		super.testInternalDB();
	}

	@Test
	public void test100kExternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "100k");
		super.testExternalDB();
	}

	@Test
	public void test100kLogicalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "100k");
		super.testLogicalDB();
	}

	@Test
	public void test500kInternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "500k");
		super.testInternalDB();
	}

	@Test
	public void test500kExternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "500k");
		super.testExternalDB();
	}

	@Test
	public void test500kLogicalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "500k");
		super.testLogicalDB();
	}

	@Test
	public void test1mInternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "1m");
		super.testInternalDB();
	}

	@Test
	public void test1mExternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "1m");
		super.testExternalDB();
	}

	@Test
	public void test1mLogicalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "1m");
		super.testLogicalDB();
	}

}
