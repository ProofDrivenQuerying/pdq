package uk.ac.ox.cs.pdq.test.algebra;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.Access;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;

/**
 *
 * @author Julien Leblay
 */
public class AccessTest extends UnaryOperatorTest {

	Access operator;
	Relation r1, r2, r3, r4, r5, r6, r7, r8;
	AccessMethod free, m1, m2;

	@Before public void setup() throws RelationalOperatorException {
        MockitoAnnotations.initMocks(this);
		when(child.getColumns()).thenReturn(outputTerms);
		when(child.getType()).thenReturn(outputType);
		when(child.getInputTerms()).thenReturn(inputTerms);
		when(child.getInputType()).thenReturn(inputType);
		when(child.deepCopy()).thenReturn(child);

		free = new AccessMethod();
		m1 = new AccessMethod("m1", Types.BOOLEAN, Lists.newArrayList(1));
		m2 = new AccessMethod("m2", Types.LIMITED, Lists.newArrayList(1, 2));
		
		r1 = new Relation("R1", Lists.<Attribute>newArrayList()) {};
		r2 = new Relation("R2", Lists.<Attribute>newArrayList(), Lists.newArrayList(free)) {};
		r3 = new Relation("R3", Lists.newArrayList(new Attribute(Integer.class, "a1"))) {};
		r4 = new Relation("R4", Lists.newArrayList(new Attribute(Integer.class, "a1")), Lists.newArrayList(free)) {};
		r5 = new Relation("R5", Lists.newArrayList(new Attribute(Integer.class, "a1")), Lists.newArrayList(m1)) {};
		r6 = new Relation("R6", Lists.newArrayList(new Attribute(Integer.class, "a1"), new Attribute(R, "a2"), new Attribute(String.class, "a3"))) {};
		r7 = new Relation("R7", Lists.newArrayList(new Attribute(Integer.class, "a1"), new Attribute(R, "a2"), new Attribute(String.class, "a3")), Lists.newArrayList(free)) {};
		r8 = new Relation("R8", Lists.newArrayList(new Attribute(Integer.class, "a1"), new Attribute(R, "a2"), new Attribute(String.class, "a3")), Lists.newArrayList(m2)) {};
	}
	
