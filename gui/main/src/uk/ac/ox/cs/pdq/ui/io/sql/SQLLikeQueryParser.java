package uk.ac.ox.cs.pdq.ui.io.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.ConjunctiveQueryBodyBuilder;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteLexer;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Column_nameContext;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Compound_select_stmtContext;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.ANTLRErrorListener;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.junit.Assert;

/**
 * The Class SQLLikeQueryParser.
 */
public class SQLLikeQueryParser {
	
	/** The log. */
	private static Logger log = Logger.getLogger(SQLLikeQueryParser.class);
	
	/** The tokens. */
	private CommonTokenStream tokens;
	
	/** The where. */
	private Token where;
	
	/**
	 * Instantiates a new SQL like query parser.
	 *
	 * @param lexer the lexer
	 */
	public SQLLikeQueryParser(CommonTokenStream tokens) {
		this.tokens = tokens;
	}
	
	public static final int
	SCOL=1, DOT=2, OPEN_PAR=3, CLOSE_PAR=4, COMMA=5, ASSIGN=6, STAR=7, PLUS=8, 
	MINUS=9, TILDE=10, PIPE2=11, DIV=12, MOD=13, LT2=14, GT2=15, AMP=16, PIPE=17, 
	LT=18, LT_EQ=19, GT=20, GT_EQ=21, EQ=22, NOT_EQ1=23, NOT_EQ2=24,
	AND=32, AS=33, FROM=75, IS=92,  JOIN=94, NOT=102, NULL=104, ON=107, OR=108,
	SELECT=128, USING=140, WHERE=146, IDENTIFIER=149, NUMERIC_LITERAL=150, 
	STRING_LITERAL=152, SINGLE_LINE_COMMENT=154, 
	MULTILINE_COMMENT=155, SPACES=156, UNEXPECTED_CHAR=157;

	
	public static final String[] tokenNames = {
			"<INVALID>", "';'", "'.'", "'('", "')'", "','", "'='", "'*'", "'+'", "'-'", 
			"'~'", "'||'", "'/'", "'%'", "'<<'", "'>>'", "'&'", "'|'", "'<'", "'<='", 
			"'>'", "'>='", "'=='", "'!='", "'<>'", "", "", "", 
			"", "", "", "", "AND", "AS", "", 
			"", "", "", "", "", "", 
			"", "", "", "", "", "", "", 
			"", "", "", "", "", 
			"", "", "", "", "", 
			"", "", "", "", "", "", 
			"", "", "", "", "", "", "", 
			"", "", "", "", "FROM", "", "", 
			"", "", "", "", "", "", "", 
			"", "", "", "", "", "", 
			"", "IS", "", "JOIN", "", "", "", "", 
			"", "", "", "NOT", "", "NULL", "", 
			"", "ON", "OR", "", "", "", "", 
			"", "", "", "", "", "", 
			"", "", "", "", "", "", 
			"", "", "", "SELECT", "", "", 
			"", "", "", "", "", "", 
			"", "", "", "USING", "", "", 
			"", "", "", "WHERE", "", "", "IDENTIFIER", 
			"NUMERIC_LITERAL", "", "STRING_LITERAL", "", 
			"SINGLE_LINE_COMMENT", "MULTILINE_COMMENT", "SPACES", "UNEXPECTED_CHAR"
		};

	private Token identifier() throws Exception
	{
		// <identifier> ::= [ <introducer><character set specification> ] <actual identifier>
		return match(IDENTIFIER);
	}

	private void search_condition() throws Exception
	{
		// <search condition> ::=
		// <boolean term>
		// |   <search condition> OR <boolean term>
		boolean_term();
		if(lookahead(1) == OR)
		{
			match(OR);
			search_condition();
		}
	}

	private void boolean_term() throws Exception
	{
		// <boolean term> ::=
		// 		<boolean factor>
		// |    <boolean term> AND <boolean factor>
		boolean_factor();
		if(lookahead(1) == AND)
		{
			match(AND);
			boolean_term();
		}
	}
	
	private void boolean_factor() throws Exception
	{
		// <boolean factor> ::= [ NOT ] <boolean test>	
		if(lookahead(1) == NOT)
		{
			match(NOT);
		}
		boolean_test();
	}


	private void boolean_test() throws Exception
	{
		// <boolean test> ::= <boolean primary> [ IS [ NOT ] <truth value> ]
		boolean_primary();
		if(lookahead(1) == IS)
		{
			match(IS);
			if(lookahead(1) == NOT)
			{
				match(NOT);
			}
			truth_value();
		}
	}

