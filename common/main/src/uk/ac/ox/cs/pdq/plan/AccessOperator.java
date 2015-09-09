package uk.ac.ox.cs.pdq.plan;

import java.util.List;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Operator;

/**
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public interface AccessOperator extends Operator {
	/**
	 * @return Relation
	 */
	Relation getRelation();
	/**
	 * @return AccessMethod
	 */
	AccessMethod getAccessMethod();
	
}
