// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.regression.junit.chasebench;

import java.io.File;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.io.CommonToPDQTranslator;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.reasoning.chase.ParallelChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.reasoningdatabase.cache.MultiInstanceFactCache;

/**
 * Abstract class for ChaseBench test cases.
 * 
 * @author Stefano
 */
public abstract class ChaseBenchAbstract {
    protected Schema s;
    protected final String fileSeparator = System.getProperty("file.separator");
    protected Map<String, Relation> relations = new HashMap<>();
    protected String testName; // test name
    protected String testDataFolder;
    protected String fileName;

    protected void init(String testName, String testDataFolder) {
        this.init(testName, testDataFolder, null);
    }

    protected void init(String testName, String testDataFolder, String fileName) {
        this.testName = testName;
        this.testDataFolder = testDataFolder;
        if (fileName != null)
            this.fileName = fileName;
        else
            this.fileName = testName;
        
        s = createSchema();
    }

    public void testInternalDB() throws DatabaseException, SQLException, IOException {
        System.out.println("INTERNAL");
        DatabaseManager dbm = getInternalDatabaseManager();
        dbm.initialiseDatabaseForSchema(s);
        testDB(dbm);
    }

    public void testExternalDB() throws DatabaseException, SQLException, IOException {
        System.out.println("EXTERNAL");
        DatabaseManager dbm = getExternalDatabaseManager();
        s = convertToStringAttributeOnly(s);
        dbm.initialiseDatabaseForSchema(s);
        testDB(dbm);
    }

    public void testLogicalDB() throws DatabaseException, SQLException, IOException {
        System.out.println("LOGICAL");
        DatabaseManager dbm = getLogicalDatabaseManager();
        Schema dbSchema = ExplorationSetUp.convertTypesToString(s);
        dbm.initialiseDatabaseForSchema(dbSchema);
        testDB(dbm);
    }

    private DatabaseManager getInternalDatabaseManager() throws DatabaseException {
        DatabaseManager dbm = new InternalDatabaseManager();
        return dbm;
    }

    private DatabaseManager getExternalDatabaseManager() throws DatabaseException {
        ExternalDatabaseManager dbm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
        return dbm;
    }

    private DatabaseManager getLogicalDatabaseManager() throws DatabaseException {
        ExternalDatabaseManager dbm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
        return new LogicalDatabaseInstance(new MultiInstanceFactCache(), dbm, 0);
    }

    private static Schema convertToStringAttributeOnly(Schema s) {
        Relation relations[] = new Relation[s.getRelations().length];
        for (int i = 0; i < relations.length; i++) {
            relations[i] = convertToStringAttributeOnly(s.getRelation(i));
        }
        return new Schema(relations, s.getAllDependencies());
    }

    private static Relation convertToStringAttributeOnly(Relation r) {
        Attribute[] attributes = new Attribute[r.getAttributes().length];
        for (int i = 0; i < attributes.length; i++) {
            if (r.getAttribute(i).getType().equals(String.class)) {
                attributes[i] = r.getAttribute(i);
            } else {
                attributes[i] = Attribute.create(String.class, r.getAttribute(i).getName());
            }
        }
        return Relation.create(r.getName(), attributes, r.getAccessMethods(), r.getForeignKeys(), r.isEquality());
    }

    protected void testDB(DatabaseManager dbm) throws DatabaseException, SQLException, IOException {
        long start,duration;
        start = System.currentTimeMillis();
        DatabaseChaseInstance state = new DatabaseChaseInstance(getTestFacts(), dbm);
        Collection<Atom> res = state.getFacts();
        System.out.println("INITIAL STATE contains " + res.size() + " facts.");
        printStats(res);
        duration = System.currentTimeMillis() - start;
        System.out.println("init took " + (duration / 1000.0) + " seconds.");
        start = System.currentTimeMillis();
        ParallelChaser chaser = new ParallelChaser();
        chaser.reasonUntilTermination(state, s.getNonEgdDependencies());
        duration = System.currentTimeMillis() - start;
        System.out.println("reasonUntilTermination took " + (duration / 1000.0) + " seconds.");
        start = System.currentTimeMillis();
        res = state.getFacts();
        System.out.println("Final state contains " + res.size() + " facts.");
        printStats(res);
        duration = System.currentTimeMillis() - start;
        System.out.println("out took " + (duration / 1000.0) + " seconds.");
        start = System.currentTimeMillis();
        runTestQueries(state);
        duration = System.currentTimeMillis() - start;
        System.out.println("Query answering took " + (duration / 1000.0) + " seconds.");
    }

