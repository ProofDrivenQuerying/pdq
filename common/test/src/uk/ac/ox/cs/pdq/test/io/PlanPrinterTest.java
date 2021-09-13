package uk.ac.ox.cs.pdq.test.io;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.db.*;
import uk.ac.ox.cs.pdq.db.Cache;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.io.PlanPrinter;
import uk.ac.ox.cs.pdq.test.algebra.SelectionTermTest;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class PlanPrinterTest extends TestCase {

    RelationalTerm p;
    public void setUp() throws Exception {
        super.setUp();
        Cache.reStartCaches();
        Schema s = new PdqTest().getScenario1().getSchema();
        Attribute[] ra1 = new Attribute[] { Attribute.create(String.class, "c1"), Attribute.create(String.class, "c2"),
                Attribute.create(String.class, "c3") };
        RelationalTerm a1 = AccessTerm.create(s.getRelation("R0"), s.getRelation("R0").getAccessMethods()[0]);
        RelationalTerm r1 = RenameTerm.create(ra1, a1);

        Attribute[] ra2 = new Attribute[] { Attribute.create(String.class, "c1"), Attribute.create(String.class, "c4"),
                Attribute.create(String.class, "c3") };
        Map<Integer, TypedConstant> map2 = new HashMap<>();
        TypedConstant constant = TypedConstant.create("a");
        map2.put(0, constant);
        AccessTerm a2 = AccessTerm.create(s.getRelation("R1"), s.getRelation("R1").getAccessMethods()[0],map2);
        RelationalTerm c2 = RenameTerm.create(ra2, a2);

        RelationalTerm joinTerm = JoinTerm.create(r1, c2);
        Attribute c1 = Attribute.create(String.class, "c1") ;

        p = ProjectionTerm.create(new Attribute[] {c1}, joinTerm);
    }



    /**
     *  Test to retrieve the province name of the projection RelationTerm
     */
    @Test
    public void testProjectionProvenance(){
        try {
            for (int i = 0; i < p.getOutputAttributes().length; i++) {
                assertEquals("a", PlanPrinter.outputAttributeProvenance(p, i).getName());
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
            JoinTerm cpt = (JoinTerm) p.getChild(0);
            ConjunctiveCondition condition = (ConjunctiveCondition) cpt.getJoinConditions();

            Attribute provenanceJoin1 =  PlanPrinter.outputAttributeProvenance(cpt, ((AttributeEqualityCondition)condition.getSimpleConditions()[0]).getPosition());
            Attribute provenanceJoin2 =  PlanPrinter.outputAttributeProvenance(cpt, ((AttributeEqualityCondition)condition.getSimpleConditions()[0]).getOther());

            Attribute provenanceJoin3 =  PlanPrinter.outputAttributeProvenance(cpt, ((AttributeEqualityCondition)condition.getSimpleConditions()[1]).getPosition());
            Attribute provenanceJoin4 =  PlanPrinter.outputAttributeProvenance(cpt, ((AttributeEqualityCondition)condition.getSimpleConditions()[1]).getOther());

            assertEquals("a", provenanceJoin1.getName());
            assertEquals("a", provenanceJoin2.getName());
            assertEquals("c", provenanceJoin3.getName());
            assertEquals("c", provenanceJoin4.getName());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     *  Test to retrieve the province name of the Selection RelationTerm
     */
    @Test
    public void testSelectionTermProvenance(){
        Relation relation = Mockito.mock(Relation.class);
        Attribute[] relationAttributes = new Attribute[] {Attribute.create(Integer.class, "a"),
                Attribute.create(Integer.class, "b"), Attribute.create(String.class, "c")};
        when(relation.getAttributes()).thenReturn(relationAttributes.clone());

        Attribute[] amAttributes = new Attribute[] {
                Attribute.create(String.class, "W"), Attribute.create(Integer.class, "X"),
                Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "Z")};

        Map<Attribute, Attribute> attributeMapping = new HashMap<Attribute, Attribute>();
        attributeMapping.put(Attribute.create(Integer.class, "X"), Attribute.create(Integer.class, "a"));
        attributeMapping.put(Attribute.create(Integer.class, "Y"), Attribute.create(Integer.class, "b"));
        attributeMapping.put(Attribute.create(String.class, "W"), Attribute.create(String.class, "c"));

        /*
         * Free access.
         */
        AccessMethodDescriptor amFree = new SelectionTermTest.ConcreteAccessMethod(amAttributes, new Integer[0], relation, attributeMapping);

        Condition condition = ConstantEqualityCondition.create(2, TypedConstant.create("BRAZIL"));

        SelectionTerm target = SelectionTerm.create(condition, AccessTerm.create(relation, amFree));

        //return the provenance from position
        Attribute selectionTermProvenance = PlanPrinter.outputAttributeProvenance(target, 2);

        //test assert ConstantEqualityCondition
        assertEquals("c",selectionTermProvenance.getName());

        condition = AttributeEqualityCondition.create(2, 0);
        target = SelectionTerm.create(condition, AccessTerm.create(relation, amFree));

        //return the provenance from position
        Attribute selectionTermProvenance1 = PlanPrinter.outputAttributeProvenance(target, 2);
        Attribute selectionTermProvenance2 = PlanPrinter.outputAttributeProvenance(target, 0);

        //Test assert AttributeEqualityCondition
        assertEquals("c",selectionTermProvenance1.getName());
        assertEquals("a",selectionTermProvenance2.getName());

    }

}