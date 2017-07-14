package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.cost.RelationalTermCardinalityMetadata;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;

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
	
	protected final Catalog catalog;

	/**
	 * Constructor for NaiveCardinalityEstimator.
	 * @param schema Schema
	 */
	public NaiveCardinalityEstimator(Catalog catalog) {
		this.catalog = catalog;
	}

	/**
	 * Clone.
	 *
	 * @return NaiveCardinalityEstimator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator#clone()
	 */
	@Override
	public NaiveCardinalityEstimator clone() {
		return new NaiveCardinalityEstimator(this.catalog);
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
		RelationalTerm leftChild = o.getChildren()[0];
		RelationalTermCardinalityMetadata lcMetadata = this.getMetadata(leftChild);
		lcMetadata.setParent(o);
		lcMetadata.setInputCardinality(inputCard);
		this.estimateIfNeeded(leftChild);
		// Compute the join cardinality itself.
		RelationalTerm rightChild = o.getChildren()[1];
		
		Double rightInputCard = inputCard;

		RelationalTermCardinalityMetadata rcMetadata = this.getMetadata(rightChild);
		rcMetadata.setParent(o);
		rcMetadata.setInputCardinality(rightInputCard);
		this.estimateIfNeeded(rightChild);
		for (RelationalTerm child: o.getChildren()) {
			Double childCard = this.getMetadata(child).getOutputCardinality();
			result *= childCard;
			largestChild = Math.max(largestChild, childCard);
		}
		
		if(o.getPredicate() instanceof SimpleCondition) 
			return Math.max(largestChild, (result / Math.pow(JOIN_REDUCTION,1)));
		else if(o.getPredicate() instanceof ConjunctiveCondition) 
			return Math.max(largestChild, (result / Math.pow(JOIN_REDUCTION, ((ConjunctiveCondition) o.getPredicate()).getNumberOfConjuncts())));
		else 
			throw new IllegalStateException("Unknown condition type");
	}
	
	/**
	 * Estimate output.
	 *
	 * @param o Join
	 * @return Double
	 */
	@Override
	protected Double estimateOutput(DependentJoinTerm o) {
		Double result = 1.0;
		Double largestChild = 1.0;
		Double inputCard = this.getParentInputCardinality(o);
		// Compute the horizontal increase of input card.
		RelationalTerm leftChild = o.getChildren()[0];
		RelationalTermCardinalityMetadata lcMetadata = this.getMetadata(leftChild);
		lcMetadata.setParent(o);
		lcMetadata.setInputCardinality(inputCard);
		this.estimateIfNeeded(leftChild);
		// Compute the join cardinality itself.
		RelationalTerm rightChild = o.getChildren()[1];
		
		Double rightInputCard = lcMetadata.getOutputCardinality() * Math.max(1.0, inputCard);
		
		RelationalTermCardinalityMetadata rcMetadata = this.getMetadata(rightChild);
		rcMetadata.setParent(o);
		rcMetadata.setInputCardinality(rightInputCard);
		this.estimateIfNeeded(rightChild);
		for (RelationalTerm child: o.getChildren()) {
			Double childCard = this.getMetadata(child).getOutputCardinality();
			result *= childCard;
			largestChild = Math.max(largestChild, childCard);
		}
		
		if(o.getPredicate() instanceof SimpleCondition) 
			return Math.max(largestChild, (result / Math.pow(JOIN_REDUCTION,1)));
		else if(o.getPredicate() instanceof ConjunctiveCondition) 
			return Math.max(largestChild, (result / Math.pow(JOIN_REDUCTION, ((ConjunctiveCondition) o.getPredicate()).getNumberOfConjuncts())));
		else 
			throw new IllegalStateException("Unknown condition type");
	}
	
	@Override
	protected Double estimateOutput(AccessTerm o) {
		AccessMethod binding = o.getAccessMethod();
		Relation relation = o.getRelation();
		if (binding.getNumberOfInputs() == 0) 
			return new Double(this.catalog.getCardinality(relation));
		else 
			return Math.max(1.0, (long) (this.catalog.getCardinality(relation) / Math.pow(SELECTIVITY_REDUCTION, binding.getNumberOfInputs())));
	}
		
	/**
	 * Estimate output.
	 *
	 * @param o Selection
	 * @return Double
	 */
	@Override
	protected Double estimateOutput(SelectionTerm o) {
		RelationalTerm child = o.getChildren()[0];
		RelationalTermCardinalityMetadata cMetadata = this.getMetadata(child);
		cMetadata.setParent(o);
		Double inputCard = this.getParentInputCardinality(o);
		cMetadata.setInputCardinality(inputCard);
		this.estimate(child);
		if(o.getPredicate() instanceof SimpleCondition) 
			return Math.max(1L, (cMetadata.getOutputCardinality() / Math.pow(SELECTIVITY_REDUCTION,1)));
		else if(o.getPredicate() instanceof ConjunctiveCondition) 
			return Math.max(1L, (cMetadata.getOutputCardinality() / Math.pow(SELECTIVITY_REDUCTION, ((ConjunctiveCondition) o.getPredicate()).getNumberOfConjuncts())));
		else 
			throw new IllegalStateException("Unknown condition type");
			
		
	}
}