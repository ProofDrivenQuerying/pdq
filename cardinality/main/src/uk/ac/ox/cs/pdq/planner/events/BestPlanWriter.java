package uk.ac.ox.cs.pdq.planner.events;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.io.xml.PlanWriter;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.eventbus.Subscribe;

/**
 * Writes plans to some given output. 
 * @author Julien Leblay
 *
 */
public class BestPlanWriter implements EventHandler {
	
	private final String output;
	
	public BestPlanWriter(String f) {
		this.output = f;
	}
	
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
