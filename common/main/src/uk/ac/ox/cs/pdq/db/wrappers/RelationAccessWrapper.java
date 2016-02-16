package uk.ac.ox.cs.pdq.db.wrappers;

import java.util.List;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Pipelineable;
import uk.ac.ox.cs.pdq.util.ResetableIterator;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Tuple;


// TODO: Auto-generated Javadoc
/**
 * The Wrapper interface provide access functions.
 * @author Julien Leblay
 *
 */
public interface RelationAccessWrapper extends Pipelineable {

	/**
	 * Performs an access to the relation with the given inputTuples as input.
	 *
	 * @return the Table containing the tuples resulting from the access.
	 */
	Table access();

	/**
	 * Performs an access to the relation without any input.
	 *
	 * @param inputHeader the input header
	 * @param inputTuples the input tuples
	 * @return the Table containing the tuples resulting from the access.
	 */
	Table access(List<? extends Attribute> inputHeader, ResetableIterator<Tuple> inputTuples);

	/**
	 * Gets the attributes.
	 *
	 * @return the list of attributes of the relation.
	 */
	List<Attribute> getAttributes();

	/**
	 * Gets the input attributes.
	 *
	 * @param b AccessMethod
	 * @return the list of attributes of the relation.
	 */
	List<Attribute> getInputAttributes(AccessMethod b);

	/**
	 * Gets the name.
	 *
	 * @return the name of the relation.
	 */
	String getName();

	/**
	 * Gets the access method.
	 *
	 * @param name the name
	 * @return an access method by its name.
	 */
	AccessMethod getAccessMethod(String name);
}
