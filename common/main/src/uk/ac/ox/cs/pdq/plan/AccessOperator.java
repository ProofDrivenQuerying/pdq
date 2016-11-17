package uk.ac.ox.cs.pdq.plan;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Operator;

// TODO: Auto-generated Javadoc
/**
 * The Interface AccessOperator.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public interface AccessOperator extends Operator {
	
	/**
	 * Gets the relation.
	 *
	 * @return Relation
	 */
	Relation getRelation();
	
	/**
	 * Gets the access method.
	 *
	 * @return AccessMethod
	 */
	AccessMethod getAccessMethod();
	
}
