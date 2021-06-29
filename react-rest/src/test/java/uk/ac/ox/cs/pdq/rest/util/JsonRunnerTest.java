package uk.ac.ox.cs.pdq.rest.util;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.rest.Controller;
import uk.ac.ox.cs.pdq.rest.jsonobjects.run.RunResults;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JsonRunnerTest {

    Controller controller = new Controller();
    int testId = 0; // Change ID to change which schema will get tested
    Schema testSchema = controller.getSchemaList().get(testId);
    ConjunctiveQuery testCq = controller.getCommonQueries().get(testId).get(testId);
    String testSQL = "SELECT a0.activity_comment\n" +
            "FROM activityFree AS a0\n" +
            "WHERE a0.target_pref_name='20'\n" +
            "AND a0.uo_units='15'";
    File testProperties = controller.getCasePropertyList().get(testId);
    RelationalTerm testPlanRT = controller.plan(testId, testId, testSQL).getPlan();


    @Test
    public void testRuntime() {
        RunResults actual = JsonRunner.runtime(testSchema, testCq, testProperties, testPlanRT);
        Assert.assertNotNull(actual);
    }

    @Test
    public void testEvaluatePlan() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = JsonRunner.class.getDeclaredMethod("evaluatePlan", RelationalTerm.class, Schema.class);
        m.setAccessible(true);

        Object actual =  m.invoke(null, testPlanRT, testSchema);
        Assert.assertNotNull(actual);
    }

}