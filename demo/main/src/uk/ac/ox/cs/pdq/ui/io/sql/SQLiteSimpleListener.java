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


public class SQLiteSimpleListener extends SQLiteBaseListener {
	private static Logger log = Logger.getLogger(SQLiteSimpleListener.class);

	private int paddingDepth = 0;
	
//	Map<String, String> resultAliasAttrs = new HashMap<String, String>();
	
	private Set<ParseTree> tableAliases = new HashSet<ParseTree>();
	private Set<ParseTree> joinConstraints = new HashSet<ParseTree>();
	private Set<ParseTree> resultColumns = new HashSet<ParseTree>();
	private ParseTree lastExpr = null;
	
	private boolean foundWhere = false;
	
	public Set<ParseTree> getJoinConstraints() {
		return this.joinConstraints;
	}
	
	public Set<ParseTree> getResultColumns() {
		return this.resultColumns;
	}
	
	public ParseTree getLastExpr() {
		return this.lastExpr;
	}
	
	public Set<ParseTree> getTableAliases() {
		return this.tableAliases;
	}
	
	private String padding() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < this.paddingDepth; i++) {
			s.append("  ");
		}
		return s.toString();
	}
	
	private void printPadded(String str) {
		log.debug(this.padding() + str);
	}
	
	@Override
	public void enterEveryRule(@NotNull ParserRuleContext ctx) {
		this.paddingDepth++;
	}
	
	@Override
	public void exitEveryRule(@NotNull ParserRuleContext ctx) {
		this.paddingDepth--;
	}
	
	@Override
	public void enterTable_name(@NotNull SQLiteParser.Table_nameContext ctx) {
		this.printPadded("enterTable_name");
	}
	
	@Override
	public void exitTable_name(@NotNull SQLiteParser.Table_nameContext ctx) {
		this.printPadded("exitTable_name");
	}
	
	@Override
	public void enterColumn_name(@NotNull SQLiteParser.Column_nameContext ctx) {
		this.printPadded("enterColumn_name");
	}
	
	@Override
	public void exitColumn_name(@NotNull SQLiteParser.Column_nameContext ctx) {
		this.printPadded("exitColumn_name");
	}

	@Override public void enterSelect_stmt(@NotNull SQLiteParser.Select_stmtContext ctx) {
		this.printPadded("enterSelect_stmt");
	}
	
	@Override public void visitTerminal(@NotNull TerminalNode node) {
		this.printPadded("visitTerminal: " + node);
		
		if( node.getText().equals("WHERE") ) {
			this.foundWhere = true;
		}
	}
	
	@Override
	public void enterJoin_constraint(@NotNull SQLiteParser.Join_constraintContext ctx) {
		this.printPadded("enterJoin_constraint");
	}
	
	@Override
	public void exitJoin_constraint(@NotNull SQLiteParser.Join_constraintContext ctx) {
		this.printPadded("exitJoin_constraint: " + ctx.getText() );
		this.joinConstraints.add(ctx.getChild(1));
	}
	
	@Override
	public void enterSelect_core(@NotNull SQLiteParser.Select_coreContext ctx) {
		this.printPadded("enterSelect_core");
	}
	
	@Override
	public void exitSelect_core(@NotNull SQLiteParser.Select_coreContext ctx) {
		this.printPadded("exitSelect_core");
	}
	
	@Override
	public void enterResult_column(@NotNull SQLiteParser.Result_columnContext ctx) {
		this.printPadded("enterResult_column");
	}
	
	@Override
	public void exitResult_column(@NotNull SQLiteParser.Result_columnContext ctx) {
		this.printPadded("exitResult_column: " + ctx.getText());
		this.resultColumns.add(ctx);
	}
	
	@Override
	public void enterExpr(@NotNull SQLiteParser.ExprContext ctx) {
		this.printPadded("enterExpr");
	}
	
	@Override
	public void exitExpr(@NotNull SQLiteParser.ExprContext ctx) {
		this.printPadded("exitExpr: " + ctx.getText());

		if( this.foundWhere ) {
			this.lastExpr = ctx;
		}
	}
	
	@Override
	public void enterTable_or_subquery(@NotNull SQLiteParser.Table_or_subqueryContext ctx) {
		this.printPadded("enterTable_or_subquery");
	}
	
	@Override
	public void exitTable_or_subquery(@NotNull SQLiteParser.Table_or_subqueryContext ctx) {
		this.printPadded("exitTable_or_subquery:" + ctx.getText());
		this.tableAliases.add(ctx);
	}
	
	@Override
	public void enterError(@NotNull SQLiteParser.ErrorContext ctx) {
		throw new RuntimeException("ANTLR Parser Error.");
	}
	
}
