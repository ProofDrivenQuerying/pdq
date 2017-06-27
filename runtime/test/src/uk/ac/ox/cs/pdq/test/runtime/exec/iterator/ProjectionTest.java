package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.wrappers.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Projection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Scan;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TopDownAccess;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

// TODO: Auto-generated Javadoc
/**
 * The Class ProjectionTest.
 *
 * @author Julien LEBLAY
 */
public class ProjectionTest extends UnaryIteratorTest {

	/** The iterator. */
	Projection iterator;
	
	/** The projected type. */
	TupleType projectedType;
	
	/** The projected. */
	List<Typed> projected;
	
	/** The renaming. */
	Map<Integer, Typed> renaming;
	
	/** The relation. */
	InMemoryTableWrapper relation;
	
	/** The mt. */
	AccessMethod mt;
	
	/** The d. */
	Attribute A = new Attribute(String.class, "A"), 
			B = new Attribute(Integer.class, "B"), 
			c = new Attribute(String.class, "c"), 
			d = new Attribute(Integer.class, "d");
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.runtime.exec.iterator.TupleIteratorTest#setup()
	 */
	@Before public void setup() {
        super.setup();
		this.projectedType = TupleType.DefaultFactory.create(Integer.class, String.class, String.class);
		this.projected = Lists.<Typed>newArrayList(b, new TypedConstant<>("x"), a);
		this.relation = new InMemoryTableWrapper("test", Lists.newArrayList(a, b, c, d));
		this.mt = new AccessMethod("mt", Types.LIMITED, Lists.newArrayList(2, 1));

		this.renaming = Maps.newLinkedHashMap();
		this.renaming.put(0, A);
	    this.renaming.put(1, B);
		
		this.relation.load(Lists.newArrayList(
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("two", 2, "str", 6), 
				outputType.createTuple("three", 3, "str", 6)));
		this.relation.addAccessMethods(mt);

        MockitoAnnotations.initMocks(this);
        when(child.getColumns()).thenReturn(outputColumns);
		when(child.getType()).thenReturn(outputType);
		when(child.getInputColumns()).thenReturn(inputColumns);
		when(child.getInputType()).thenReturn(inputType);
		when(child.deepCopy()).thenReturn(child);
		when(child.next()).thenReturn(
				outputType.createTuple("one", 1, "str", 6), 
				outputType.createTuple("two", 2, "str", 6), 
				outputType.createTuple("three", 3, "str", 6));
		this.iterator = new Projection(projected, renaming, child);
	}

	/**
	 * Inits the projection.
	 */
	@Test public void initProjection() {
		this.iterator = new Projection(projected, child);
		Assert.assertEquals("Projection header match that of initialization", this.projected, this.iterator.getColumns());
		Assert.assertEquals("Projection child match that of initialization", this.child, this.iterator.getChild());
	}

	/**
	 * Inits the projection with renaming.
	 */
	@Test public void initProjectionWithRenaming() {
		this.iterator = new Projection(projected, renaming, child);
		List<Typed> renamedOutput = Lists.<Typed>newArrayList(B, new TypedConstant<>("x"), A);
		List<Typed> renamedInput = Lists.<Typed>newArrayList(B, A);
		Assert.assertEquals("Projection input type must match that of initialization", this.inputType, this.iterator.getInputType());
		Assert.assertEquals("Projection header type must match that of initialization", this.projectedType, this.iterator.getType());
		Assert.assertEquals("Projection child must match that of initialization", this.child, this.iterator.getChild());
		Assert.assertEquals("Projection renamed output must match that of initialization", renamedOutput, this.iterator.getColumns());
		Assert.assertEquals("Projection renamed input must match that of initialization", renamedInput, this.iterator.getInputColumns());
	}
	
