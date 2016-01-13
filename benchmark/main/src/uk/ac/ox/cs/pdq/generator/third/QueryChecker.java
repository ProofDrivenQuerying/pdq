package uk.ac.ox.cs.pdq.generator.third;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.pretty.AlgebraLikeLeftDeepPlanWriter;
import uk.ac.ox.cs.pdq.io.pretty.VeryPrettyQueryWriter;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.Planner;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;

/**
 * 
 * @author Julien LEBLAY
 *
 */
public class QueryChecker implements Runnable {

	/** Logger. */
	private static Logger log = Logger.getLogger(QueryChecker.class);

	private static Integer seeds = 0;

	private final Integer threadId;
	private final Schema schema;
	private final List<ConjunctiveQuery> queries;

	private Integer done = 0;
	
	public QueryChecker(Integer threadId, Schema schema, List<ConjunctiveQuery> queries) {
		this.threadId = threadId;
		this.schema = schema;
		this.queries = queries;
	}

	public static synchronized Integer getSeed() {
		return seeds++;
	}

	@Override
	public void run() {
		try {
			Schema schemaNoDep = Schema.builder(this.schema).disableDependencies().build();

			for (ConjunctiveQuery query : this.queries) {

				if (query != null) {
					System.out.println("######################################");
					VeryPrettyQueryWriter.to(System.out).write(query);
					try {
						PlannerParameters p = new PlannerParameters();
						CostParameters c = new CostParameters();
						ReasoningParameters r = new ReasoningParameters();
						
						p.setTimeout(600000);
						Planner plannerNoDep = new Planner(p, c, r, schemaNoDep);
						Plan planNoDep = plannerNoDep.search(query);
						if (planNoDep != null) {
							System.out.print("+++ Answerable w/o IC ");
						} else {
							System.out.print("--- Not answerable w/o IC ");
						}
						Planner planner = new Planner(p, c, r, this.schema);
						Plan plan = planner.search(query);
						if (plan != null) {
							System.out.println("\t+++ Answerable " + plan.getCost() + " ");
							AlgebraLikeLeftDeepPlanWriter.to(System.out).write((LeftDeepPlan) plan);
						} else {
							System.out.println("\t--- Not answerable");
						}
						if (planNoDep == null && plan != null) {
							System.out.println(":):):):):):):):):):):):):):):):)");
						}
					} catch (PlannerException e) {
						System.out.println("Exception " + e);
					}
					
					System.out.println("######################################");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		try (FileInputStream fis = new FileInputStream("test/input/web-schema-minimal.xml")) {
			Schema schema = new SchemaReader().read(fis);

			List<ConjunctiveQuery> queries = new LinkedList<>();
			for (File f: new File("../pdq.benchmark/test/dag/web/na/").listFiles()) {
				try(FileInputStream qis = new FileInputStream(f)) {
					ConjunctiveQuery q = new QueryReader(schema).read(qis);
					if (q != null) {
						queries.add(q);
					}
				}
			}
			ExecutorService exec = Executors.newFixedThreadPool(1);
			exec.submit(new QueryChecker(1, schema, queries));
			exec.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
