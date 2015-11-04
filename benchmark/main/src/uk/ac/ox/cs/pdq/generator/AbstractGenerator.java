package uk.ac.ox.cs.pdq.generator;

import java.io.IOException;
import java.io.PrintStream;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public abstract class AbstractGenerator {

	protected final BenchmarkParameters parameters;

	protected final String schemaFile;
	
	protected final String queryFile;
	
	protected final PrintStream out;
	
	public AbstractGenerator(BenchmarkParameters parameters, String schemaFile, String queryFile, PrintStream out) {
		this.parameters = parameters;
		this.schemaFile = schemaFile;
		this.queryFile = queryFile;
		this.out = out;
	}
	
	public abstract void make() throws IOException;
}
