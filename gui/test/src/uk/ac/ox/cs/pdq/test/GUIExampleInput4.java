// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

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


public class GUIExampleInput4 {
	
	// test method tests example input
	@Test
	public void test()
	{
		Schema schema = testschema();
	}
	static public Schema testschema() {
		Relation nation = Relation.create("nation",
			new Attribute[] {
					Attribute.create(Integer.class, "n_nationkey"),
					Attribute.create(String.class, "n_name"),
					Attribute.create(Integer.class, "n_regionkey"),
					Attribute.create(String.class, "n_comment")
					},
			new AccessMethodDescriptor[] {
					AccessMethodDescriptor.create("m10", new Integer[] { }),
					AccessMethodDescriptor.create("m11", new Integer[] { 0, 1 })
					});
		Relation region = Relation.create("region",
			new Attribute[] {
					Attribute.create(String.class, "r_regionkey"),
					Attribute.create(String.class, "r_name"),
					Attribute.create(String.class, "r_comment")},
			new AccessMethodDescriptor[] {
					AccessMethodDescriptor.create("m12", new Integer[] { }),
					AccessMethodDescriptor.create("m11", new Integer[] { 0 })
					});
					
					

		Schema schema = new Schema(new Relation[] {			
			nation,
			region});
		return schema;
	}

}
