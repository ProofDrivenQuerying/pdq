package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.List;

import org.mockito.Mock;

import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class NaryIteratorTest.
 *
 * @author Julien LEBLAY
 */
public abstract class NaryIteratorTest extends TupleIteratorTest {

	/** The child5. */
	@Mock TupleIterator child1, child2, child3, child4, child5;
	
	/** The e. */
	Typed a = Attribute.create(String.class, "a"), 
		b = Attribute.create(Integer.class, "b"), 
		c = Attribute.create(Integer.class, "c"), 
		d = TypedConstant.create("d"), 
		e = Attribute.create(String.class, "e");
	
	/** The child1 columns. */
	List<Typed> child1Columns = Lists.<Typed>newArrayList(a, b, c, d);
	
	/** The child2 columns. */
	List<Typed> child2Columns = Lists.<Typed>newArrayList(c, e);
	
	/** The child3 columns. */
	List<Typed> child3Columns = Lists.<Typed>newArrayList(e);
	
	/** The child4 columns. */
	List<Typed> child4Columns = Lists.<Typed>newArrayList(c, e);
	
	/** The child5 columns. */
	List<Typed> child5Columns = Lists.<Typed>newArrayList();
	
	/** The child1 to2. */
	List<Typed> child1To2 = Lists.newArrayList(a, b, c, d, c, e);
	
	/** The child1 and3. */
	List<Typed> child1And3 = Lists.newArrayList(a, b, c, d, e);
	
	/** The child1 to4. */
	List<Typed> child1To4 = Lists.newArrayList(a, b, c, d, c, e, e, c, e);

	/** The child1 input columns. */
	List<Typed> child1InputColumns = Lists.<Typed>newArrayList(a, c);
	
	/** The child2 input columns. */
	List<Typed> child2InputColumns = Lists.<Typed>newArrayList(e);
	
	/** The child3 input columns. */
	List<Typed> child3InputColumns = Lists.<Typed>newArrayList(e);
	
	/** The child4 input columns. */
	List<Typed> child4InputColumns = Lists.<Typed>newArrayList(c);
	
	/** The child5 input columns. */
	List<Typed> child5InputColumns = Lists.<Typed>newArrayList();
	
	/** The child1 to2 input. */
	List<Typed> child1To2Input = Lists.<Typed>newArrayList(a, c, e);
	
	/** The child1 and3 input. */
	List<Typed> child1And3Input = Lists.<Typed>newArrayList(a, c, e);
	
	/** The child1 to4 input. */
	List<Typed> child1To4Input = Lists.<Typed>newArrayList(a, c, e, e, c);

	/** The child1 type. */
	TupleType child1Type = TupleType.DefaultFactory.createFromTyped(child1Columns);
	
	/** The child1 input type. */
	TupleType child1InputType = TupleType.DefaultFactory.createFromTyped(child1InputColumns);

	/** The child2 type. */
	TupleType child2Type = TupleType.DefaultFactory.createFromTyped(child2Columns);
	
	/** The child2 input type. */
	TupleType child2InputType = TupleType.DefaultFactory.createFromTyped(child2InputColumns);

	/** The child3 type. */
	TupleType child3Type = TupleType.DefaultFactory.createFromTyped(child3Columns);
	
	/** The child3 input type. */
	TupleType child3InputType = TupleType.DefaultFactory.createFromTyped(child3InputColumns);

	/** The child4 type. */
	TupleType child4Type = TupleType.DefaultFactory.createFromTyped(child4Columns);
	
	/** The child4 input type. */
	TupleType child4InputType = TupleType.DefaultFactory.createFromTyped(child4InputColumns);

	/** The child5 type. */
	TupleType child5Type = TupleType.EmptyTupleType;
	
	/** The child5 input type. */
	TupleType child5InputType = TupleType.EmptyTupleType;
	
	/** The child1 to2 type. */
	TupleType child1To2Type = TupleType.DefaultFactory.createFromTyped(child1To2);
	
	/** The child1 to2 input type. */
	TupleType child1To2InputType = TupleType.DefaultFactory.createFromTyped(child1To2Input);
	
	/** The child1 and3 type. */
	TupleType child1And3Type = TupleType.DefaultFactory.createFromTyped(child1And3);
	
	/** The child1 and3 input type. */
	TupleType child1And3InputType = TupleType.DefaultFactory.createFromTyped(child1And3Input);
	
	/** The child1 to4 type. */
	TupleType child1To4Type = TupleType.DefaultFactory.createFromTyped(child1To4);
	
	/** The child1 to4 input type. */
	TupleType child1To4InputType = TupleType.DefaultFactory.createFromTyped(child1To4Input);
			
}