	private void boolean_primary() throws Exception
	{
		// <boolean primary> ::= <predicate> | <left paren> <search condition> <right paren>
		if(lookahead(1) != OPEN_PAR)
		{
			predicate();
		}
		else
		{
			match(OPEN_PAR);
			search_condition();
			match(CLOSE_PAR);
		}
	}
	
	private void truth_value() throws Exception
	{
		// <truth value> ::= TRUE | FALSE | UNKNOWN
		if(lookahead(1) == IDENTIFIER)
		{
			Token token = match(IDENTIFIER);
			if(token.getText().equals("TRUE"))
			{
				System.out.println("TRUE");
			}
			else if(token.getText().equals("FALSE"))
			{
				System.out.println("FALSE");
			}
			else //if(token.getText().equals("UNKNOWN"))
			{
				System.out.println("UNKNOWN");
			}
		}
	}
	
	private void predicate() throws Exception
	{
		// <predicate> ::=
		// 		<comparison predicate>
		//	|   <between predicate>
		//	|   <in predicate>
		//	|   <like predicate>
		//	|   <null predicate>
		//	|   <quantified comparison predicate>
		//	|   <exists predicate>
		//	|   <match predicate>
		//	|   <overlaps predicate>
		comparison_predicate();
	}

	private void comparison_predicate() throws Exception
	{
		// <comparison predicate> ::= <row value constructor> <comp op> <row value constructor>
		row_value_constructor();
		match_comp_op();
		row_value_constructor();
	}
	
	private void row_value_constructor() throws Exception
	{
		// <row value constructor> ::=
		// 		<row value constructor element>
		// 	|   <left paren> <row value constructor list> <right paren>
		//	|   <row subquery>
		if(lookahead(1) == OPEN_PAR)
		{
			match(OPEN_PAR);
			row_value_constructor_list();
			match(CLOSE_PAR);
		}
		else
		{
			row_value_constructor_element();
		}
	}

	private void row_value_constructor_list() throws Exception
	{
		// <row value constructor list> ::= <row value constructor element> [ { <comma> <row value constructor element> } ... ]
		row_value_constructor_element();
		while(lookahead(1) == COMMA)
		{
			match(COMMA);
			row_value_constructor_element();
		}
	}
	
	private void row_value_constructor_element() throws Exception
	{
		// <row value constructor element> ::=
		// 		<value expression>
		// 	|   <null specification>
		// 	|   <default specification>
		if((lookahead(1) == IDENTIFIER) ||
		   (lookahead(1) == NUMERIC_LITERAL) ||
	       (lookahead(1) == STRING_LITERAL))
		{
			value_expression();
		}
		else if(lookahead(1) == NULL)
		{
			null_specification();
		}
		else
		{
			error("Missing: ROW_VALUE_CONSTRUCTOR_ELEMENT");
		}
	}
	
	private void null_specification() throws Exception
	{
		// <null specification> ::= NULL
		match(NULL);
	}

	private void value_expression() throws Exception
	{
		// <value expression> ::=
		//	   <numeric value expression>
		// |   <string value expression>
		// |   <datetime value expression>
		// |   <interval value expression>
		int token = lookahead(1);
		if(token == IDENTIFIER)
		{
			identifier();
			if(lookahead(1) == DOT)
			{
				match(DOT);
				match(IDENTIFIER);
			}
		}
		else if(token == NUMERIC_LITERAL)
		{
			numeric_value_expression();
		}
		else if(token == STRING_LITERAL)
		{
			string_value_expression();
		}
		else
		{
			error("Missing: VALUE_EXPRESSION");
		}
	}
	
	
	private void query_specification() throws Exception
	{
		match(SELECT);
		select_list();
		table_expression();
	}
	
	private void select_list() throws Exception
	{
		// <select list> ::=
		//		<asterisk>
		// | <select sublist> [ { <comma> <select sublist> }... ]	
		if(lookahead(1) == STAR)
		{
			match(STAR);
		}
		else
		{
			select_sublist();
			while(lookahead(1) == COMMA)
			{
				match(COMMA);
				select_sublist();
			}
		}
	}
	
	private void select_sublist() throws Exception
	{
		// <select sublist> ::= <derived column> | <qualifier> <period> <asterisk>
		if((lookahead(1) == IDENTIFIER))
		{
			// MR derived_column();
			Token token = identifier();
			if(lookahead(1) == DOT)
			{
				match(DOT);
				Token token2 = identifier();
				columnNames.add(token2.getText());
			}
		}
		else if(lookahead(1) != FROM)
		{
			qualifier();
			assert(lookahead(1) == DOT);
			assert(lookahead(1) == STAR);
		}
	}

