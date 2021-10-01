// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.estimators;

import com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Relation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Compute the estimated input and output cardinalities of a logical operator
 * and its descendants, based on a naive criteria, in particular using fixed
 * selectivity ratios.
 *
 * @author Julien Leblay
 */
public class NaiveCardinalityEstimator implements CardinalityEstimator {

	private final Map<RelationalTerm,NaiveRelationalTermCardinalityMetadata> cardinalityMetadata;

	/** The Constant UNION_REDUCTION. */
	public static final Double UNION_REDUCTION = 2.0;

	/** The Constant SELECTIVITY_REDUCTION. */
	public static final Double SELECTIVITY_REDUCTION = 10.0;

	/** The Constant DISTINCT_REDUCTION. */
	public static final Double DISTINCT_REDUCTION = 2.0;

	/** The Constant JOIN_REDUCTION. */
	public static final Double JOIN_REDUCTION = 10.0;

	private final Catalog catalog;

	/**
	 * Constructor for NaiveCardinalityEstimator.
	 * @param catalog
	 */
	public NaiveCardinalityEstimator(Catalog catalog) {
		Preconditions.checkNotNull(catalog);
		this.catalog = catalog;
		this.cardinalityMetadata = new LinkedHashMap<>();
	}

	/**
	 * Cardinality estimations are cached, this will execute the estimation only if we have a 0 cached value.
	 * @param term LogicalOperator
	 */
	public void estimateCardinalityIfNeeded(RelationalTerm term) {
		NaiveRelationalTermCardinalityMetadata metadata = this.getCardinalityMetadata(term);
		if (metadata.getOutputCardinality() < 0) {
			this.estimateCardinality(term);
		}
	}

	/**
	 * Main estimation function
	 *
	 * @param term LogicalOperator
	 */
	@Override
	public void estimateCardinality(RelationalTerm term) {
		synchronized (term) {
			Double output = -1.0;
			NaiveRelationalTermCardinalityMetadata metadata = this.getCardinalityMetadata(term);
			Double input = metadata.getInputCardinality();
			RelationalTerm parent = metadata.getParent();
			if (input < 0) {
				input = 0.0;
				if (parent != null) 
					input = this.getCardinalityMetadata(parent).getInputCardinality();
				throw new IllegalStateException("Inconsistent input cardinality '" + input + "' for " + term);
			}

			// For Scan, Access, Distinct, Union, Selection and Join
			// The estimation is delegated to specialised estimators.
			if (term instanceof DependentJoinTerm) {
				output = this.estimateOutputCardinality((DependentJoinTerm) term);
			} 
			else if (term instanceof JoinTerm) {
					output = this.estimateOutputCardinality((JoinTerm) term);
			} 
			else if (term instanceof AccessTerm) {
				output = this.estimateOutputCardinality((AccessTerm) term);
			} 
			else if (term instanceof SelectionTerm) {
				output = this.estimateOutputCardinality((SelectionTerm) term);
			} 
			else if (term instanceof ProjectionTerm || term instanceof RenameTerm) {
				RelationalTerm child = term.getChild(0);
				NaiveRelationalTermCardinalityMetadata childMetadata = this.getCardinalityMetadata(child);
				childMetadata.setParent(term);
				childMetadata.setInputCardinality(input);
				this.estimateCardinalityIfNeeded(child);
				output = childMetadata.getOutputCardinality();
			} 
			// Cross Products: cardinality is the product of the children's cardinalities
			else if (term instanceof CartesianProductTerm) {
				output = 1.0;
				for (int childIndex = 0; childIndex < 2; ++childIndex) {
					RelationalTerm child = term.getChild(childIndex);
					NaiveRelationalTermCardinalityMetadata childMetadata = this.getCardinalityMetadata(child);
					childMetadata.setParent(term);
					childMetadata.setInputCardinality(input);
					this.estimateCardinalityIfNeeded(child);
					output *= childMetadata.getOutputCardinality();
				}
			} 
			metadata.setInputCardinality(input);
			metadata.setOutputCardinality(output);
		}
	}

	/**
	 * 
	 *
	 * @param o LogicalOperator
	 * @return M
	 */
	@Override
	public NaiveRelationalTermCardinalityMetadata getCardinalityMetadata(RelationalTerm o) {
		NaiveRelationalTermCardinalityMetadata result = this.cardinalityMetadata.get(o);
		if (result == null) {
			result = this.initMetadata(o);
			this.cardinalityMetadata.put(o, result);
		}
		return result;
	}

