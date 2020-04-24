package uk.ac.ox.cs.pdq.test.databasemanagement;

import org.junit.Test;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;

/**
 * Tests the query differences feature of the database manager
 *
 * @author Gabor
 */
public class TestQueryDifferencesPostgres extends TestQueryDifferences {

    /**
     * In this first part of the test we create the following query difference: Left
     * query: exists[x,y](R(x,y,z) & S(x,y)) Right query:exists[x,y,z](R(x,y,z) &
     * (S(x,y) & T(z,res1,res2)))
     * <p>
     * Second part of the test: Left query: exists[x,y](R(x,y,z) & S(x,y)) Right
     * query:exists[x,y,z,res2](R(x,y,z) & (S(x,y) & T(res1,res2,z)))
     * <p>
     * The result should be all facts that only satisfy the first query, but not the
     * second one. In both cases there should be one record that matches these query
     * differences.
     *
     * @throws DatabaseException
     */
    @Test
    public void largeTableQueryDifferencePostgres() throws DatabaseException {
        largeTableQueryDifferenceTGD(DatabaseParameters.Postgres);
        largeTableQueryDifferenceEGD(DatabaseParameters.Postgres);
    }

    @Test
    public void testComplicatedQueryDifference() throws DatabaseException {
        complicatedQueryDifference(DatabaseParameters.Postgres);
    }

}
