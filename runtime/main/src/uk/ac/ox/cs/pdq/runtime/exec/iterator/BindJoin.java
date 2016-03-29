package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jcs.JCS;
import org.apache.jcs.access.CacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheManager;

import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * BindJoinTest is an implementation of a physical dependent open join.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class BindJoin extends Join {

	/** The input sideways mapping. */
	protected final List<Integer> inputSidewaysMapping;

	/** The left. */
	protected final TupleIterator left;
	
	/** The right. */
	protected final TupleIterator right;
	
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

	/**
	 * Instantiates a new join.
	 * 
	 * @param left TupleIterator
	 * @param right TupleIterator
	 */
	public BindJoin(TupleIterator left, TupleIterator right) {
		this(inferNaturalJoin(toList(left, right)), 
			inferInputMappings(left.getColumns(), right.getInputColumns()), 
			left, right);
	}

	/**
	 * Instantiates a new join.
	 * @param pred Atom
	 * @param left TupleIterator
	 * @param right TupleIterator
	 */
	public BindJoin(Predicate pred, TupleIterator left, TupleIterator right) {
		this(pred, 
				inferInputMappings(left.getColumns(), right.getInputColumns()),
				left, right);
	}

	/**
	 * Instantiates a new join.
	 * @param pred Atom
	 * @param sideWays List<Integer>
	 * @param left TupleIterator
	 * @param right TupleIterator
	 */
	public BindJoin(Predicate pred, List<Integer> sideWays, TupleIterator left, TupleIterator right) {
		super(pred, inferInputColumns(left, right), toList(left, right));
		this.left = left;
		this.right = right;
		this.inputSidewaysMapping = sideWays;
		this.joinId = BindJoin.joinIds++;
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

	/**
	 * Infer input mappings.
	 *
	 * @param leftOutput the left output
	 * @param rightInput the right input
	 * @return a mapping from right input position to their position on the left output.
	 * Right input positions that comes from the parent are distinguished as negative numbers.
	 */
	private static List<Integer> inferInputMappings(
			List<Typed> leftOutput, List<? extends Typed> rightInput) {
		List<Integer> result = new ArrayList<>(rightInput.size());
		for (Typed t: rightInput) {
			result.add(leftOutput.indexOf(t));
		}
		return result;
	}

	/**
	 * Method inferInputColumns.
	 * @param left TupleIterator
	 * @param right TupleIterator
	 * @return List<Typed>
	 */
	private static List<Typed> inferInputColumns(TupleIterator left, TupleIterator right) {
		List<Typed> result = Lists.newArrayList(left.getInputColumns());
		List<Typed> rInputs = right.getInputColumns();
		for (int i = 0, l = rInputs.size(); i < l; i++) {
			Typed t = rInputs.get(i);
			if (!left.getColumns().contains(t)) {
				result.add(rInputs.get(i));
			}
		}
		return result;
	}

	/**
	 * Project.
	 *
	 * @param currentInput the current input
	 * @param leftInput the left input
	 * @return an input tuple obtained by mixing inputs coming from the parent
	 * (currentInput) and the LHS (leftInput).
	 */
	protected Tuple project(Tuple currentInput, Tuple leftInput) {
		Object[] result = new Object[this.right.getInputType().size()];
		for (int i = 0, j = 0, l = this.inputSidewaysMapping.size(); i < l; i++) {
			if (this.inputSidewaysMapping.get(i) >= 0) {
				result[i] = leftInput.getValue(this.inputSidewaysMapping.get(i));
			} else {
				result[i] = currentInput.getValue(this.left.inputType.size() + j++);
			}
		}
		return this.right.getInputType().createTuple(result);
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.Join#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append(this.predicate).append('(');
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
	 * Move the iterator forward and prepares the next tuple to be returned.
	 * 
	 */
	@Override
	protected void nextTuple() {
		Preconditions.checkState(this.inputType.size() == 0 || this.currentInput != null);
		if (this.cachedIterator != null) {
			while (this.cachedIterator.hasNext()) {
				Tuple right = this.cachedIterator.next();
				if(this.doCache) {  		
					this.cached.add(right); 
				}							
				Tuple t = this.leftTuple.appendTuple(right);
				if (this.predicate.isSatisfied(t)) {
					this.nextTuple = t;
					return;
				}
			}
		}
		this.nextTuple = null;
		do {
			if (!this.left.hasNext()) {
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
			this.leftTuple = this.left.next();
			this.rInput = this.project(this.currentInput, this.leftTuple);
			this.cached = (Deque<Tuple>) this.cache.get(Pair.of(this.joinId, this.rInput));
			if (this.cached == null) {
				this.right.bind(this.rInput);
				this.cachedIterator = this.right;
				this.cached = new LinkedList<>();
				this.doCache = true;
			} else {
				this.cachedIterator = new MemoryScan(this.right.getColumns(), this.cached);
				this.cachedIterator.open();
				this.doCache = false;
			}
			while (this.cachedIterator.hasNext()) {
				Tuple rightTuple = this.cachedIterator.next();
				if(this.doCache) {				
					this.cached.add(rightTuple);
				}								
				Tuple t = this.leftTuple.appendTuple(rightTuple);
				if (this.predicate.isSatisfied(t)) {
					this.nextTuple = t;
					return;
				}
			}
		} while (!this.cachedIterator.hasNext());
	}

	/**
	 * Deep copy.
	 *
	 * @return BindJoinTest
	 * @see uk.ac.ox.cs.pdq.util.ResetableIterator#deepCopy()
	 */
	@Override
	public BindJoin deepCopy() {
		return new BindJoin(this.predicate, this.left.deepCopy(), this.right.deepCopy());
	}

	/**
	 * Bind.
	 *
	 * @param t Tuple
	 */
	@Override
	public void bind(Tuple t) {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		Preconditions.checkArgument(t != null);
		Preconditions.checkArgument(t.getType().equals(this.inputType));
		this.left.bind(this.project(this.left, t));
		this.currentInput = t;
	}
	
}