	RelationalOperator getOperator() {
		return this.operator;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTest2ArgumentsInconsistentAccessMethod1() {
		new Access(r1, free);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTest2ArgumentsInconsistentAccessMethod2() {
		new Access(r4, m1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTest2ArgumentsInconsistentAccessMethod3() {
		new Access(r8, free);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTest2ArgumentsInconsistentAccessMethod4() {
		new Access(r8, m1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTest3ArgumentsInconsistentAccessMethod1() {
		new Access(r2, free, child);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTest3ArgumentsInconsistentAccessMethod2() {
		new Access(r4, free, child);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTest3ArgumentsInconsistentAccessMethod3() {
		new Access(r7, free, child);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTestNull2Arguments1() {
		new Access(null, free);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTestNull2Arguments2() {
		new Access(r2, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTestNull2Arguments() {
		new Access(null, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTestNullArguments() {
		new Access(null, null, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTestNullArgument1() {
		new Access(null, free, child);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void initAccessTestNullArgument2() {
		new Access(r2, null, child);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setChildOnFreeAccess() {
		this.operator = new Access(r2, free);
		this.operator.setChild(child);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setChildNull() {
		this.operator = new Access(r5, m1, child);
		this.operator.setChild(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setChild() {
		this.operator = new Access(r8, m2);
		this.operator.setChild(child);
	}

	
	@Test public void initAccessTest2ArgsArityNil() {
		this.operator = new Access(r2, free);
		Assert.assertEquals("Access operator type must match that of child", r2.getType(), this.operator.getType());
		Assert.assertNull("Child must match that used for initialization", this.operator.getChild());
		Assert.assertTrue("Access operator inputs must match that of initialization", this.operator.getInputTerms().isEmpty());
		Assert.assertEquals("Access operator input type must match that of initialization", TupleType.EmptyTupleType, this.operator.getInputType());
		Assert.assertEquals("Access operator relation must match that of initialization", r2, this.operator.getRelation());
		Assert.assertEquals("Access operator access method must match that of initialization", free, this.operator.getAccessMethod());
	}
	
	@Test public void initAccessTest2ArgsArityOne() {
		this.operator = new Access(r4, free);
		Assert.assertEquals("Access operator type must match that of child", r4.getType(), this.operator.getType());
		Assert.assertNull("Child must match that used for initialization", this.operator.getChild());
		Assert.assertTrue("Access operator inputs must match that of initialization", this.operator.getInputTerms().isEmpty());
		Assert.assertEquals("Access operator input type must match that of initialization", TupleType.EmptyTupleType, this.operator.getInputType());
		Assert.assertEquals("Access operator relation must match that of initialization", r4, this.operator.getRelation());
		Assert.assertEquals("Access operator access method must match that of initialization", free, this.operator.getAccessMethod());
	}
	
	@Test public void initAccessTest2ArgsArityMoreThanOne() {
		this.operator = new Access(r7, free);
		Assert.assertEquals("Access operator type must match that of child", r7.getType(), this.operator.getType());
		Assert.assertNull("Child must match that used for initialization", this.operator.getChild());
		Assert.assertTrue("Access operator inputs must match that of initialization", this.operator.getInputTerms().isEmpty());
		Assert.assertEquals("Access operator input type must match that of initialization", TupleType.EmptyTupleType, this.operator.getInputType());
		Assert.assertEquals("Access operator relation must match that of initialization", r7, this.operator.getRelation());
		Assert.assertEquals("Access operator access method must match that of initialization", free, this.operator.getAccessMethod());
	}
	
	@Test public void initAccessTest3ArgsArityOne() {
		when(child.getInputTerms()).thenReturn(Lists.<Term>newArrayList(new Variable("a")));
		when(child.getInputType()).thenReturn(TupleType.DefaultFactory.create(Integer.class));
		this.operator = new Access(r5, m1, child);
		Assert.assertEquals("Access operator type must match that of child", r5.getType(), this.operator.getType());
		Assert.assertEquals("Child must match that used for initialization", child, this.operator.getChild());
		Assert.assertEquals("Access operator inputs must match that of initialization", m1.getInputs().size(), this.operator.getInputTerms().size());
		Assert.assertEquals("Access operator input type must match that of initialization", TupleType.DefaultFactory.createFromTyped(r5.getInputAttributes(m1)), this.operator.getInputType());
		Assert.assertEquals("Access operator relation must match that of initialization", r5, this.operator.getRelation());
		Assert.assertEquals("Access operator access method must match that of initialization", m1, this.operator.getAccessMethod());
	}
	
	@Test public void initAccessTest3ArgsArityMoreThanOne() {
		this.operator = new Access(r8, m2, child);
		Assert.assertEquals("Access operator type must match that of child", r8.getType(), this.operator.getType());
		Assert.assertEquals("Child must match that used for initialization", child, this.operator.getChild());
		Assert.assertEquals("Access operator inputs must match that of initialization", m2.getInputs().size(), this.operator.getInputTerms().size());
		Assert.assertEquals("Access operator input type must match that of initialization", TupleType.DefaultFactory.createFromTyped(r8.getInputAttributes(m2)), this.operator.getInputType());
		Assert.assertEquals("Access operator relation must match that of initialization", r8, this.operator.getRelation());
		Assert.assertEquals("Access operator access method must match that of initialization", m2, this.operator.getAccessMethod());
	}
	
	@Test public void deepCopy() throws RelationalOperatorException {
		this.operator = new Access(r2, free);
		Access copy = this.operator.deepCopy();
		Assert.assertEquals("Access operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Access operators deep copy child must be equals to itself", this.operator.getChild(), copy.getChild());
		Assert.assertEquals("Access copy output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Access copy type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("Access copy inputs must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Access copy input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("Access copy relation must match that of operator", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("Access copy access method must match that of operator", this.operator.getAccessMethod(), copy.getAccessMethod());

		this.operator = new Access(r4, free);
		copy = this.operator.deepCopy();
		Assert.assertEquals("Access operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Access operators deep copy child must be equals to itself", this.operator.getChild(), copy.getChild());
		Assert.assertEquals("Access copy output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Access copy type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("Access copy inputs must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Access copy input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("Access copy relation must match that of operator", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("Access copy access method must match that of operator", this.operator.getAccessMethod(), copy.getAccessMethod());

		this.operator = new Access(r7, free);
		copy = this.operator.deepCopy();
		Assert.assertEquals("Access operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Access operators deep copy child must be equals to itself", this.operator.getChild(), copy.getChild());
		Assert.assertEquals("Access copy output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Access copy type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("Access copy inputs must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Access copy input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("Access copy relation must match that of operator", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("Access copy access method must match that of operator", this.operator.getAccessMethod(), copy.getAccessMethod());

		this.operator = new Access(r8, m2, child);
		copy = this.operator.deepCopy();
		Assert.assertEquals("Access operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Access operators deep copy child must be equals to itself", this.operator.getChild(), copy.getChild());
		Assert.assertEquals("Access copy output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Access copy type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("Access copy inputs must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Access copy input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("Access copy relation must match that of operator", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("Access copy access method must match that of operator", this.operator.getAccessMethod(), copy.getAccessMethod());

		when(child.getInputTerms()).thenReturn(Lists.<Term>newArrayList(new Variable("a")));
		when(child.getInputType()).thenReturn(TupleType.DefaultFactory.create(Integer.class));
		this.operator = new Access(r5, m1, child);
		copy = this.operator.deepCopy();
		Assert.assertEquals("Access operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Access operators deep copy child must be equals to itself", this.operator.getChild(), copy.getChild());
		Assert.assertEquals("Access copy output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Access copy type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("Access copy inputs must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Access copy input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("Access copy relation must match that of operator", this.operator.getRelation(), copy.getRelation());
		Assert.assertEquals("Access copy access method must match that of operator", this.operator.getAccessMethod(), copy.getAccessMethod());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumnNilArity() {
		this.operator = new Access(r2, m1, child);
		this.operator.getColumn(this.r2.getArity());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumnArityOne() {
		this.operator = new Access(r4, m1, child);
		this.operator.getColumn(this.r4.getArity());
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumnArityMoreThanOne() {
		this.operator = new Access(r7, m1, child);
		this.operator.getColumn(this.r7.getArity());
	}
	

	@Test public void isClosed() {
		this.operator = new Access(r2, free);
		Assert.assertTrue("Operator's isClosed must be consistent with relation's access method", this.operator.isClosed());

		this.operator = new Access(r8, m2, child);
		Assert.assertFalse("Operator's isClosed must be consistent with relation's access method", this.operator.isClosed());
	}

	@Test public void isQuasiLeaf() {
		this.operator = new Access(r2, free);
		Assert.assertTrue("Operator's isQuasiLeaf must be true iff the access is free", this.operator.isQuasiLeaf());

		this.operator = new Access(r8, m2, child);
		Assert.assertFalse("Operator's isQuasiLeaf must be true iff the access is free", this.operator.isQuasiLeaf());
	}

	@Test public void isLeftDeep() {
		this.operator = new Access(r8, m2, child);
		Mockito.when(child.isLeftDeep()).thenReturn(true);
		Assert.assertEquals("Operator's isLeftDeep must match that of child", child.isLeftDeep(), getOperator().isLeftDeep());

		Mockito.when(child.isLeftDeep()).thenReturn(false);
		Assert.assertEquals("Operator's isLeftDeep must match that of child", child.isLeftDeep(), getOperator().isLeftDeep());
	}

	@Test public void isRightDeep() {
		this.operator = new Access(r8, m2, child);
		Mockito.when(child.isRightDeep()).thenReturn(true);
		Assert.assertEquals("Operator's isRightDeep must match that of child", child.isRightDeep(), getOperator().isRightDeep());

		Mockito.when(child.isRightDeep()).thenReturn(false);
		Assert.assertEquals("Operator's isRightDeep must match that of child", child.isRightDeep(), getOperator().isRightDeep());
	}
	
	@Test public void getDepth() {
		this.operator = new Access(r2, free);
		Assert.assertEquals("Operator's depth must be exactly that of child + 1", (int) 1, (int) getOperator().getDepth());
		
		this.operator = new Access(r8, m2, child);
		Mockito.when(child.getDepth()).thenReturn(10);
		Assert.assertEquals("Operator's depth must be exactly that of child + 1", (int) 11, (int) getOperator().getDepth());
		Mockito.when(child.getDepth()).thenReturn(123124);
		Assert.assertEquals("Operator's depth must be exactly that of child + 1", (int) 123125, (int) getOperator().getDepth());
	}

	@Test(expected=AssertionError.class) 
	public void getDepthNegative() {
		this.operator = new Access(r8, m2, child);
		Mockito.when(child.getDepth()).thenReturn(-1);
		this.operator.getDepth();
	}

	@Test(expected=IllegalArgumentException.class) 
	public void getNegativeColumn() {
		this.operator = new Access(r2, free);
		this.operator.getColumn(-1);
	}

}
