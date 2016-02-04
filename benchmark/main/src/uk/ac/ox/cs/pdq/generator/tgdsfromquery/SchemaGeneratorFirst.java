package uk.ac.ox.cs.pdq.generator.tgdsfromquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.metadata.RelationMetadata;
import uk.ac.ox.cs.pdq.db.metadata.StaticMetadata;
import uk.ac.ox.cs.pdq.generator.SchemaGenerator;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Creates the relations of a schema
 * 
 * @author Efthymia Tsamoura
 * @author Julien LEBLAY
 * 
 */
public class SchemaGeneratorFirst implements SchemaGenerator {

	protected final BenchmarkParameters params;
	protected final Random random;

	/**
	 * 
	 * @param params
	 */
	public SchemaGeneratorFirst(BenchmarkParameters params) {
		this.params = params;
		this.random = new Random(params.getSeed());
	}

	/**
	 * Generates the relations of a random schema 
	 * 
	 * @return the newly generated schema.
	 */
	@Override
	public Schema generate() {
		return new Schema(this.generateRelations());
	}

	/**
	 * Generates schema relations.
	 * The method reads from a 
	 * 
	 * @return 
	 * the list of generated relations
	 */
	protected List<Relation> generateRelations() {
		List<Relation> relations = new ArrayList<>();
		for (int r = 0; r < this.params.getNumberOfRelations(); r++) {
			int arity = this.params.getArity();
			if (r < this.params.getNumberOfRelations() - 2) {
				arity = this.random.nextInt(this.params.getArity()) + 1;
			}
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
	
	protected void addMetadata(Relation relation, AccessMethod binding) {
		switch (this.params.getCostType()) {
		case SIMPLE_CONSTANT:
			((StaticMetadata) relation.getMetadata()).setPerInputTupleCost(binding, new DoubleCost(this.params.getMeanCost()));
			break;
		default:
			((StaticMetadata) relation.getMetadata()).setPerInputTupleCost(binding, new DoubleCost(Utility.meanDist(this.random, this.params.getMeanCost(), 0.0, this.params.getMaxCost())));
			break;
		}
	}

	protected void addAccessMethods(Relation relation) {
		if (this.params.getNumberOfViews() == 0 || this.params.getNumberOfAccessMethods() > 0) {
			List<AccessMethod> bindings = new ArrayList<>();
			for (int b = 0; b < this.params.getNumberOfAccessMethods() && b < relation.getArity(); ++b) {
				AccessMethod binding = null;
				do{
					binding = this.generateAccessMethod(Types.LIMITED, relation.getArity(), this.params.getInputPosition());
				}while(relation.getAccessMethods().contains(binding));
				
				this.addMetadata(relation, binding);
				bindings.add(binding);
			}
			if (this.params.getFreeAccess() > this.random.nextDouble()) {
				if (bindings.size() >= this.params.getNumberOfAccessMethods()) {
					bindings.remove(this.random.nextInt(bindings.size()));
				}
				AccessMethod binding = this.generateAccessMethod(Types.FREE, relation.getArity(), this.params.getInputPosition());
				this.addMetadata(relation, binding);
				bindings.add(binding);
			}
			relation.setAccessMethods(bindings);
		}
	}

	/**
	 * Generates an access method
	 * 
	 * @param type
	 *            the type of binding pattern to generate among {FREE, LIMITED,
	 *            BOOLEAN}
	 * @param arity
	 *            the arity of the relation
	 *            
	 * @param inputProbability
	 *            the probability that a position is input (only used for type=LIMITED)
	 * @return
	 */
	protected AccessMethod generateAccessMethod(Types type, int arity, double inputProbability) {
		List<Integer> positions = new ArrayList<>();
		switch (type) {
		case LIMITED:
			for (int index = 2; index <= arity; ++index) {
				if (this.random.nextDouble() > inputProbability) {
					positions.add(index);
				}
			}
			if (positions.isEmpty()) {
				positions.add(1);
			}
			break;
		case BOOLEAN:
			for (int index = 1; index <= arity; ++index) {
				positions.add(index);
			}
			break;
		case FREE:
		default:
			break;
		}
		return new AccessMethod(type, positions);
	}
}
