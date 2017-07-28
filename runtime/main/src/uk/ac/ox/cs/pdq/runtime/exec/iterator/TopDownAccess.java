package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.ac.ox.cs.pdq.datasources.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.datasources.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;
import uk.ac.ox.cs.pdq.util.Typed;


// TODO: Auto-generated Javadoc
/**
 * Access over a relation, where the input are provided by the parent operator.
 * 
 * @author Julien Leblay
 */
public class TopDownAccess extends TupleIterator {


	/** The input table of the access. */
	private final RelationAccessWrapper relation;

	/** The access method to use. */
	private final AccessMethod accessMethod;

	/** The complete input type, including statically and dynamically bound. */
	private final TupleType inputBindingType;

	/** The map some of the inputs to static values. */
	private final Map<Integer, TypedConstant> staticInputs;

	/** Iterator over the output tuples. */
	protected Map<Tuple, ResetableIterator<Tuple>> outputs = null;

	/** Iterator over the output tuples. */
	protected ResetableIterator<Tuple> iterator = null;

	/**Next tuple to returns. */
	protected Tuple nextTuple = null;

	/** The last input tuple bound. */
	private Tuple lastInput;

	/**
	 * Instantiates a new join.
	 * 
	 * @param relation RelationAccessWrapper
	 * @param accessMethod AccessMethod
	 */
	public TopDownAccess(RelationAccessWrapper relation, AccessMethod accessMethod) {
		this(relation, accessMethod, ImmutableMap.<Integer, TypedConstant>of());
	}

	/**
	 * Instantiates a new join.
	 * 
	 * @param relation RelationAccessWrapper
	 * @param accessMethod AccessMethod
	 * @param staticInputs maps of the inputs in the access method that are 
	 * statically provided (indices correspond to original attributes positions
	 * in the relation, regardless of how input positions are ordered.)
	 */
	public TopDownAccess(RelationAccessWrapper relation, AccessMethod accessMethod, Map<Integer, TypedConstant> staticInputs) {
		super(inferInput(relation, accessMethod, keySet(staticInputs)),
				Lists.<Typed>newArrayList(relation.getAttributes()));
		Preconditions.checkArgument(accessMethod != null);
		Preconditions.checkArgument(relation.getAccessMethod(accessMethod.getName()) != null);
		this.relation = relation;
		this.accessMethod = accessMethod;
		this.staticInputs = staticInputs;
		this.inputBindingType = TupleType.DefaultFactory.createFromTyped(RuntimeUtilities.getInputAttributes(relation, accessMethod));
		Preconditions.checkArgument(isInputConstantsConsistentWithAccessMethod(relation, accessMethod, staticInputs));
	}

	/**
	 * Checks if is static inputs consistent with access method.
	 *
	 * @param relation the relation
	 * @param mt the mt
	 * @param inputConstants the static inputs
	 * @return the boolean
	 */
	private static Boolean isInputConstantsConsistentWithAccessMethod(
			RelationAccessWrapper relation, AccessMethod mt, Map<Integer, TypedConstant> inputConstants) {
		for (Integer i: mt.getZeroBasedInputPositions()) {
			if (inputConstants.containsKey(i)) {
				if (!relation.getAttributes()[i].getType().equals(inputConstants.get(i).getType())) {
					return false;
				}
			}
		}
		List<Integer> remains = Lists.newArrayList(inputConstants.keySet());
		remains.removeAll(Arrays.asList(mt.getZeroBasedInputPositions()));
		return remains.isEmpty();
	}

	/**
	 * Key set.
	 *
	 * @param m the m
	 * @return the sets the
	 */
	private static Set<Integer> keySet(Map<Integer, ?> m) {
		Preconditions.checkArgument(m != null);
		return m.keySet();
	}
	
