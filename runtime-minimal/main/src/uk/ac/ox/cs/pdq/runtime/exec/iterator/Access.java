package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import jersey.repackaged.com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.datasources.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.datasources.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;
import uk.ac.ox.cs.pdq.util.TupleType;


/**
 * Access over a relation, where the input are provided by the parent operator.
 * 
 * @author Julien Leblay
 */
public class Access extends TupleIterator {

	/** The input table of the access. */
	protected final RelationAccessWrapper relation;

	/** The access method to use. */
	protected final AccessMethod accessMethod;

	/** The types of all input positions */
	protected final TupleType inputTupleType;

	/** The map some of the inputs to static values. */
	protected final Map<Integer, TypedConstant> inputConstants;
	
	protected final Attribute[] attributesInInputPositions;

	/** Iterator over the output tuples. */
	protected Map<Tuple, ResetableIterator<Tuple>> outputTuplesCache = null;

	/** Iterator over the output tuples. */
	protected ResetableIterator<Tuple> outputTuplesIterator = null;

	/**Next tuple to returns. */
	protected Tuple nextTuple = null;

	/** The last input tuple bound. */
	private Tuple tupleReceivedFromParent;

	public Access(RelationAccessWrapper relation, AccessMethod accessMethod) {
		this(relation, accessMethod, new HashMap<Integer, TypedConstant>());
	}

	public Access(RelationAccessWrapper relation, AccessMethod accessMethod, Map<Integer, TypedConstant> inputConstants) {
		super(RuntimeUtilities.computeInputAttributes(relation, accessMethod, inputConstants), relation.getAttributes());
		Preconditions.checkNotNull(relation);
		Preconditions.checkNotNull(accessMethod);
		for(Integer position:inputConstants.keySet()) {
			Preconditions.checkArgument(position < relation.getArity());
			Preconditions.checkArgument(Arrays.asList(accessMethod.getInputs()).contains(position));
		}
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.inputConstants = new LinkedHashMap<>();
		for(java.util.Map.Entry<Integer, TypedConstant> entry:inputConstants.entrySet()) 
			this.inputConstants.put(entry.getKey(), entry.getValue().clone());
		this.attributesInInputPositions = RuntimeUtilities.computeInputAttributes(relation, accessMethod);
		this.inputTupleType = TupleType.DefaultFactory.createFromTyped(this.attributesInInputPositions);
	}
	
	@Override
	public TupleIterator[] getChildren() {
		return new TupleIterator[]{};
	}

	@Override
	public TupleIterator getChild(int childIndex) {
		return null;
	}
	
	public RelationAccessWrapper getRelation() {
		return this.relation;
	}

	public AccessMethod getAccessMethod() {
		return this.accessMethod;
	}

	public Map<Integer, TypedConstant> getInputConstants() {
		return this.inputConstants;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('[').append(this.relation.getName()).append('/');
		result.append(this.accessMethod).append(']');
		return result.toString();
	}

	@Override
	public void open() {
		Preconditions.checkState(this.open == null);
		this.outputTuplesCache = new LinkedHashMap<>();
		this.open = true;
		// If there is no dynamic input, bind the empty tuple once and for all
		if (this.inputAttributes.length == 0) {
			receiveTupleFromParentAndPassItToChildren(Tuple.EmptyTuple);
		}
	}
	
	@Override
	public void close() {
		super.close();
		for (ResetableIterator<Tuple> i: this.outputTuplesCache.values()) {
			if (i instanceof TupleIterator) {
				((TupleIterator) i).close();
			}
		}
		this.outputTuplesCache = null;
	}

	@Override
	public void interrupt() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.interrupted = true;
	}

	@Override
	public void reset() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.outputTuplesIterator.reset();
		this.nextTuple();
	}

	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		if (this.interrupted) {
			return false;
		}
		if (this.nextTuple != null) {
			return true;
		}
		this.nextTuple();
		return this.nextTuple != null;
	}

	@Override
	public Tuple next() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		Preconditions.checkState(this.tupleReceivedFromParent != null);
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		Tuple result = this.nextTuple;
		this.nextTuple = null;
		if (!this.hasNext() && result == null) {
			throw new NoSuchElementException("End of operator reached.");
		}
		return result;
	}

	public void nextTuple() {
		this.nextTuple = null;
		if (this.interrupted) {
			return;
		}
		if (this.outputTuplesIterator == null) {
			// If iterator has not been set at this stage, it implies all 
			// inputs this access are statically defined.
			// Assert.assertTrue(this.inputType.size() == 0);
			Tuple tupleOfInputConstants = this.makeInputTupleByCombiningInputsFromParentsWithInputConstants(Tuple.EmptyTuple);
			Table inputs = new Table(this.attributesInInputPositions);
			inputs.appendRow(tupleOfInputConstants);
			this.outputTuplesIterator = this.relation.iterator(this.attributesInInputPositions, inputs.iterator());
			this.outputTuplesIterator.open();
			this.outputTuplesCache.put(tupleOfInputConstants, this.outputTuplesIterator);
		}
		if (this.outputTuplesIterator.hasNext()) {
			this.nextTuple = this.outputTuplesIterator.next();
		}
	}

	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple tuple) {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		Preconditions.checkState(tuple != null);
		Preconditions.checkState(RuntimeUtilities.typeOfAttributesEqualsTupleType(tuple.getType(), this.inputAttributes));
		Tuple combinedInputs = this.makeInputTupleByCombiningInputsFromParentsWithInputConstants(tuple);
		this.outputTuplesIterator = this.outputTuplesCache.get(combinedInputs);
		if (this.outputTuplesIterator == null) {
			Table inputs = new Table(this.attributesInInputPositions);
			inputs.appendRow(combinedInputs);
			this.outputTuplesIterator = this.relation.iterator(this.attributesInInputPositions, inputs.iterator());
			this.outputTuplesIterator.open();
			this.outputTuplesCache.put(combinedInputs, this.outputTuplesIterator);
		} else {
			this.outputTuplesIterator.reset();
		}
		this.nextTuple();
		this.tupleReceivedFromParent = tuple;
	}
	
	/**
	 * @return an tuple obtained by mixing input from dynamicInput with inputs
	 * defined statically for this access.
	 */
	private Tuple makeInputTupleByCombiningInputsFromParentsWithInputConstants(Tuple dynamicInput) {
		Object[] result = new Object[this.inputTupleType.size()];
		int j = 0, k = 0;
		for (int i : this.accessMethod.getInputs()) {
			TypedConstant staticInput = this.inputConstants.get(i);
			if (staticInput != null) {
				result[k++] = staticInput.getValue();
			} else {
				result[k++] = dynamicInput.getValue(j++);
			}
		}
		return this.inputTupleType.createTuple(result);
	}
}