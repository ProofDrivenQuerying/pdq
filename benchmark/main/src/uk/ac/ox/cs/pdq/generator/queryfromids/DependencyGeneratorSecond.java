package uk.ac.ox.cs.pdq.generator.queryfromids;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.LinearGuarded;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.generator.DependencyGenerator;
import uk.ac.ox.cs.pdq.generator.tgdsfromquery.AbstractDependencyGenerator;

// TODO: Auto-generated Javadoc
/**
 * Creates inclusion dependencies.
 * 
 * 
 * 	Input parameters
 * 	NR=number of relations
	MAR=max arity
	MaxAcc= maximal number of accesses
	MaxCost
	Acc=accessibility: probability that a relation has any access at all
	Free=free-ness: probability that a position in a limited access is free
	
	Conn=connnectivity: probability that two relations are connected by an ID
	Proj=projectivity: probability that a position is projected in an ID
	
	Given this we generate a schema as follows:
	
	For i=1 to NR
		Choose an arity in MAR for R_i
	 	With probability (1-Acc) give R_i no access
	 	Else 
	     	Choose k randomly in [1,MaxAcc]
	     	For j=1 to k
	          	Make access to R_i with positions made free/bound with probability Free
	
			For each R_i, R_j
			        With probability Conn, make an ID between R_i and R_j,
			        projecting out a position with probability Proj

 * 
 * @author Efthymia Tsamoura
 * 
 */
public class DependencyGeneratorSecond extends AbstractDependencyGenerator implements DependencyGenerator{

	/** Logger. */
	private static Logger log = Logger.getLogger(DependencyGeneratorSecond.class);

	/**
	 * Instantiates a new dependency generator second.
	 *
	 * @param schema the schema
	 * @param params the params
	 */
	public DependencyGeneratorSecond(Schema schema, BenchmarkParameters params) {
		super(schema, params);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.generator.DependencyGenerator#generate()
	 */
	@Override
	public Schema generate() {
		SchemaBuilder sb = Schema.builder(this.schema);
		return sb.addDependencies(this.generateInclusionDependencies()).build();

	}

	/**
	 * Generates inclusion dependencies randomly from a collection of relations.
	 *
	 * @return a list of randomly generated inclusion dependencies.
	 */
	public List<Dependency> generateInclusionDependencies() {
		List<Relation> relations = this.schema.getRelations();
		List<Dependency> result = new ArrayList<>();

		for(int i = 0; i < relations.size(); ++i) {
			for(int j = 0; j < relations.size(); ++j) {
				if(i != j && this.params.getConnectivity() > this.random.nextDouble()) {
					Relation r1 = relations.get(i);
					Relation r2 = relations.get(j);
					ForeignKey fk = new ForeignKey();
					fk.setForeignRelation(r2);

					List<Attribute> att1 = new ArrayList<>(r1.getAttributes());
					List<Attribute> att2 = new ArrayList<>(r2.getAttributes());
					for (int k = 0; k < Math.min(att1.size(), att2.size()); k++) {
						if(this.params.getProjectivity() > this.random.nextDouble()) {
							Reference ref = new Reference(att1.get(k), att2.get(k));
							fk.addReference(ref);
						}
					}
					if(fk.getReferenceCount() > 0) {
						result.add(new LinearGuarded(r1, fk));
					}
				}
			}
		}
		return result;
	}

}
