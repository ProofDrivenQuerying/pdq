package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jcs.JCS;
import org.apache.jcs.access.CacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.junit.Assert;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;

// TODO: Auto-generated Javadoc
/**
 * BindJoinTest is an implementation of a physical dependent open join.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class DependentJoin extends TupleIterator {
	
	/** The children. */
	protected final TupleIterator[] children = new TupleIterator[2];
	
	/** The predicate. */
	protected final Condition joinConditions;

	/** Positions that will be populated with values from the left hand child. */
	protected final Map<Integer, Integer> positionsInLeftChildThatAreInputToRightChild;
	
	protected final Integer[] inputPositionsForChild1;

	protected final Integer[] inputPositionsForChild2;
	
	protected final TupleType child1TupleType;
	
	protected final TupleType child2TupleType;
	
	/** Determines whether the operator is known to have an empty result. */
	protected boolean isEmpty = false;
	
	/** The cached iterator. */
	protected TupleIterator cachedIterator;

	/**  All RHS tuples that have been acquired so far. */
	private CacheAccess cache = null;

	/**  RHS tuples acquired given the current left tuple. */
	private Deque<Tuple> cached = null;

	/**  True if the currently acquired RHS tuples are not present in the cache. */
	private boolean doCache = false;

	/** The left tuple. */
	protected Tuple leftTuple = null;
	
	/** The current input. */
	protected Tuple currentInput = null;
	
	/** The r input. */
	private Tuple rInput = null;
	
	/** The join ids. */
	private static Integer joinIds = 0;
	
	/** The join id. */
	private final Integer joinId;
	
	/** The next tuple to return. */
	protected Tuple nextTuple = null;

	public DependentJoin(TupleIterator child1, TupleIterator child2) {
		super(RuntimeUtilities.computeInputAttributes(child1, child2), RuntimeUtilities.computeOutputAttributes(child1, child2));
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		for(int inputAttributeIndex = 0; inputAttributeIndex < child2.getNumberOfInputAttributes(); ++inputAttributeIndex) 
			Assert.assertTrue(Arrays.asList(child1.getOutputAttributes()).contains(child2.getInputAttributes()[inputAttributeIndex]));
		this.children[0] = child1;
		this.children[1] = child2;
		this.positionsInLeftChildThatAreInputToRightChild = RuntimeUtilities.computePositionsInRightChildThatAreBoundFromLeftChild(child1, child2);
		this.joinConditions = RuntimeUtilities.computeJoinConditions(this.children);
		this.joinId = DependentJoin.joinIds++;
		this.inputPositionsForChild1 = new Integer[child1.getNumberOfInputAttributes()];
		this.inputPositionsForChild2 = new Integer[child2.getNumberOfInputAttributes()];
		int index = 0;
		for(int inputAttributeIndex = 0; inputAttributeIndex < child1.getNumberOfInputAttributes(); ++inputAttributeIndex) { 
			int position = Arrays.asList(child1.getOutputAttributes()).indexOf(child1.getInputAttribute(inputAttributeIndex));
			Assert.assertTrue(position >= 0);
			this.inputPositionsForChild1[index++] = position;
		}
		index = 0;
		for(int inputAttributeIndex = 0; inputAttributeIndex < child2.getNumberOfInputAttributes(); ++inputAttributeIndex) { 
			int position = Arrays.asList(child2.getOutputAttributes()).indexOf(child2.getInputAttribute(inputAttributeIndex));
			Assert.assertTrue(position >= 0);
			this.inputPositionsForChild2[index++] = position;
		}
//		this.cachedChildrenInputTupleType = TupleType.DefaultFactory.createFromTyped(this.inputAttributes);
		this.child1TupleType = TupleType.DefaultFactory.createFromTyped(this.children[0].getInputAttributes());
		this.child2TupleType = TupleType.DefaultFactory.createFromTyped(this.children[1].getInputAttributes());
		
		CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance(); 
		Properties properties = new Properties(); 
		try {
			properties.load(ClassLoader.getSystemResourceAsStream("resources/runtime-cache.ccf"));
			// Julien: Quickfix-#4: This whole cache initialization business must
			// done outside this class.
			Class.forName(properties.getProperty("jcs.default.cacheattributes"));
			Class.forName(properties.getProperty("jcs.default.cacheattributes.MemoryCacheName"));
			Class.forName(properties.getProperty("jcs.region.bindjoin.cacheattributes"));
			Class.forName(properties.getProperty("jcs.region.bindjoin.cacheattributes.MemoryCacheName"));
			Class.forName(properties.getProperty("jcs.auxiliary.DC"));
			Class.forName(properties.getProperty("jcs.auxiliary.DC.attributes"));
			// End-of-fix
			ccm.configure(properties); 
			this.cache = JCS.getInstance("bindjoin");
		} catch (IOException | CacheException | ClassNotFoundException e) {
			throw new IllegalStateException("Cache not properly initialized.", e);
		}
	}
	
	@Override
	public TupleIterator[] getChildren() {
		return this.children.clone();
	}

	@Override
	public TupleIterator getChild(int childIndex) {
		Assert.assertTrue(childIndex < 2 && childIndex >= 0);
		return this.children[childIndex];
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.Join#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append(this.joinConditions).append('(');
		if (this.children != null) {
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
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#open()
	 */
	@Override
	public void open() {
		Assert.assertTrue(this.open == null || this.open);
		for (TupleIterator child: this.children) {
			child.open();
		}
		this.open = true;
		if (this.inputAttributes.length == 0) {
			this.nextTuple();
		}
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
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
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
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.interrupted = true;
		for (TupleIterator child: this.children) {
			child.interrupt();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
		if (this.interrupted) {
			return false;
		}
		if (this.nextTuple != null) {
			return true;
		}
		if (this.isEmpty) {
			return false;
		}
		this.nextTuple();
		return this.nextTuple != null;
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		Tuple result = this.nextTuple;
		this.nextTuple = null;
		if ((!this.hasNext() && result == null) || this.isEmpty) {
			throw new NoSuchElementException("End of operator reached.");
		}
		return result;
	}
	
	/**
	 * Move the iterator forward and prepares the next tuple to be returned.
	 * 
	 */
	protected void nextTuple() {
		Assert.assertTrue(this.inputAttributes.length == 0 || this.currentInput != null);
		if (this.cachedIterator != null) {
			while (this.cachedIterator.hasNext()) {
				Tuple right = this.cachedIterator.next();
				if(this.doCache) {  		
					this.cached.add(right); 
				}							
				Tuple t = this.leftTuple.appendTuple(right);
				if (RuntimeUtilities.isSatisfied(this.joinConditions, t)) {
					this.nextTuple = t;
					return;
				}
			}
		}
		this.nextTuple = null;
		do {
			if (!this.children[0].hasNext()) {
				return;
			}
			//Before retrieving the next left tuple,
			//store the tuples of the LHS that are retrieved for the current left tuple
			try {
				if(this.doCache) {								
					this.cache.put(Pair.of(this.joinId, this.rInput), this.cached);	
				}												
			} catch (CacheException e) {
				throw new IllegalStateException();
			}
			this.leftTuple = this.children[0].next();
			this.rInput = this.projectInputValuesForRightChild(this.currentInput, this.leftTuple);
			this.cached = (Deque<Tuple>) this.cache.get(Pair.of(this.joinId, this.rInput));
			if (this.cached == null) {
				this.children[1].bind(this.rInput);
				this.cachedIterator = this.children[1];
				this.cached = new LinkedList<>();
				this.doCache = true;
			} else {
				this.cachedIterator = new MemoryScan(this.children[1].getOutputAttributes(), this.cached);
				this.cachedIterator.open();
				this.doCache = false;
			}
			while (this.cachedIterator.hasNext()) {
				Tuple rightTuple = this.cachedIterator.next();
				if(this.doCache) {				
					this.cached.add(rightTuple);
				}								
				Tuple t = this.leftTuple.appendTuple(rightTuple);
				if (RuntimeUtilities.isSatisfied(this.joinConditions, t)) {
					this.nextTuple = t;
					return;
				}
			}
		} while (!this.cachedIterator.hasNext());
	}
	
	/**
	 * Project.
	 *
	 * @param currentInput the current input
	 * @param leftInput the left input
	 * @return an input tuple obtained by mixing inputs coming from the parent
	 * (currentInput) and the LHS (leftInput).
	 */
	protected Tuple projectInputValuesForRightChild(Tuple currentInput, Tuple leftInput) {
		Object[] result = new Object[this.children[1].getNumberOfInputAttributes()];
		for(int attributeIndex = 0; attributeIndex < this.children[1].getNumberOfInputAttributes(); ++attributeIndex) {
			Integer positionInLeftChild = this.positionsInLeftChildThatAreInputToRightChild.get(attributeIndex);
			if(positionInLeftChild != null) 
				result[attributeIndex] = leftInput.getValue(positionInLeftChild);
			else 
				result[attributeIndex] = currentInput.getValue(this.children[0].getNumberOfInputAttributes() + attributeIndex);
		}
		return this.child2TupleType.createTuple(result);
	}
	
	/**
	 * Bind.
	 *
	 * @param tuple Tuple
	 */
	@Override
	public void bind(Tuple tuple) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		Assert.assertTrue(tuple != null);
		Object[] inputsForLeftChild = RuntimeUtilities.projectValuesInInputPositions(tuple, this.inputPositionsForChild1);
		this.children[0].bind(this.child1TupleType.createTuple(inputsForLeftChild));
		this.currentInput = tuple;
	}
}