	/**
	 * Inits the selection null child.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initSelectionNullChild() {
		new Projection(projected, renaming, null);
	}
	
	/**
	 * Inits the selection null header.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initSelectionNullHeader() {
		new Projection(null, renaming, child);
	}
		
	/**
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		this.iterator = new Projection(projected, renaming, child);
		Projection copy = this.iterator.deepCopy();
		Assert.assertEquals("Projection iterators deep copy child must be equals to itself", this.child, copy.getChild());
		Assert.assertEquals("Projection iterator columns must match that of initialization", this.iterator.getColumns(), copy.getColumns());
		Assert.assertEquals("Projection iterator type must match that of child", this.iterator.getType(), copy.getType());
		Assert.assertEquals("Projection iterator inputs must match that of child", this.iterator.getInputColumns(), copy.getInputColumns());
		Assert.assertEquals("Projection iterator input type must match that of child", this.iterator.getInputType(), copy.getInputType());
	}

	/**
	 * Checks for next.
	 */
	@Test public void hasNext() {
		this.iterator = new Projection(projected, renaming, new Scan(relation));
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); this.iterator.reset();
	}

	/**
	 * Next.
	 */
	@Test public void next() {
		this.iterator = new Projection(projected, renaming, child);
		this.iterator.open();
		Assert.assertEquals(projectedType.createTuple(1, "x", "one"), this.iterator.next()); 
		Assert.assertEquals(projectedType.createTuple(2, "x", "two"), this.iterator.next());
		Assert.assertEquals(projectedType.createTuple(3, "x", "three"), this.iterator.next());
	}

	/**
	 * Reset.
	 */
	@Test public void reset() {
		this.iterator = new Projection(projected, renaming, new Scan(relation));
		this.iterator.open();
		
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(projectedType.createTuple(1, "x", "one"), this.iterator.next()); 
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(projectedType.createTuple(2, "x", "two"), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(projectedType.createTuple(3, "x", "three"), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
		this.iterator.reset();
		
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(projectedType.createTuple(1, "x", "one"), this.iterator.next()); 
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(projectedType.createTuple(2, "x", "two"), this.iterator.next());
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(projectedType.createTuple(3, "x", "three"), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}
	
	/**
	 * Bind null.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindNull() {
		this.iterator = new Projection(projected, renaming, new TopDownAccess(relation, mt));
		this.iterator.open();
		this.iterator.bind(null);
	}
	
	/**
	 * Bind on unopened.
	 */
	@Test(expected=IllegalStateException.class)
	public void bindOnUnopened() {
		this.iterator = new Projection(projected, renaming, new TopDownAccess(relation, mt));
		this.iterator.bind(inputType.createTuple(4, "four"));
	}
	
	/**
	 * Bind illegal type.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindIllegalType() {
		this.iterator = new Projection(projected, renaming, new TopDownAccess(relation, mt));
		this.iterator.open();
		this.iterator.bind(inputType.createTuple("four", 4));
	}
	
	/**
	 * Bind empty tuple.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void bindEmptyTuple() {
		this.iterator = new Projection(projected, renaming, new TopDownAccess(relation, mt));
		this.iterator.open();
		this.iterator.bind(Tuple.EmptyTuple);
	}
	
	/**
	 * Bind.
	 */
	@Test public void bind() {
		this.iterator = new Projection(projected, renaming, new TopDownAccess(relation, mt));
		this.iterator.open();
		this.iterator.bind(inputType.createTuple(4, "four"));
		Assert.assertFalse(this.iterator.hasNext());

		this.iterator.bind(inputType.createTuple(3, "four"));
		Assert.assertFalse(this.iterator.hasNext());

		this.iterator.bind(inputType.createTuple(4, "three"));
		Assert.assertFalse(this.iterator.hasNext());

		this.iterator.bind(inputType.createTuple(3, "three"));
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(projectedType.createTuple(3, "x", "three"), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());


		this.iterator.bind(inputType.createTuple(1, "one"));
		Assert.assertTrue(this.iterator.hasNext());
		Assert.assertEquals(projectedType.createTuple(1, "x", "one"), this.iterator.next());
		Assert.assertFalse(this.iterator.hasNext());
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.runtime.exec.iterator.TupleIteratorTest#getIterator()
	 */
	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
}
