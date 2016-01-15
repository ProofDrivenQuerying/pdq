package uk.ac.ox.cs.pdq.test.algebra;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.ox.cs.pdq.algebra.Count;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * @author Julien Leblay
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectionTest extends UnaryOperatorTest {

	Projection operator;
	TupleType projectedType = TupleType.DefaultFactory.create(String.class, Integer.class);
	Term[] projected = new Term[] {new TypedConstant<>("c"), new Variable("a")};
	Map<Integer, Term> renaming = Maps.newLinkedHashMap();
	List<Term> renamedOutput = Lists.<Term>newArrayList(new TypedConstant<>("c"), new Variable("d"));
	List<Term> renamedInput = Lists.<Term>newArrayList(new Variable("d"), new Skolem("e"));
	
	@Before public void setup() throws RelationalOperatorException {
		super.setup();
		int i = 0;
		for (Term t: renamedInput) {
	    	this.renaming.put(i++, t);
		}

        MockitoAnnotations.initMocks(this);
        when(child.getColumns()).thenReturn(outputTerms);
        when(child.getColumn(0)).thenReturn(outputTerms.get(0), outputTerms.get(1), outputTerms.get(2));
		when(child.getType()).thenReturn(outputType);
		when(child.getInputTerms()).thenReturn(inputTerms);
		when(child.getInputType()).thenReturn(inputType);
		when(child.deepCopy()).thenReturn(child);

		this.operator = new Projection(child, renaming, projected);
	}
	
	RelationalOperator getOperator() {
		return this.operator;
	}
	
	@Test(expected=NullPointerException.class)
	public void initProjectionNullArguments() {
		new Projection(null, null, (List) null);
	}
	
	@Test(expected=NullPointerException.class)
	public void initProjectionTestNullArgument1() {
		new Projection(null, renaming, projected);
	}
	
	@Test(expected=NullPointerException.class)
	public void initProjectionTestNullArgument3() {
		new Projection(child, renaming, (List) null);
	}
	
	@Test(expected=AssertionError.class)
	public void initProjectionChildBadRenaming() {
		Map<Integer, Term> renaming = new HashMap<>();
		renaming.put(this.projected.length + 1, new Variable("SomeTerm"));
		new Projection(child, renaming);
	}

	@Test(expected=AssertionError.class)
	public void initProjectionChildBadHead() {
		new Projection(child, Lists.<Term>newArrayList(new Variable("UnrelatedTerm")));
	}

	@Test public void initProjectionChildHeadArray() {
		this.operator = new Projection(child, projected);
		Assert.assertEquals("Projection operator type must match that of child", projectedType, this.operator.getType());
		Assert.assertEquals("Projection operator inputs must match that of child", child.getInputTerms(), this.operator.getInputTerms());
		Assert.assertEquals("Projection operator input type must match that of child", child.getInputType(), this.operator.getInputType());
		Assert.assertEquals("Projection projected list must match that of initialization", Lists.newArrayList(this.projected), this.operator.getProjected());
		Assert.assertEquals("Projection renaming must be empty if unspecified", new HashMap<>(), this.operator.getRenaming());
	}

	@Test public void initProjectionChildHead() {
		this.operator = new Projection(child,  Lists.newArrayList(this.projected));
		Assert.assertEquals("Projection operator type must match that of child", projectedType, this.operator.getType());
		Assert.assertEquals("Projection operator inputs must match that of child", child.getInputTerms(), this.operator.getInputTerms());
		Assert.assertEquals("Projection operator input type must match that of child", child.getInputType(), this.operator.getInputType());
		Assert.assertEquals("Projection projected list must match that of initialization", Lists.newArrayList(this.projected), this.operator.getProjected());
		Assert.assertEquals("Projection renaming must be empty if unspecified", new HashMap<>(), this.operator.getRenaming());
	}

	@Ignore @Test public void initProjectionChildRenaming() {
		this.operator = new Projection(child, renaming);
		Assert.assertEquals("Projection operator type must match that of child", child.getType(), this.operator.getType());
		Assert.assertEquals("Projection operator inputs must match that of child", renamedInput, this.operator.getInputTerms());
		Assert.assertEquals("Projection operator input type must match that of child", child.getInputType(), this.operator.getInputType());
		Assert.assertEquals("Projection projected list must match that of initialization", renamedOutput, this.operator.getColumns());
		Assert.assertEquals("Projection renaming must match that of initialization", this.renaming, this.operator.getRenaming());
	}

	@Test public void initProjectionChildRenamingHeadArray() {
		this.operator = new Projection(child, renaming, projected);
		Assert.assertEquals("Projection operator type must match that of child", projectedType, this.operator.getType());
		Assert.assertEquals("Projection operator inputs must match that of child", renamedInput, this.operator.getInputTerms());
		Assert.assertEquals("Projection operator input type must match that of child", child.getInputType(), this.operator.getInputType());
		Assert.assertEquals("Projection projected list must match that of initialization", Lists.newArrayList(this.projected), this.operator.getProjected());
		Assert.assertEquals("Projection renaming must match that of initialization", this.renaming, this.operator.getRenaming());
		Assert.assertEquals("Projection projected list must match that of initialization", renamedOutput, this.operator.getColumns());
}

	@Test public void initProjectionChildRenamingHead() {
		this.operator = new Projection(child, renaming,  Lists.newArrayList(this.projected));
		Assert.assertEquals("Projection operator type must match that of child", projectedType, this.operator.getType());
		Assert.assertEquals("Projection operator inputs must match that of child", renamedInput, this.operator.getInputTerms());
		Assert.assertEquals("Projection operator input type must match that of child", child.getInputType(), this.operator.getInputType());
		Assert.assertEquals("Projected list must match that of initialization", Lists.newArrayList(this.projected), this.operator.getProjected());
		Assert.assertEquals("Projection renaming must match that of initialization", this.renaming, this.operator.getRenaming());
		Assert.assertEquals("Projected list must match that of initialization", renamedOutput, this.operator.getColumns());
	}

	@Test public void deepCopy() throws RelationalOperatorException {
		Projection copy = this.operator.deepCopy();
		Assert.assertEquals("Projection operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Projection operators deep copy child must be equals to itself", this.child, copy.getChild());
		Assert.assertEquals("Projection operator type must match that of child", this.operator.getType(), copy.getType());
		Assert.assertEquals("Projection operator inputs must match that of child", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Projection operator input type must match that of child", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("Projection output must match that of initialization", this.operator.getColumns(), this.operator.getColumns());
		Assert.assertEquals("Projection output type must match that of initialization", this.operator.getType(), this.operator.getType());
		Assert.assertEquals("Projection projected list must match that of initialization", this.operator.getProjected(), copy.getProjected());
		Assert.assertEquals("Projection renaming must match that of initialization", this.operator.getRenaming(), copy.getRenaming());
	}

	@Test public void getColumn() {
		for (int i = 0, l = this.renamedOutput.size(); i < l; i++) {
			Assert.assertEquals("Projection operator's " + i + "th column must match that of child",
					renamedOutput.get(i), this.operator.getColumn(i));
		}
	}

	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumn() {
		this.operator.getColumn(this.outputTerms.size() + 1);
	}
	@Test public void testHashCode() throws RelationalOperatorException {
		Set<RelationalOperator> s = new LinkedHashSet<>();

		s.add(getOperator());
		Assert.assertEquals("Operator set must have size 1", 1, s.size());

		s.add(getOperator());
		Assert.assertEquals("Operator set must have size 1, after being adding twice to set", 1, s.size());

		s.add(new Projection(getOperator().deepCopy()));
		Assert.assertEquals("Operator set must have size 2, after new count is added", 2, s.size());

		// TODO: agree on semantics of adding deep copy.
		s.add(getOperator().deepCopy());
		Assert.assertEquals("Operator set must have size 2, after deep copy is added", 2, s.size());
	}
}
