// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.datasources.tuple.DistinctIterator;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;
import uk.ac.ox.cs.pdq.fol.TypedConstant;

/**
 * This class extends the functionality of an AccessMethodDescriptor used in
 * common with attribute mapping. Different accessMethod types such as database
 * or webservice access methods can have different set of attributes, mapping of
 * such attributes to the relation's attributes happens here.
 * 
 * @author gabor
 *
 */
public abstract class ExecutableAccessMethod extends AccessMethodDescriptor {
	private static final long serialVersionUID = 1L;
	/**
	 * A Constant DEFAULT_PREFIX for all automatically generated access methods
	 * names
	 */
	public static String DEFAULT_PREFIX = "mt_";

	/** Output attributes. */
	protected Attribute[] attributes;

	/** Associated relation. */
	protected Relation relation;
	protected TupleType relationTupleType;

	/** Input attribute positions in the external schema. */
	private Integer[] inputsFromRelation;

	/** Mapping between AccessMethod and Relation attributes */
	protected Map<Attribute, Attribute> attributeMapping;
	protected Map<Attribute, Attribute> inverseAttributeMapping;

	private Attribute[] outputAttributesInternal;
	private Attribute[] outputAttributesExternal;

	private Attribute[] inputAttributesInternal;
	private Attribute[] inputAttributesExternal;

	protected TupleType inputTupleTypeInternal;
	protected TupleType inputTupleTypeExternal;

	/** String representation of the object. */
	protected String toString = null;

	public ExecutableAccessMethod(Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(inputs);
		this.attributes = attributes;
		this.relation = relation;
		this.attributeMapping = attributeMapping;
		init();
	}


	public ExecutableAccessMethod(String name, Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(name, inputs);
		this.attributes = attributes;
		this.relation = relation;
		this.attributeMapping = attributeMapping;
		init();
	}

	public ExecutableAccessMethod(Attribute[] attributes, Set<Attribute> inputAttributes, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(convertInputs(attributes, inputAttributes));
		this.attributes = attributes;
		this.relation = relation;
		this.attributeMapping = attributeMapping;
		init();
	}

	public ExecutableAccessMethod(String name, Attribute[] attributes, Set<Attribute> inputAttributes, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(name, convertInputs(attributes, inputAttributes));
		this.attributes = attributes;
		this.relation = relation;
		this.attributeMapping = attributeMapping;
		init();
	}

	/**
	 * Converts between the method's input positions and the relation's input
	 * positions. 
	 * Lets have R(a,b,c) with and input on attribute "b", giving input position "1" (the numbering starts from zero). 
	 * If we have access method attributes (j,k,l,m) with a mapping of a->k,b->l,c->m, then we
	 * need to map the input position 1 (attribute b in the relation) to input
	 * position 2 (attribute l in the access method)
	 * 
	 * @param attributes
	 * @param inputs
	 * @param relation
	 * @param attributeMapping
	 * @return
	 */
	private Integer[] computeInputsFromRelation(Integer[] inputs, Relation relation) {
		Integer[] ret = new Integer[inputs.length];
		int index = 0;
		for (Integer methodInput : inputs) {
			Attribute methodAttr = attributes[methodInput];
			Attribute relationAttr = getAttributeMapping(false).get(methodAttr);
			ret[index] = Arrays.asList(relation.getAttributes()).indexOf(relationAttr);
			index++;
		}
		return ret;
	}

