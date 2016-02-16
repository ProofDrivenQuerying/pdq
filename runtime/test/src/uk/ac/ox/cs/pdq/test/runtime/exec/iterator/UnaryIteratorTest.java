package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.List;

import org.mockito.Mock;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class UnaryIteratorTest.
 *
 * @author Julien LEBLAY
 */
public abstract class UnaryIteratorTest extends TupleIteratorTest {

	/** The b. */
	Attribute a = new Attribute(String.class, "a"), 
			b = new Attribute(Integer.class, "b");
	
	/** The child. */
	@Mock TupleIterator child;
	
	/** The output columns. */
	List<Typed> outputColumns = Lists.<Typed>newArrayList(a, b, new TypedConstant<>("str"), new TypedConstant<>(6));
	
	/** The input columns. */
	List<Typed> inputColumns = Lists.<Typed>newArrayList(b, a);
	
	/** The output type. */
	TupleType outputType = TupleType.DefaultFactory.create(String.class, Integer.class, String.class, Integer.class);
	
	/** The input type. */
	TupleType inputType = TupleType.DefaultFactory.create(Integer.class, String.class);
}
