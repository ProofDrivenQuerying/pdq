package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 * An executable query plan. A logical plan is decorated to make it executable. 
 * 
 * @author Tim Hobson
 *
 */
public abstract class ExecutablePlan implements Plan, AutoCloseable {

	protected final Plan decoratedPlan;
	private PlanDecorator decorator;

	public ExecutablePlan(Plan plan, PlanDecorator decorator) {
		// Note that we do not attempt to decorate the children of the given plan.
		// This would not be easy, since the children field is protected, and is
		// also unnecessary, since we can decorate on-the-fly when required.
		this.decoratedPlan = plan;
		this.decorator = decorator;
	}
	
	/**
	 * Executes the plan.
	 * 
	 * @return a Table containing the result tuples from this plan
	 * @throws Exception 
	 */
	public Table execute() {
		
		Supplier<Table> supplier = () -> new Table(this.getOutputAttributes());
		Table ret = this.stream().collect(Table.toTable(supplier));
		try {
			this.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * Returns a sequential Stream with this plan as its source.
	 * 
	 * @return a sequential Stream over the tuples in this plan
	 * @throws Exception 
	 */
	public Stream<Tuple> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}
	
	/**
	 * Creates a spliterator over the tuples in this plan.
	 * 
	 * @return a spliterator over the tuples in this plan
	 * @throws Exception 
	 */
	public abstract Spliterator<Tuple> spliterator();
	
	public abstract void setInputTuples(Iterator<Tuple> inputTuples);
	
	// Override the close method signature.
	@Override
	public abstract void close();
	
	/**
	 * Construct a Tuple projection function.
	 * 
	 * @param from an attribute array
	 * @param onto an attribute array 
	 * @return A function that projects a tuple onto the {@code t} onto the given indices.
	 */
	public static Function<Tuple, Tuple> tupleProjector(Attribute[] from, Attribute[] onto) {
		
		Preconditions.checkArgument(Arrays.stream(onto).allMatch(attr -> Arrays.asList(from).contains(attr)), 
				"Tuple projection source must contain all target attributes");
		
		final int[] indices = Arrays.stream(onto)
				.mapToInt(attr -> Arrays.asList(from).indexOf(attr))
				.toArray();
		final TupleType tupleType = TupleType.DefaultFactory.createFromTyped(onto);

		return tuple -> {
			Object[] values = new Object[indices.length];
			for (int i = 0; i != indices.length; i++)
				values[i] = tuple.getValues()[indices[i]];
			return tupleType.createTuple(values);
		};
	}
	
	public Plan getDecoratedPlan() {
		return decoratedPlan;
	}
	
	public Attribute[] getOutputAttributes() {
		return decoratedPlan.getOutputAttributes();
	}

	public Attribute[] getInputAttributes() {
		return decoratedPlan.getInputAttributes();
	}

	@Override
	public Integer[] getInputIndices() {
		return decoratedPlan.getInputIndices();
	}

	@Override
	public int getAttributePosition(Attribute attribute) {
		return decoratedPlan.getAttributePosition(attribute);
	}
	
	@Override
	public Attribute getInputAttribute(int index) {
		return decoratedPlan.getInputAttribute(index);
	}
	
	@Override
	public int getNumberOfInputAttributes() {
		return decoratedPlan.getNumberOfInputAttributes();
	}
	
	@Override
	public Attribute getOutputAttribute(int index) {
		return decoratedPlan.getOutputAttribute(index);
	}
	
	@Override
	public int getNumberOfOutputAttributes() {
		return decoratedPlan.getNumberOfOutputAttributes();
	}
	
	public Plan[] getChildren() {
		return decoratedPlan.getChildren();
	}
	
	@Override
	public Plan getChild(int index) {
		return decoratedPlan.getChild(index);
	}
	
	@Override
	public String toString() {
		return decoratedPlan.toString();
	}
	
	@Override
	public boolean isClosed() {
		return decoratedPlan.isClosed();
	}
	
	public PlanDecorator getDecorator() {
		return this.decorator;
	}
}
