package uk.ac.ox.cs.pdq.algebra2;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;

// TODO: Auto-generated Javadoc
/**
 * The Interface AccessOperator.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public interface AccessOperator {
	
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
