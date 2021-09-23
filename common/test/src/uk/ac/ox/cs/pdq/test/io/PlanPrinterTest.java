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
import java.util.Set;

import static org.mockito.Mockito.when;

public class PlanPrinterTest extends TestCase {

    public class ConcreteAccessMethod  extends AccessMethodDescriptor {
        private static final long serialVersionUID = 1L;

        public ConcreteAccessMethod(Attribute[] attributes, Integer[] inputs, Relation relation,
                                    Map<Attribute, Attribute> attributeMapping) {
            super(inputs);
        }

        public ConcreteAccessMethod(Attribute[] attributes, Set<Attribute> inputAttributes,
                                    Relation relation, Map<Attribute, Attribute> attributeMapping) {
            super(new Integer[] {0});
        }

    }

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

    /**
     *  Test to retrieve the province name of the Access RelationTerm
     */
    @Test
    public void testAccessTermProvenance(){
        AccessMethodDescriptor accessMethod;
        AccessTerm target;
        Integer[] inputs;
        Map<Attribute, TypedConstant> inputConstants;

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
         *  Plan: Free access
         */
        inputs = new Integer[0];
        accessMethod= new PlanPrinterTest.ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);

        target = AccessTerm.create(relation, accessMethod);
        Assert.assertNotNull(target);

        /*
         *  Plan: Access with input at index 0 and no input constants.
         */
        inputs = new Integer[] {0};
        accessMethod= new PlanPrinterTest.ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);

        target = AccessTerm.create(relation, accessMethod);
        Assert.assertNotNull(target);

        /*
         *  Plan: Access with input at index 0 in the access method schema and an input constant for that input.
         */
        inputConstants = new HashMap<>();
        inputConstants.put(Attribute.create(String.class, "c"), TypedConstant.create("CONSTANT STRING"));

        target = AccessTerm.create(relation, accessMethod);
        Assert.assertNotNull(target);

        Attribute provenanceName = PlanPrinter.outputAttributeProvenance(target,target.getAccessMethod().getInputs()[0]);

        assertEquals("a",provenanceName.getName());

        /*
         *  Plan: Access with inputs at indices 0 & 1 in the access method schema and input constants for those inputs.
         */
        inputs = new Integer[] {0, 1};
        accessMethod= new PlanPrinterTest.ConcreteAccessMethod(amAttributes, inputs, relation, attributeMapping);

        inputConstants = new HashMap<>();
        inputConstants.put(Attribute.create(Integer.class, "a"), TypedConstant.create(1));
        inputConstants.put(Attribute.create(String.class, "c"), TypedConstant.create("CONSTANT STRING"));

        target = AccessTerm.create(relation, accessMethod);

        Attribute provenanceName1 = PlanPrinter.outputAttributeProvenance(target,target.getAccessMethod().getInputs()[0]);
        Attribute provenanceName2 = PlanPrinter.outputAttributeProvenance(target,target.getAccessMethod().getInputs()[1]);

        assertEquals("a",provenanceName1.getName());
        assertEquals("b",provenanceName2.getName());

        Assert.assertNotNull(target);
    }

}