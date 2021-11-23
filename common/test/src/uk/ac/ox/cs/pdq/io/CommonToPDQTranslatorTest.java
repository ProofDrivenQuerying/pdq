package uk.ac.ox.cs.pdq.io;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * unit test for the CommonToPDQTranslator class to verify the process of translating the "ascii" format used by chasebench to the objects used in PDQ (e.g. dependencies)
 * @Author Brandon Moore
 */
public class CommonToPDQTranslatorTest extends TestCase {

    File schemaDir = new File("..//regression//test//chaseBench//Ontology-256//schema");
    File dependencyDir = new File("..//regression//test//chaseBench//Ontology-256//dependencies");
    Map<String, Relation> relations = new HashMap<>();

    /**
     * Check and Parse chasebench files: the first two contain schema information and dependencies, the next few contain dependencies
     */
    @Test
    public void testReadTablesFromFile() {
        Map<String, Relation> tables = CommonToPDQTranslator.parseTables(schemaDir.getAbsolutePath() + "//Ontology-256.s-schema.txt");
        Map<String, Relation> tables1 = CommonToPDQTranslator.parseTables(schemaDir.getAbsolutePath() + "//Ontology-256.t-schema.txt");

        Assert.assertEquals(218, tables.size());
        Assert.assertEquals(444, tables1.size());

        List<Dependency> dependencies1 = CommonToPDQTranslator.parseDependencies(relations, dependencyDir .getAbsolutePath() + "//Ontology-256.st-tgds.txt");
        List<Dependency> dependencies2 = CommonToPDQTranslator.parseDependencies(relations, dependencyDir .getAbsolutePath() + "//Ontology-256.st-tgds.txt");
        List<Dependency> dependencies3 = CommonToPDQTranslator.parseDependencies(relations, dependencyDir .getAbsolutePath() + "//Ontology-256.st-tgds.txt");

        Assert.assertEquals(256, dependencies1.size());
        Assert.assertEquals(256, dependencies2.size());
        Assert.assertEquals(256, dependencies3.size());
    }

    /**
     * check to process a single line example into bodyAtom, HeadAtoms and equality Atoms
     */
    @Test
    public void testParseAtom() {
        //read chasebench schema files to create relations
        Map<String, Relation> tables = CommonToPDQTranslator.parseTables(schemaDir.getAbsolutePath() + "//Ontology-256.s-schema.txt");
        Map<String, Relation> tables1 = CommonToPDQTranslator.parseTables(schemaDir.getAbsolutePath() + "//Ontology-256.t-schema.txt");

        relations.putAll(tables);
        relations.putAll(tables1);

        String line1 = "test_ad_1_nl0_ce0(?a,?b,?c) -> touch_ad_1_nl0_ce0(?a,?b,?c,?d) .";
        String line2 = "pipe_ad_2_nl0_ce0(?a,?b,?c,?d,?e) -> future_ad_2_nl0_ce0(?a,?b,?c,?d,?e,?f) .";
        String line3 = "use_vnm_64_nl0_ce0(?influence_ad_33_nl0_ae0ke0,?oil_vnm_64_nl0_ae00), use_vnm_64_nl0_ce0(?influence_ad_33_nl0_ae0ke00,?oil_vnm_64_nl0_ae00) -> ?influence_ad_33_nl0_ae0ke0 = ?influence_ad_33_nl0_ae0ke00 .";

        //line1
        String[] sides1 = line1.split("->");
        List<Atom> bodyAtoms1 = CommonToPDQTranslator.parseAtoms(relations, sides1[0]);
        List<Atom> headAtoms1 = CommonToPDQTranslator.parseAtoms(relations, sides1[1]);
        Atom equality1 = CommonToPDQTranslator.parseEquality(sides1[1]);
        Assert.assertEquals("test_ad_1_nl0_ce0(a,b,c)",bodyAtoms1.get(0).toString());
        Assert.assertEquals("touch_ad_1_nl0_ce0(a,b,c,d)",headAtoms1.get(0).toString());
        Assert.assertNull(equality1);


        //line2
        String[] sides2 = line2.split("->");
        List<Atom> bodyAtoms2 = CommonToPDQTranslator.parseAtoms(relations, sides2[0]);
        List<Atom> headAtoms2 = CommonToPDQTranslator.parseAtoms(relations, sides2[1]);
        Atom equality2 = CommonToPDQTranslator.parseEquality(sides2[1]);
        Assert.assertEquals("pipe_ad_2_nl0_ce0(a,b,c,d,e)",bodyAtoms2.get(0).toString());
        Assert.assertEquals("future_ad_2_nl0_ce0(a,b,c,d,e,f)",headAtoms2.get(0).toString());
        Assert.assertNull(equality2);

        //line3
        String[] sides3 = line3.split("->");
        List<Atom> bodyAtoms3 = CommonToPDQTranslator.parseAtoms(relations, sides3[0]);
        List<Atom> headAtoms3 = CommonToPDQTranslator.parseAtoms(relations, sides3[1]);
        Atom equality3 = CommonToPDQTranslator.parseEquality(sides3[1]);
        Assert.assertEquals("use_vnm_64_nl0_ce0(influence_ad_33_nl0_ae0ke0,oil_vnm_64_nl0_ae00)",bodyAtoms3.get(0).toString());
        //parseatoms is only looking for non-equality atoms, so should be zero as it's a equality
        Assert.assertEquals(0,headAtoms3.size());
        //this is the result of parseEquality, so should be not null, since it is equality
        Assert.assertNotNull(equality3);
    }


}