	/**
	 * 
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
		NaiveRelationalTermCardinalityMetadata metadata = this.getCardinalityMetadata(o);
		RelationalTerm parent = metadata.getParent();
		if (parent != null) {
			return this.getCardinalityMetadata(parent).getInputCardinality();
		}
		return 0.0;
	}

	/**
	 * Call to estimate a join
	 *
	 * @param o Join
	 * @return Double
	 */
	protected Double estimateOutputCardinality(JoinTerm o) {
		Double result = 1.0;
		Double largestChild = 1.0;
		Double inputCard = this.getParentInputCardinality(o);
		// Compute the horizontal increase of input card.
		RelationalTerm leftChild = o.getChild(0);
		NaiveRelationalTermCardinalityMetadata lcMetadata = this.getCardinalityMetadata(leftChild);
		lcMetadata.setParent(o);
		lcMetadata.setInputCardinality(inputCard);
		this.estimateCardinalityIfNeeded(leftChild);
		// Compute the join cardinality itself.
		RelationalTerm rightChild = o.getChild(1);

		Double rightInputCard = inputCard;

		NaiveRelationalTermCardinalityMetadata rcMetadata = this.getCardinalityMetadata(rightChild);
		rcMetadata.setParent(o);
		rcMetadata.setInputCardinality(rightInputCard);
		this.estimateCardinalityIfNeeded(rightChild);
		for (int childIndex = 0; childIndex < o.getNumberOfChildren(); ++childIndex) {
			RelationalTerm child = o.getChild(childIndex);
			Double childCard = this.getCardinalityMetadata(child).getOutputCardinality();
			result *= childCard;
			largestChild = Math.max(largestChild, childCard);
		}

		if(o.getJoinConditions() instanceof SimpleCondition) 
			return Math.max(largestChild, (result / Math.pow(JOIN_REDUCTION,1)));
		else if(o.getJoinConditions() instanceof ConjunctiveCondition) 
			return Math.max(largestChild, (result / Math.pow(JOIN_REDUCTION, ((ConjunctiveCondition) o.getJoinConditions()).getNumberOfConjuncts())));
		else 
			throw new IllegalStateException("Unknown condition type");
	}

	/**
	 * Estimate call for a dependent join
	 *
	 * @param o Join
	 * @return Double
	 */
	protected Double estimateOutputCardinality(DependentJoinTerm o) {
		Double result = 1.0;
		Double largestChild = 1.0;
		Double inputCard = this.getParentInputCardinality(o);
		// Compute the horizontal increase of input card.
		RelationalTerm leftChild = o.getChild(0);
		NaiveRelationalTermCardinalityMetadata lcMetadata = this.getCardinalityMetadata(leftChild);
		lcMetadata.setParent(o);
		lcMetadata.setInputCardinality(inputCard);
		this.estimateCardinalityIfNeeded(leftChild);
		// Compute the join cardinality itself.
		RelationalTerm rightChild = o.getChild(1);

		Double rightInputCard = lcMetadata.getOutputCardinality() * Math.max(1.0, inputCard);

		NaiveRelationalTermCardinalityMetadata rcMetadata = this.getCardinalityMetadata(rightChild);
		rcMetadata.setParent(o);
		rcMetadata.setInputCardinality(rightInputCard);
		this.estimateCardinalityIfNeeded(rightChild);
		for (int childIndex = 0; childIndex < o.getNumberOfChildren(); ++childIndex) {
			RelationalTerm child = o.getChild(childIndex);
			Double childCard = this.getCardinalityMetadata(child).getOutputCardinality();
			result *= childCard;
			largestChild = Math.max(largestChild, childCard);
		}

		if(o.getJoinConditions() instanceof SimpleCondition) 
			return Math.max(largestChild, (result / Math.pow(JOIN_REDUCTION,1)));
		else if(o.getJoinConditions() instanceof ConjunctiveCondition) 
			return Math.max(largestChild, (result / Math.pow(JOIN_REDUCTION, ((ConjunctiveCondition) o.getJoinConditions()).getNumberOfConjuncts())));
		else 
			throw new IllegalStateException("Unknown condition type");
	}

	protected Double estimateOutputCardinality(AccessTerm o) {
		AccessMethodDescriptor binding = o.getAccessMethod();
		Relation relation = o.getRelation();
		if (binding.getNumberOfInputs() == 0) 
			return (double)(this.catalog.getCardinality(relation));
		else 
			return Math.max(1.0, (long) (this.catalog.getCardinality(relation) / Math.pow(SELECTIVITY_REDUCTION, binding.getNumberOfInputs())));
	}

	/**
	 * Call for a selection
	 *
	 * @param o Selection
	 * @return Double
	 */
	protected Double estimateOutputCardinality(SelectionTerm o) {
		RelationalTerm child = o.getChild(0);
		NaiveRelationalTermCardinalityMetadata cMetadata = this.getCardinalityMetadata(child);
		cMetadata.setParent(o);
		Double inputCard = this.getParentInputCardinality(o);
		cMetadata.setInputCardinality(inputCard);
		this.estimateCardinality(child);
		if(o.getSelectionCondition() instanceof SimpleCondition) 
			return Math.max(1L, (cMetadata.getOutputCardinality() / Math.pow(SELECTIVITY_REDUCTION,1)));
		else if(o.getSelectionCondition() instanceof ConjunctiveCondition) 
			return Math.max(1L, (cMetadata.getOutputCardinality() / Math.pow(SELECTIVITY_REDUCTION, ((ConjunctiveCondition) o.getSelectionCondition()).getNumberOfConjuncts())));
		else 
			throw new IllegalStateException("Unknown condition type");	
	}
}