	private void init() {
		this.inputsFromRelation = computeInputsFromRelation(inputs, relation);
		Arrays.sort(inputsFromRelation);
		
		// Check that the attribute names are distinct.
		Preconditions.checkArgument(Arrays.stream(attributes).map(a -> a.getName()).allMatch(new HashSet<>()::add),
				"Attribute names must be distinct");

		// Check that all of the relation attributes feature in the set of attribute
		// mapping values.
		Preconditions.checkArgument(attributeMapping.values().containsAll(Arrays.asList(relation.getAttributes())),
				"All relation attributes must feature in the attribute mapping values set");

		// Check for compatibility between this access method and the relation via the
		// attributeMapping.
		for (Attribute key : attributeMapping.keySet()) {
			Preconditions.checkArgument(key.getType().equals(attributeMapping.get(key).getType()),
					"Inconsistent types detected in attribute mapping");
			Preconditions.checkArgument(Arrays.asList(attributes).contains(key),
					"Attribute mapping key not found in access method attributes: " + key);
			Preconditions.checkArgument(Arrays.asList(relation.getAttributes()).contains(attributeMapping.get(key)),
					"Attribute mapping value not found in relation attributes");
		}

		// Check that the input indices are valid and that the input attributes are
		// featured
		// in the attribute mapping (else calling this.inputPositions(true) would fail).
		for (int i : inputs) {
			Preconditions.checkArgument(i >= 0 && i < attributes.length, "Invalid input attribute index");
			Preconditions.checkArgument(attributeMapping.containsKey(attributes[i]),
					"All input attributes must feature in the attribute mapping");
		}
		Arrays.sort(inputs);

		// Assign the fields (order matters here).
		this.outputAttributesInternal = this.outputAttributes(false);
		this.outputAttributesExternal = this.outputAttributes(true);

		this.inverseAttributeMapping = this.getAttributeMapping(true);

		this.inputAttributesInternal = this.inputAttributes(false);
		this.inputAttributesExternal = this.inputAttributes(true);

		this.relationTupleType = TupleType.createFromTyped(this.outputAttributesExternal);

		this.inputTupleTypeInternal = TupleType.createFromTyped(this.inputAttributesInternal);
		this.inputTupleTypeExternal = TupleType.createFromTyped(this.inputAttributesExternal);
	}

	/**
	 * Fetches tuples conforming to the schema defined by the output attributes of
	 * this {@code AccessMethod}. The {@code inputTuples} must also conform to the
	 * same schema.
	 * 
	 * @param inputTuples
	 *            An {@code Iterator} of type {@code Tuple}, or null if there are no
	 *            inputs.
	 * @return A {@code Stream} over {@code Tuple}s.
	 */
	protected abstract Stream<Tuple> fetchTuples(Iterator<Tuple> inputTuples);

	public Iterable<Tuple> access(boolean relationSchema) {

		Preconditions.checkState(this.inputAttributes().length == 0);
		return this.access(null, relationSchema);
	}

	public Iterable<Tuple> access(Iterator<Tuple> inputTuples, boolean relationSchema) {

		// If necessary, map the inputTuples from the internal to the external schema.
		if (inputTuples != null && relationSchema) {
			Iterator<Tuple> it = inputTuples;
			Iterable<Tuple> iterable = () -> it;
			inputTuples = StreamSupport.stream(iterable.spliterator(), false).map(tuple -> this.mapInputTuple(tuple))
					.iterator();
		}

		if (inputTuples != null)
			inputTuples = new DistinctIterator<Tuple>(inputTuples);
		Stream<Tuple> ret = this.fetchTuples(inputTuples);

		if (relationSchema)
			ret = ret.map(tuple -> this.mapOutputTuple(tuple));
		return ret::iterator;
	}

	public Iterable<Tuple> access() {
		return this.access(true);
	}

	public Iterable<Tuple> access(Iterator<Tuple> inputTuples) {
		return this.access(inputTuples, true);
	}

	public Attribute[] outputAttributes() {
		return this.outputAttributes(true);
	}

	public Attribute[] inputAttributes() {
		return this.inputAttributes(true);
	}

	public Attribute[] outputAttributes(boolean relationSchema) {

		if (!relationSchema)
			return this.attributes.clone();

		// Note that, when relationSchema is true, the order of the output attributes
		// is that defined in the Relation.
		return this.getRelation().getAttributes();
	}

	public Attribute[] inputAttributes(boolean relationSchema) {

		Attribute[] ret = new Attribute[this.inputsFromRelation.length];
		for (int i = 0; i != this.inputsFromRelation.length; i++)
			ret[i] = this.outputAttributes(relationSchema)[this.inputPositions(relationSchema)[i]];
		return ret;
	}

	public Map<Attribute, Attribute> getAttributeMapping(boolean invert) {

		if (!invert)
			return new HashMap<Attribute, Attribute>(this.attributeMapping);
		return this.attributeMapping.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
	}

	public Integer[] getInputs() {
		return this.inputsFromRelation.clone();
	}

	public int getNumberOfInputs() {
		return this.inputsFromRelation.length;
	}

	public Integer[] inputPositions(boolean relationSchema) {

		if (!relationSchema)
			return super.getInputs();  
		return this.getInputs();
	}

