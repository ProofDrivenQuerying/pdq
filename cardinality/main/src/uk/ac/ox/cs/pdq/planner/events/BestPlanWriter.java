/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.events;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.io.xml.PlanWriter;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.eventbus.Subscribe;

// TODO: Auto-generated Javadoc
/**
 * Writes plans to some given output. 
 * @author Julien Leblay
 *
 */
public class BestPlanWriter implements EventHandler {
	
	/** The output. */
	private final String output;
	
	/**
	 * Instantiates a new best plan writer.
	 *
	 * @param f the f
	 */
	public BestPlanWriter(String f) {
		this.output = f;
	}
	
	/**
	 * Write plan.
	 *
	 * @param plan the plan
	 */
	@Subscribe
	public void writePlan(Plan plan) {
		if (plan != null) {
			try (PrintStream fos = new PrintStream(this.output)) {
				PlanWriter.to(fos).write(plan);
			} catch (FileNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}
	}

}
