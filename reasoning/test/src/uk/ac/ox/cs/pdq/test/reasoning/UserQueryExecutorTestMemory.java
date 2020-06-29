// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.reasoning;

import org.junit.Test;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.InternalDatabaseManager;

import java.io.IOException;

/**
 * @author gabor tests the UserQueryExecutor in both internal db mode and
 *         external db mode.
 */
public class UserQueryExecutorTestMemory extends UserQueryExecutorTest {

	@Test
	public void internalTest() throws DatabaseException, IOException {
		DatabaseManager db = new InternalDatabaseManager();
		test(db);
	}

	@Test
	public void internalTestCAFiltering() throws DatabaseException, IOException {
		DatabaseManager db = new InternalDatabaseManager();
		testCAFiltering(db);
	}
}