	/**
	 * Map a tuple from the {@code AccessMethod} schema to the {@code Relation}
	 * schema.
	 * 
	 * @param tuple
	 *            A {@code Tuple} conforming to the {@code AccessMethod} schema.
	 * @return A {@code Tuple} conforming to the {@code Relation} schema.
	 * @throws IllegalArgumentException
	 *             if the given tuple is not valid w.r.t. the relevant schema.
	 */
	protected Tuple mapOutputTuple(Tuple tuple) {

		// Get the values from the given tuple.
		Object[] values = tuple.getValues();

		// Rearrange to respect the order of the external schema.
		Object[] rearranged = Arrays.stream(this.outputAttributesExternal).map(
				a -> values[Arrays.asList(this.outputAttributesInternal).indexOf(this.inverseAttributeMapping.get(a))])
				.toArray(Object[]::new);

		return relationTupleType.createTuple(rearranged);
	}

	/**
	 * Maps a tuple conforming to the types of the input attributes in the internal
	 * schema of the {@code Relation} to the corresponding tuple conforming to the
	 * types of the input attributes in the external schema of the
	 * {@code AccessMethod}.
	 * 
	 * @param tuple
	 *            A {@code Tuple}.
	 * @return A {@code Tuple}.
	 * @throws IllegalArgumentException
	 *             if the given tuple does no conform to the types of the input
	 *             attributes in the internal schema of the {@code Relation}.
	 */
	protected Tuple mapInputTuple(Tuple tuple) {

		// Check that the input tuple conforms to the types of the input attributes in
		// the external schema.
		Preconditions.checkArgument(tuple.getType().getTypes().equals(this.inputTupleTypeExternal.getTypes()));

		// Get the values from the next tuple, which is assumed to correspond
		// to the inputAttributes in the internal schema.
		Object[] values = tuple.getValues();

		// Rearrange to respect the order of the internal schema.
		Object[] rearranged = Arrays.stream(this.inputAttributesInternal)
				.map(a -> values[Arrays.asList(this.inputAttributesExternal).indexOf(this.attributeMapping.get(a))])
				.toArray(Object[]::new);

		return inputTupleTypeInternal.createTuple(rearranged);
	}

	/**
	 * Returns a {@code ConjunctiveCondition} encapsulating the information in the
	 * given input {@code Tuple}.
	 * 
	 * @param inputTuple
	 *            A {@code Tuple} conforming to the schema of this
	 *            {@code AccessMethod}.
	 * 
	 * @return A {@code ConjunctiveCondition} corresponding to the given input
	 *         {@code Tuple}.
	 */
	protected ConjunctiveCondition accessCondition(Tuple inputTuple) {

		// The input tuple must conform to the external schema.
		Preconditions.checkArgument(inputTuple.getType().getTypes().equals(this.inputTupleTypeInternal.getTypes()));

		List<SimpleCondition> predicates = new ArrayList<SimpleCondition>();
		Integer[] positions = this.inputPositions(false);
		for (int i = 0; i != inputTuple.size(); i++)
			predicates
					.add(ConstantEqualityCondition.create(positions[i], TypedConstant.create(inputTuple.getValue(i))));

		return ConjunctiveCondition.create(predicates.toArray(new SimpleCondition[0]));
	}

	@Override
	public String toString() {
		if (this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append(this.name);
			if (this.getInputs().length > 0) {
				result.append(':');
				char sep = '[';
				for (int i : this.getInputs()) {
					result.append(sep).append(i);
					sep = ',';
				}
				result.append(']');
			}
			this.toString = result.toString();
		}
		return this.toString;
	}

	public String getName() {
		return this.name;
	}

	public Relation getRelation() {
		return this.relation;
	}

	protected static Map<Attribute, Attribute> createTrivialAttributeMapping(Relation relation) {
		Map<Attribute, Attribute> attributeMapping = new LinkedHashMap<>();
		for (int attributeIndex = 0; attributeIndex < relation.getArity(); ++attributeIndex)
			attributeMapping.put(relation.getAttribute(0), relation.getAttribute(0));
		return attributeMapping;
	}

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

	/**
	 * Creates a default mapping when the access method attributes are matching the
	 * relation's attributes.
	 * 
	 * @param relation
	 * @return
	 */
	public static Map<Attribute, Attribute> getDefaultMapping(Relation relation) {
		Map<Attribute, Attribute> res = new HashMap<>();
		for (Attribute a : relation.getAttributes())
			res.put(a, a);
		return res;
	}
	
	public abstract void close();
	public abstract boolean isClosed() throws Exception;


	public void updateRelation(Relation relation) {
		this.relation = relation;
		init();
	}
	
}
