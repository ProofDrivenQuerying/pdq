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
public abstract class UnaryIteratorTest extends TupleIteratorTest {

	Attribute a = new Attribute(String.class, "a"), 
			b = new Attribute(Integer.class, "b");
	@Mock TupleIterator child;
	List<Typed> outputColumns = Lists.<Typed>newArrayList(a, b, new TypedConstant<>("str"), new TypedConstant<>(6));
	List<Typed> inputColumns = Lists.<Typed>newArrayList(b, a);
	TupleType outputType = TupleType.DefaultFactory.create(String.class, Integer.class, String.class, Integer.class);
	TupleType inputType = TupleType.DefaultFactory.create(Integer.class, String.class);
}
