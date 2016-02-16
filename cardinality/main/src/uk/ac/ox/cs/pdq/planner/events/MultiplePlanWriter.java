/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.events;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.xml.PlanWriter;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;

// TODO: Auto-generated Javadoc
/**
 * Writes plan to some given output, under different names.
 *  
 * @author Julien Leblay
 *
 */
public class MultiplePlanWriter implements EventHandler {
	
	/**  The logger. */
	public static Logger log = Logger.getLogger(MultiplePlanWriter.class);
	
	/** The schema. */
	private final Schema schema;
	
	/** The output. */
	private final String output;
	
	/** The counter. */
	private int counter = 0;
	
	/**
	 * Instantiates a new multiple plan writer.
	 *
	 * @param f the f
	 * @param s the s
	 */
	public MultiplePlanWriter(String f, Schema s) {
		this.output = f;
		this.schema = s;
	}
	
	/**
	 * Write plan.
	 *
	 * @param plan the plan
	 */
	@Subscribe
	public void writePlan(Plan plan) {
		if (plan != null) {
			String filename = this.output + Strings.padStart("" + (++this.counter), 4, '0');
			try (PrintStream fos = new PrintStream(filename)) {
				PlanWriter.to(fos).write(plan);
			} catch (FileNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
