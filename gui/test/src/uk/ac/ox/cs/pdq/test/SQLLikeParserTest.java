// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryParser;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteLexer;


public class SQLLikeParserTest {
	
	// testParse method tests various SQL strings
	@Test
	public void testParse() {

		int size = 28;
		boolean[] succ = new boolean[size];
		String[] sql = new String[size];
		succ[0]  = false; sql[0]  = ""; // Line:1 Char:0 Missing SELECT Got: <EOF>
		succ[1]  = false; sql[1]  = "SELECT"; // Line:1 Char:6 Missing: IDENTIFIER Got: <EOF>
		succ[2]  = false; sql[2]  = "SELECT *"; // Line:1 Char:8 Missing TABLE_NAME Got: <EOF>
		succ[3]  = false; sql[3]  = "SELECT * FROM"; //  Line:1 Char:13 Missing TABLE_NAME Got: <EOF>
		succ[4]  = true;  sql[4]  = "SELECT * FROM R"; // Success
		succ[5]  = false; sql[5]  = "SELECT * FROM R AS"; // Line:1 Char:18 Missing: IDENTIFIER Got: <EOF>
		succ[6]  = true;  sql[6]  = "SELECT * FROM R AS a0"; // Success
		succ[7]  = false; sql[7]  = "SELECT * FROM R AS a0 JOIN"; // Line:1 Char:26 Missing TABLE_NAME Got: <EOF>
		succ[8]  = true;  sql[8]  = "SELECT * FROM R AS a0 JOIN S"; // Success
		succ[9]  = false; sql[9]  = "SELECT * FROM R AS a0 JOIN S AS"; // Line:1 Char:31 Missing: IDENTIFIER Got: <EOF>
		succ[10] = true;  sql[10] = "SELECT * FROM R AS a0 JOIN S AS a1"; // Success
		succ[11] = false; sql[11] = "SELECT * FROM R AS a0 JOIN S AS a1 ON"; // Line:1 Char:37 Missing: ROW_VALUE_CONSTRUCTOR_ELEMENT Got: <EOF>
		succ[12] = false; sql[12] = "SELECT * FROM R AS a0 JOIN S AS a1 ON a0"; // Line:1 Char:40 Missing COMPARISON_OPERATOR Got: <EOF>
		succ[13] = false; sql[13] = "SELECT * FROM R AS a0 JOIN S AS a1 ON a0.x"; // Line:1 Char:42 Missing COMPARISON_OPERATOR Got: <EOF>
		succ[14] = false; sql[14] = "SELECT * FROM R AS a0 JOIN S AS a1 ON a0.x ="; // Line:1 Char:44 Missing: ROW_VALUE_CONSTRUCTOR_ELEMENT Got: <EOF>
		succ[15] = true;  sql[15] = "SELECT * FROM R AS a0 JOIN S AS a1 ON a0.x = a1"; // Success
		succ[16] = true;  sql[16] = "SELECT * FROM R AS a0 JOIN S AS a1 ON a0.x = a1.y"; // Success
		succ[17] = false; sql[17] = "SELECT * FROM R AS a0 WHERE"; // Line:1 Char:27 Missing: ROW_VALUE_CONSTRUCTOR_ELEMENT Got: <EOF>
		succ[18] = false; sql[18] = "SELECT * FROM R AS a0 WHERE a0"; // Line:1 Char:30 Missing COMPARISON_OPERATOR Got: <EOF>
		succ[19] = false; sql[19] = "SELECT * FROM R AS a0 WHERE a0.x"; // Line:1 Char:32 Missing COMPARISON_OPERATOR Got: <EOF>
		succ[20] = true;  sql[20] = "SELECT * FROM R AS a0 WHERE a0.x = 0"; // Success
		succ[21] = false; sql[21] = "SELECT x"; // Line:1 Char:6 Missing: IDENTIFIER Got: <EOF>
		succ[22] = true;  sql[22] = "SELECT x FROM R"; // Success
		succ[23] = true;  sql[23] = "SELECT x, y FROM R"; // Success
		succ[24] = true;  sql[24] = "SELECT x, y, z FROM R"; // Success
		succ[25] = true;  sql[25] = "SELECT R.x FROM R"; // Success
		succ[26] = true;  sql[26] = "SELECT R.x, R.y FROM R"; // Success
		succ[27] = true;  sql[27] = "SELECT R.x, R.y, R.z FROM R"; // Success
		
		for(int i = 0; i < size; i++)
		{
			try
			{
				CharStream stream = new ANTLRInputStream(sql[i]);
				SQLiteLexer lexer = new SQLiteLexer(stream);
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				SQLLikeQueryParser parser = new SQLLikeQueryParser(tokens);
		       	parser.parse();
				System.out.println("---Success---");
		    }
			catch(Exception e)
			{
				if(succ[i] == false)
				{
					System.out.println("---Caught Exception---");
				}
				else
				{
					System.out.println("---Uncaught Exception---");				
				}
				System.out.println(e);
			}
		}
	}
}