    protected void runTestQueries(DatabaseChaseInstance state) throws IOException, DatabaseException {
        Collection<ConjunctiveQuery> queries = getTestQueries();
        int counter = 1;
        for (ConjunctiveQuery q : queries) {
            System.out.println("Processing query #" + counter + q);
            try {
                List<Match> matches = state.getMatches(q, new HashMap<>());
                System.out.println("#" + counter + " query results:\n\t" + matches);
            } catch (OutOfMemoryError e) {
                System.err.println("Answering #" + counter + " took too much memory!");
                System.err.println("Heap: " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
                System.err.println("NonHeap: " + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
                List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
                for (MemoryPoolMXBean bean: beans) {
                    System.err.println(bean.getName() + ": " + bean.getUsage());
                }
                for (GarbageCollectorMXBean bean: ManagementFactory.getGarbageCollectorMXBeans()) {
                    System.err.println(bean.getName() + ": " + bean.getCollectionCount() + "; " + bean.getCollectionTime());
                }
                throw e;
            }
            counter++;
        }
    }

    protected void printStats(Collection<Atom> res) {
        Map<String, Integer> dataMap = new HashMap<>();
        for (Atom a : res) {
            String name = a.getPredicate().getName();
            Integer i = dataMap.get(name);
            if (i != null) {
                dataMap.put(name, i + 1);
            } else {
                dataMap.put(name, 1);
            }
        }
        for (String name : dataMap.keySet()) {
            System.out.println(name + "\t\t has \t" + dataMap.get(name) + " facts");

        }
    }

    protected Schema createSchema() {
        File schemaDir = new File("test" + fileSeparator + "chaseBench" + fileSeparator + testName + fileSeparator
                + testDataFolder + fileSeparator + "schema");
        File dependencyDir = new File("test" + fileSeparator + "chaseBench" + fileSeparator + testName + fileSeparator
                + testDataFolder + fileSeparator + "dependencies");
        Map<String, Relation> tables = CommonToPDQTranslator
                .parseTables(schemaDir.getAbsolutePath() + fileSeparator + fileName + ".s-schema.txt");
        Map<String, Relation> tables1 = CommonToPDQTranslator
                .parseTables(schemaDir.getAbsolutePath() + fileSeparator + fileName + ".t-schema.txt");
        relations.putAll(tables);
        relations.putAll(tables1);
        List<Dependency> dependencies = CommonToPDQTranslator.parseDependencies(relations,
                dependencyDir.getAbsolutePath() + fileSeparator + fileName + ".st-tgds.txt");
        dependencies.addAll(CommonToPDQTranslator.parseDependencies(relations,
                dependencyDir.getAbsolutePath() + fileSeparator + fileName + ".t-tgds.txt"));
        return new Schema(relations.values().toArray(new Relation[relations.size()]),
                dependencies.toArray(new Dependency[dependencies.size()]));

    }

    protected Collection<Atom> getTestFacts() {
        File dataDir = new File("test" + fileSeparator + "chaseBench" + fileSeparator + testName + fileSeparator
                + testDataFolder + fileSeparator + "data");
        Collection<Atom> facts = new ArrayList<>();
        for (File f : dataDir.listFiles()) {
            if (f.getName().endsWith(".csv")) {
                String name = f.getName().substring(0, f.getName().indexOf("."));
                if (s.getRelation(name) == null) {
                    System.out.println("Can't process file: " + f.getAbsolutePath());
                } else {
                    facts.addAll(CommonToPDQTranslator.importFacts(s, name, f.getAbsolutePath()));
                }
            }
        }
        return facts;
    }

    protected Collection<ConjunctiveQuery> getTestQueries() throws IOException {
        File dataDir = new File("test" + fileSeparator + "chaseBench" + fileSeparator + testName + fileSeparator
                + testDataFolder + fileSeparator + "queries");
        Collection<ConjunctiveQuery> facts = new ArrayList<>();
        Map<String, Relation> relations = new HashMap<>();
        for (Relation r : s.getRelations()) {
            relations.put(r.getName(), r);
        }
        for (File f : dataDir.listFiles()) {
            if (f.getName().endsWith(".txt")) {
                facts.add(CommonToPDQTranslator.parseQuery(relations, f.getAbsolutePath()));
            }
        }
        return facts;
    }

}
