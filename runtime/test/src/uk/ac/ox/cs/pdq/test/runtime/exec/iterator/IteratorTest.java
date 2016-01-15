package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.wrappers.InMemoryTableWrapper;
import uk.ac.ox.cs.pdq.db.wrappers.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.BindJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.MemoryScan;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.NestedLoopJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Projection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.Selection;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.SymmetricMemoryHashJoin;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TopDownAccess;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

@Ignore
public class IteratorTest {

	final Table empty = new Table();
	final TupleType booleanTuple = TupleType.DefaultFactory.create(Boolean.class);
	final TupleType intTuple = TupleType.DefaultFactory.create(Integer.class);

	List<Attribute> columns;
	TupleType type;
	Table tuples;
	RelationAccessWrapper relation;
	RelationAccessWrapper emptyRelation;
	AccessMethod accessMethod;
	Collection<Tuple> inputs;

	@Before
	public void prepare() {
		Utility.assertsEnabled();
		this.columns = Lists.<Attribute>newArrayList(
				new Attribute(String.class, "Col1"),
				new Attribute(Integer.class, "Col2"),
				new Attribute(Double.class, "Col3"));
		this.type = TupleType.DefaultFactory.create(String.class, Integer.class, Double.class);
		this.tuples = new Table(this.columns);
		this.tuples.appendRow(this.type.createTuple("A", 1, 10.0));
		this.tuples.appendRow(this.type.createTuple("B", -2, 8.0));
		this.tuples.appendRow(this.type.createTuple("C", 3, 6.0));
		this.tuples.appendRow(this.type.createTuple("D", 5, 4.0));
		this.tuples.appendRow(this.type.createTuple("E", 5, 2.0));
		this.tuples.appendRow(this.type.createTuple("F", 5, 0.0));
		this.tuples.appendRow(this.type.createTuple("G", 5, -1.0));
		this.tuples.appendRow(this.type.createTuple("I", -8, -3.0));
		this.tuples.appendRow(this.type.createTuple("J", 9, -5.0));
		this.tuples.appendRow(this.type.createTuple("K", -10, -7.0));
		this.accessMethod = new AccessMethod(Types.LIMITED, Lists.newArrayList(2));
		this.relation = new InMemoryTableWrapper("test", (List<Attribute>) this.tuples.getHeader(),
				Lists.newArrayList(this.accessMethod));
		((InMemoryTableWrapper) this.relation).load(this.tuples.getData());
		this.emptyRelation = new InMemoryTableWrapper("empty", (List<Attribute>) this.tuples.getHeader(),
				Lists.newArrayList(this.accessMethod));

		this.inputs = Lists.newArrayList();
		this.inputs.add(this.intTuple.createTuple(5));
		this.inputs.add(this.intTuple.createTuple(1));
//		this.inputs.add(this.intTuple.createTuple(5));
		this.inputs.add(this.intTuple.createTuple(-2));
		this.inputs.add(this.intTuple.createTuple(5));
		this.inputs.add(this.intTuple.createTuple(5));
		this.inputs.add(this.intTuple.createTuple(1));
		this.inputs.add(this.intTuple.createTuple(-2));
		this.inputs.add(this.intTuple.createTuple(5));
	}

	@Test
	public void testAccess() {
		try(TupleIterator it = new TopDownAccess(this.relation, this.accessMethod)) {
			int i = 0;
			it.open();
			for (Tuple t : this.inputs) {
				it.bind(t);
				while (it.hasNext()) {
					Assert.assertTrue(this.tuples.contains(it.next()));
					i++;
				}
			}
			Assert.assertEquals(20, i);
		}
	}

