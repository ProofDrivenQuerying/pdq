// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.regression.junit.chasebench;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import uk.ac.ox.cs.pdq.exceptions.DatabaseException;

/**
 * The test case called "DoctorsFD" from the chasebench project.
 * New test results (Stefano 2022):
 * - 10k
 *   + Internal : 1s
 *   + External : 
 *   + Logical  : 2s
 * - 100k
 *   + Internal : 15s
 *   + External :
 *   + Logical  : 24s
 * - 500k
 *   + Internal : 143s
 *   + External :
 *   + Logical  :
 * - 1m
 *   + Internal : 553s
 *   + External :
 *   + Logical  :
 * 
 * @author Gabor
 * @contributor Brandon Moore
 * @contributor Stefano
 */
public class DoctorsFD extends ChaseBenchAbstract {

	@Test
	public void test10kInternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "10k", "doctors-fd");
		super.testInternalDB();
	}

	@Test
	public void test10kExternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "10k", "doctors-fd");
		super.testExternalDB();
	}

	@Test
	public void test10kLogicalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "10k", "doctors-fd");
		super.testLogicalDB();
	}

	@Test
	public void test100kInternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "100k", "doctors-fd");
		super.testInternalDB();
	}

	@Test
	public void test100kExternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "100k", "doctors-fd");
		super.testExternalDB();
	}

	@Test
	public void test100kLogicalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "100k", "doctors-fd");
		super.testLogicalDB();
	}

	@Test
	public void test500kInternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "500k", "doctors-fd");
		super.testInternalDB();
	}

	@Test
	public void test500kExternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "500k", "doctors-fd");
		super.testExternalDB();
	}

	@Test
	public void test500kLogicalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "500k", "doctors-fd");
		super.testLogicalDB();
	}

	@Test
	public void test1mInternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "1m", "doctors-fd");
		super.testInternalDB();
	}

	@Test
	public void test1mExternalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "1m", "doctors-fd");
		super.testExternalDB();
	}

	@Test
	public void test1mLogicalDB() throws DatabaseException, SQLException, IOException {
		init("doctors", "1m", "doctors-fd");
		super.testLogicalDB();
	}

}
