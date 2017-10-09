package uk.ac.ox.cs.pdq.test.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseInstance;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * This test when executed as a java application can detect memory leaks in the
 * SQL database connections. It starts 3 threads one for derby, one for MySql
 * and one Postgres. Each threads adds and deletes facts keeping the database
 * size on minimum, but doing lots of operations in an infinite loop.
 * Each thread reports how many loops it have been doing. and what's the average speed.
 * 
 * @author Gabor
 * 
 * 07/09/2017 algebra-changes branch: 
 *   - Derby    : average 0.65 loops per second with 1000 facts inserted and deleted 1 times in each loop.
 *   - MySql    : average 29.0 loops per second with 1000 facts inserted and deleted 1 times in each loop.
 *   - Postgres : average 18.0 loops per second with 1000 facts inserted and deleted 1 times in each loop.
 */
public class SqlRacer {
	private final boolean print = false;
	protected final static int insertCacheSize = 1000;
	private int repeat = 1;
	private final int PARALLEL_THREADS = 10;
	
	protected final long timeout = 3600000;
	private Relation rel1;
	private Relation rel2;
	private Relation rel3;

	private TGD tgd;
	private TGD tgd2;
	private EGD egd;

	private Schema schema;
	private final DatabaseConnection dcMySql;
	private final DatabaseConnection dcPostgresSql;
	private final DatabaseConnection dcDerby;
	private DatabaseInstance derbyInstance;
	private DatabaseInstance postgresInstance;
	private DatabaseInstance mySqlInstance;
	public static void main(String[] args) {
		try {
			new SqlRacer();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public SqlRacer() throws SQLException {
		try {
			setup();
			dcMySql = new DatabaseConnection(DatabaseParameters.MySql, this.schema,PARALLEL_THREADS);
			dcPostgresSql = new DatabaseConnection(DatabaseParameters.Postgres, this.schema,PARALLEL_THREADS);
			dcDerby = new DatabaseConnection(DatabaseParameters.Derby, this.schema);

			derbyInstance = new DatabaseInstance(dcDerby) {
				
				@Override
				public Collection<Atom> getFacts() {
					return null;
				}
			};			
			postgresInstance = new DatabaseInstance(dcPostgresSql) {
				
				@Override
				public Collection<Atom> getFacts() {
					return null;
				}
			};			
			mySqlInstance = new DatabaseInstance(dcMySql) {
				
				@Override
				public Collection<Atom> getFacts() {
					return null;
				}
			};			
			setupThreads();
		} catch (SQLException e) {
			throw e;
		}
	}

	@Before
	public void setup() throws SQLException {
		Attribute factId = Attribute.create(Integer.class, "InstanceID");

		Attribute at11 = Attribute.create(String.class, "at11");
		Attribute at12 = Attribute.create(String.class, "at12");
		Attribute at13 = Attribute.create(String.class, "at13");
		this.rel1 = Relation.create("R1", new Attribute[] { at11, at12, at13 });

		Attribute at21 = Attribute.create(String.class, "at21");
		Attribute at22 = Attribute.create(String.class, "at22");
		this.rel2 = Relation.create("R2", new Attribute[] { at21, at22, factId });

		Attribute at31 = Attribute.create(String.class, "at31");
		Attribute at32 = Attribute.create(String.class, "at32");
		this.rel3 = Relation.create("R3", new Attribute[] { at31, at32, factId });

		Atom R1 = Atom.create(this.rel1, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom R2 = Atom.create(this.rel2, new Term[] { Variable.create("y"), Variable.create("z") });
		Atom R2p = Atom.create(this.rel2, new Term[] { Variable.create("y"), Variable.create("w") });

		Atom R3 = Atom.create(this.rel3, new Term[] { Variable.create("y"), Variable.create("w") });

		this.tgd = TGD.create(new Atom[] { R1 }, new Atom[] { R2 });
		this.tgd2 = TGD.create(new Atom[] { R1 }, new Atom[] { R3 });
		this.egd = EGD.create(new Atom[] {R2, R2p}, new Atom[]{ Atom.create(Predicate.create("EQUALITY", 2, true), Variable.create("z"), Variable.create("w")) });
		this.schema = new Schema(new Relation[] { this.rel1, this.rel2, this.rel3 }, new Dependency[] { this.tgd, this.tgd2, this.egd });
	}

	public void setupThreads() throws SQLException {
		Thread mySqlThread = new Thread() {
			public void run() {
				try {
					race(mySqlInstance, dcMySql, "MySql     ");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		};
		mySqlThread.start();
		Thread postgresThread = new Thread() {
			public void run() {
				try {
					race(postgresInstance, dcPostgresSql, "PostgresSql");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		};
		postgresThread.start();

		Thread derbyThread = new Thread() {
			public void run() {
				try {
					race(derbyInstance, dcDerby, "Derby     ");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		};
		derbyThread.start();
		synchronized (derbyThread) {
			try {
				derbyThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void race(DatabaseInstance instance, DatabaseConnection dc, String name) throws SQLException {
		long startTime = System.currentTimeMillis();
		System.out.println(name + "started. Each loop will create and delete 1000 times " + repeat + " amount of facts.");
		long counter = 0;
		while (true) {
			for (int i = 0; i < repeat; i++) {
				PdqTest.reInitalize(this);
				Collection<Atom> facts = createTestFacts1000();
				instance.addFacts(facts);
				Statement sqlStatement = dc.getSynchronousConnections(0).createStatement();
				ResultSet rs = sqlStatement.executeQuery("select * from "+dc.getDatabaseParameters().getDatabaseName()+".R1");
				Assert.assertEquals(1000, checkTestFacts(rs, print));
				rs.close();
			}
			Statement sqlStatement = dc.getSynchronousConnections(0).createStatement();
			int deleted = sqlStatement.executeUpdate("delete from "+dc.getDatabaseParameters().getDatabaseName()+".R1");
			if (deleted < repeat*1000) {
				System.err.println(deleted + " amount of tuples were created out of 1000.");
			}
			counter++;
			if (counter % 10 == 0) {
				long duration = System.currentTimeMillis() - startTime;
				int loopPer100Second = (int)((((double)counter)/duration)*1000*1000);
				System.out.println(name + "\t#" + counter + " \tthroughput: \t" + (loopPer100Second/1000.00) + " \tloops per second.");
			}
		}
	}

	private int checkTestFacts(ResultSet rs, boolean print) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		if (print) {
			for (int i = 1; i <= columnsNumber; i++) {
				System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println("");
		}
		int resultsRowCount = 0;
		
		while (rs.next()) {
			resultsRowCount++;
			String columnValue = rs.getString(1);
			if (print)
				System.out.print(columnValue + "\t");
			for (int i = 2; i <= columnsNumber; i++) {
				columnValue = rs.getString(i);
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
		for (int i = 0; i < 1000; i++) {
			Atom a = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k"+i), UntypedConstant.create("c"), UntypedConstant.create("c"+i) });
			l.add(a);
		}
		return l;
	}
}
