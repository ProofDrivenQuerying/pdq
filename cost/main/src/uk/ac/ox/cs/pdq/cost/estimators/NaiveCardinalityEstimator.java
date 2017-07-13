package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.RelationalTermCardinalityMetadata;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;

// TODO: Auto-generated Javadoc
/**
 * Compute the estimated input and output cardinalities of a logical operator
 * and its descendants, based on a naive criteria, in particular using fixed
 * selectivity ratios.
 *
 * @author Julien Leblay
 */
public class NaiveCardinalityEstimator extends AbstractCardinalityEstimator {

	/** The Constant UNION_REDUCTION. */
	public static final Double UNION_REDUCTION = 2.0;
	
	/** The Constant SELECTIVITY_REDUCTION. */
	public static final Double SELECTIVITY_REDUCTION = 10.0;
	
	/** The Constant DISTINCT_REDUCTION. */
	public static final Double DISTINCT_REDUCTION = 2.0;
	
	/** The Constant JOIN_REDUCTION. */
	public static final Double JOIN_REDUCTION = 10.0;

	/** The schema. */
	protected final Schema schema;

	/**
	 * Constructor for NaiveCardinalityEstimator.
	 * @param schema Schema
	 */
	public NaiveCardinalityEstimator(Schema schema) {
		this.schema = schema;
	}

	/**
	 * Clone.
	 *
	 * @return NaiveCardinalityEstimator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator#clone()
	 */
	@Override
	public NaiveCardinalityEstimator clone() {
		return new NaiveCardinalityEstimator(this.schema);
	}

	/**
	 * Inits the metadata.
	 *
	 * @param o LogicalOperator
	 * @return NaiveMetadata
	 */
	@Override
	protected NaiveMetadata initMetadata(RelationalTerm o) {
		return new NaiveMetadata();
	}

	/**
	 * Estimate output.
	 *
	 * @param o Scan
	 * @return Double
	 */
	@Override
	protected Double estimateOutput(Scan o) {
		return (double) this.schema.getRelation(o.getRelation().getName()).getMetadata().getSize();
	}

	/**
	 * Gets the parent input cardinality.
	 *
	 * @param o LogicalOperator
	 * @return Double
	 */
	private Double getParentInputCardinality(RelationalTerm o) {
		RelationalTermCardinalityMetadata metadata = this.getMetadata(o);
		RelationalTerm parent = metadata.getParent();
		if (parent != null) {
			return this.getMetadata(parent).getInputCardinality();
		}
		return 0.0;
	}

	/**
	 * Estimate output.
	 *
	 * @param o Join
	 * @return Double
	 */
	@Override
	protected Double estimateOutput(JoinTerm o) {
		Double result = 1.0;
		Double largestChild = 1.0;
		Double inputCard = this.getParentInputCardinality(o);

		// Compute the horizontal increase of input card.
		Double rightInputCard = inputCard;
		RelationalTerm leftChild = o.getChildren().get(0);
		NaiveMetadata lcMetadata = this.getMetadata(leftChild);
		lcMetadata.setParent(o);
		lcMetadata.setInputCardinality(inputCard);
		this.estimateIfNeeded(leftChild);
		if (o instanceof DependentJoinTerm) {
			rightInputCard = lcMetadata.getOutputCardinality() * Math.max(1.0, inputCard);
		}

		// Compute the join cardinality itself.
		RelationalOperator rightChild = o.getChildren().get(1);
		NaiveMetadata rcMetadata = this.getMetadata(rightChild);
		rcMetadata.setParent(o);
		rcMetadata.setInputCardinality(rightInputCard);
		this.estimateIfNeeded(rightChild);
		for (RelationalOperator child: o.getChildren()) {
			Double childCard = this.getMetadata(child).getOutputCardinality();
			result *= childCard;
			largestChild = Math.max(largestChild, childCard);
		}
		return Math.max(largestChild,
				result / Math.pow(JOIN_REDUCTION,
						((ConjunctivePredicate) o.getPredicate()).size()));
	}

	/**
	 * Estimate output.
	 *
	 * @param o Access
	 * @return Double
	 */
	@Override
	protected Double estimateOutput(AccessTerm o) {
		RelationalOperator child = o.getChild();
		if (child == null) {
			return (double) o.getRelation().getMetadata().getSize();
		}
		this.estimateIfNeeded(child);
		return (this.getMetadata(child).getOutputCardinality()
				* o.getRelation().getMetadata().getSize()
				/ SELECTIVITY_REDUCTION);
	}

	/**
	 * Estimate output.
	 *
	 * @param o DependentAccess
	 * @return Double
	 */
	@Override
	protected Double estimateOutput(DependentAccess o) {
		AccessMethod binding = o.getAccessMethod();
		Relation relation = o.getRelation();
		if (binding.getType() == Types.FREE) {
			return (double) relation.getMetadata().getSize();
		}
		return Math.max(1.0, (long) (relation.getMetadata().getSize()
				/ Math.pow(SELECTIVITY_REDUCTION, binding.getInputs().size())));
	}

	/**
	 * Estimate output.
	 *
	 * @param o Selection
	 * @return Double
	 */
	@Override
	protected Double estimateOutput(Selection o) {
		RelationalOperator child = o.getChild();
		NaiveMetadata cMetadata = this.getMetadata(child);
		cMetadata.setParent(o);
		Double inputCard = this.getParentInputCardinality(o);
		cMetadata.setInputCardinality(inputCard);
		this.estimate(child);
		return Math.max(1L,
				(cMetadata.getOutputCardinality()
						/ Math.pow(SELECTIVITY_REDUCTION,
								((ConjunctivePredicate) o.getPredicate()).size())));
	}
}