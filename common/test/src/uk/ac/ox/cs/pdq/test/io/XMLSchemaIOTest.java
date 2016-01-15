package uk.ac.ox.cs.pdq.test.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.LinearGuarded;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.FormulaEquivalence;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaWriter;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

public class XMLSchemaIOTest {
	
	AccessMethod 
		free = new AccessMethod("mt1", Types.FREE, Lists.<Integer>newArrayList()),
		limited = new AccessMethod("mt2", Types.LIMITED, Lists.newArrayList(1)),
		bool = new AccessMethod("mt3", Types.BOOLEAN, Lists.newArrayList(1));
	Attribute 
		a = new Attribute(String.class, "a"),
		b = new Attribute(Integer.class, "b"), 
		c = new Attribute(Double.class, "c");
	Relation 
		r1 = new Relation("r1", Lists.newArrayList(a), Lists.newArrayList(free)) {}, 
		r2 = new Relation("r2", Lists.newArrayList(a, b), Lists.newArrayList(limited, bool)) {},
		r3 = new Relation("r3", Lists.newArrayList(a, b, c)) {};
	Schema s = Schema.builder()
			.addRelation(r1)
			.addRelation(r2)
			.addRelation(r3)
			.build();
	Constraint c1, c2, c3, c4, c5;
	View 
		v1 = new View("v1", Lists.newArrayList(a), Lists.newArrayList(free)) {}, 
		v2 = new View("v2", Lists.newArrayList(a, b), Lists.newArrayList(limited, bool)) {},
		v3 = new View("v3", Lists.newArrayList(a, b, c)) {};

	ByteArrayOutputStream bos = new ByteArrayOutputStream();

	@Before public void setup() {
		Utility.assertsEnabled();
		this.bos = new ByteArrayOutputStream();
	}

	@Test
	public void testReadWriteSchemaRelationsOnly() {
		SchemaWriter writer = new SchemaWriter();
		SchemaReader reader = new SchemaReader();

		writer.write(new PrintStream(bos), s);
		Schema observed = reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(s.getRelations(), observed.getRelations());
		Assert.assertEquals(s.getViews(), observed.getViews());
		Assert.assertEquals(s.getDependencies(), observed.getDependencies());
		Assert.assertEquals(s.getConstants(), observed.getConstants());
		Assert.assertEquals(s.getMaxArity(), observed.getMaxArity());
		Assert.assertEquals(s.isCyclic(), observed.isCyclic());
	}

	@Test
	public void testReadWriteSchemaWithoutViews() {
		Predicate atom1 = r1.createAtoms();
		Predicate atom2 = r2.createAtoms();
		Predicate atom3 = r3.createAtoms();
		c1 = new LinearGuarded(atom2, Conjunction.of(atom1));
		c2 = new TGD(Conjunction.of(atom1, atom2), Conjunction.of(atom3));
		c3 = new TGD(Conjunction.of(atom3), Conjunction.of(atom1, atom2));
		Schema s = Schema.builder()
				.addRelation(r1)
				.addRelation(r2)
				.addRelation(r3)
				.addDependency(c1)
				.addDependency(c2)
				.addDependency(c3)
				.build();

		SchemaWriter writer = new SchemaWriter();
		SchemaReader reader = new SchemaReader();
		writer.write(new PrintStream(bos), s);
		Schema observed = reader.read(new ByteArrayInputStream(bos.toByteArray()));
		
		Assert.assertEquals(s.getRelations(), observed.getRelations());
		Assert.assertEquals(s.getViews(), observed.getViews());
		Assert.assertEquals(s.getDependencies().size(), observed.getDependencies().size());
		for (int i = 0, l = s.getDependencies().size(); i < l; i++) {
			Assert.assertTrue(FormulaEquivalence.approximateEquivalence(
					(Formula) s.getDependencies().get(i), 
					(Formula) observed.getDependencies().get(i)));
		}
		Assert.assertEquals(s.getConstants(), observed.getConstants());
		Assert.assertEquals(s.getMaxArity(), observed.getMaxArity());
		Assert.assertEquals(s.isCyclic(), observed.isCyclic());
	}
}
