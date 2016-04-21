package uk.ac.ox.cs.pdq.generator.reverse;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.ReasonerFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseListState;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManagerFactory;

import com.google.common.eventbus.EventBus;

/**
 * The reverse query generator attempts to find query answerable for some 
 * given schema, access restrictions and dependencies, by running a reasoning
 * procedures on an arbitrarily large query, and extracting relevant sub-queries
 * from each partial proof.
 * 
 * Queries are then filter using a conjunctive set of QuerySelector which
 * rule out queries not satisfying certain properties.
 * 
 * 
 * @author Julien Leblay
 */
public class ReverseQueryGenerator implements Runnable {

	/** Logger. */
	private static Logger log = Logger.getLogger(ReverseQueryGenerator.class);

	/** The randomizer seed. */
	private static Integer seeds = 0;

	/** The schema. */
	private final Schema schema;
	
	/** The query. */
	private final ConjunctiveQuery query;
	
	/**
	 * Instantiates a new reverse query generator.
	 *
	 * @param threadId the thread id
	 * @param schema the schema
	 * @param query the query
	 */
	public ReverseQueryGenerator(Integer threadId, Schema schema, ConjunctiveQuery query) {
		this.schema = schema;
		this.query = query;
	}

	/**
	 * Gets the seed.
	 *
	 * @return the randomizer seed currently in use.
	 */
	public static synchronized Integer getSeed() {
		return seeds++;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			ReasoningParameters reasoningParams = new ReasoningParameters();
			this.schema.updateConstants(this.query.getSchemaConstants());
			AccessibleSchema accessibleSchema = new AccessibleSchema(this.schema);
			EventBus eb = new EventBus();
			Chaser reasoner = new ReasonerFactory(
					eb, 
					true, 
					reasoningParams).getInstance(); 
			MatchMaker mm = new MatchMaker(
					new LengthBasedQuerySelector(2, 6),
					new ConstantRatioQuerySelector(0.2),
					new CrossProductFreeQuerySelector(),
					new NoAllFreeAccessQuerySelector(),
					new DubiousRepeatedPredicateQuerySelector(),
					new JoinOnVariableQuerySelector(),
					new DiversityQuerySelector()
//					new UnanswerableQuerySelector(params, this.schema)
					);
			eb.register(mm);
			Runtime.getRuntime().addShutdownHook(new MatchReport(mm));

			Query<?> accessibleQuery = accessibleSchema.accessible(this.query);
			try(HomomorphismManager detector =
				new HomomorphismManagerFactory().getInstance(accessibleSchema, reasoningParams)) {
				
				detector.addQuery(accessibleQuery);
				AccessibleChaseState state = (AccessibleChaseState) 
						new AccessibleDatabaseListState(query, accessibleSchema, (DatabaseHomomorphismManager) detector);
				
				log.info("Phase 1");
				reasoner.reasonUntilTermination(state, this.schema.getDependencies());
				log.info("Phase 2");
				reasoner.reasonUntilTermination(state, CollectionUtils.union(
						accessibleSchema.getAccessibilityAxioms(),
						accessibleSchema.getInferredAccessibilityAxioms()));
				log.info("Reasoning complete.");
				detector.clearQuery();
		}
			
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * Runs the reverse generator from command line.
	 *
	 * @param args the arguments
	 */
	public static void main(String... args) {
		long timeout = 120000;
		try(FileInputStream fis = new FileInputStream("../pdq.benchmark/test/dag/web/schemas/schema-all.xml");
			FileInputStream qis = new FileInputStream("../pdq.benchmark/test/dag/web/queries/query-all.xml")) {
			Schema schema = new SchemaReader().read(fis);
			ConjunctiveQuery query = new QueryReader(schema).read(qis);
			ExecutorService exec = Executors.newFixedThreadPool(2);
			exec.submit(new ReverseQueryGenerator(1, schema, query));
			exec.submit(new ShowStopper(timeout));
			exec.shutdown();
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * Report the finding of a MatchMaker.
	 *
	 * @author Julien Leblay
	 */
	public static class MatchReport extends Thread {

		/** The match maker. */
		private final MatchMaker mm;

		/**
		 * Instantiates a new match report.
		 *
		 * @param mm the mm
		 */
		public MatchReport(MatchMaker mm) {
			this.mm = mm;
		}
		
		/**
		 * {@inheritDoc}
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			this.mm.report();
		}
	}
		
	
	/**
	 * This is aimed at forcing the end of a test, thus by-passing the internal 
	 * search timeout mechanism.
	 * 
	 * @author Julien Leblay
	 *
	 */
	public static class ShowStopper extends Thread {
	
		/** The timeout (in milliseconds). */
		private long timeout = -1L;
		
		/**
		 * Instantiates a new show stopper.
		 *
		 * @param timeout the timeout
		 */
		public ShowStopper(long timeout) {
			this.setDaemon(true);
			this.timeout = timeout;
		}
		
		/**
		 * {@inheritDoc}
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			if (this.timeout > 0l) {
				try {
					Thread.sleep(this.timeout);
					Runtime.getRuntime().exit(-1);
				} catch (InterruptedException e) {
					log.error(e.getMessage(),e);
				}
			}
		}
	}
}
