// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.reasoning.chase.state;

import org.junit.Test;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;

import java.sql.SQLException;

/**
 * (4) Create the following unit tests for EGDchaseStep
 * <p>
 * a. the facts of the chase instance are A(c_i,c_{i+1}), for i=1,...,1000 The input matches contain
 * the EGD B(x,y), B(x,y') -> y=y' and the i-th match in the input collection
 * contains the mapping {x=k1, y'=c_i, y=c_{i+1}} for i=1,...,1000 after you do
 * this operation, the database should contain only one A fact.
 * <p>
 * b. the facts of the chase instance are A(c_i,c_{i+1}), for i=1,...,1000 The
 * input matches contain the EGD B(x,y), B(x,y') -> y=y' and the i-th match in
 * the input collection contains the mapping {x=k1, y'=c_i, y=c_{i+1}} for
 * i=1,...,500 after you do this operation, the database should contain 501 A
 * facts.
 *
 * @author Gabor
 */
public class TestEGDChaseStepPostgres extends TestEGDChaseStep {

    /**
     * a. the facts of the chase instance are A(c_i,c_{i+1}), for i=1,...,1000 The
     * input matches contain the EGD B(x,y), B(x,y') -> y=y' and the i-th match in
     * the input collection contains the mapping {x=k1, y'=c_i, y=c_{i+1}} for
     * i=1,...,1000 after you do this operation, the database should contain only
     * one A fact.
     * <p>
     * Should have one in the database as a result.
     *
     * @param conn
     * @param sqlType
     * @throws DatabaseException
     * @throws SQLException
     * @throws SQLException,     DatabaseException
     */
    @Test
    public void testA_External() throws SQLException, DatabaseException {
        testA(getDatabaseConnectionExternal(s));
    }


    /**
     * b. the facts of the chase instance are A(c_i,c_{i+1}), for i=1,...,1000 The
     * input matches contain the EGD B(x,y), B(x,y') -> y=y' and the i-th match in
     * the input collection contains the mapping {x=k1, y'=c_i, y=c_{i+1}} for
     * i=1,...,500 after you do this operation, the database should contain 501 A
     * facts.
     * <p>
     * Should have 501 in the database as a result.
     *
     * @param sqlType
     * @throws SQLException
     * @throws SQLException, DatabaseException
     */

    @Test
    public void testB_External() throws SQLException, DatabaseException {
        testB(getDatabaseConnectionExternal(s));
    }

}
