package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.io.Writer;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;

/**
 * Writes a concise representation of a plan to the given output
 * 
 * @author Julien Leblay
 *
 */
public class PrettyLeftDeepPlanWriter extends PrettyWriter<LeftDeepPlan> implements Writer<LeftDeepPlan> {
	/**
	 * The default out to which plans should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/**
	 * 
	 * @param out the default output
	 */
	PrettyLeftDeepPlanWriter(PrintStream out) {
		this.out = out;
	}

	/**
	 * 
	 */
	public PrettyLeftDeepPlanWriter() {
		this(System.out);
	}
	
	/**
	 * Fluent pretty writer provider.
	 * @param out
	 * @return a new PrettyLeftDeepPlanWriter with the given default output.
	 */
	public static PrettyLeftDeepPlanWriter to(PrintStream out) {
		return new PrettyLeftDeepPlanWriter(out);
	}
	
	/**
	 * @param out PrintStream
	 * @param plan LeftDeepPlan
	 */
	@Override
	public void write(PrintStream out, LeftDeepPlan plan) {
		out.println(plan + ": " + plan.getCost());
	}
	
	/**
	 * @param q LeftDeepPlan
	 */
	@Override
	public void write(LeftDeepPlan q) {
		this.write(this.out, q);
	}
	
//	/**
//	 * For simple test purpose only.
//	 * @param args
//	 */
//	public static void main(String... args) {
//		try(InputStream sin = new FileInputStream("../dag_benchmark/test/runtime/tpch/postgresql/inputs/blackbox/tpch_001/schemas/fk_inputs/queries/case_005/schema.xml");
//			InputStream qin = new FileInputStream("../dag_benchmark/test/runtime/tpch/postgresql/inputs/blackbox/tpch_001/schemas/fk_inputs/queries/case_005/expected-plan.xml")) {
//			Schema s = Readers.from(sin).read();
//			LeftDeepPlan p = Readers.from(qin).with(s).read();
//			PrettyLeftDeepPlanWriter.to(System.out).write(p);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
