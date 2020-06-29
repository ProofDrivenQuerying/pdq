// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.reasoning;

import org.junit.Test;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager;

import java.io.IOException;

/**
 * @author gabor tests the UserQueryExecutor in both internal db mode and
 * external db mode.
 */
public class UserQueryExecutorTestPostgres extends UserQueryExecutorTest {

    @Test
    public void ExternalTest() throws DatabaseException, IOException {
        DatabaseManager db = new ExternalDatabaseManager(DatabaseParameters.Postgres);
        test(db);
    }


    @Test
    public void ExternalTestCAFiltering() throws DatabaseException, IOException {
        DatabaseManager db = new ExternalDatabaseManager(DatabaseParameters.Postgres);
        testCAFiltering(db);
    }
}
