package uk.ac.ox.cs.pdq.test.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.Access;
import uk.ac.ox.cs.pdq.algebra.Count;
import uk.ac.ox.cs.pdq.algebra.CrossProduct;
import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.DependentJoin;
import uk.ac.ox.cs.pdq.algebra.Distinct;
import uk.ac.ox.cs.pdq.algebra.IsEmpty;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.Scan;
import uk.ac.ox.cs.pdq.algebra.Selection;
import uk.ac.ox.cs.pdq.algebra.StaticInput;
import uk.ac.ox.cs.pdq.algebra.Union;
import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.OperatorReader;
import uk.ac.ox.cs.pdq.io.xml.OperatorWriter;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class XMLRelationOperatorIOTest.
 */
public class XMLRelationOperatorIOTest {
	
	/** The limited. */
	AccessMethod 
		free = new AccessMethod(),
		limited = new AccessMethod(Types.LIMITED, Lists.newArrayList(1));
	
	/** The c. */
	Attribute 
		a = new Attribute(String.class, "a"),
		b = new Attribute(Integer.class, "b"), 
		c = new Attribute(Double.class, "c");
	
	/** The r2. */
	Relation 
		r1 = new Relation("r1", Lists.newArrayList(a, c), Lists.newArrayList(free)) {}, 
		r2 = new Relation("r2", Lists.newArrayList(a, b, c), Lists.newArrayList(limited)) {};
	
	/** The s. */
	Schema s = Schema.builder()
			.addRelation(r1)
			.addRelation(r2)
			.build();
	
	/** The writer. */
	OperatorWriter writer;
	
	/** The reader. */
	OperatorReader reader;
	
	/** The bos. */
	ByteArrayOutputStream bos;