	@Test
	public void testBindJoin() {
		try(MemoryScan c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new TopDownAccess(this.relation, this.accessMethod);
			TupleIterator it = new BindJoin(c1, c2)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				Assert.assertEquals(this.intTuple.size() + this.type.size(), it.next().size());
				i++;
			}
			Assert.assertEquals(20, i);
		}
	}

	@Test
	public void testSelectionNonEmptyResult() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = 
					new Selection(
							new ConstantEqualityPredicate(1, new TypedConstant<>(5)), 
					new TopDownAccess(this.relation, this.accessMethod));
			TupleIterator it = new BindJoin(c1, c2)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				it.next();
				i++;
			}
			Assert.assertEquals(16, i);
		}
	}

	@Test
	public void testSelectionNoPredicate() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new Selection(
					new ConjunctivePredicate<>(),
					new TopDownAccess(this.relation, this.accessMethod));
			TupleIterator it = new BindJoin(c1, c2)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				it.next();
				i++;
			}
			Assert.assertEquals(20, i);
		}
	}

	@Test
	public void testSelectionEmptyResult() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new Selection(
					new ConstantEqualityPredicate(1, new TypedConstant<>(-9999)),
					new TopDownAccess(this.relation, this.accessMethod));
			TupleIterator it = new BindJoin(c1, c2)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				it.next();
				i++;
			}
			Assert.assertEquals(0, i);
		}
	}

	@Test
	public void testSelectionEmptyChild() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new Selection(
					new ConstantEqualityPredicate(1, new TypedConstant<>(5)), 
					new TopDownAccess(this.emptyRelation, this.accessMethod));
			TupleIterator it = new BindJoin(c1, c2)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				it.next();
				i++;
			}
			Assert.assertEquals(0, i);
		}
	}

	@Test
	public void testProjectionRetainAll() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new Projection(Lists.<Typed>newArrayList(this.columns), new TopDownAccess(this.relation, this.accessMethod));
			TupleIterator it = new BindJoin(c1, c2)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				Assert.assertEquals(this.intTuple.size() + this.type.size(), it.next().size());
				i++;
			}
			Assert.assertEquals(20, i);
		}
	}

	@Test
	public void testProjectionRemoveAll() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new Projection(Lists.<Typed>newArrayList(), new TopDownAccess(this.relation, this.accessMethod));
			TupleIterator it = new BindJoin(c1, c2)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				Assert.assertEquals(this.intTuple.size(), it.next().size());
				i++;
			}
			Assert.assertEquals(20, i);
		}
	}

	@Test
	public void testProjectionFirstColumn() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new Projection(Lists.<Typed> newArrayList(new Attribute(String.class, "Col1")), new TopDownAccess(this.relation, this.accessMethod));
			TupleIterator it = new BindJoin(c1, c2)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				Tuple t = it.next();
				Assert.assertEquals(this.intTuple.size() + 1, t.size());
				Assert.assertTrue("ABCDEFGHIJK".contains((String) t.getValue(1)));
				i++;
			}
			Assert.assertEquals(20, i);
		}
	}

	@Test
	public void testNestedLoopEmpty1() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new MemoryScan(Lists.<Typed>newArrayList(), this.empty);
			TupleIterator c3 = new TopDownAccess(this.relation, this.accessMethod);
			TupleIterator c4 = new NestedLoopJoin(c3.getInputColumns(), c2, c3);
			TupleIterator it = new BindJoin(c1, c4)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				it.next();
				i++;
			}
			Assert.assertEquals(0, i);
		}
	}

	@Test
	public void testNestedLoopEmpty2() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new TopDownAccess(this.relation, this.accessMethod);
			TupleIterator c3 = new MemoryScan(Lists.<Typed>newArrayList(), this.empty);
			TupleIterator c4 = new NestedLoopJoin(c2.getInputColumns(), c2, c3);
			TupleIterator it = new BindJoin(c1, c4)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				it.next();
				i++;
			}
			Assert.assertEquals(0, i);
		}
	}

	@Test
	public void testNestedLoop() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new TopDownAccess(this.relation, this.accessMethod);
			TupleIterator c3 = new TopDownAccess(this.relation, this.accessMethod);
			TupleIterator c4 = new NestedLoopJoin(c2.getInputColumns(), c2, c3);
			TupleIterator it = new BindJoin(c1, c4)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				it.next();
				i++;
			}
			Assert.assertEquals(20, i);
		}
	}

	@Test
	public void testSymmetricHashJoinEmpty1() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new MemoryScan(Lists.<Typed>newArrayList(), this.empty);
			TupleIterator c3 = new TopDownAccess(this.relation, this.accessMethod);
			TupleIterator c4 = new SymmetricMemoryHashJoin(c3.getInputColumns(), c2, c3);
			TupleIterator it = new BindJoin(c1, c4)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				it.next();
				i++;
			}
			Assert.assertEquals(0, i);
		}
	}

	@Test
	public void testSymmetricHashJoinEmpty2() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new TopDownAccess(this.relation, this.accessMethod);
			TupleIterator c3 = new MemoryScan(Lists.<Typed>newArrayList(), this.empty);
			TupleIterator c4 = new SymmetricMemoryHashJoin(c2.getInputColumns(), c2, c3);
			TupleIterator it = new BindJoin(c1, c4)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				it.next();
				i++;
			}
			Assert.assertEquals(0, i);
		}
	}

	@Test
	public void testSymmetricHashJoin() {
		try(TupleIterator c1 = new MemoryScan(Lists.<Typed>newArrayList(new Attribute(Integer.class, "Col2")), this.inputs);
			TupleIterator c2 = new TopDownAccess(this.relation, this.accessMethod);
			TupleIterator c3 = new TopDownAccess(this.relation, this.accessMethod);
			TupleIterator c4 = new SymmetricMemoryHashJoin(c2.getInputColumns(), c2, c3);
			TupleIterator it = new BindJoin(c1, c4)) {
			int i = 0;
			it.open();
			while (it.hasNext()) {
				it.next();
				i++;
			}
			Assert.assertEquals(20, i);
		}
	}
}