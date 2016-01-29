package uk.ac.ox.cs.pdq.generator.queryfromids2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.LinearGuarded;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.generator.DependencyGenerator;
import uk.ac.ox.cs.pdq.generator.tgdsfromquery.AbstractDependencyGenerator;

/**
 * Creates inclusion dependencies given a set of relations and external parameters.
 * 
 * @author Julien LEBLAY
 * 
 */
public class DependencyGeneratorThird extends AbstractDependencyGenerator implements DependencyGenerator{

	/** Logger. */
	private static Logger log = Logger.getLogger(DependencyGeneratorThird.class);
	/**
	 * Default constructor.
	 * 
	 * @param schema
	 */
	public DependencyGeneratorThird(Schema schema, BenchmarkParameters params) {
		super(schema, params);
	}
	
	@Override
	public Schema generate() {
		SchemaBuilder sb = Schema.builder(this.schema);
		return sb.addDependencies(this.generateInclusionDependencies()).build();

	}


	/**
	 * Generates inclusion dependencies randomly from a collection of relations
	 * 
	 * @param relations
	 * @return a list of randomly generated inclusion dependencies.
	 */
	public List<Constraint> generateInclusionDependencies() {
		List<Relation> relations = this.schema.getRelations();
		List<Constraint> result = new ArrayList<>();
		for (int i = 0, l = this.params.getNumberOfConstraints(); i < l; i++) {
			Relation r1 = relations.get(this.random.nextInt(relations.size()));
			Relation r2 = relations.get(this.random.nextInt(relations.size()));
			int a1 = r1.getArity();
			int a2 = r2.getArity();
			
			ForeignKey fk = new ForeignKey();
			fk.setForeignRelation(r2);
			List<Attribute> att1 = new ArrayList<>(r1.getAttributes());
			List<Attribute> att2 = new ArrayList<>(r2.getAttributes());
			
			int nbRef = this.random.nextInt(Math.min(a1, a2)) + 1;
			for (int j = 0; j < nbRef; j++) {
				Collections.shuffle(att1, this.random);
				Collections.shuffle(att2, this.random);
				Reference ref = new Reference(att1.remove(0), att2.remove(0));
				fk.addReference(ref);
			}
			result.add(new LinearGuarded(r1, fk));
		}
		return result;
	}
	
}
