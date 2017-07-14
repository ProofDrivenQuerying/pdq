package uk.ac.ox.cs.pdq.cost.estimators;

import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
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
public class NaiveCardinalityEstimator implements CardinalityEstimator {
	
	private Map<RelationalTerm,RelationalTermCardinalityMetadata> cardinalityMetadata;

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
	 * Estimate if needed.
	 *
	 * @param term LogicalOperator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator#estimateIfNeeded(RelationalOperator)
	 */
	@Override
	public void estimateIfNeeded(RelationalTerm term) {
		RelationalTermCardinalityMetadata metadata = this.getMetadata(term);
		if (metadata.getOutputCardinality() < 0) {
			this.estimate(term);
		}
	}

	/**
	 * Estimate.
	 *
	 * @param term LogicalOperator
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator#estimate(RelationalOperator)
	 */
	@Override
	public void estimate(RelationalTerm term) {
		synchronized (term) {
			Double output = -1.0;
			RelationalTermCardinalityMetadata metadata = this.getMetadata(term);
			Double input = metadata.getInputCardinality();
			RelationalTerm parent = metadata.getParent();
			if (input < 0) {
				input = 0.0;
				if (parent != null) 
					input = this.getMetadata(parent).getInputCardinality();
				throw new IllegalStateException("Inconsistent input cardinality '" + input + "' for " + term);
			}

			// For Scan, Access, Distinct, Union, Selection and Join
			// The estimation is delegated to specialised estimators.
			if (term instanceof JoinTerm) {
				output = this.estimateOutput((JoinTerm) term);
			} 
			else if (term instanceof AccessTerm) {
				output = this.estimateOutput((AccessTerm) term);
			} 
			else if (term instanceof SelectionTerm) {
				output = this.estimateOutput((SelectionTerm) term);
			} 
			else if (term instanceof ProjectionTerm || term instanceof RenameTerm) {
				RelationalTerm child = term.getChildren()[0];
				RelationalTermCardinalityMetadata childMetadata = this.getMetadata(child);
				childMetadata.setParent(term);
				childMetadata.setInputCardinality(input);
				this.estimateIfNeeded(child);
				output = childMetadata.getOutputCardinality();
			} 
			// Cross Products: cardinality is the product of the children's cardinalities
			else if (term instanceof CartesianProductTerm) {
				output = 1.0;
				for (RelationalTerm child: term.getChildren()) {
					RelationalTermCardinalityMetadata childMetadata = this.getMetadata(child);
					childMetadata.setParent(term);
					childMetadata.setInputCardinality(input);
					this.estimateIfNeeded(child);
					output *= childMetadata.getOutputCardinality();
				}
			} 
			metadata.setInputCardinality(input);
			metadata.setOutputCardinality(output);
		}
	}
	
	/**
	 * Gets the metadata.
	 *
	 * @param o LogicalOperator
	 * @return M
	 */
	@Override
	public RelationalTermCardinalityMetadata getMetadata(RelationalTerm o) {
		RelationalTermCardinalityMetadata result = this.cardinalityMetadata.get(o);
		if (result == null) {
			this.cardinalityMetadata.put(o, this.initMetadata(o));
		}
		return result;
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
	protected NaiveRelationalTermCardinalityMetadata initMetadata(RelationalTerm o) {
		return new NaiveRelationalTermCardinalityMetadata();
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