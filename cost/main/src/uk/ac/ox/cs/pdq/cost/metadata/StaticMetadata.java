package uk.ac.ox.cs.pdq.cost.metadata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.util.Tuple;



// TODO: Auto-generated Javadoc
/**
 * Implementation of relation statistics.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class StaticMetadata implements RelationMetadata {

	/**  The size (in tuples of the relation). */
	private long size;

	/** The per input tuple cost. */
	private final Map<AccessMethod, Cost> perInputTupleCost;

	/**
	 * Instantiates a new static metadata.
	 * TCOMMENT: what is static vs dynamic metadata
	 */
	public StaticMetadata() {
		this(0L, new LinkedHashMap<AccessMethod, Cost>());
	}

	/**
	 * Constructor for StaticMetadata.
	 * @param size Long
	 */
	public StaticMetadata(Long size) {
		this(size, new LinkedHashMap<AccessMethod, Cost>());
	}

	/**
	 * Constructor for StaticMetadata.
	 * TOCOMMENT: difference from above and bdlow
	 * @param perTupleCost Map<AccessMethod,Cost>
	 */
	public StaticMetadata(Map<AccessMethod, Cost> perTupleCost) {
		this(1L, perTupleCost);
	}

	/**
	 * Constructor for StaticMetadata.
	 * @param size Long
	 * @param perTupleCost Map<AccessMethod,Cost>
	 */
	public StaticMetadata(Long size, Map<AccessMethod, Cost> perTupleCost) {
		this.size = size;
		this.perInputTupleCost = perTupleCost;
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
		return 0.1;
	}

	/**
	 * Gets the selectivity.
	 * TOCOMMENT: difference from above?
	 *
	 * @param positions List<Integer>
	 * @param tuple Tuple
	 * @return Double
	 * @see uk.ac.ox.cs.pdq.costs.statistics.RelationMetadata#getSelectivity(List<Integer>, Tuple)
	 */
	@Override
	public Double getSelectivity(List<Integer> positions, Tuple tuple) {
		return 0.1;
	}

	/**
	 * Gets the per input tuple cost.
	 *
	 * @param binding AccessMethod
	 * @return Cost
	 * @see uk.ac.ox.cs.pdq.cost.metadata.RelationMetadata#getPerInputTupleCost(AccessMethod)
	 */
	@Override
	public Cost getPerInputTupleCost(AccessMethod binding) {
		Cost result = this.perInputTupleCost.get(binding);
		if (result == null) {
			result = DoubleCost.UPPER_BOUND;
		}
		return result;
	}

	/**
	 * Sets the per input tuple cost.
	 *
	 * @param binding AccessMethod
	 * @param c Cost
	 * @see uk.ac.ox.cs.pdq.cost.metadata.RelationMetadata#setPerInputTupleCost(AccessMethod, Cost)
	 */
	@Override
	public void setPerInputTupleCost(AccessMethod binding, Cost c) {
		this.perInputTupleCost.put(binding, c);
	}

	/**
	 * Sets the per input tuple costs.
	 * TOCOMMENT: difference from above?
	 *
	 * @param accessCosts Map<AccessMethod,Cost>
	 * @see uk.ac.ox.cs.pdq.costs.statistics.RelationMetadata#setPerInputTupleCosts(Map<AccessMethod,Cost>)
	 */
	@Override
	public void setPerInputTupleCosts(Map<AccessMethod, Cost> accessCosts) {
		this.perInputTupleCost.putAll(accessCosts);
	}

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
}
