package uk.ac.ox.cs.pdq.datasources;

import java.util.List;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * TOCOMMENT I understqnd that there are two views of database objects reflected in the code in common. 
 * On is the traditional where we don't have access restrictions, hence we have normal relations etc.
 * The other is the "access restrictions" perspective and this is why this object exists. Is this the case? 
 * By putting this in a package called wrappers and naming it a wrapper you don't do justice to it if it's the main
 * "access restriction perspective" object.
 * 
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
	 * Gets the name of the relation.
	 *
	 * @return the name of the relation.
	 */
	String getName();

	/**
	 * Gets the access method by its name.
	 *
	 * @param name the name
	 * @return an access method by its name.
	 */
	AccessMethod getAccessMethod(String name);
}
