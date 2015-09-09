package uk.ac.ox.cs.pdq.db.wrappers;

import java.util.List;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Pipelineable;
import uk.ac.ox.cs.pdq.util.ResetableIterator;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Tuple;


/**
 * The Wrapper interface provide access functions.
 * @author Julien Leblay
 *
 */
public interface RelationAccessWrapper extends Pipelineable {

	/**
	 * Performs an access to the relation with the given inputTuples as input
	 * @return the Table containing the tuples resulting from the access.
	 */
	Table access();

	/**
	 * Performs an access to the relation without any input
	 * @param inputHeader
	 * @param inputTuples
	 * @return the Table containing the tuples resulting from the access.
	 */
	Table access(List<? extends Attribute> inputHeader, ResetableIterator<Tuple> inputTuples);

	/**
	 * @return the list of attributes of the relation.
	 */
	List<Attribute> getAttributes();

	/**
	 * @param b AccessMethod
	 * @return the list of attributes of the relation.
	 */
	List<Attribute> getInputAttributes(AccessMethod b);

	/**
	 * @return the name of the relation.
	 */
	String getName();

	/**
	 * @return an access method by its name.
	 */
	AccessMethod getAccessMethod(String name);
}
