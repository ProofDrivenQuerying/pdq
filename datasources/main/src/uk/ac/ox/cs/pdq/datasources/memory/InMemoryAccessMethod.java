package uk.ac.ox.cs.pdq.datasources.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jersey.repackaged.com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

public class InMemoryAccessMethod extends ExecutableAccessMethod {

	private static final long serialVersionUID = 5268175711548627539L;

	/**  The underlying data. */
	private Collection<Tuple> data = new ArrayList<>();

	public InMemoryAccessMethod(Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(attributes, inputs, relation, attributeMapping);
	}

	public InMemoryAccessMethod(String name, Attribute[] attributes, Integer[] inputs, Relation relation,
			Map<Attribute, Attribute> attributeMapping) {
		super(name, attributes, inputs, relation, attributeMapping);
	}

	public InMemoryAccessMethod(Attribute[] attributes, Set<Attribute> inputAttributes,
			Relation relation, Map<Attribute, Attribute> attributeMapping) {
		super(attributes, inputAttributes, relation, attributeMapping);
	}

	public InMemoryAccessMethod(String name, Attribute[] attributes, Set<Attribute> inputAttributes,
			Relation relation, Map<Attribute, Attribute> attributeMapping) {
		super(name, attributes, inputAttributes, relation, attributeMapping);
	}

	/**  Get the underlying data. */
	public Collection<Tuple> getData() {
		return this.data;
	}

	@Override
	protected Stream<Tuple> fetchTuples(Iterator<Tuple> inputTuples) {
		
		if (inputTuples == null)
			return this.getData().stream();
		
		Preconditions.checkArgument(inputTuples.hasNext());

		// In the case of in-memory data, free access is possible so we need only filter according to the input tuples.
		// Construct a predicate that returns true on tuples matching _any_ of the input tuples.
		List<ConjunctiveCondition> accessConditions = new ArrayList<ConjunctiveCondition>();
		while (inputTuples.hasNext())
			accessConditions.add(this.accessCondition(inputTuples.next()));
		Predicate<Tuple> filterPredicate = (tuple) -> accessConditions.stream().anyMatch(c -> c.isSatisfied(tuple)); 

		return this.getData().stream().filter(filterPredicate);	
	}

	public void load(Collection<Tuple> tuples) {

		// The given tuples must respect the AccessMethod's external schema.
		TupleType tt = TupleType.createFromTyped(this.outputAttributes(false));
		for (Tuple tuple: tuples) {
			Preconditions.checkArgument(tuple.getType().equals(tt), 
					"Failed to load tuple. Invalid type: %s", tuple.getType().toString());
			this.data.add(tuple);
		}
	}

	@Override
	public void close() {
		data = null; // let garbage collector do its magic.
	}

}
