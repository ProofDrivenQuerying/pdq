package uk.ac.ox.cs.pdq.generator.third;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.generator.AbstractGenerator;
import uk.ac.ox.cs.pdq.generator.first.SchemaGeneratorFirst;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.QueryWriter;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaWriter;
import uk.ac.ox.cs.pdq.planner.Planner;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;

/**
 * Creates inclusion dependencies and then queries using the previously created dependencies
 * @author Efthymia Tsamoura
 *
 */
public class GeneratorThird extends AbstractGenerator{

	private static Logger log = Logger.getLogger(GeneratorThird.class);
	
	public GeneratorThird(BenchmarkParameters parameters, String schemaFile, String queryFile, PrintStream out) {
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
			query = new QueryGeneratorThird(schema, this.parameters).generate();
			new QueryWriter().write(this.out, query);
			return;
		}
		schema = this.makeSchema();
		new SchemaWriter().write(this.out, schema);
	}

	/**
	 * Makes a query from the given parameters.
	 * 
	 */
	public Schema makeSchema() {
		// Loading schema
		Schema schema = new SchemaGeneratorFirst(this.parameters).generate();
		// Creation random fk/inclusion dependencies
		schema = new DependencyGeneratorThird(schema, this.parameters).generate();
		if (schema.getRelations().isEmpty()) {
			throw new IllegalStateException("Input schema is empty. Cannot proceed.");
		}
		return schema;
	}

	public static void main (String... args) {
		try(FileInputStream fis = new FileInputStream("test/input/web-schema.xml")) {
			PlannerParameters planParams = new PlannerParameters();
			CostParameters costParams = new CostParameters();
			ReasoningParameters reasoningParams = new ReasoningParameters();
			Schema schema = new SchemaReader().read(fis);
			String[] queryFiles = new File("test/output/").list(new FilenameFilter() {
				@Override public boolean accept(File dir, String name) {
					return name.startsWith("query") && name.endsWith(".xml");
				}});
			Arrays.sort(queryFiles);
			for (String queryFile: queryFiles) {
				try(FileInputStream qis = new FileInputStream("test/output/" + queryFile)) {
					ConjunctiveQuery query = new QueryReader(schema).read(qis);
					
					log.trace(queryFile);
					planParams.setMaxDepth(1);
					Planner planner = new Planner(planParams, costParams, reasoningParams, schema, query);
					if (planner.search() != null) {
						log.trace(" not answerable without constraints");
					}
					planParams.setMaxDepth(10);
					planner = new Planner(planParams, costParams, reasoningParams, schema, query);
					if (planner.search() != null) {
						log.trace(", not answerable with constraints (depth=10)");
					}
					log.trace("\n");
				}
			}
		} catch (PlannerException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(),e);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(),e);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}
}
