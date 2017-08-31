package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.NoSuchElementException;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.datasources.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.datasources.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.db.Attribute;


// TODO: Auto-generated Javadoc
/**
 * Scan over a relation
 * Julien: although this class is only marginally used, please do not delete.
 * Planning to use it in future work.
 * 
 * @author Julien Leblay
 */
public class Scan extends TupleIterator {

	/** The underlying relation. */
	protected final RelationAccessWrapper relation;	

	/** The next tuple to return. */
	protected ResetableIterator<Tuple> outputTuplesIterator = null;

	/**  The next tuple to return. */
	protected Tuple nextTuple;

	public Scan(RelationAccessWrapper relation) {
		super(new Attribute[0], relation.getAttributes());
		Assert.assertNotNull(relation);
		this.relation = relation;
	}

	public RelationAccessWrapper getRelation() {
		return this.relation;
	}
	
	@Override
	public TupleIterator[] getChildren() {
		return new TupleIterator[]{};
	}

	@Override
	public TupleIterator getChild(int childIndex) {
		return null;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('(').append(this.relation.getName()).append(')');
		return result.toString();
	}

	@Override
	public void open() {
		Assert.assertTrue(this.open == null || this.open);
		this.open = true;
		this.outputTuplesIterator = this.relation.iterator();
		this.outputTuplesIterator.open();
		this.nextTuple();
	}

	@Override
	public void close() {
		Assert.assertTrue(this.open != null && this.open);
		super.close();
	}

	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.outputTuplesIterator.reset();
		this.nextTuple();
	}

	@Override
	public void interrupt() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.interrupted = true;
	}

	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
		return !this.interrupted && this.nextTuple != null;
	}

	@Override
	public Tuple next() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		if (this.nextTuple == null) {
			throw new NoSuchElementException();
		}
		Tuple result = this.nextTuple;
		this.nextTuple();
		return result;
	}
	
	/**
	 * Moves to the next valid tuple to return.
	 */
	private void nextTuple() {
		while (this.outputTuplesIterator.hasNext()) {
			this.nextTuple = this.outputTuplesIterator.next();
			return;
		}
		this.nextTuple = null;
	}
	
	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple t) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		Assert.assertTrue(t != null);
		Assert.assertTrue(t.size() == 0);
		// Important: this iterator MUST be reiterated from scratch
		this.reset();
	}
}