package uk.ac.ox.cs.pdq.db.metadata;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.util.Tuple;



/**
 * Implementation of relation statistics
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class RelationalDynamicMetadata implements RelationMetadata {

	private Relation relation;

	/** The size (in tuples of the relation) */
	private long size;

	private Map<AccessMethod, Cost> perInputTupleCost;

	private Map<List<Integer>, Double> positionSelectivity;

	private Map<Pair<List<Integer>, Tuple>, Double> valueSelectivity;

	/**
	 * Constructor for RelationalDynamicMetadata.
	 * @param relation Relation
	 */
	public RelationalDynamicMetadata(Relation relation) {
		this.relation = relation;
		this.collectStatistics();
	}

	private void collectStatistics() {
	}

	/**
	 * @return Long
	 * @see uk.ac.ox.cs.pdq.db.metadata.RelationMetadata#getSize()
	 */
	@Override
	public Long getSize() {
		return this.size;
	}

	/**
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

	/**
	 * @param accessMethod AccessMethod
	 * @param c Cost
	 * @see uk.ac.ox.cs.pdq.db.metadata.RelationMetadata#setPerInputTupleCost(AccessMethod, Cost)
	 */
	@Override
	public void setPerInputTupleCost(AccessMethod accessMethod, Cost c) {
		this.perInputTupleCost.put(accessMethod, c);
	}

	/**
	 * @param accessCosts Map<AccessMethod,Cost>
	 * @see uk.ac.ox.cs.pdq.costs.statistics.RelationMetadata#setPerInputTupleCosts(Map<AccessMethod,Cost>)
	 */
	@Override
	public void setPerInputTupleCosts(Map<AccessMethod, Cost> accessCosts) {
		this.perInputTupleCost.putAll(accessCosts);
	}

	/**
	 * @param s Long
	 * @see uk.ac.ox.cs.pdq.db.metadata.RelationMetadata#setSize(Long)
	 */
	@Override
	public void setSize(Long s) {
		this.size = s;
	}

	/**
	 * @param accessMethod AccessMethod
	 * @return Cost
	 * @see uk.ac.ox.cs.pdq.db.metadata.RelationMetadata#getPerInputTupleCost(AccessMethod)
	 */
	@Override
	public Cost getPerInputTupleCost(AccessMethod accessMethod) {
		return this.perInputTupleCost.get(accessMethod);
	}
}
