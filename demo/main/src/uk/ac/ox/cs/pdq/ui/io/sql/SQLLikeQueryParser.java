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
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.ANTLRErrorListener;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.junit.Assert;

// TODO: Auto-generated Javadoc
/**
 * The Class SQLLikeQueryReader.
 */
public class SQLLikeQueryParser {
	
	/** The log. */
	private static Logger log = Logger.getLogger(SQLLikeQueryParser.class);
	
	/** The schema. */
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
	LT=18, LT_EQ=19, GT=20, GT_EQ=21, EQ=22, NOT_EQ1=23, NOT_EQ2=24, ABORT=25, 
	ACTION=26, ADD=27, AFTER=28, ALL=29, ALTER=30, ANALYZE=31, 
	AND=32, AS=33, ASC=34, ATTACH=35, AUTOINCREMENT=36, BEFORE=37, 
	BEGIN=38, BETWEEN=39, BY=40, CASCADE=41, CASE=42, CAST=43, 
	CHECK=44, COLLATE=45, COLUMN=46, COMMIT=47, CONFLICT=48, CONSTRAINT=49, 
	CREATE=50, CROSS=51, CURRENT_DATE=52, CURRENT_TIME=53, CURRENT_TIMESTAMP=54, 
	DATABASE=55, DEFAULT=56, DEFERRABLE=57, DEFERRED=58, DELETE=59, 
	DESC=60, DETACH=61, DISTINCT=62, DROP=63, EACH=64, ELSE=65, 
	END=66, ESCAPE=67, EXCEPT=68, EXCLUSIVE=69, EXISTS=70, EXPLAIN=71, 
	FAIL=72, FOR=73, FOREIGN=74, FROM=75, FULL=76, GLOB=77, GROUP=78, 
	HAVING=79, IF=80, IGNORE=81, IMMEDIATE=82, IN=83, INDEX=84, 
	INDEXED=85, INITIALLY=86, INNER=87, INSERT=88, INSTEAD=89, INTERSECT=90, 
	INTO=91, IS=92, ISNULL=93, JOIN=94, KEY=95, LEFT=96, LIKE=97, 
	LIMIT=98, MATCH=99, NATURAL=100, NO=101, NOT=102, NOTNULL=103, 
	NULL=104, OF=105, OFFSET=106, ON=107, OR=108, ORDER=109, OUTER=110, 
	PLAN=111, PRAGMA=112, PRIMARY=113, QUERY=114, RAISE=115, RECURSIVE=116, 
	REFERENCES=117, REGEXP=118, REINDEX=119, RELEASE=120, RENAME=121, 
	REPLACE=122, RESTRICT=123, RIGHT=124, ROLLBACK=125, ROW=126, 
	SAVEPOINT=127, SELECT=128, SET=129, TABLE=130, TEMP=131, TEMPORARY=132, 
	THEN=133, TO=134, TRANSACTION=135, TRIGGER=136, UNION=137, UNIQUE=138, 
	UPDATE=139, USING=140, VACUUM=141, VALUES=142, VIEW=143, VIRTUAL=144, 
	WHEN=145, WHERE=146, WITH=147, WITHOUT=148, IDENTIFIER=149, NUMERIC_LITERAL=150, 
	BIND_PARAMETER=151, STRING_LITERAL=152, BLOB_LITERAL=153, SINGLE_LINE_COMMENT=154, 
	MULTILINE_COMMENT=155, SPACES=156, UNEXPECTED_CHAR=157;

	
	public static final String[] tokenNames = {
			"<INVALID>", "';'", "'.'", "'('", "')'", "','", "'='", "'*'", "'+'", "'-'", 
			"'~'", "'||'", "'/'", "'%'", "'<<'", "'>>'", "'&'", "'|'", "'<'", "'<='", 
			"'>'", "'>='", "'=='", "'!='", "'<>'", "ABORT", "ACTION", "ADD", 
			"AFTER", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", 
			"ATTACH", "AUTOINCREMENT", "BEFORE", "BEGIN", "BETWEEN", "BY", 
			"CASCADE", "CASE", "CAST", "CHECK", "COLLATE", "COLUMN", "COMMIT", 
			"CONFLICT", "CONSTRAINT", "CREATE", "CROSS", "CURRENT_DATE", 
			"CURRENT_TIME", "CURRENT_TIMESTAMP", "DATABASE", "DEFAULT", "DEFERRABLE", 
			"DEFERRED", "DELETE", "DESC", "DETACH", "DISTINCT", "DROP", 
			"EACH", "ELSE", "END", "ESCAPE", "EXCEPT", "EXCLUSIVE", "EXISTS", 
			"EXPLAIN", "FAIL", "FOR", "FOREIGN", "FROM", "FULL", "GLOB", 
			"GROUP", "HAVING", "IF", "IGNORE", "IMMEDIATE", "IN", "INDEX", 
			"INDEXED", "INITIALLY", "INNER", "INSERT", "INSTEAD", "INTERSECT", 
			"INTO", "IS", "ISNULL", "JOIN", "KEY", "LEFT", "LIKE", "LIMIT", 
			"MATCH", "NATURAL", "NO", "NOT", "NOTNULL", "NULL", "OF", 
			"OFFSET", "ON", "OR", "ORDER", "OUTER", "PLAN", "PRAGMA", 
			"PRIMARY", "QUERY", "RAISE", "RECURSIVE", "REFERENCES", "REGEXP", 
			"REINDEX", "RELEASE", "RENAME", "REPLACE", "RESTRICT", "RIGHT", 
			"ROLLBACK", "ROW", "SAVEPOINT", "SELECT", "SET", "TABLE", 
			"TEMP", "TEMPORARY", "THEN", "TO", "TRANSACTION", "TRIGGER", 
			"UNION", "UNIQUE", "UPDATE", "USING", "VACUUM", "VALUES", 
			"VIEW", "VIRTUAL", "WHEN", "WHERE", "WITH", "WITHOUT", "IDENTIFIER", 
			"NUMERIC_LITERAL", "BIND_PARAMETER", "STRING_LITERAL", "BLOB_LITERAL", 
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
		else if(lookahead(1) == DEFAULT)
		{
			default_specification();
		}
		else
		{
			error("row_value_constructor_element()");
		}
	}
	