	/**
	 * Infer input.
	 *
	 * @param relation RelationAccessWrapper
	 * @param accessMethod AccessMethod
	 * @param staticInputs Set<Integer>
	 * @return List<Typed>
	 */
	private static List<Typed> inferInput(RelationAccessWrapper relation, AccessMethod accessMethod, Set<Integer> staticInputs) {
		Preconditions.checkArgument(relation != null);
		Preconditions.checkArgument(accessMethod != null);
		List<Typed> result = new ArrayList<>();
		for (Integer i: accessMethod.getZeroBasedInputPositions()) {
			if (!staticInputs.contains(i)) {
				result.add(relation.getAttributes()[i]);
			}
		}
		return result;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('[').append(this.relation.getName()).append('/');
		result.append(this.accessMethod).append(']');
		return result.toString();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#open()
	 */
	@Override
	public void open() {
		Preconditions.checkState(this.open == null);
		this.outputs = Maps.newLinkedHashMap();
		this.open = true;
		// If there is no dynamic input, bind the empty tuple once and for all
		if (this.inputType.size() == 0) {
			bind(Tuple.EmptyTuple);
		}
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#reset()
	 */
	@Override
	public void reset() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.iterator.reset();
		this.nextTuple();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
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

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		Preconditions.checkState(this.lastInput != null);
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

	/**
	 * Next tuple.
	 */
	public void nextTuple() {
		this.nextTuple = null;
		if (this.interrupted) {
			return;
		}
		if (this.iterator == null) {
			// If iterator has not been set at this stage, it implies all 
			// inputs this access are statically defined.
			// Preconditions.checkState(this.inputType.size() == 0);
			Tuple staticInput = this.makeInput(Tuple.EmptyTuple);
			Table inputs = new Table(RuntimeUtilities.getInputAttributes(this.relation, this.accessMethod));
			inputs.appendRow(staticInput);
			this.iterator = this.relation.iterator(RuntimeUtilities.getInputAttributes(this.relation, this.accessMethod), inputs.iterator());
			this.iterator.open();
			this.outputs.put(staticInput, this.iterator);
		}
		if (this.iterator.hasNext()) {
			this.nextTuple = this.iterator.next();
		}
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#bind(uk.ac.ox.cs.pdq.datasources.utility.Tuple)
	 */
	@Override
	public void bind(Tuple input) {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		Preconditions.checkArgument(input != null);
		Preconditions.checkArgument(input.getType().equals(this.inputType));
		Tuple combinedInputs = this.makeInput(input);
		this.iterator = this.outputs.get(combinedInputs);
		if (this.iterator == null) {
			Table inputs = new Table(RuntimeUtilities.getInputAttributes(this.relation, this.accessMethod));
			inputs.appendRow(combinedInputs);
			this.iterator = this.relation.iterator(RuntimeUtilities.getInputAttributes(this.relation, this.accessMethod), inputs.iterator());
			this.iterator.open();
			this.outputs.put(combinedInputs, this.iterator);
		} else {
			this.iterator.reset();
		}
		this.nextTuple();
		this.lastInput = input;
	}

	/**
	 * Make input.
	 *
	 * @param dynamicInput the dynamic input
	 * @return an tuple obtained by mixing input from dynamicInput with inputs
	 * defined statically for this access.
	 */
	private Tuple makeInput(Tuple dynamicInput) {
		Object[] result = new Object[this.inputBindingType.size()];
		int j = 0, k = 0;
		for (int i : this.accessMethod.getZeroBasedInputPositions()) {
			TypedConstant staticInput = this.staticInputs.get(i);
			if (staticInput != null) {
				result[k++] = staticInput.getValue();
			} else {
				result[k++] = dynamicInput.getValue(j++);
			}
		}
		return this.inputBindingType.createTuple(result);
	}

	/**
	 * Gets the relation.
	 *
	 * @return the relation being accessed.
	 */
	public RelationAccessWrapper getRelation() {
		return this.relation;
	}

	/**
	 * Gets the access method.
	 *
	 * @return the access method in use
	 */
	public AccessMethod getAccessMethod() {
		return this.accessMethod;
	}

	/**
	 * Gets the static inputs.
	 *
	 * @return the static inputs
	 */
	public Map<Integer, TypedConstant> getStaticInputs() {
		return this.staticInputs;
	}

	/**
	 * Close.
	 *
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		super.close();
		for (ResetableIterator<Tuple> i: this.outputs.values()) {
			if (i instanceof TupleIterator) {
				((TupleIterator) i).close();
			}
		}
		this.outputs = null;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#interrupt()
	 */
	@Override
	public void interrupt() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.interrupted = true;
	}
}