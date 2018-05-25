package uk.ac.ox.cs.pdq.test.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseParameters;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * This test when executed as a java application can detect memory leaks in the
 * SQL database connections. It starts 2 threads one for MySql
 * and one Postgres. Each threads adds and deletes facts keeping the database
 * size on minimum, but doing lots of operations in an infinite loop.
 * Each thread reports how many loops it have been doing. and what's the average speed.
 * 
 * @author Gabor
 * 
 * 07/09/2017 algebra-changes branch: 
 *   - D.erby    : average 0.65 loops per second with 1000 facts inserted and deleted 1 times in each loop.
 *   - MySql    : average 29.0 loops per second with 1000 facts inserted and deleted 1 times in each loop.
 *   - Postgres : average 18.0 loops per second with 1000 facts inserted and deleted 1 times in each loop.
 */
public class SqlRacer {
	private final boolean print = false;
	protected final static int insertCacheSize = 1000;
	private int repeat = 1;
	protected final long timeout = 3600000;
	private Relation rel1;
	private Relation rel2;
	private Relation rel3;
	private final int NUMBER_OF_FACTS = 1000;
	private TGD tgd;
	private TGD tgd2;
	private EGD egd;

	private Schema schema;
	//private final DatabaseManager dcMySql;
	private final DatabaseManager dcPostgresSql;
	private final DatabaseManager dcMemory;
	private Atom R1;
	public static void main(String[] args) {
		try {
			new SqlRacer();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public SqlRacer() throws SQLException, DatabaseException {
		try {
			setup();
//			dcMySql = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(DatabaseParameters.MySql),1);
			
			dcPostgresSql = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(DatabaseParameters.Postgres),1);
			dcMemory = new InternalDatabaseManager();			
	//		dcMySql.initialiseDatabaseForSchema(this.schema);
			dcPostgresSql.initialiseDatabaseForSchema(this.schema);
			dcMemory.initialiseDatabaseForSchema(this.schema);
			setupThreads();
		} catch (SQLException e) {
			throw e;
		}
	}

	@Before
	public void setup() throws SQLException {
		Attribute at11 = Attribute.create(String.class, "at11");
		Attribute at12 = Attribute.create(String.class, "at12");
		Attribute at13 = Attribute.create(String.class, "at13");
		this.rel1 = Relation.create("R1", new Attribute[] { at11, at12, at13 });

		Attribute at21 = Attribute.create(String.class, "at21");
		Attribute at22 = Attribute.create(String.class, "at22");
		this.rel2 = Relation.create("R2", new Attribute[] { at21, at22 });

		Attribute at31 = Attribute.create(String.class, "at31");
		Attribute at32 = Attribute.create(String.class, "at32");
		this.rel3 = Relation.create("R3", new Attribute[] { at31, at32 });

		R1 = Atom.create(this.rel1, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom R2 = Atom.create(this.rel2, new Term[] { Variable.create("y"), Variable.create("z") });
		Atom R2p = Atom.create(this.rel2, new Term[] { Variable.create("y"), Variable.create("w") });

		Atom R3 = Atom.create(this.rel3, new Term[] { Variable.create("y"), Variable.create("w") });

		this.tgd = TGD.create(new Atom[] { R1 }, new Atom[] { R2 });
		this.tgd2 = TGD.create(new Atom[] { R1 }, new Atom[] { R3 });
		this.egd = EGD.create(new Atom[] {R2, R2p}, new Atom[]{ Atom.create(Predicate.create("EQUALITY", 2, true), Variable.create("z"), Variable.create("w")) });
		this.schema = new Schema(new Relation[] { this.rel1, this.rel2, this.rel3 }, new Dependency[] { this.tgd, this.tgd2, this.egd });
	}

	public void setupThreads() throws SQLException {
//		Thread mySqlThread = new Thread() {
//			public void run() {
//				try {
//					race(dcMySql, "MySql     ");
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//		};
		Thread postgresThread = new Thread() {
			public void run() {
				try {
					race(dcPostgresSql, "PostgresSql");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};

		Thread memoryThread = new Thread() {
			public void run() {
				try {
					race(dcMemory, "Memory    ");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		//mySqlThread.start();
		postgresThread.start();
		memoryThread.start();
	}

	private void race(DatabaseManager instance, String name) throws SQLException, DatabaseException {
		long startTime = System.currentTimeMillis();
		System.out.println(name + "started. Each loop will create and delete "+NUMBER_OF_FACTS+" facts " + repeat + " amount of times.");
		long counter = 0;
		while (true) {
			List<Match> rs = null;
			ConjunctiveQuery cq = ConjunctiveQuery.create(R1.getVariables(), new Atom[] {R1});
			Collection<Atom> facts = createTestFacts1000();
			long durationAdd =0;
			long durationQ =0;
			long durationDel =0;
			for (int i = 0; i < repeat; i++) {
				//PdqTest.reInitalize(this);
				int resCount = 0;
				
				long start =  System.currentTimeMillis();
				instance.addFacts(facts);
				durationAdd = System.currentTimeMillis() - start;
				
				start =  System.currentTimeMillis();
				rs = instance.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] {cq}));
				durationQ = System.currentTimeMillis() - start;
				
				resCount = checkTestFacts(rs, print);
				if (resCount != NUMBER_OF_FACTS) {
					System.err.println("query result is not "+NUMBER_OF_FACTS+", but " + resCount + " in " + name);
					Assert.assertEquals(NUMBER_OF_FACTS, checkTestFacts(rs, print));
				}
			}
			long start =  System.currentTimeMillis();
			instance.deleteFacts(instance.getCachedFacts());
			rs = instance.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] {cq}));
			durationDel = System.currentTimeMillis() - start;
			
			int resCount = checkTestFacts(rs, print);
			if (resCount != 0) {
				System.err.println("query result is not "+0+", but " + resCount + " in " + name);
				Assert.assertEquals(NUMBER_OF_FACTS, checkTestFacts(rs, print));
			}
			
			
			
			counter++;
			if (counter % 10 == 0) {
				long duration = System.currentTimeMillis() - startTime;
				int loopPer100Second = (int)((((double)counter)/duration)*1000*1000);
				System.out.println(name + "\t#" + counter + " \tthroughput: \t" + (loopPer100Second/1000.00) + " \tloops per second. (Add:"+durationAdd+"ms, Query:"+durationQ+"ms, del:"+durationDel+"ms, )");
			}
		}
	}

	private int checkTestFacts(List<Match> rs, boolean print) throws SQLException {
		if (print) {
			for (Match m: rs) {
				for (Variable v:m.getMapping().keySet()) {
					System.out.print(v.getSymbol() + "\t");
				}
				System.out.println("");
				break;
			}
		}
		int resultsRowCount = 0;
		
		for (Match m: rs) {
			resultsRowCount++;
			for (Variable v:m.getMapping().keySet()) {
				Constant columnValue = m.getMapping().get(v);
				if (columnValue!=null)
				if (print)
					System.out.print(columnValue + "\t");
			}
			if (print)
				System.out.println("");
		}
		return resultsRowCount;
	}

	protected Collection<Atom> createTestFacts() {
		Atom f20 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k1"), UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f21 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k2"), UntypedConstant.create("c"), UntypedConstant.create("c2") });
		Atom f22 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k3"), UntypedConstant.create("c"), UntypedConstant.create("c3") });
		Atom f23 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k4"), UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f24 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k5"), UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f25 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k6"), UntypedConstant.create("c"), TypedConstant.create(new String("Michael")) });
		return Lists.newArrayList(f20, f21, f22, f23, f24, f25);
	}
	private Collection<Atom> createTestFacts1000() {
		List<Atom> l = new ArrayList<>();
		for (int i = 0; i < NUMBER_OF_FACTS; i++) {
			Atom a = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k"+i), UntypedConstant.create("c"), UntypedConstant.create("c"+i) });
			l.add(a);
		}
		return l;
	}
}
