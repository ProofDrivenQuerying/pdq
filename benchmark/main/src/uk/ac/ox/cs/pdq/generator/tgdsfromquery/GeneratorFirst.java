package uk.ac.ox.cs.pdq.generator.tgdsfromquery;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.generator.AbstractGenerator;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaWriter;

// TODO: Auto-generated Javadoc
/**
 * Creates tuple generating dependencies or views given an input query.
 * First, it creates the relations of the schema. 
 * Second, given the schema relations, it creates the query and, finally, 
 * it creates the views/dependencies.
 * 
 * Supports the creation of guarded queries (queries having a guard in their body), 
 * chain guarded queries (a chain query with a guard) and acyclic queries 
 * 
 * 
 * @author Efthymia Tsamoura
 *
 */
public class GeneratorFirst extends AbstractGenerator{

	/**
	 * Instantiates a new generator first.
	 *
	 * @param parameters the parameters
	 * @param schemaFile the schema file
	 * @param queryFile the query file
	 * @param out the out
	 */
	public GeneratorFirst(BenchmarkParameters parameters, String schemaFile, String queryFile, PrintStream out) {
		super(parameters, schemaFile, queryFile, out);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.generator.AbstractGenerator#make()
	 */
	@Override
	public void make() throws IOException {
		// Load the statistic collector/logger
		SchemaWriter writer = new SchemaWriter();
		Schema schema = null;
		ConjunctiveQuery query = null;
		if (this.schemaFile != null && !this.schemaFile.trim().isEmpty()) {
			try (FileInputStream fis = new FileInputStream(this.schemaFile)) {
				schema = Schema.builder(new SchemaReader().read(fis)).build();
			}
			if (this.queryFile != null && !this.queryFile.trim().isEmpty()) {
				try (FileInputStream fis = new FileInputStream(this.queryFile)) {
					query = new QueryReader(schema).read(fis);
				}
				if (this.parameters.getNumberOfViews() > 0) {
					schema = new ViewGeneratorFirst(schema, query, this.parameters).generate();
					writer.write(this.out, schema);
					return;
				}
				schema = new DependencyGeneratorFirst(schema, query, this.parameters).generate();
				writer.write(this.out, schema);
				return;
			} 
			else {
				query = new QueryGeneratorFirst(schema, this.parameters).generate();
				writer.write(this.out, schema);
				return;
			}
		}
		schema = new SchemaGeneratorFirst(this.parameters).generate();
		writer.write(this.out, schema);
	}
	

}
