package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;



// TODO: Auto-generated Javadoc
/**
 * Superclass to all n-ary physical open operator.
 * Factorises the open, close, reset methods and the default constructor.
 * 
 * @author Julien Leblay
 */
public abstract class NaryIterator extends TupleIterator {

	/** The children. */
	protected List<TupleIterator> children;

	/** For each child, a mapping from this operator's input positions to the child's. */
	protected Map<TupleIterator, List<Integer>> relativeInputPositions;
	
	/**
	 * Instantiates a new operator.
	 *
	 * @param inputType the input type
	 * @param inputs List<Typed>
	 * @param outputType TupleType
	 * @param outputColumns List<Typed>
	 * @param children            the children
	 */
	public NaryIterator(
			TupleType inputType, List<Typed> inputs,
			TupleType outputType, List<Typed> outputColumns,
			Collection<TupleIterator> children) {
		super(inputType, inputs, outputType, outputColumns);
		Preconditions.checkArgument(children != null);
		Preconditions.checkArgument(outputColumns.containsAll(inputColumns));
		this.children = ImmutableList.copyOf(children);
	}
	
	/**
	 * Infer type.
	 *
	 * @param children Collection<TupleIterator>
	 * @return TupleType
	 */
	protected static TupleType inferType(Collection<TupleIterator> children) {
		TupleType result = null;
		for (TupleIterator child: children) {
			if (result == null) {
				result = child.getType();
			} else {
				result = result.append(child.getType());
			}
		}
		return result;
	}
	
	/**
	 * Infer input type.
	 *
	 * @param children Collection<TupleIterator>
	 * @return TupleType
	 */
	protected static TupleType inferInputType(Collection<TupleIterator> children) {
		TupleType result = null;
		for (TupleIterator child: children) {
			if (result == null) {
				result = child.getInputType();
			} else {
				result = result.append(child.getInputType());
			}
		}
		return result;
	}
	
	/**
	 * Infer columns.
	 *
	 * @param children Collection<TupleIterator>
	 * @return List<Typed>
	 */
	protected static List<Typed> inferColumns(Collection<TupleIterator> children) {
		Preconditions.checkArgument(children != null);
		List<Typed> result = Lists.newArrayList();
		for (TupleIterator child: children) {
			result.addAll(child.getColumns());
		}
		return result;
	}
	
	/**
	 * Infer input columns.
	 *
	 * @param children Collection<TupleIterator>
	 * @return List<Typed>
	 */
	protected static List<Typed> inferInputColumns(Collection<TupleIterator> children) {
		Preconditions.checkArgument(children != null);
		List<Typed> result = Lists.newArrayList();
		for (TupleIterator child: children) {
			result.addAll(child.getInputColumns());
		}
		return result;
	}
	
	/**
	 * First child.
	 *
	 * @param children Collection<TupleIterator>
	 * @return The output columns of the first child
	 */
	protected static TupleIterator firstChild(Collection<TupleIterator> children) {
		Preconditions.checkArgument(children != null);
		Preconditions.checkArgument(!children.isEmpty());
		return children.iterator().next();
	}
	
	/**
	 * Infer type first child.
	 *
	 * @param children Collection<TupleIterator>
	 * @return The type of the first child
	 */
	protected static TupleType inferTypeFirstChild(Collection<TupleIterator> children) {
		Preconditions.checkArgument(children != null);
		Preconditions.checkArgument(!children.isEmpty());
		return children.iterator().next().getType();
	}

	/**
	 * To list.
	 *
	 * @param children the children
	 * @return the list
	 */
	protected static List<TupleIterator> toList(TupleIterator... children) {
		Preconditions.checkArgument(children != null);
		for (TupleIterator i: children) {
			Preconditions.checkArgument(i != null);
		}
		return Arrays.asList(children);
	}
	
	/**
	 * Infer input mappings.
	 *
	 * @param inputColumns List<? extends Typed>
	 * @param operators Collection<TupleIterator>
	 * @return Map<TupleIterator,List<Integer>>
	 */
	protected static Map<TupleIterator, List<Integer>> inferInputMappings(
			List<? extends Typed> inputColumns, Collection<TupleIterator> operators) {
		Map<TupleIterator, List<Integer>> result = Maps.newLinkedHashMap();
		List<Typed> unassigned = Lists.newArrayList(inputColumns);
		for (TupleIterator op: operators) {
			List<Typed> inputs = op.getInputColumns();
			List<Integer> positions = new ArrayList<>(inputs.size());
			for (Typed t: inputs) {
				int position = inputColumns.indexOf(t);
				if (position >= 0) {
					positions.add(position);
					unassigned.remove(t);
				}
			}
			result.put(op, positions);
		}
		if (!unassigned.isEmpty()) {
			throw new IllegalStateException("Inconsistent input mapping");
		}
		return result;
	}
	
	/**
	 * Project.
	 *
	 * @param op TupleIterator
	 * @param input Tuple
	 * @return Tuple
	 */
	protected Tuple project(TupleIterator op, Tuple input) {
		Object[] result = new Object[op.getInputType().size()];
		List<Integer> positions = this.relativeInputPositions.get(op);
		for (int i = 0, l = positions.size(); i < l; i++) {
			result[i] = input.getValue(positions.get(i));
		}
		return op.getInputType().createTuple(result);
	}

	/**
	 * Gets the children.
	 * 
	 * @return the children
	 */
	public List<TupleIterator> getChildren() {
		return this.children;
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName()).append('(');
		if (this.children != null && !this.children.isEmpty()) {
			for (TupleIterator child: this.children) {
				result.append(child.toString()).append(',');
			}
			result.deleteCharAt(result.length() - 1);
		}
		result.append(')');
		return result.toString();
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#setEventBus(com.google.common.eventbus.EventBus)
	 */
	@Override
	public void setEventBus(EventBus eb) {
		super.setEventBus(eb);
		for (TupleIterator child: this.children) {
			child.setEventBus(eb);
		}
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#open()
	 */
	@Override
	public void open() {
		Preconditions.checkState(this.open == null || this.open);
		for (TupleIterator child: this.children) {
			child.open();
		}
		this.open = true;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#close()
	 */
	@Override
	public void close() {
		super.close();
		for (TupleIterator child: this.children) {
			child.close();
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
		for (TupleIterator child: this.children) {
			child.reset();
		}
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
		for (TupleIterator child: this.children) {
			child.interrupt();
		}
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#bind(uk.ac.ox.cs.pdq.datasources.utility.Tuple)
	 */
	@Override
	public void bind(Tuple t) {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		for (TupleIterator child: this.children) {
			child.bind(this.project(child, t));
		}
	}
}
