package uk.ac.ox.cs.pdq.generator.second;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.metadata.RelationMetadata;
import uk.ac.ox.cs.pdq.db.metadata.StaticMetadata;
import uk.ac.ox.cs.pdq.generator.first.SchemaGeneratorFirst;

/**
 * Creates the relations of a schema
 * 
 *  Input parameters
 * 	NR=number of relations
	MAR=max arity
	MaxAcc= maximal number of accesses
	MaxCost
	Acc=accessibility: probability that a relation has any access at all
	Free=free-ness: probability that a position in a limited access is free
	
 * @author Efthymia Tsamoura
 * 
 */
public class SchemaGeneratorSecond extends  SchemaGeneratorFirst{

	/**
	 * Default constructor.
	 * 
	 * @param schema
	 */
	public SchemaGeneratorSecond(BenchmarkParameters params) {
		super(params);
	}

	/**
	 * Generates schema relations
	 * 
	 * @return the list of generated relations
	 */
	@Override
	protected List<Relation> generateRelations() {
		List<Relation> relations = new ArrayList<>();
		for (int r = 0; r < this.params.getNumberOfRelations(); r++) {
			int arity = this.random.nextInt(this.params.getArity()) + 1;
			// TODO: use relation factory
			Relation relation = uk.ac.ox.cs.pdq.generator.utils.Utility.createStringsRelation("R" + new Integer(r).toString(), arity);
			RelationMetadata rstatistics = new StaticMetadata(
					(long) this.random.nextInt(this.params.getRelationSize()));
			relation.setMetadata(rstatistics);
			this.addAccessMethods(relation);
			relations.add(relation);
		}
		return relations;
	}

	@Override
	protected void addAccessMethods(Relation relation) {
		if(this.params.getAccessibility() > this.random.nextDouble()) {
			List<AccessMethod> bindings = new ArrayList<>();
			int maxAccessMethods = 1 + this.random.nextInt(this.params.getMaxBindings());
			for (int b = 0; b < Math.min(maxAccessMethods, relation.getArity()); ++b) {
				AccessMethod binding = null;
				do{
					binding = this.generateAccessMethod(relation.getArity(), this.params.getFreePosition());
				}while(relation.getAccessMethods().contains(binding));
				this.addMetadata(relation, binding);
				bindings.add(binding);
			}
			relation.setAccessMethods(bindings);
		}
	}

	/**
	 * Generates a binding pattern
	 * 
	 * @param arity
	 *            the arity of the relation
	 *            
	 * @param freeProbability
	 *            the probability that a position is free
	 * @return
	 */
	protected AccessMethod generateAccessMethod(int arity, double freeProbability) {
		List<Integer> positions = new ArrayList<>();
		for (int index = 1; index <= arity; ++index) {
			if (freeProbability < this.random.nextDouble()) {
				positions.add(index);
			}
		}
		if(positions.isEmpty()) {
			return new AccessMethod(Types.FREE, positions);
		}
		else {
			return new AccessMethod(Types.LIMITED, positions);
		}
	}
}
