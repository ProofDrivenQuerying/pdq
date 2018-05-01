package uk.ac.ox.cs.pdq.test.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Utility unit test for the uk.ac.ox.cs.pdq.util.Utility class.
 *
 * @author Julien Leblay
 */
public class UtilityTest {

	protected AccessMethodDescriptor method0 = AccessMethodDescriptor.create(new Integer[] {});
	protected AccessMethodDescriptor method1 = AccessMethodDescriptor.create(new Integer[] { 0 });
	protected AccessMethodDescriptor method2 = AccessMethodDescriptor.create(new Integer[] { 0, 1 });
	protected AccessMethodDescriptor method3 = AccessMethodDescriptor.create(new Integer[] { 1 });

	protected Attribute a = Attribute.create(String.class, "a");
	protected Attribute b = Attribute.create(String.class, "b");
	protected Attribute c = Attribute.create(String.class, "c");
	protected Attribute d = Attribute.create(String.class, "d");

	protected Relation R;
	protected Relation S;
	protected Relation T;

	/**
	 * Makes sure assertions are enabled.
	 */
	@Before
	public void setup() {
		Utility.assertsEnabled();
		this.R = Relation.create("R", new Attribute[] { a, b, c }, new AccessMethodDescriptor[] { this.method0, this.method2 });
		this.S = Relation.create("S", new Attribute[] { a, c }, new AccessMethodDescriptor[] { this.method0, this.method1, this.method2 });
		this.T = Relation.create("T", new Attribute[] { a, b, c, d }, new AccessMethodDescriptor[] { this.method0, this.method1, this.method2 });
		this.R.setKey(PrimaryKey.create(new Attribute[] { this.a, this.b }));
		this.S.setKey(PrimaryKey.create(new Attribute[] { this.a, this.c }));
		this.T.setKey(PrimaryKey.create(new Attribute[] { this.a, this.b, this.c }));
		ForeignKey fk = new ForeignKey();
		fk.addReference(new Reference(this.a, this.a));
		fk.addReference(new Reference(this.b, this.b));
		fk.setForeignRelation(this.R);
		this.T.addForeignKey(new ForeignKey());
	}

	@Test
	public void testGetEGDs1() {
		EGD egds = Utility.getEGD(this.R, this.R.getKey().getAttributes());
		Assert.assertNotNull(egds);
		Assert.assertNotNull(egds.getAtoms());
		Assert.assertEquals(3, egds.getAtoms().length);
		Assert.assertNotNull(egds.getAtoms()[0]);
		Assert.assertNotNull(egds.getAtoms()[1]);
		Assert.assertNotNull(egds.getAtoms()[2]);
		Assert.assertTrue(!(egds.getAtoms()[0].isEquality()));
		Assert.assertTrue(!(egds.getAtoms()[1].isEquality()));
		Assert.assertTrue(egds.getAtoms()[2].isEquality());
		Assert.assertEquals(Variable.create("c"),egds.getAtoms()[2].getTerm(0));
		Assert.assertEquals(Variable.create("?c"),egds.getAtoms()[2].getTerm(1));
	}

	@Test(expected = RuntimeException.class)
	public void testGetEGDs2() {
		Utility.getEGD(this.S, this.S.getKey().getAttributes());
	}

	@Test
	public void testGetEGDs3() {
		EGD egds = Utility.getEGD(this.T, this.T.getKey().getAttributes());
		Assert.assertNotNull(egds);
		Assert.assertNotNull(egds.getAtoms());
		Assert.assertEquals(3, egds.getAtoms().length);
		Assert.assertNotNull(egds.getAtoms()[0]);
		Assert.assertNotNull(egds.getAtoms()[1]);
		Assert.assertNotNull(egds.getAtoms()[2]);
		Assert.assertTrue(!(egds.getAtoms()[0].isEquality()));
		Assert.assertTrue(!(egds.getAtoms()[1].isEquality()));
		Assert.assertTrue(egds.getAtoms()[2].isEquality());
		Assert.assertEquals(Variable.create("d"),egds.getAtoms()[2].getTerm(0));
		Assert.assertEquals(Variable.create("?d"),egds.getAtoms()[2].getTerm(1));
	}

}
