package uk.ac.ox.cs.pdq.test.io;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.io.PlanPrinter;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.test.algebra.RelationalTermAsLogicTest;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlanPrinterTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     *  Test to retrieve the province name of the projection RelationTerm
     */
    @Test
    public void testProjectionProvenance(){
        try {
            Schema s = new PdqTest().getScenario1().getSchema();
            Attribute[] ra1 = new Attribute[] { Attribute.create(String.class, "c1"), Attribute.create(String.class, "c2"),
                    Attribute.create(String.class, "c3") };
            RelationalTerm a1 = AccessTerm.create(s.getRelation("R0"), s.getRelation("R0").getAccessMethod("mt_4"));
            RelationalTerm r1 = RenameTerm.create(ra1, a1);

            Attribute[] ra2 = new Attribute[] { Attribute.create(String.class, "c1"), Attribute.create(String.class, "c4"),
                    Attribute.create(String.class, "c3") };
            Map<Integer, TypedConstant> map2 = new HashMap<>();
            TypedConstant constant = TypedConstant.create("a");
            map2.put(0, constant);
            AccessTerm a2 = AccessTerm.create(s.getRelation("R1"), s.getRelation("R1").getAccessMethod("mt_5"),map2);
            RelationalTerm c2 = RenameTerm.create(ra2, a2);

            RelationalTerm joinTerm = JoinTerm.create(r1, c2);
            Attribute c1 = Attribute.create(String.class, "c1") ;
            RelationalTerm p = ProjectionTerm.create(new Attribute[] {c1}, joinTerm);

            ArrayList<Integer> positions = PlanPrinter.getProjectionPositionIndex((ProjectionTerm) p);

            for (int i = 0; i < p.getOutputAttributes().length; i++) {
                PlanPrinter.outputAttributeProvenance(p, positions.get(i)).getName();
                assertEquals(PlanPrinter.outputAttributeProvenance(p, positions.get(i)).getName(), "a");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     *  Test to retrieve the province name of the Join RelationTerm
     */
    @Test
    public void testJoinProvenance(){
        try {
            Schema s = new PdqTest().getScenario1().getSchema();
            Attribute[] ra1 = new Attribute[] { Attribute.create(String.class, "c1"), Attribute.create(String.class, "c2"),
                    Attribute.create(String.class, "c3") };
            RelationalTerm a1 = AccessTerm.create(s.getRelation("R0"), s.getRelation("R0").getAccessMethod("mt_4"));
            RelationalTerm r1 = RenameTerm.create(ra1, a1);

            Attribute[] ra2 = new Attribute[] { Attribute.create(String.class, "c1"), Attribute.create(String.class, "c4"),
                    Attribute.create(String.class, "c3") };
            Map<Integer, TypedConstant> map2 = new HashMap<>();
            TypedConstant constant = TypedConstant.create("a");
            map2.put(0, constant);
            AccessTerm a2 = AccessTerm.create(s.getRelation("R1"), s.getRelation("R1").getAccessMethod("mt_5"),map2);
            RelationalTerm c2 = RenameTerm.create(ra2, a2);

            RelationalTerm joinTerm = JoinTerm.create(r1, c2);
            Attribute c1 = Attribute.create(String.class, "c1") ;
            RelationalTerm p = ProjectionTerm.create(new Attribute[] {c1}, joinTerm);

            ArrayList<Integer> positions = PlanPrinter.getProjectionPositionIndex((ProjectionTerm) p);

            for (int i = 0; i < p.getOutputAttributes().length; i++) {
                assertEquals(PlanPrinter.projectionProvenance(p.getChildren(), positions.get(i)).getName(), "a");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}