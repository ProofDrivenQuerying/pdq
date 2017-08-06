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
	protected ResetableIterator<Tuple> tupleIterator = null;

	/**  The next tuple to return. */
	protected Tuple nextTuple;

	/**
	 * Instantiates a new join.
	 * 
	 * @param relation RelationAccessWrapper
	 * @param filter additional filtering condition
	 */
	public Scan(RelationAccessWrapper relation) {
		super(new Attribute[0], relation.getAttributes());
		Assert.assertNotNull(relation);
		this.relation = relation;
	}

	/**
	 * Gets the relation.
	 *
	 * @return the relation being scanned
	 */
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

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('(').append(this.relation.getName()).append(')');
		return result.toString();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#open()
	 */
	@Override
	public void open() {
		Assert.assertTrue(this.open == null || this.open);
		this.open = true;
		this.tupleIterator = this.relation.iterator();
		this.tupleIterator.open();
		this.nextTuple();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#close()
	 */
	@Override
	public void close() {
		Assert.assertTrue(this.open != null && this.open);
		super.close();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#reset()
	 */
	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.tupleIterator.reset();
		this.nextTuple();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#interrupt()
	 */
	@Override
	public void interrupt() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.interrupted = true;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
		return !this.interrupted && this.nextTuple != null;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
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
		while (this.tupleIterator.hasNext()) {
			this.nextTuple = this.tupleIterator.next();
			return;
		}
		this.nextTuple = null;
	}
	
	/**
	 * Bind.
	 *
	 * @param t Tuple
	 */
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