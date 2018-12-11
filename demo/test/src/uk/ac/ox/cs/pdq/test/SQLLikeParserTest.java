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

		int size = 17;
		boolean[] ex = new boolean[size];
		String[] sql = new String[size];
		ex[0] = true;  sql[0] = "";
		ex[1] = true;  sql[1] = "SELECT";
		ex[2] = true;  sql[2] = "SELECT *";
		ex[3] = true;  sql[3] = "SELECT * FROM";
		ex[4] = false; sql[4] = "SELECT * FROM R";
		ex[5] = true;  sql[5] = "SELECT * FROM R AS";
		ex[6] = false; sql[6] = "SELECT * FROM R AS a0";
		ex[7] = true;  sql[7] = "SELECT * FROM R AS a0 JOIN";
		ex[8] = true;  sql[8] = "SELECT * FROM R AS a0 JOIN S";
		ex[9] = true;  sql[9] = "SELECT * FROM R AS a0 JOIN S AS";
		ex[10] = true; sql[10] = "SELECT * FROM R AS a0 JOIN S AS a1";
		ex[11] = true; sql[11] = "SELECT * FROM R AS a0 JOIN S AS a1 ON";
		ex[12] = true; sql[12] = "SELECT * FROM R AS a0 JOIN S AS a1 ON a0";
		ex[13] = true; sql[13] = "SELECT * FROM R AS a0 JOIN S AS a1 ON a0.x";
		ex[14] = true; sql[14] = "SELECT * FROM R AS a0 JOIN S AS a1 ON a0.x =";
		ex[15] = true; sql[15] = "SELECT * FROM R AS a0 JOIN S AS a1 ON a0.x = a1";
		ex[16] = false; sql[16] = "SELECT * FROM R AS a0 JOIN S AS a1 ON a0.x = a1.y";
		
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
				if(ex[i] == true)
				{
					System.out.println("---Failure Caught---");
				}
				else
				{
					System.out.println("---Failure Uncaught---");				
				}
				System.out.println(e);
			}
		}
	}
}