	/**
	 * Setup.
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
		this.writer = new OperatorWriter();
		this.reader = new OperatorReader(s);
		this.bos = new ByteArrayOutputStream();
	}
	
	/**
	 * Test read write top down access.
	 */
	@Test
	public void testReadWriteTopDownAccess() {
		DependentAccess expected = new DependentAccess(r2, limited);
		writer.write(new PrintStream(bos), expected);
		DependentAccess observed = (DependentAccess) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getRelation(), observed.getRelation());
		Assert.assertEquals(expected.getAccessMethod(), observed.getAccessMethod());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write scan.
	 */
	@Test
	public void testReadWriteScan() {
		Scan expected = new Scan(r1);
		writer.write(new PrintStream(bos), expected);
		Scan observed = (Scan) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getRelation(), observed.getRelation());
		Assert.assertEquals(expected.getAccessMethod(), observed.getAccessMethod());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write bottom up access.
	 */
	@Test
	public void testReadWriteBottomUpAccess() {
		Scan scan = new Scan(r1);
		Access expected = new Access(r2, limited, scan);
		writer.write(new PrintStream(bos), expected);
		Access observed = (Access) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getRelation(), observed.getRelation());
		Assert.assertEquals(expected.getAccessMethod(), observed.getAccessMethod());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write projection.
	 */
	@Test
	public void testReadWriteProjection() {
		Scan scan = new Scan(r1);
		Projection expected = new Projection(scan, Lists.newArrayList(scan.getColumn(1)));
		writer.write(new PrintStream(bos), expected);
		Projection observed = (Projection) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write open projection.
	 */
	@Test
	public void testReadWriteOpenProjection() {
		DependentAccess scan = new DependentAccess(r2, limited);
		Projection expected = new Projection(scan, Lists.newArrayList(scan.getColumn(1)));
		writer.write(new PrintStream(bos), expected);
		Projection observed = (Projection) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write projection with constant.
	 */
	@Test
	public void testReadWriteProjectionWithConstant() {
		Scan scan = new Scan(r1);
		Projection expected = new Projection(scan, Lists.newArrayList(scan.getColumn(1), new TypedConstant<>("A")));
		writer.write(new PrintStream(bos), expected);
		Projection observed = (Projection) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write projection with constant and renaming.
	 */
	@Test
	public void testReadWriteProjectionWithConstantAndRenaming() {
		Scan scan = new Scan(r1);
		Map<Integer, Term> renaming = new LinkedHashMap<>();
		renaming.put(1, new Variable("v"));
		Projection expected = new Projection(scan,
				renaming,
				Lists.newArrayList(scan.getColumn(1), new TypedConstant<>("A")));
		writer.write(new PrintStream(bos), expected);
		Projection observed = (Projection) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write selection.
	 */
	@Test
	public void testReadWriteSelection() {
		Scan scan = new Scan(r1);
		AttributeEqualityCondition p1 = new AttributeEqualityCondition(0, 1);
		ConstantEqualityCondition p2 = new ConstantEqualityCondition(0, new TypedConstant<>("A"));
		ConjunctiveCondition p3 = new ConjunctiveCondition(Lists.newArrayList(p1, p2));
		Selection expected = new Selection(p3, scan);
		writer.write(new PrintStream(bos), expected);
		Selection observed = (Selection) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getPredicate(), observed.getPredicate());
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write open selection.
	 */
	@Test
	public void testReadWriteOpenSelection() {
		DependentAccess access = new DependentAccess(r2, limited);
		AttributeEqualityCondition p1 = new AttributeEqualityCondition(0, 1);
		ConstantEqualityCondition p2 = new ConstantEqualityCondition(0, new TypedConstant<>("A"));
		ConjunctiveCondition p3 = new ConjunctiveCondition(Lists.newArrayList(p1, p2));
		Selection expected = new Selection(p3, access);
		writer.write(new PrintStream(bos), expected);
		Selection observed = (Selection) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getPredicate(), observed.getPredicate());
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write distinct.
	 */
	@Test
	public void testReadWriteDistinct() {
		Scan scan = new Scan(r1);
		Distinct expected = new Distinct(scan);
		writer.write(new PrintStream(bos), expected);
		Distinct observed = (Distinct) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write open distinct.
	 */
	@Test
	public void testReadWriteOpenDistinct() {
		DependentAccess access = new DependentAccess(r2, limited);
		Distinct expected = new Distinct(access);
		writer.write(new PrintStream(bos), expected);
		Distinct observed = (Distinct) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write is empty.
	 */
	@Test
	public void testReadWriteIsEmpty() {
		Scan scan = new Scan(r1);
		IsEmpty expected = new IsEmpty(scan);
		writer.write(new PrintStream(bos), expected);
		IsEmpty observed = (IsEmpty) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write open is empty.
	 */
	@Test
	public void testReadWriteOpenIsEmpty() {
		DependentAccess access = new DependentAccess(r2, limited);
		IsEmpty expected = new IsEmpty(access);
		writer.write(new PrintStream(bos), expected);
		IsEmpty observed = (IsEmpty) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write count.
	 */
	@Test
	public void testReadWriteCount() {
		Scan scan = new Scan(r1);
		Count expected = new Count(scan);
		writer.write(new PrintStream(bos), expected);
		Count observed = (Count) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write open count.
	 */
	@Test
	public void testReadWriteOpenCount() {
		DependentAccess access = new DependentAccess(r2, limited);
		Count expected = new Count(access);
		writer.write(new PrintStream(bos), expected);
		Count observed = (Count) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write static input.
	 */
	@Test
	public void testReadWriteStaticInput() {
		StaticInput expected = new StaticInput(Lists.<TypedConstant<?>>newArrayList(
				new TypedConstant<>("1"),
				new TypedConstant<>(1),
				new TypedConstant<>(1.0)));
		writer.write(new PrintStream(bos), expected);
		StaticInput observed = (StaticInput) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getTuples(), observed.getTuples());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write union.
	 */
	@Test
	public void testReadWriteUnion() {
		Scan scan1 = new Scan(r1);
		Scan scan2 = new Scan(r1);
		Union expected = new Union(scan1, scan2);
		writer.write(new PrintStream(bos), expected);
		Union observed = (Union) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChildren(), observed.getChildren());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write open union.
	 */
	@Test
	public void testReadWriteOpenUnion() {
		Scan scan = new Scan(r1);
		DependentAccess access = new DependentAccess(r2, limited);
		Projection proj = new Projection(access, Lists.newArrayList(
				access.getColumn(0), access.getColumn(1)));
		Union expected = new Union(scan, proj);
		writer.write(new PrintStream(bos), expected);
		Union observed = (Union) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChildren(), observed.getChildren());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write cross product.
	 */
	@Test
	public void testReadWriteCrossProduct() {
		Scan scan1 = new Scan(r1);
		Scan scan2 = new Scan(r1);
		CrossProduct expected = new CrossProduct(scan1, scan2);
		writer.write(new PrintStream(bos), expected);
		CrossProduct observed = (CrossProduct) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChildren(), observed.getChildren());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write open cross product.
	 */
	@Test
	public void testReadWriteOpenCrossProduct() {
		Scan scan = new Scan(r1);
		DependentAccess access = new DependentAccess(r2, limited);
		CrossProduct expected = new CrossProduct(scan, access);
		writer.write(new PrintStream(bos), expected);
		CrossProduct observed = (CrossProduct) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChildren(), observed.getChildren());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write join.
	 */
	@Test
	public void testReadWriteJoin() {
		Scan scan1 = new Scan(r1);
		Scan scan2 = new Scan(r1);
		Join expected = new Join (scan1, scan2);
		writer.write(new PrintStream(bos), expected);
		Join observed = (Join) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getChildren(), observed.getChildren());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write open join.
	 */
	@Test
	public void testReadWriteOpenJoin() {
		Scan scan = new Scan(r1);
		DependentAccess access = new DependentAccess(r2, limited);
		Join expected = new Join(scan, access);
		writer.write(new PrintStream(bos), expected);
		Join observed = (Join) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getPredicate(), observed.getPredicate());
		Assert.assertEquals(expected.getChildren(), observed.getChildren());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write dependent join.
	 */
	@Test
	public void testReadWriteDependentJoin() {
		Scan scan = new Scan(r1);
		DependentAccess access = new DependentAccess(r2, limited);
		DependentJoin expected = new DependentJoin(scan, access);
		writer.write(new PrintStream(bos), expected);
		DependentJoin observed = (DependentJoin) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getPredicate(), observed.getPredicate());
		Assert.assertEquals(expected.getChildren(), observed.getChildren());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write join with alt predicate.
	 */
	@Test
	public void testReadWriteJoinWithAltPredicate() {
		Scan scan = new Scan(r1);
		DependentAccess access = new DependentAccess(r2, limited);
		AttributeEqualityCondition p1 = new AttributeEqualityCondition(0, 1);
		ConstantEqualityCondition p2 = new ConstantEqualityCondition(0, new TypedConstant<>("A"));
		ConjunctiveCondition p3 = new ConjunctiveCondition(Lists.newArrayList(p1, p2));
		Join expected = new Join(p3, scan, access);
		writer.write(new PrintStream(bos), expected);
		Join observed = (Join) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getPredicate(), observed.getPredicate());
		Assert.assertEquals(expected.getChildren(), observed.getChildren());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
	
	/**
	 * Test read write deep unary.
	 */
	@Test
	public void testReadWriteDeepUnary() {
		Scan scan = new Scan(r1);
		AttributeEqualityCondition p1 = new AttributeEqualityCondition(0, 1);
		ConstantEqualityCondition p2 = new ConstantEqualityCondition(0, new TypedConstant<>("A"));
		ConjunctiveCondition p3 = new ConjunctiveCondition(Lists.newArrayList(p1, p2));
		ConstantEqualityCondition p4 = new ConstantEqualityCondition(0, new TypedConstant<>(1.0));
		ConstantEqualityCondition p5 = new ConstantEqualityCondition(0, new TypedConstant<>(1));
		
		Projection pro1 = new Projection(scan, Lists.newArrayList(scan.getColumn(0), new TypedConstant<>("B")));
		Selection sel1 = new Selection(p3, pro1);
		Projection pro2 = new Projection(sel1, Lists.newArrayList(sel1.getColumn(1), new TypedConstant<>(2)));
		Selection sel2 = new Selection(p3, pro2);
		Projection pro3 = new Projection(sel2, Lists.newArrayList(new TypedConstant<>(-123.90), sel2.getColumn(1)));
		Selection sel3 = new Selection(p4, pro3);
		Projection pro4 = new Projection(sel3, Lists.newArrayList(sel3.getColumn(1)));
		Selection sel4 = new Selection(p5, pro4);
		Projection pro5 = new Projection(sel4, Lists.newArrayList(sel4.getColumn(0)));
		Selection expected = new Selection(p5, pro5);
		writer.write(new PrintStream(bos), expected);
		Selection observed = (Selection) reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(expected.getPredicate(), observed.getPredicate());
		Assert.assertEquals(expected.getChild(), observed.getChild());
		Assert.assertEquals(expected.getColumns(), observed.getColumns());
		Assert.assertEquals(expected.getDepth(), observed.getDepth());
		Assert.assertEquals(expected.getInputTerms(), observed.getInputTerms());
		Assert.assertEquals(expected.getInputType(), observed.getInputType());
		Assert.assertEquals(expected.getType(), observed.getType());
	}
}
