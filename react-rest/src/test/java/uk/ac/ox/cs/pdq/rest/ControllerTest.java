package uk.ac.ox.cs.pdq.rest;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.ac.ox.cs.pdq.rest.jsonobjects.plan.Plan;
import uk.ac.ox.cs.pdq.rest.jsonobjects.run.RunResults;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.Dependencies;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.RelationArray;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.SchemaArray;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.HashMap;

public class ControllerTest {
    private final Controller underTest = new Controller();

    @Test
    public void contextLoads () {
        Assertions.assertThat(underTest).isNotNull();
    }

    @Test
    public void testInitSchemas() {
        SchemaArray actual = underTest.initSchemas();
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.schemas.length > 0);
    }

    @Test
    public void testGetRelations() {
        final int id = 0;
        RelationArray actual = underTest.getRelations(id);
        Assert.assertNotNull(actual);
        Assert.assertEquals(id, actual.id);
        Assert.assertNotNull(actual.name);
        Assert.assertTrue(actual.relations.length > 0);
    }

    @Test
    public void testGetDependencies() {
        final int id = 0;
        Dependencies actual = underTest.getDependencies(id);
        Assert.assertNotNull(actual);
        Assert.assertEquals(id, actual.id);
        Assert.assertNotNull(actual.EGDDependencies);
        Assert.assertNotNull(actual.TGDDependencies);
    }

    @Test
    public void testVerifyQuery() {
        final Integer schemaID = 0;
        final Integer queryID = 0;
        final String SQL = "SELECT a0.activity_comment\n" +
                "FROM activityFree AS a0\n" +
                "WHERE a0.target_pref_name='20'\n" +
                "AND a0.uo_units='15'";
        boolean actual = underTest.verifyQuery(schemaID, queryID, SQL);
        Assert.assertTrue(actual);
    }

    @Test
    public void testPlan() {
        final Integer schemaID = 0;
        final Integer queryID = 0;
        final String SQL = "SELECT a0.activity_comment\n" +
                "FROM activityFree AS a0\n" +
                "WHERE a0.target_pref_name='20'\n" +
                "AND a0.uo_units='15'";
        Plan actual = underTest.plan(schemaID, queryID, SQL);
        Assert.assertNotNull(actual);
        Assert.assertNotNull(actual.bestPlan);
        Assert.assertNotNull(actual.graphicalPlan);
    }

    @Test
    public void testDownloadPlan() {
        final Integer schemaID = 0;
        final Integer queryID = 0;
        final String SQL = "SELECT a0.activity_comment\n" +
                "FROM activityFree AS a0\n" +
                "WHERE a0.target_pref_name='20'\n" +
                "AND a0.uo_units='15'";
        // Have to plan first to make sure the plan.xml exists
        Plan plan = underTest.plan(schemaID, queryID, SQL);

        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        ResponseEntity<Resource> actual = underTest.downloadPlan(schemaID, queryID, SQL, httpServletRequest);
        Assert.assertNotNull(actual);
        Assert.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assert.assertNotNull(actual.getBody());
    }

    @Test
    public void testRun() {
        final Integer schemaID = 0;
        final Integer queryID = 0;
        final String SQL = "SELECT a0.activity_comment\n" +
                "FROM activityFree AS a0\n" +
                "WHERE a0.target_pref_name='20'\n" +
                "AND a0.uo_units='15'";
        // Have to plan first to make sure the plan.xml exists for the run
        Plan plan = underTest.plan(schemaID, queryID, SQL);
        RunResults actual = underTest.run(schemaID, queryID, SQL);
        Assert.assertNotNull(actual);
        Assert.assertNotNull(actual.results);
        Assert.assertNotNull(actual.table);
    }

    @Test
    public void testDownloadRun() {
        final Integer schemaID = 0;
        final Integer queryID = 0;
        final String SQL = "SELECT a0.activity_comment\n" +
                "FROM activityFree AS a0\n" +
                "WHERE a0.target_pref_name='20'\n" +
                "AND a0.uo_units='15'";
        // Have to plan first to make sure the plan.xml exists for the run
        Plan plan = underTest.plan(schemaID, queryID, SQL);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        ResponseEntity<Resource> actual = underTest.downloadRun(schemaID, queryID, SQL, httpServletRequest);
        Assert.assertNotNull(actual);
        Assert.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assert.assertNotNull(actual.getBody());
    }

    @Test
    public void testLoadFileAsResource() throws NoSuchFieldException, IllegalAccessException {
        // Get private paths field from the Controller class to use in our test

        Field pathsField = underTest.getClass().getDeclaredField("paths");
        pathsField.setAccessible(true);
        HashMap<Integer, String> paths = (HashMap<Integer, String>) pathsField.get(underTest);

        final Integer schemaID = 0;
        final Integer queryID = 0;
        final String SQL = "SELECT a0.activity_comment\n" +
                "FROM activityFree AS a0\n" +
                "WHERE a0.target_pref_name='20'\n" +
                "AND a0.uo_units='15'";
        // Have to plan and run first to make sure the resources exist for the run
        Plan plan = underTest.plan(schemaID, queryID, SQL);
        RunResults run = underTest.run(schemaID, queryID, SQL);

        // Tested method arguments
        final String fileName = paths.get(schemaID) + "/results" + queryID + ".csv";
        Resource actual = underTest.loadFileAsResource(fileName);
        Assert.assertNotNull(actual);
    }


}