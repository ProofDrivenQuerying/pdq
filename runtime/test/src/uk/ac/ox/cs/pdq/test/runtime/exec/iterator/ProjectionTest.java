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

/**
 * 
 * @author Julien LEBLAY
 */
public class ProjectionTest extends UnaryIteratorTest {

	Projection iterator;
	TupleType projectedType;
	List<Typed> projected;
	Map<Integer, Typed> renaming;
	InMemoryTableWrapper relation;
	AccessMethod mt;
	Attribute A = new Attribute(String.class, "A"), 
			B = new Attribute(Integer.class, "B"), 
			c = new Attribute(String.class, "c"), 
			d = new Attribute(Integer.class, "d");
	
	@Before public void setup() {
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
		this.relation.addAccessMethod(mt);

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

	@Test public void initProjection() {
		this.iterator = new Projection(projected, child);
		Assert.assertEquals("Projection header match that of initialization", this.projected, this.iterator.getColumns());
		Assert.assertEquals("Projection child match that of initialization", this.child, this.iterator.getChild());
	}

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
	
	@Test(expected=IllegalArgumentException.class) 
	public void initSelectionNullChild() {
		new Projection(projected, renaming, null);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void initSelectionNullHeader() {
		new Projection(null, renaming, child);
	}
		
	@Test public void deepCopy() throws RelationalOperatorException {
		this.iterator = new Projection(projected, renaming, child);
		Projection copy = this.iterator.deepCopy();
		Assert.assertEquals("Projection iterators deep copy child must be equals to itself", this.child, copy.getChild());
		Assert.assertEquals("Projection iterator columns must match that of initialization", this.iterator.getColumns(), copy.getColumns());
		Assert.assertEquals("Projection iterator type must match that of child", this.iterator.getType(), copy.getType());
		Assert.assertEquals("Projection iterator inputs must match that of child", this.iterator.getInputColumns(), copy.getInputColumns());
		Assert.assertEquals("Projection iterator input type must match that of child", this.iterator.getInputType(), copy.getInputType());
	}

	@Test public void hasNext() {
		this.iterator = new Projection(projected, renaming, new Scan(relation));
		this.iterator.open();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertTrue(this.iterator.hasNext());  this.iterator.next();
		Assert.assertFalse(this.iterator.hasNext()); this.iterator.reset();
	}

	@Test public void next() {
		this.iterator = new Projection(projected, renaming, child);
		this.iterator.open();
		Assert.assertEquals(projectedType.createTuple(1, "x", "one"), this.iterator.next()); 
		Assert.assertEquals(projectedType.createTuple(2, "x", "two"), this.iterator.next());
		Assert.assertEquals(projectedType.createTuple(3, "x", "three"), this.iterator.next());
	}

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
	
	@Test(expected=IllegalArgumentException.class)
	public void bindNull() {
		this.iterator = new Projection(projected, renaming, new TopDownAccess(relation, mt));
		this.iterator.open();
		this.iterator.bind(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void bindOnUnopened() {
		this.iterator = new Projection(projected, renaming, new TopDownAccess(relation, mt));
		this.iterator.bind(inputType.createTuple(4, "four"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void bindIllegalType() {
		this.iterator = new Projection(projected, renaming, new TopDownAccess(relation, mt));
		this.iterator.open();
		this.iterator.bind(inputType.createTuple("four", 4));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void bindEmptyTuple() {
		this.iterator = new Projection(projected, renaming, new TopDownAccess(relation, mt));
		this.iterator.open();
		this.iterator.bind(Tuple.EmptyTuple);
	}
	
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

	@Override
	protected TupleIterator getIterator() {
		return this.iterator;
	}
}
