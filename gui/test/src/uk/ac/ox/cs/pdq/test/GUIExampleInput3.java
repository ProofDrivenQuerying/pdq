package uk.ac.ox.cs.pdq.test;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryParser;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteLexer;


public class GUIExampleInput3 {
	
	// test method tests example input
	@Test
	public void test()
	{
		Schema schema = testschema();
	}
	static public Schema testschema() {
		Relation R = Relation.create("R",
			new Attribute[] {
					Attribute.create(String.class, "x")},
			new AccessMethodDescriptor[] {
					AccessMethodDescriptor.create("R_free", new Integer[] { })});
		Relation S = Relation.create("S",
			new Attribute[] {
					Attribute.create(String.class, "x"),
					Attribute.create(String.class, "y")},
			new AccessMethodDescriptor[] {
					AccessMethodDescriptor.create("S_free", new Integer[] { })});
		Relation T = Relation.create("T",
			new Attribute[] {
					Attribute.create(String.class, "y"),
					Attribute.create(String.class, "z")},
			new AccessMethodDescriptor[] {
					AccessMethodDescriptor.create("T_free", new Integer[] { })});
					

		Schema schema = new Schema(new Relation[] {			
			R,
			S,
			T});
		return schema;
	}

}