	private void derived_column() throws Exception
	{
		//	<derived column> ::= <value expression> [ <as clause> ]
		value_expression();
		if(lookahead(1) == AS)
		{
			as_clause();
		}
	}
	
	private void numeric_value_expression() throws Exception
	{
		match(NUMERIC_LITERAL);
	}
		
	private void string_value_expression() throws Exception
	{
		// <string value expression> ::= <character value expression> | <bit value expression>
		match(STRING_LITERAL);
	}
	
	private void as_clause() throws Exception
	{
		//	<as clause> ::= [ AS ] <column name>
		if(lookahead(1) == AS)
		{
			match(AS);
		}
		column_name();
	}
	
	private void column_name() throws Exception
	{
		// <column name> ::= <identifier>
		identifier();
	}
	
	private void qualifier() throws Exception
	{
		// <qualifier> ::= <table name> | <correlation name>
		int token = lookahead(1);
		if(token == IDENTIFIER)
		{
			table_name();
		}
		else
		{
			correlation_name();
		}
	}
	
	private Token table_name() throws Exception
	{
		// <table name> ::= <qualified name> | <qualified local table name>
		if(lookahead(1) == STRING_LITERAL)
		{
			return qualified_local_table_name();
		}
		else if(lookahead(1) == -1)
		{
			error("Missing TABLE_NAME");
			return null;
		}
		else
		{
			return qualified_name();
		}
	}

	private Token correlation_name() throws Exception
	{
		// <correlation name> ::= <identifier>
		return identifier();
	}

	private Token qualified_name() throws Exception
	{
		// <qualified name> ::= [ <schema name> <period> ] <qualified identifier>
		if((lookahead(1) == IDENTIFIER) && (lookahead(2) == DOT))
		{
			schema_name();
			match(DOT);
		}
		return qualified_identifier();
	}
	
	private Token qualified_local_table_name() throws Exception
	{
		// <qualified local table name> ::= MODULE <period> <local table name>
		match("MODULE");			
		match(DOT);			
		return local_table_name();
	}
	
	private Token local_table_name() throws Exception
	{
		// <local table name> ::= <qualified identifier>
		return qualified_identifier();
	}
	
	private Token qualified_identifier() throws Exception
	{
		// <qualified identifier> ::= <identifier>
		return identifier();
	}
	
	private void schema_name() throws Exception
	{
		// <schema name> ::= [ <catalog name> <period> ] <unqualified schema name>
		if((lookahead(1) == IDENTIFIER) && (lookahead(2) == DOT))
		{
			catalog_name();
			match(DOT);						
		}
		unqualified_schema_name();
	}
	
	private void catalog_name() throws Exception
	{
		// <catalog name> ::= <identifier>
		identifier();
	}
	
	private void unqualified_schema_name() throws Exception
	{
		// <unqualified schema name> ::= <identifier>
		identifier();
	}
	
	private void table_expression() throws Exception
	{
		// <table expression> ::=
		//		<from clause>
		//		[ <where clause> ]
		//		[ <group by clause> ]
		//		[ <having clause> ]
		from_clause();
		if(lookahead(1) == WHERE)
		{
			where_clause();
		}
		matchEOF();
	}
		
	private void from_clause() throws Exception
	{
		if(lookahead(1) == FROM)
		{
			match(FROM);
		}
		table_reference(true);
		while(lookahead(1) == COMMA)
		{
			match(COMMA);
			table_reference(true);
		}
	}

	private void table_reference(boolean join) throws Exception
	{
		//	<table reference> ::=
		//		    <table name> [ <correlation specification> ]
		//		|   <derived table> <correlation specification>
		//		| 	<joined table>
		int index = marker();
		Token token1 = table_name();
		if(lookahead(1) == AS ||
		   lookahead(1) == IDENTIFIER)
		{
			Token token2 = correlation_specification();
			tableAliases.add(token1.getText() + " AS " + token2.getText());
		}
		if(lookahead(1) == JOIN)
		{
			if(join)
			{
				rollback(index);
				joined_table();
			}
		}
	/*	else if(lookahead(1) != ON)
		{
			rollback(index);
			derived_table();
			correlation_specification();
		}*/
	}
	
	private Token correlation_specification() throws Exception
	{
		//	<correlation specification> ::=
		//	[ AS ] <correlation name> [ <left paren> <derived column list> <right paren> ]
		if(lookahead(1) == AS)
		{
			match(AS);
		}
		Token token = correlation_name();
		if(lookahead(1) == OPEN_PAR)
		{
			match(OPEN_PAR);
			derived_column_list();
			match(CLOSE_PAR);
		}
		return token;
	}
	
	private void derived_column_list() throws Exception
	{
		column_name_list();
	}
	
