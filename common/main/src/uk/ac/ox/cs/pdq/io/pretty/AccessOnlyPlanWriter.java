package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.output.WriterOutputStream;

import uk.ac.ox.cs.pdq.io.Writer;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.LinearPlan;

/**
 * Writes a concise representation of a plan to the given output
 * 
 * @author Julien Leblay
 *
 */
public class AccessOnlyPlanWriter extends PrettyWriter<LinearPlan> implements Writer<LinearPlan> {

	/**
	 * The default out to which plans should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/**
	 * 
	 * @param out the default output
	 */
	private AccessOnlyPlanWriter(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Fluent pretty writer provider.
	 * @param out
	 * @return a new PrettyLinearPlanWriter with the given default output.
	 */
	public static AccessOnlyPlanWriter to(PrintStream out) {
		return new AccessOnlyPlanWriter(out);
	}

	/**
	 * Fluent pretty writer provider.
	 * @param out
	 * @return a new PrettyLinearPlanWriter with the given default output.
	 */
	public static AccessOnlyPlanWriter to(java.io.Writer out) {
		return new AccessOnlyPlanWriter(new PrintStream(new WriterOutputStream(out)));
	}

	/**
	 * @param out PrintStream
	 * @param plan LinearPlan
	 */
	@Override
	public void write(PrintStream out, LinearPlan plan) {
		StringBuilder result = new StringBuilder();
		List<AccessOperator> accesses = new ArrayList<>(plan.getAccesses());
		String sep = "";
		for (Iterator<AccessOperator> it = accesses.iterator(); it.hasNext();) {
			AccessOperator command = it.next();
			result.append(sep).append(command);
			sep = ",";
		}
		out.println(result);
	}
	
	/**
	 * @param q LinearPlan
	 */
	@Override
	public void write(LinearPlan q) {
		this.write(this.out, q);
	}
	
//	/**
//	 * @param args String[]
//	 */
//	public static void main(String... args) {
//		try(FileInputStream sis = new FileInputStream(args[0]);
//			FileInputStream pis = new FileInputStream(args[1])) {
//			Schema schema = new SchemaReader().read(sis);
//			LinearPlan plan = Readers.with(schema).from(pis).read();
//			new AccessOnlyPlanWriter(System.out).write(plan);
//		} catch ( IOException e) {
//			e.printStackTrace();
//		}
//	}
}
