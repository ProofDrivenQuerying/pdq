package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.NoSuchElementException;

import org.junit.Assert;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;


/**
 * Selection operator.
 * 
 * @author Julien Leblay
 */
public class Selection extends TupleIterator {
	
	protected final TupleIterator child;
	
	protected final TupleType childTupleType;

	/** The predicate associated with this selection. */
	protected final Condition selectionCondition;

	/**  The next Tuple to return. */
	protected Tuple nextTuple = null;

	public Selection(Condition selectionCondition, TupleIterator child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
		Assert.assertNotNull(selectionCondition);
		Assert.assertNotNull(child);
		Assert.assertTrue(AlgebraUtilities.assertSelectionCondition(selectionCondition, child.getOutputAttributes()));
		this.selectionCondition = selectionCondition;
		this.child = child;
		this.childTupleType = TupleType.DefaultFactory.createFromTyped(this.inputAttributes);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('{').append(this.selectionCondition).append('}');
		result.append('(').append(this.child.toString()).append(')');
		return result.toString();
	}
	
	@Override
	public TupleIterator[] getChildren() {
		return new TupleIterator[]{this.child};
	}

	@Override
	public TupleIterator getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}
	
	public Condition getSelectionCondition() {
		return this.selectionCondition;
	}
	
	@Override
	public void setEventBus(EventBus eb) {
		super.setEventBus(eb);
		this.child.setEventBus(eb);
	}

	@Override
	public void open() {
		Assert.assertTrue(this.open == null || this.open);
		this.child.open();
		this.open = true;
	}

	@Override
	public void close() {
		Assert.assertTrue(this.open != null && this.open);
		super.close();
		this.child.close();
	}

	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.child.reset();
	}

	@Override
	public void interrupt() {
		Assert.assertTrue(this.open != null && this.open);
		this.interrupted = true;
		this.child.interrupt();
	}
	
	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
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
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		this.hasNext();
		if (this.nextTuple == null) {
			throw new NoSuchElementException();
		}
		Tuple result = this.nextTuple;
		this.nextTuple();
		return result;
	}
	
	/**
	 * Prepares the next tuple to be returned. If the end of the iterator
	 * was reached, this.nextTuple is null.
	 */
	private void nextTuple() {
		while (this.child.hasNext()) {
			Tuple next = this.child.next();
			if (RuntimeUtilities.isSatisfied(this.selectionCondition, next)) {
				this.nextTuple = next;
				return;
			}
		}
		this.nextTuple = null;
	}
	
	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple t) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(t != null);
		Assert.assertTrue(t.getType().equals(this.childTupleType));
		this.child.receiveTupleFromParentAndPassItToChildren(t);
	}
}