	private void null_specification() throws Exception
	{
		// <null specification> ::= NULL
		match(NULL);
	}

	private void default_specification() throws Exception
	{
		// <default specification> ::= DEFAULT
		match(DEFAULT);
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
			error("value_expression()");
		}
	}
	
/*
<table name> ::= <qualified name> | <qualified local table name>

<qualified name> ::= [ <schema name> <period> ] <qualified identifier>

<character set specification> ::=
		<standard character repertoire name>
	|	<implementation-defined character repertoire name>
	|	<user-defined character repertoire name>
	|	<standard universal character form-of-use name>
	|	<implementation-defined universal character form-of-use name>

<standard character repertoire name> ::= <character set name>

<implementation-defined character repertoire name> ::= <character set name>

<user-defined character repertoire name> ::= <character set name>

<standard universal character form-of-use name> ::= <character set name>

<implementation-defined universal character form-of-use name> ::= <character set name>

<character set name> ::= [ <schema name> <period> ] <SQL language identifier>
*/

// <introducer> ::= <underscore>

// <actual identifier> ::= <regular identifier> | <delimited identifier>


/*
<boolean term> ::=
		<boolean factor>
	|   <boolean term> AND <boolean factor>

<boolean factor> ::= [ NOT ] <boolean test>

<boolean test> ::= <boolean primary> [ IS [ NOT ] <truth value> ]

<boolean primary> ::= <predicate> | <left paren> <search condition> <right paren>

<predicate> ::=
		<comparison predicate>
	|   <between predicate>
	|   <in predicate>
	|   <like predicate>
	|   <null predicate>
	|   <quantified comparison predicate>
	|   <exists predicate>
	|   <match predicate>
	|   <overlaps predicate>

<comparison predicate> ::= <row value constructor> <comp op> <row value constructor>

<row value constructor> ::=
		<row value constructor element>
	|   <left paren> <row value constructor list> <right paren>
	|   <row subquery>

<row value constructor element> ::=
		<value expression>
	|   <null specification>
	|   <default specification>

<value expression> ::=
		<numeric value expression>
	|   <string value expression>
	|   <datetime value expression>
	|   <interval value expression>

<numeric value expression> ::=
		<term>
	|   <numeric value expression> <plus sign> <term>
	|   <numeric value expression> <minus sign> <term>

<term> ::=
		<factor>
	|   <term> <asterisk> <factor>
	|   <term> <solidus> <factor>

<factor> ::= [ <sign> ] <numeric primary>

<numeric primary> ::= <value expression primary> | <numeric value function>

<value expression primary> ::=
		<unsigned value specification>
	|   <column reference>
	|   <set function specification>
	|   <scalar subquery>
	|   <case expression>
	|   <left paren> <value expression> <right paren>
	|   <cast specification>

<unsigned value specification> ::= <unsigned literal> | <general value specification>

<unsigned literal> ::= <unsigned numeric literal> | <general literal>

<general value specification> ::=
		<parameter specification>
	|   <dynamic parameter specification>
	|   <variable specification>
	|   USER
	|   CURRENT_USER
	|   SESSION_USER
	|   SYSTEM_USER
	|   VALUE

<parameter specification> ::= <parameter name> [ <indicator parameter> ]

<parameter name> ::= <colon> <identifier>

<indicator parameter> ::= [ INDICATOR ] <parameter name>

<dynamic parameter specification> ::= <question mark>

<variable specification> ::= <embedded variable name> [ <indicator variable> ]

<embedded variable name> ::= <colon><host identifier>

<host identifier> ::= <identifier>

<indicator variable> ::= [ INDICATOR ] <embedded variable name>

<column reference> ::= [ <qualifier> <period> ] <column name>

<qualifier> ::= <table name> | <correlation name>

<correlation name> ::= <identifier>

<set function specification> ::=
	COUNT <left paren> <asterisk> <right paren>
|   <general set function>

<general set function> ::=
	<set function type> <left paren> [ <set quantifier> ] <value expression> <right paren>

<set function type> ::= AVG | MAX | MIN | SUM | COUNT

<set quantifier> ::= DISTINCT | ALL
*/
	
	private void query_specification() throws Exception
	{
		if(lookahead(1) == SELECT)
		{
			match(SELECT);
			select_list();
			table_expression();
		}
		else
		{
			error("Missing SELECT");
		}
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
				if(lookahead(1) == IDENTIFIER)
				{
					Token token2 = identifier();
					columnNames.add(token2.getText());
				}
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
		if(lookahead(1) == IDENTIFIER)
		{
			return qualified_name();
		}
		else
		{
			return qualified_local_table_name();
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
	}
		
	private void from_clause() throws Exception
	{
		if(lookahead(1) == FROM)
		{
			match(FROM);
		}
		table_reference();
		while(lookahead(1) == COMMA)
		{
			match(COMMA);
			table_reference();
		}
	}

	private void table_reference() throws Exception
	{
		//	<table reference> ::=
		//		    <table name> [ <correlation specification> ]
		//		|   <derived table> <correlation specification>
		//		| 	<joined table>
		try
		{
			Token token1 = table_name();
			Token token2 = correlation_specification();
			tableAliases.add(token1.getText() + " AS " + token2.getText());
		}
		catch(Exception e1)
		{
			try
			{
				derived_table();
				correlation_specification();
			}
			catch(Exception e2)
			{				
				joined_table();
			}
		}
	}
	
	private void joined_table() throws Exception
	{
//		<joined table> ::=
//				<cross join>
//			|   <qualified join>
//			|   <left paren> <joined table> <right paren>		
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
//	<derived column list> ::= <column name list>
	}
	
	private void derived_table() throws Exception
	{
//	<derived table> ::= <table subquery>
	}
	
	private void table_subquery() throws Exception
	{
//	<table subquery> ::= <subquery>
	}

//	private void joined_table()
//	{
//		<joined table> ::=
//			<cross join>
//		|   <qualified join>
//	| <left paren> <joined table> <right paren>
//	}
/*	<cross join> ::=
			<table reference> CROSS JOIN <table reference>

	<qualified join> ::=
			<table reference> [ NATURAL ] [ <join type> ] JOIN <table reference> [ <join specification> ]

	<join type> ::=
			INNER
		|   <outer join type> [ OUTER ]
		|   UNION

	<outer join type> ::= LEFT | RIGHT | FULL

	<join specification> ::= <join condition> | <named columns join>

	<join condition> ::= ON <search condition>

	<named columns join> ::= USING <left paren> <join column list> <right paren>

	<join column list> ::= <column name list>*/
	
	private void where_clause() throws Exception
	{
		match(WHERE);
		this.where = tokens.get(tokens.index());
		search_condition();
	}
	
		/**
	 * From string.
	 *
	 * @param str the str
	 * @return the conjunctive query
	 * @throws Exception the exception
	 */
	public void parse() throws Exception
	{
		query_specification();
	}
	
	public void error(String caller) throws Exception
	{
		Token token = tokens.get(tokens.index());
		throw new Exception("Line:" + token.getLine() + " Char:" + token.getCharPositionInLine() + " " + caller + ": " + token.getText());
	}

	public String gettext()
	{
		return tokens.get(tokens.index()).getText();
	}
	
	public int lookahead(int n)
	{
		return tokens.LA(n);
	}

	public void rollback(int n, int index) throws Exception
	{
		if(lookahead(1) == n)
		{
			tokens.seek(index);
			throw new Exception("rollback");
		}
	}

	public Token match(int token) throws Exception
	{
		Token tok = tokens.get(tokens.index());
		if(tok.getType() != token)
		{
			error("Mismatched token");
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
			error("Mismatched string: " + tok.getText() + " instead of: " + str);
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
			error("Mismatched comparison operator");
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
