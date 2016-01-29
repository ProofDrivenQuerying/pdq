package uk.ac.ox.cs.pdq.generator.queryfromids;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.generator.AbstractGenerator;
import uk.ac.ox.cs.pdq.io.xml.QueryWriter;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaWriter;

/**
 * Creates inclusion dependencies and then queries using the previously created dependencies
 * @author Efthymia Tsamoura
 *
 */
public class GeneratorSecond extends AbstractGenerator{

	public GeneratorSecond(BenchmarkParameters parameters, String schemaFile, String queryFile, PrintStream out) {
		super(parameters, schemaFile, queryFile, out);
	}

	@Override
	public void make() throws IOException {
		// Load the statistic collector/logger
		Schema schema = null;
		ConjunctiveQuery query = null;
		if (this.schemaFile != null && !this.schemaFile.trim().isEmpty()) {
			try (FileInputStream fis = new FileInputStream(this.schemaFile)) {
				schema = Schema.builder(new SchemaReader().read(fis)).build();
			}
			query = new QueryGeneratorSecond(schema, this.parameters).generate();
			new QueryWriter().write(this.out, query);
			return;
		}
		schema = new SchemaGeneratorSecond(this.parameters).generate();
		schema = new DependencyGeneratorSecond(schema, this.parameters).generate();
		new SchemaWriter().write(this.out, schema);
	}

}
