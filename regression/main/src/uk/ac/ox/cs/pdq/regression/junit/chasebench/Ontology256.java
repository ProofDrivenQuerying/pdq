// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.regression.junit.chasebench;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import uk.ac.ox.cs.pdq.exceptions.DatabaseException;

/**
 * The test case called "Ontology-256" from the chasebench project.
 * 
 * <pre>
 * New test results (Stefano 2022):
 *  - 
 *    + Internal : `java.lang.ArrayIndexOutOfBoundsException` after 482s
 *    + External : 342s
 * Current test result (on a laptop):
 *   - case : Can't parse input files.  
 * Old PDQ results were: 
 *   - case :  timeout
 * </pre>
 * 
 * @author Gabor
 * @contributor Brandon Moore
 * @contributor Stefano
 */
public class Ontology256 extends ChaseBenchAbstract {

	@Test
	public void testInternalDB() throws DatabaseException, SQLException, IOException {
		init("Ontology-256", "");
		super.testInternalDB();
	}

	@Test
	public void testExternalDB() throws DatabaseException, SQLException, IOException {
		init("Ontology-256", "");
		super.testExternalDB();
	}

	@Test
	public void testLogicalDB() throws DatabaseException, SQLException, IOException {
		init("Ontology-256", "");
		super.testLogicalDB();
	}

}
