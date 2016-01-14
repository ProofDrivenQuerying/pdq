package uk.ac.ox.cs.pdq.generator.third;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters.QueryTypes;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerJoinCardinalityEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.generator.second.QueryGeneratorSecond;
import uk.ac.ox.cs.pdq.io.pretty.AlgebraLikeLeftDeepPlanWriter;
import uk.ac.ox.cs.pdq.io.pretty.VeryPrettyQueryWriter;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.Planner;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;

import com.google.common.collect.Sets;

/**
 * 
 * @author Julien LEBLAY
 *
 */
public class MultiThreadedQueryGenerator implements Runnable {

	protected static Logger log = Logger.getLogger(MultiThreadedQueryGenerator.class);
	
	private static Integer seeds = 0;

	private final Schema schema;
	private final Integer threadId;
	private final Set<List<Object>> configs;

	private Integer done = 0;
	
	public MultiThreadedQueryGenerator(Integer threadId, Schema schema, Set<List<Object>> configs) {
		this.threadId = threadId;
		this.schema = schema;
		this.configs = configs;
	}

	public static synchronized Integer getSeed() {
		return seeds++;
	}

	@Override
	public void run() {
		BenchmarkParameters params = new BenchmarkParameters();
		Schema schemaNoDep = Schema.builder(this.schema).disableDependencies().build();

		try (PrintStream fos = new PrintStream(new File("test/output/query-gen3-thread-"
				+ String.format("%03d", this.threadId) + ".log"))) {
				for (List<Object> config : this.configs) {

				params.setQueryType((QueryTypes) config.get(0));
				params.setQueryConjuncts((Integer) config.get(1));
				params.setFreeVariable((Double) config.get(2));
				ConjunctiveQuery query = new QueryGeneratorSecond(this.schema, params).generate();

				fos.println("######################################");
				VeryPrettyQueryWriter.to(fos).write(query);
				try {
					PlannerParameters pParams = new PlannerParameters();
					CostParameters cParams = new CostParameters();
					ReasoningParameters reasoningParams = new ReasoningParameters();
					params.setTimeout(60000);
					Planner plannerNoDep = new Planner(pParams, cParams, reasoningParams, schemaNoDep);
					Plan planNoDep = plannerNoDep.search(query);
					if (planNoDep != null) {
						fos.print("+++ Answerable w/o IC ");
					} else {
						fos.print("--- Not answerable w/o IC ");
					}
					Planner planner = new Planner(pParams, cParams, reasoningParams, this.schema);
					Plan plan = planner.search(query);
					if (plan != null) {
						fos.println("\t+++ Answerable " + plan.getCost() + " ");
						AlgebraLikeLeftDeepPlanWriter.to(fos).write((LeftDeepPlan) plan);
					} else {
						fos.println("\t--- Not answerable");
					}
					if (planNoDep == null && plan != null) {
						fos.println(":):):):):):):):):):):):):):):):)");
					}
				} catch (PlannerException e) {
					fos.println("Exception " + e);
				}
				
				fos.println("######################################");
				fos.flush();
				log.trace("Thread " + this.threadId + ": done " + (++this.done) + " cases.");
				}
		} catch (FileNotFoundException e1) {
			throw new IllegalStateException();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	public static void main(String... args) {
		try (FileInputStream fis = new FileInputStream("test/input/web-schema-02.xml")) {
			Schema schema = new SchemaReader().read(fis);

			Set<QueryTypes> queryTypes = Sets.newLinkedHashSet();
			Collections.addAll(queryTypes, QueryTypes.ACYCLIC, QueryTypes.GUARDED, QueryTypes.CHAINGUARDED);
			Set<Integer> conjuncts = Sets.newLinkedHashSet();
			Collections.addAll(conjuncts, 1, 3, 5, 10);
			Set<Double> freeVarRatios = Sets.newLinkedHashSet();
			Collections.addAll(freeVarRatios, 0.1, 0.2);

			Set<List<Object>> configs = Sets.<Object> cartesianProduct(queryTypes, conjuncts, freeVarRatios);
			ExecutorService exec = Executors.newFixedThreadPool(50);
			for (int j = 0, k = 20; j < k; j++) {
				exec.submit(new MultiThreadedQueryGenerator(j, schema, configs));
			}
			exec.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
