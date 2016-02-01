package uk.ac.ox.cs.pdq.test.algebra;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.Scan;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.EntityRelation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.util.Utility;

public class Operators {

	EntityRelation P = new EntityRelation("P", Types.FREE);
	EntityRelation Q = new EntityRelation("Q", Types.BOOLEAN);
	EntityRelation R = new EntityRelation("R", Types.FREE);
	EntityRelation S = new EntityRelation("S", Types.BOOLEAN);
	EntityRelation T = new EntityRelation("T", Types.FREE);
	Schema schema;

	@Before
	public void before() throws IOException {
		 Utility.assertsEnabled();
		 schema = new SchemaBuilder()
			.addRelation(P)
			.addRelation(Q)
			.addRelation(R)
			.addRelation(S)
			.addRelation(T)
			.build();
	}

	@Test
	public void testLeftDeep1() {
		Scan b1 = new Scan(P);
		Projection b3 = new Projection(b1, b1.getColumn(0));
		DependentAccess b4 = new DependentAccess(Q, this.Q.getAccessMethod());
		Scan b5 = new Scan(R);
		DependentAccess b6 = new DependentAccess(S, this.S.getAccessMethod());
		Join j1 = new Join(b4, b3);
		Join j2 = new Join(j1, b5);
		Join j3 = new Join(j2, b6);
		Assert.assertTrue("Plan is expected (quasi)-left deep", j3.isLeftDeep());
		Assert.assertFalse("Plan is expected not (quasi)-right deep", j3.isRightDeep());
	}

	@Test
	public void testRightDeep1() {
		Scan b1 = new Scan(P);
		Projection b3 = new Projection(b1, b1.getColumn(0));
		DependentAccess b4 = new DependentAccess(Q, this.Q.getAccessMethod());
		Scan b5 = new Scan(R);
		DependentAccess b6 = new DependentAccess(S, this.S.getAccessMethod());
		Join j1 = new Join(b4, b3);
		Join j2 = new Join(b5, j1);
		Join j3 = new Join(b6, j2);
		Assert.assertFalse("Plan is expected not (quasi)-left deep", j3.isLeftDeep());
		Assert.assertTrue("Plan is expected (quasi)-right deep", j3.isRightDeep());
	}

	@Test
	public void testBushy1() {
		Scan b1 = new Scan(P);
		Projection b3 = new Projection(b1, b1.getColumn(0));
		DependentAccess b4 = new DependentAccess(Q, this.Q.getAccessMethod());
		Scan b5 = new Scan(R);
		DependentAccess b6 = new DependentAccess(S, this.S.getAccessMethod());
		Join j1 = new Join(b4, b3);
		Join j2 = new Join(b5, b6);
		Join j3 = new Join(j1, j2);
		Assert.assertFalse("Plan is expected not (quasi)-left deep", j3.isLeftDeep());
		Assert.assertFalse("Plan is expected not (quasi)-right deep", j3.isRightDeep());
	}
}
