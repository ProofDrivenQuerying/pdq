package uk.ac.ox.cs.pdq.planner.events;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.rewrite.sql.SQLTranslator;
import uk.ac.ox.cs.pdq.rewrite.sql.SQLTranslator.SupportedDialect;

import com.google.common.eventbus.Subscribe;

// TODO: Auto-generated Javadoc
/**
 * Writes the SQL translation of a plan to some given output. 
 * @author Julien Leblay
 *
 */
public class BestSQLPlanWriter implements EventHandler {
	
	/** The output. */
	private final String output;
	
	/**
	 * Instantiates a new best sql plan writer.
	 *
	 * @param f the f
	 */
	public BestSQLPlanWriter(String f) {
		this.output = f;
	}
	
	/**
	 * Write plan.
	 *
	 * @param config the config
	 */
	@Subscribe
	public void writePlan(DAGChaseConfiguration config) {
		if (config != null) {
			try (PrintStream fos = new PrintStream(this.output)) {
				fos.append(
						SQLTranslator.target(SupportedDialect.POSTGRESQL)
							.toSQL(config.getPlan().getEffectiveOperator()));
				fos.flush();
			} catch (RewriterException | FileNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}
	}

}
