// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.reasoning;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;

import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.reasoning.CertainAnswerExecutor;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * @author gabor tests the UserQueryExecutor in both internal db mode and
 * external db mode.
 */
public class UserQueryExecutorTest extends PdqTest {

    public UserQueryExecutorTest() {
    }

    void test(DatabaseManager db) throws DatabaseException, IOException {
        TestScenario ts = super.getScenario1();
        db.initialiseDatabaseForSchema(ts.getSchema());
        db.addFacts(ts.getExampleAtoms1());

        CertainAnswerExecutor executor = new CertainAnswerExecutor(db);
        File outputFile = new File("outputFile.csv").getAbsoluteFile();
        outputFile.delete();
        executor.findCertainAnswersQuery(ts.getQuery(), outputFile);
        Assert.assertTrue(outputFile.exists());
        Assert.assertEquals(404, outputFile.length());

    }

    void testCAFiltering(DatabaseManager db) throws DatabaseException, IOException {
        TestScenario ts = super.getScenario1();
        db.initialiseDatabaseForSchema(ts.getSchema());
        db.addFacts(ts.getExampleAtoms1());
        db.addFacts(Arrays.asList(new Atom[]{Atom.create(ts.getSchema().getRelation(2),
                new Term[]{TypedConstant.create(12), TypedConstant.create(31), UntypedConstant.create("k100")})}));

        CertainAnswerExecutor executor = new CertainAnswerExecutor(db);
        File outputFile = new File("outputFile.csv").getAbsoluteFile();
        outputFile.delete();
        executor.findCertainAnswersQuery(ts.getQuery(), outputFile);
        Assert.assertTrue(outputFile.exists());
        Assert.assertEquals(404, outputFile.length());
    }

    @After
    public void tearDown() {
        File outputFile = new File("outputFile.csv").getAbsoluteFile();
        outputFile.delete();
    }
}
