package uk.ac.ox.cs.pdq.rest;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.rest.jsonobjects.plan.Plan;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.Dependencies;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.RelationArray;
import uk.ac.ox.cs.pdq.rest.jsonobjects.schema.SchemaArray;

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
    }


}