package uk.ac.ox.cs.pdq.datasources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * This class extends the functionality of an AccessMethodDescriptor used in
 * common with attribute mapping. Different accessMethod types such as database
 * or webservice access methods can have different set of attributes, mapping of
 * such attributes to the relation's attributes happens here.
 * 
 * @author gabor
 *
 */
public abstract class AbstractAccessMethod extends AccessMethod {
	private static final long serialVersionUID = 1L;

	public AbstractAccessMethod(Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(inputs);
	}

	public AbstractAccessMethod(String name, Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(name, inputs);
	}

	public AbstractAccessMethod(Attribute[] attributes, Set<Attribute> inputAttributes, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(convertInputs(attributes, inputAttributes));
	}

	public AbstractAccessMethod(String name, Attribute[] attributes, Set<Attribute> inputAttributes, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(name, convertInputs(attributes, inputAttributes));
	}

	protected abstract Stream<Tuple> fetchTuples(Iterator<Tuple> inputTuples);

	/**
	 * Converts the set of input attributes to an array of input indexes.
	 * 
	 * @param attributes
	 * @param inputAttributes
	 * @return
	 */
	private static Integer[] convertInputs(Attribute[] attributes, Set<Attribute> inputAttributes) {
		Integer inputs[] = new Integer[inputAttributes.size()];
		List<Attribute> attributeList = Arrays.asList(attributes);
		int index = 0;
		for (Attribute a : inputAttributes) {
			inputs[index] = attributeList.indexOf(a);
			index++;
		}
		return inputs;
	}
	
	protected Attribute[] outputAttributes(boolean b) {
		return null;
	}
	
	protected Attribute[] inputAttributes(boolean b) {
		return null;
	}
	
	protected ConjunctiveCondition accessCondition(Tuple next) {
		return null;
	}

	/** Creates a default mapping when the access method attributes are matching the relation's attributes.
	 * @param relation
	 * @return
	 */
	public static Map<Attribute,Attribute> getDefaultMapping(Relation relation) {
		Map<Attribute,Attribute> res = new HashMap<>();
		for (Attribute a: relation.getAttributes())
			res.put(a, a);
		return res;
	}
}
