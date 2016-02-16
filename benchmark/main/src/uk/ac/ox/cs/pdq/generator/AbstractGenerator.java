package uk.ac.ox.cs.pdq.generator;

import java.io.IOException;
import java.io.PrintStream;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractGenerator.
 *
 * @author Efthymia Tsamoura
 */
public abstract class AbstractGenerator {

	/** The parameters. */
	protected final BenchmarkParameters parameters;

	/** The schema file. */
	protected final String schemaFile;
	
	/** The query file. */
	protected final String queryFile;
	
	/** The out. */
	protected final PrintStream out;
	
	/**
	 * Instantiates a new abstract generator.
	 *
	 * @param parameters the parameters
	 * @param schemaFile the schema file
	 * @param queryFile the query file
	 * @param out the out
	 */
	public AbstractGenerator(BenchmarkParameters parameters, String schemaFile, String queryFile, PrintStream out) {
		this.parameters = parameters;
		this.schemaFile = schemaFile;
		this.queryFile = queryFile;
		this.out = out;
	}
	
	/**
	 * Make.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void make() throws IOException;
}
