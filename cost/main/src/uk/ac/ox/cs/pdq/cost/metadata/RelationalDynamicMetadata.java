package uk.ac.ox.cs.pdq.cost.metadata;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Tuple;



// TODO: Auto-generated Javadoc
/**
 * Implementation of relation statistics.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class RelationalDynamicMetadata implements RelationMetadata {

	/** The relation. */
	private Relation relation;

	/**  The size (in tuples of the relation). */
	private long size;

	/** The per input tuple cost. */
	private Map<AccessMethod, Cost> perInputTupleCost;

	/** TOCOMMENT: what is this?. */
	private Map<List<Integer>, Double> positionSelectivity;

	/** TOCOMMENT: what is this? */
	private Map<Pair<List<Integer>, Tuple>, Double> valueSelectivity;

	/**
	 * Constructor for RelationalDynamicMetadata.
	 * TOCOMMENT: hwat is this
	 * @param relation Relation
	 */
	public RelationalDynamicMetadata(Relation relation) {
		this.relation = relation;
		this.collectStatistics();
	}

	/**
	 * Collect statistics.
	 * TCOMMENT: meaning?
	 */
	private void collectStatistics() {
	}

	/**
	 * Gets the size.
	 *
	 * @return Long
	 * @see uk.ac.ox.cs.pdq.cost.metadata.RelationMetadata#getSize()
	 */
	@Override
	public Long getSize() {
		return this.size;
	}

	/**
	 * Gets the selectivity.
	 *
	 * @param positions List<Integer>
	 * @return Double
	 * @see uk.ac.ox.cs.pdq.costs.statistics.RelationMetadata#getSelectivity(List<Integer>)
	 */
	@Override
	public Double getSelectivity(List<Integer> positions) {
		Double result = this.positionSelectivity.get(positions);
		if (result != null) {
			return result;
		}
		return 0.0;
	}

	/**
	 * Gets the selectivity.
	 * TOCOMMENT: what is going on here?
	 *
	 * @param positions List<Integer>
	 * @param tuple Tuple
	 * @return Double
	 * @see uk.ac.ox.cs.pdq.costs.statistics.RelationMetadata#getSelectivity(List<Integer>, Tuple)
	 */
	@Override
	public Double getSelectivity(List<Integer> positions, Tuple tuple) {
		Double result = this.positionSelectivity.get(positions);
		if (result != null) {
			return result;
		}
		return 0.0;
	}

//	/**
//	 * Sets the per input tuple cost.
//	 *
//	 * @param accessMethod AccessMethod
//	 * @param c Cost
//	 * @see uk.ac.ox.cs.pdq.cost.metadata.RelationMetadata#setPerInputTupleCost(AccessMethod, Cost)
//	 */
//	@Override
//	public void setPerInputTupleCost(AccessMethod accessMethod, Cost c) {
//		this.perInputTupleCost.put(accessMethod, c);
//	}
//
//	/**
//	 * Sets the per input tuple costs.
//	 * TOCOMMENT: what is the difference from the previous?
//	 *
//	 * @param accessCosts Map<AccessMethod,Cost>
//	 * @see uk.ac.ox.cs.pdq.costs.statistics.RelationMetadata#setPerInputTupleCosts(Map<AccessMethod,Cost>)
//	 */
//	@Override
//	public void setPerInputTupleCosts(Map<AccessMethod, Cost> accessCosts) {
//		this.perInputTupleCost.putAll(accessCosts);
//	}

	/**
	 * Sets the size.
	 *
	 * @param s Long
	 * @see uk.ac.ox.cs.pdq.cost.metadata.RelationMetadata#setSize(Long)
	 */
	@Override
	public void setSize(Long s) {
		this.size = s;
	}

//	/**
//	 * Gets the per input tuple cost.
//	 *
//	 * @param accessMethod AccessMethod
//	 * @return Cost
//	 * @see uk.ac.ox.cs.pdq.cost.metadata.RelationMetadata#getPerInputTupleCost(AccessMethod)
//	 */
//	@Override
//	public Cost getPerInputTupleCost(AccessMethod accessMethod) {
//		return this.perInputTupleCost.get(accessMethod);
//	}
}
