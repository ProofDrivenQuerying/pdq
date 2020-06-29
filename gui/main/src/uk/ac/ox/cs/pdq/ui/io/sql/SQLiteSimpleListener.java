// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.io.sql;

import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser;


// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving SQLiteSimple events.
 * The class that is interested in processing a SQLiteSimple
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSQLiteSimpleListener<code> method. When
 * the SQLiteSimple event occurs, that object's appropriate
 * method is invoked.
 *
 * @see SQLiteSimpleEvent
 */
public class SQLiteSimpleListener extends SQLiteBaseListener {
	
	/** The log. */
	private static Logger log = Logger.getLogger(SQLiteSimpleListener.class);

	/** The padding depth. */
	private int paddingDepth = 0;
	
	//	Map<String, String> resultAliasAttrs = new HashMap<String, String>();
	
	/** The table aliases. */
	private Set<ParseTree> tableAliases = new HashSet<ParseTree>();
	
	/** The join constraints. */
	private Set<ParseTree> joinConstraints = new HashSet<ParseTree>();
	
	/** The result columns. */
	private Set<ParseTree> resultColumns = new HashSet<ParseTree>();
	
	/** The result columns. */
	private Set<ParseTree> columnNames = new HashSet<ParseTree>();
	
	/** The last expr. */
	private ParseTree lastExpr = null;
	
	/** The found where. */
	private boolean foundWhere = false;
	
	/**
	 * Gets the join constraints.
	 *
	 * @return the join constraints
	 */
	public Set<ParseTree> getJoinConstraints() {
		return this.joinConstraints;
	}
	
	/**
	 * Gets the result columns.
	 *
	 * @return the result columns
	 */
	public Set<ParseTree> getResultColumns() {
		return this.resultColumns;
	}
	
	/**
	 * Gets the last expr.
	 *
	 * @return the last expr
	 */
	public ParseTree getLastExpr() {
		return this.lastExpr;
	}
	
	/**
	 * Gets the table aliases.
	 *
	 * @return the table aliases
	 */
	public Set<ParseTree> getTableAliases() {
		return this.tableAliases;
	}
	
	/**
	 * Gets the column names.
	 *
	 * @return the column names
	 */
	public Set<ParseTree> getColumnNames() {
		return this.columnNames;
	}
	
	/**
	 * Padding.
	 *
	 * @return the string
	 */
	private String padding() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < this.paddingDepth; i++) {
			s.append("  ");
		}
		return s.toString();
	}
	
	/**
	 * Prints the padded.
	 *
	 * @param str the str
	 */
	private void printPadded(String str) {
		log.debug(this.padding() + str);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#enterEveryRule(org.antlr.v4.runtime.ParserRuleContext)
	 */
	@Override
	public void enterEveryRule(@NotNull ParserRuleContext ctx) {
		this.paddingDepth++;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#exitEveryRule(org.antlr.v4.runtime.ParserRuleContext)
	 */
	@Override
	public void exitEveryRule(@NotNull ParserRuleContext ctx) {
		this.paddingDepth--;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#enterTable_name(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Table_nameContext)
	 */
	@Override
	public void enterTable_name(@NotNull SQLiteParser.Table_nameContext ctx) {
		this.printPadded("enterTable_name");
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#exitTable_name(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Table_nameContext)
	 */
	@Override
	public void exitTable_name(@NotNull SQLiteParser.Table_nameContext ctx) {
		this.printPadded("exitTable_name");
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#enterColumn_name(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Column_nameContext)
	 */
	@Override
	public void enterColumn_name(@NotNull SQLiteParser.Column_nameContext ctx) {
		this.printPadded("enterColumn_name");
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#exitColumn_name(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Column_nameContext)
	 */
	@Override
	public void exitColumn_name(@NotNull SQLiteParser.Column_nameContext ctx) {
		this.printPadded("exitColumn_name");
		this.columnNames.add(ctx);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#enterSelect_stmt(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Select_stmtContext)
	 */
	@Override public void enterSelect_stmt(@NotNull SQLiteParser.Select_stmtContext ctx) {
		this.printPadded("enterSelect_stmt");
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)
	 */
	@Override public void visitTerminal(@NotNull TerminalNode node) {
		this.printPadded("visitTerminal: " + node);
		
		if( node.getText().equals("WHERE") ) {
			this.foundWhere = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#enterJoin_constraint(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Join_constraintContext)
	 */
	@Override
	public void enterJoin_constraint(@NotNull SQLiteParser.Join_constraintContext ctx) {
		this.printPadded("enterJoin_constraint");
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#exitJoin_constraint(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Join_constraintContext)
	 */
	@Override
	public void exitJoin_constraint(@NotNull SQLiteParser.Join_constraintContext ctx) {
		this.printPadded("exitJoin_constraint: " + ctx.getText() );
		this.joinConstraints.add(ctx.getChild(1));
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#enterSelect_core(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Select_coreContext)
	 */
	@Override
	public void enterSelect_core(@NotNull SQLiteParser.Select_coreContext ctx) {
		this.printPadded("enterSelect_core");
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#exitSelect_core(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Select_coreContext)
	 */
	@Override
	public void exitSelect_core(@NotNull SQLiteParser.Select_coreContext ctx) {
		this.printPadded("exitSelect_core");
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#enterResult_column(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Result_columnContext)
	 */
	@Override
	public void enterResult_column(@NotNull SQLiteParser.Result_columnContext ctx) {
		this.printPadded("enterResult_column");
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#exitResult_column(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Result_columnContext)
	 */
	@Override
	public void exitResult_column(@NotNull SQLiteParser.Result_columnContext ctx) {
		this.printPadded("exitResult_column: " + ctx.getText());
		this.resultColumns.add(ctx);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#enterExpr(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.ExprContext)
	 */
	@Override
	public void enterExpr(@NotNull SQLiteParser.ExprContext ctx) {
		this.printPadded("enterExpr");
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#exitExpr(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.ExprContext)
	 */
	@Override
	public void exitExpr(@NotNull SQLiteParser.ExprContext ctx) {
		this.printPadded("exitExpr: " + ctx.getText());

		if( this.foundWhere ) {
			this.lastExpr = ctx;
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#enterTable_or_subquery(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Table_or_subqueryContext)
	 */
	@Override
	public void enterTable_or_subquery(@NotNull SQLiteParser.Table_or_subqueryContext ctx) {
		this.printPadded("enterTable_or_subquery");
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#exitTable_or_subquery(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.Table_or_subqueryContext)
	 */
	@Override
	public void exitTable_or_subquery(@NotNull SQLiteParser.Table_or_subqueryContext ctx) {
		this.printPadded("exitTable_or_subquery:" + ctx.getText());
		this.tableAliases.add(ctx);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteBaseListener#enterError(uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteParser.ErrorContext)
	 */
	@Override
	public void enterError(@NotNull SQLiteParser.ErrorContext ctx) {
		throw new RuntimeException("ANTLR Parser Error.");
	}
	
}
