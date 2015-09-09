package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.List;

import org.mockito.Mock;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.collect.Lists;

/**
 * @author Julien LEBLAY
 */
public abstract class NaryIteratorTest extends TupleIteratorTest {

	@Mock TupleIterator child1, child2, child3, child4, child5;
	Typed a = new Attribute(String.class, "a"), 
		b = new Attribute(Integer.class, "b"), 
		c = new Attribute(Integer.class, "c"), 
		d = new TypedConstant<>("d"), 
		e = new Attribute(String.class, "e");
	List<Typed> child1Columns = Lists.<Typed>newArrayList(a, b, c, d);
	List<Typed> child2Columns = Lists.<Typed>newArrayList(c, e);
	List<Typed> child3Columns = Lists.<Typed>newArrayList(e);
	List<Typed> child4Columns = Lists.<Typed>newArrayList(c, e);
	List<Typed> child5Columns = Lists.<Typed>newArrayList();
	List<Typed> child1To2 = Lists.newArrayList(a, b, c, d, c, e);
	List<Typed> child1And3 = Lists.newArrayList(a, b, c, d, e);
	List<Typed> child1To4 = Lists.newArrayList(a, b, c, d, c, e, e, c, e);

	List<Typed> child1InputColumns = Lists.<Typed>newArrayList(a, c);
	List<Typed> child2InputColumns = Lists.<Typed>newArrayList(e);
	List<Typed> child3InputColumns = Lists.<Typed>newArrayList(e);
	List<Typed> child4InputColumns = Lists.<Typed>newArrayList(c);
	List<Typed> child5InputColumns = Lists.<Typed>newArrayList();
	List<Typed> child1To2Input = Lists.<Typed>newArrayList(a, c, e);
	List<Typed> child1And3Input = Lists.<Typed>newArrayList(a, c, e);
	List<Typed> child1To4Input = Lists.<Typed>newArrayList(a, c, e, e, c);

	TupleType child1Type = TupleType.DefaultFactory.createFromTyped(child1Columns);
	TupleType child1InputType = TupleType.DefaultFactory.createFromTyped(child1InputColumns);

	TupleType child2Type = TupleType.DefaultFactory.createFromTyped(child2Columns);
	TupleType child2InputType = TupleType.DefaultFactory.createFromTyped(child2InputColumns);

	TupleType child3Type = TupleType.DefaultFactory.createFromTyped(child3Columns);
	TupleType child3InputType = TupleType.DefaultFactory.createFromTyped(child3InputColumns);

	TupleType child4Type = TupleType.DefaultFactory.createFromTyped(child4Columns);
	TupleType child4InputType = TupleType.DefaultFactory.createFromTyped(child4InputColumns);

	TupleType child5Type = TupleType.EmptyTupleType;
	TupleType child5InputType = TupleType.EmptyTupleType;
	
	TupleType child1To2Type = TupleType.DefaultFactory.createFromTyped(child1To2);
	TupleType child1To2InputType = TupleType.DefaultFactory.createFromTyped(child1To2Input);
	
	TupleType child1And3Type = TupleType.DefaultFactory.createFromTyped(child1And3);
	TupleType child1And3InputType = TupleType.DefaultFactory.createFromTyped(child1And3Input);
	
	TupleType child1To4Type = TupleType.DefaultFactory.createFromTyped(child1To4);
	TupleType child1To4InputType = TupleType.DefaultFactory.createFromTyped(child1To4Input);
			
}
