package uk.ac.ox.cs.pdq.db.metadata;

import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * Interface of any relation-specific statistics that is useful for plan cost estimation
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public interface RelationMetadata {

	/**
	 * @return the cardinality of the relation.
	 */
	Long getSize();

	/**
	 * Sets the cardinality of the relation.
	 * @param s Long
	 */
	void setSize(Long s);

	/**
	 * @param positions List<Integer>
	 * @return the cardinality of the relation.
	 */
	Double getSelectivity(List<Integer> positions);

	/**
	 * @param positions List<Integer>
	 * @param tuple Tuple
	 * @return the cardinality of the relation.
	 */
	Double getSelectivity(List<Integer> positions, Tuple tuple);

	/**
	 * @param accessMethod AccessMethod
	 * @return the cardinality of the relation.
	 */
	Cost getPerInputTupleCost(AccessMethod accessMethod);

	/**
	 * @param accessMethod AccessMethod
	 * @param c Cost
	 */
	void setPerInputTupleCost(AccessMethod accessMethod, Cost c);

	/**
	 * @param costs Map<AccessMethod,Cost>
	 */
	void setPerInputTupleCosts(Map<AccessMethod, Cost> costs);
}