	private void derived_table() throws Exception
	{
		table_subquery();
	}
	
	private void table_subquery() throws Exception
	{
		subquery();
	}

	private void column_name_list() throws Exception
	{
	}
	
	private void subquery() throws Exception
	{
	}
	
	private void joined_table() throws Exception
	{
		// <joined table> ::=
		//	<cross join>
		// |   <qualified join>
		// |   <left paren> <joined table> <right paren>
		if(lookahead(1) == OPEN_PAR)
		{
			match(OPEN_PAR);
			joined_table();
			match(CLOSE_PAR);
		}
		else
		{		
			table_reference(false);
			while(lookahead(1) == JOIN)
			{
				match(JOIN);
				table_reference(true);
				join_specification();
			}
		}
	}
	
	private void join_specification() throws Exception
	{
		//	<join specification> ::= <join condition> | <named columns join>
		if(lookahead(1) == ON)
		{
			join_condition();
		}
		else if(lookahead(1) == USING)
		{
			named_columns_join();
		}
	}
	

	private void join_condition() throws Exception
	{
		// <join condition> ::= ON <search condition>
		match(ON);
		int index1 = marker();
		search_condition();
		int index2 = marker();
		joinConstraints.add(tokens.getText(new Interval(index1, index2 - 1)));
	}

	
	private void named_columns_join() throws Exception
	{
		// <named columns join> ::= USING <left paren> <join column list> <right paren>
		match(USING);
		match(OPEN_PAR);
		join_column_list();
		match(CLOSE_PAR);
	}
	
	private void join_column_list() throws Exception
	{
		// <join column list> ::= <column name list>
		 column_name_list();
	}
	
	private void where_clause() throws Exception
	{
		match(WHERE);
		this.where = tokens.get(tokens.index());
		search_condition();
	}

	public void parse() throws Exception
	{
		lookahead(1);
		query_specification();
	}
	
	public void error(String message) throws Exception
	{
		Token token = tokens.get(tokens.index());
		throw new Exception("Line:" + token.getLine() + " Char:" + token.getCharPositionInLine() + " " + message + " Got: " + token.getText());
	}

	public String gettext()
	{
		return tokens.get(tokens.index()).getText();
	}
	
	public int lookahead(int n)
	{
		return tokens.LA(n);
	}
	
	public int marker()
	{
		return tokens.index();
	}

	public void rollback(int index) throws Exception
	{
		tokens.seek(index);
	}

	public void matchEOF() throws Exception
	{
		if(lookahead(1) != -1)
		{
			error("Missing EOF");
		}
	}
	public Token match(int token) throws Exception
	{
		Token tok = tokens.get(tokens.index());
		if(tok.getType() != token)
		{
			error("Missing: " + tokenNames[token]);
		}
		tokens.consume();
		System.out.println(tok.getText());
		return tok;
	}
	
	public Token match(String str) throws Exception
	{
		Token tok = tokens.get(tokens.index());
		if(!tok.getText().equals(str))
		{
			error("Missing: " + str);
		}
		tokens.consume();
		System.out.println(tok.getText());
		return tok;
	}
	
	public Token match_comp_op() throws Exception
	{
		if(lookahead(1) == ASSIGN) return match(ASSIGN);
		else if(lookahead(1) == LT) return match(LT);
		else if(lookahead(1) == LT_EQ) return match(LT_EQ);
		else if(lookahead(1) == GT)	return match(GT);
		else if(lookahead(1) == GT_EQ) return match(GT_EQ);
		else if(lookahead(1) == EQ) return match(EQ);
		else if(lookahead(1) == NOT_EQ1) return match(NOT_EQ1);
		else if(lookahead(1) == NOT_EQ2) return match(NOT_EQ2);
		else
		{
			error("Missing COMPARISON_OPERATOR");
		}
		return null;
	}
	
	/** The column names */
	private ArrayList<String> columnNames = new ArrayList<>();	
	
	/** The table aliases */
	private ArrayList<String> tableAliases = new ArrayList<>();
	
	/** The join constraints */
	private ArrayList<String> joinConstraints = new ArrayList<>();
	
	/** The result columns */
	private ArrayList<String> resultColumns = new ArrayList<>();
	
	
	List<String> getColumnNames()
	{
		return columnNames;
	}
	
	List<String> getTableAliases()
	{
		return tableAliases;
	}
	
	List<String> getJoinConstraints()
	{
		return joinConstraints;
	}
	
	String getLastExpr()
	{
		return tokens.getText(this.where, tokens.get(tokens.index()));
	}
	
	List<String> getResultColumns()
	{
		return resultColumns;
	}
	
}
