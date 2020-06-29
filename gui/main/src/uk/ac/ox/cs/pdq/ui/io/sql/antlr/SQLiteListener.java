// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.io.sql.antlr;
// Generated from SQLite.g4 by ANTLR 4.3
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

// TODO: Auto-generated Javadoc
/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SQLiteParser}.
 *
 * @see SQLiteEvent
 */
public interface SQLiteListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#create_virtual_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_virtual_table_stmt(@NotNull SQLiteParser.Create_virtual_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#create_virtual_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_virtual_table_stmt(@NotNull SQLiteParser.Create_virtual_table_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#type_name}.
	 * @param ctx the parse tree
	 */
	void enterType_name(@NotNull SQLiteParser.Type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#type_name}.
	 * @param ctx the parse tree
	 */
	void exitType_name(@NotNull SQLiteParser.Type_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSavepoint_stmt(@NotNull SQLiteParser.Savepoint_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSavepoint_stmt(@NotNull SQLiteParser.Savepoint_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#drop_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_index_stmt(@NotNull SQLiteParser.Drop_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#drop_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_index_stmt(@NotNull SQLiteParser.Drop_index_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(@NotNull SQLiteParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(@NotNull SQLiteParser.Table_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#join_constraint}.
	 * @param ctx the parse tree
	 */
	void enterJoin_constraint(@NotNull SQLiteParser.Join_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#join_constraint}.
	 * @param ctx the parse tree
	 */
	void exitJoin_constraint(@NotNull SQLiteParser.Join_constraintContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#cte_table_name}.
	 * @param ctx the parse tree
	 */
	void enterCte_table_name(@NotNull SQLiteParser.Cte_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#cte_table_name}.
	 * @param ctx the parse tree
	 */
	void exitCte_table_name(@NotNull SQLiteParser.Cte_table_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#indexed_column}.
	 * @param ctx the parse tree
	 */
	void enterIndexed_column(@NotNull SQLiteParser.Indexed_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#indexed_column}.
	 * @param ctx the parse tree
	 */
	void exitIndexed_column(@NotNull SQLiteParser.Indexed_columnContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#literal_value}.
	 * @param ctx the parse tree
	 */
	void enterLiteral_value(@NotNull SQLiteParser.Literal_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#literal_value}.
	 * @param ctx the parse tree
	 */
	void exitLiteral_value(@NotNull SQLiteParser.Literal_valueContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#delete_stmt_limited}.
	 * @param ctx the parse tree
	 */
	void enterDelete_stmt_limited(@NotNull SQLiteParser.Delete_stmt_limitedContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#delete_stmt_limited}.
	 * @param ctx the parse tree
	 */
	void exitDelete_stmt_limited(@NotNull SQLiteParser.Delete_stmt_limitedContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#transaction_name}.
	 * @param ctx the parse tree
	 */
	void enterTransaction_name(@NotNull SQLiteParser.Transaction_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#transaction_name}.
	 * @param ctx the parse tree
	 */
	void exitTransaction_name(@NotNull SQLiteParser.Transaction_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#column_def}.
	 * @param ctx the parse tree
	 */
	void enterColumn_def(@NotNull SQLiteParser.Column_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#column_def}.
	 * @param ctx the parse tree
	 */
	void exitColumn_def(@NotNull SQLiteParser.Column_defContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#sql_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSql_stmt(@NotNull SQLiteParser.Sql_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#sql_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSql_stmt(@NotNull SQLiteParser.Sql_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#new_table_name}.
	 * @param ctx the parse tree
	 */
	void enterNew_table_name(@NotNull SQLiteParser.New_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#new_table_name}.
	 * @param ctx the parse tree
	 */
	void exitNew_table_name(@NotNull SQLiteParser.New_table_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#keyword}.
	 * @param ctx the parse tree
	 */
	void enterKeyword(@NotNull SQLiteParser.KeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#keyword}.
	 * @param ctx the parse tree
	 */
	void exitKeyword(@NotNull SQLiteParser.KeywordContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#error_message}.
	 * @param ctx the parse tree
	 */
	void enterError_message(@NotNull SQLiteParser.Error_messageContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#error_message}.
	 * @param ctx the parse tree
	 */
	void exitError_message(@NotNull SQLiteParser.Error_messageContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#create_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_index_stmt(@NotNull SQLiteParser.Create_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#create_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_index_stmt(@NotNull SQLiteParser.Create_index_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#unary_operator}.
	 * @param ctx the parse tree
	 */
	void enterUnary_operator(@NotNull SQLiteParser.Unary_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#unary_operator}.
	 * @param ctx the parse tree
	 */
	void exitUnary_operator(@NotNull SQLiteParser.Unary_operatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#rollback_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRollback_stmt(@NotNull SQLiteParser.Rollback_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#rollback_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRollback_stmt(@NotNull SQLiteParser.Rollback_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#sql_stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterSql_stmt_list(@NotNull SQLiteParser.Sql_stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#sql_stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitSql_stmt_list(@NotNull SQLiteParser.Sql_stmt_listContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#join_operator}.
	 * @param ctx the parse tree
	 */
	void enterJoin_operator(@NotNull SQLiteParser.Join_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#join_operator}.
	 * @param ctx the parse tree
	 */
	void exitJoin_operator(@NotNull SQLiteParser.Join_operatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#collation_name}.
	 * @param ctx the parse tree
	 */
	void enterCollation_name(@NotNull SQLiteParser.Collation_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#collation_name}.
	 * @param ctx the parse tree
	 */
	void exitCollation_name(@NotNull SQLiteParser.Collation_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#pragma_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPragma_stmt(@NotNull SQLiteParser.Pragma_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#pragma_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPragma_stmt(@NotNull SQLiteParser.Pragma_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_stmt(@NotNull SQLiteParser.Update_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_stmt(@NotNull SQLiteParser.Update_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alias(@NotNull SQLiteParser.Column_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alias(@NotNull SQLiteParser.Column_aliasContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_stmt(@NotNull SQLiteParser.Alter_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_stmt(@NotNull SQLiteParser.Alter_table_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#attach_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAttach_stmt(@NotNull SQLiteParser.Attach_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#attach_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAttach_stmt(@NotNull SQLiteParser.Attach_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(@NotNull SQLiteParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(@NotNull SQLiteParser.NameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_or_index_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_or_index_name(@NotNull SQLiteParser.Table_or_index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_or_index_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_or_index_name(@NotNull SQLiteParser.Table_or_index_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#module_name}.
	 * @param ctx the parse tree
	 */
	void enterModule_name(@NotNull SQLiteParser.Module_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#module_name}.
	 * @param ctx the parse tree
	 */
	void exitModule_name(@NotNull SQLiteParser.Module_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#compound_select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCompound_select_stmt(@NotNull SQLiteParser.Compound_select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#compound_select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCompound_select_stmt(@NotNull SQLiteParser.Compound_select_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_alias}.
	 * @param ctx the parse tree
	 */
	void enterTable_alias(@NotNull SQLiteParser.Table_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_alias}.
	 * @param ctx the parse tree
	 */
	void exitTable_alias(@NotNull SQLiteParser.Table_aliasContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#drop_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_trigger_stmt(@NotNull SQLiteParser.Drop_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#drop_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_trigger_stmt(@NotNull SQLiteParser.Drop_trigger_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#module_argument}.
	 * @param ctx the parse tree
	 */
	void enterModule_argument(@NotNull SQLiteParser.Module_argumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#module_argument}.
	 * @param ctx the parse tree
	 */
	void exitModule_argument(@NotNull SQLiteParser.Module_argumentContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_trigger_stmt(@NotNull SQLiteParser.Create_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_trigger_stmt(@NotNull SQLiteParser.Create_trigger_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#signed_number}.
	 * @param ctx the parse tree
	 */
	void enterSigned_number(@NotNull SQLiteParser.Signed_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#signed_number}.
	 * @param ctx the parse tree
	 */
	void exitSigned_number(@NotNull SQLiteParser.Signed_numberContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#ordering_term}.
	 * @param ctx the parse tree
	 */
	void enterOrdering_term(@NotNull SQLiteParser.Ordering_termContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#ordering_term}.
	 * @param ctx the parse tree
	 */
	void exitOrdering_term(@NotNull SQLiteParser.Ordering_termContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#simple_select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSimple_select_stmt(@NotNull SQLiteParser.Simple_select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#simple_select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSimple_select_stmt(@NotNull SQLiteParser.Simple_select_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_or_subquery}.
	 * @param ctx the parse tree
	 */
	void enterTable_or_subquery(@NotNull SQLiteParser.Table_or_subqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_or_subquery}.
	 * @param ctx the parse tree
	 */
	void exitTable_or_subquery(@NotNull SQLiteParser.Table_or_subqueryContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#error}.
	 * @param ctx the parse tree
	 */
	void enterError(@NotNull SQLiteParser.ErrorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#error}.
	 * @param ctx the parse tree
	 */
	void exitError(@NotNull SQLiteParser.ErrorContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#commit_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCommit_stmt(@NotNull SQLiteParser.Commit_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#commit_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCommit_stmt(@NotNull SQLiteParser.Commit_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#select_or_values}.
	 * @param ctx the parse tree
	 */
	void enterSelect_or_values(@NotNull SQLiteParser.Select_or_valuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#select_or_values}.
	 * @param ctx the parse tree
	 */
	void exitSelect_or_values(@NotNull SQLiteParser.Select_or_valuesContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#select_core}.
	 * @param ctx the parse tree
	 */
	void enterSelect_core(@NotNull SQLiteParser.Select_coreContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#select_core}.
	 * @param ctx the parse tree
	 */
	void exitSelect_core(@NotNull SQLiteParser.Select_coreContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#savepoint_name}.
	 * @param ctx the parse tree
	 */
	void enterSavepoint_name(@NotNull SQLiteParser.Savepoint_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#savepoint_name}.
	 * @param ctx the parse tree
	 */
	void exitSavepoint_name(@NotNull SQLiteParser.Savepoint_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#factored_select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFactored_select_stmt(@NotNull SQLiteParser.Factored_select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#factored_select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFactored_select_stmt(@NotNull SQLiteParser.Factored_select_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#conflict_clause}.
	 * @param ctx the parse tree
	 */
	void enterConflict_clause(@NotNull SQLiteParser.Conflict_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#conflict_clause}.
	 * @param ctx the parse tree
	 */
	void exitConflict_clause(@NotNull SQLiteParser.Conflict_clauseContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#compound_operator}.
	 * @param ctx the parse tree
	 */
	void enterCompound_operator(@NotNull SQLiteParser.Compound_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#compound_operator}.
	 * @param ctx the parse tree
	 */
	void exitCompound_operator(@NotNull SQLiteParser.Compound_operatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#trigger_name}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_name(@NotNull SQLiteParser.Trigger_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#trigger_name}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_name(@NotNull SQLiteParser.Trigger_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#begin_stmt}.
	 * @param ctx the parse tree
	 */
	void enterBegin_stmt(@NotNull SQLiteParser.Begin_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#begin_stmt}.
	 * @param ctx the parse tree
	 */
	void exitBegin_stmt(@NotNull SQLiteParser.Begin_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#foreign_key_clause}.
	 * @param ctx the parse tree
	 */
	void enterForeign_key_clause(@NotNull SQLiteParser.Foreign_key_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#foreign_key_clause}.
	 * @param ctx the parse tree
	 */
	void exitForeign_key_clause(@NotNull SQLiteParser.Foreign_key_clauseContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#reindex_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReindex_stmt(@NotNull SQLiteParser.Reindex_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#reindex_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReindex_stmt(@NotNull SQLiteParser.Reindex_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#any_name}.
	 * @param ctx the parse tree
	 */
	void enterAny_name(@NotNull SQLiteParser.Any_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#any_name}.
	 * @param ctx the parse tree
	 */
	void exitAny_name(@NotNull SQLiteParser.Any_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void enterInsert_stmt(@NotNull SQLiteParser.Insert_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void exitInsert_stmt(@NotNull SQLiteParser.Insert_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#common_table_expression}.
	 * @param ctx the parse tree
	 */
	void enterCommon_table_expression(@NotNull SQLiteParser.Common_table_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#common_table_expression}.
	 * @param ctx the parse tree
	 */
	void exitCommon_table_expression(@NotNull SQLiteParser.Common_table_expressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#drop_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_table_stmt(@NotNull SQLiteParser.Drop_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#drop_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_table_stmt(@NotNull SQLiteParser.Drop_table_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#release_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRelease_stmt(@NotNull SQLiteParser.Release_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#release_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRelease_stmt(@NotNull SQLiteParser.Release_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#vacuum_stmt}.
	 * @param ctx the parse tree
	 */
	void enterVacuum_stmt(@NotNull SQLiteParser.Vacuum_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#vacuum_stmt}.
	 * @param ctx the parse tree
	 */
	void exitVacuum_stmt(@NotNull SQLiteParser.Vacuum_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(@NotNull SQLiteParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(@NotNull SQLiteParser.ExprContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_stmt(@NotNull SQLiteParser.Create_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_stmt(@NotNull SQLiteParser.Create_table_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#drop_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_view_stmt(@NotNull SQLiteParser.Drop_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#drop_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_view_stmt(@NotNull SQLiteParser.Drop_view_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#index_name}.
	 * @param ctx the parse tree
	 */
	void enterIndex_name(@NotNull SQLiteParser.Index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#index_name}.
	 * @param ctx the parse tree
	 */
	void exitIndex_name(@NotNull SQLiteParser.Index_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#view_name}.
	 * @param ctx the parse tree
	 */
	void enterView_name(@NotNull SQLiteParser.View_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#view_name}.
	 * @param ctx the parse tree
	 */
	void exitView_name(@NotNull SQLiteParser.View_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#detach_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDetach_stmt(@NotNull SQLiteParser.Detach_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#detach_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDetach_stmt(@NotNull SQLiteParser.Detach_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void enterWith_clause(@NotNull SQLiteParser.With_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void exitWith_clause(@NotNull SQLiteParser.With_clauseContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#qualified_table_name}.
	 * @param ctx the parse tree
	 */
	void enterQualified_table_name(@NotNull SQLiteParser.Qualified_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#qualified_table_name}.
	 * @param ctx the parse tree
	 */
	void exitQualified_table_name(@NotNull SQLiteParser.Qualified_table_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_view_stmt(@NotNull SQLiteParser.Create_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_view_stmt(@NotNull SQLiteParser.Create_view_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#column_constraint}.
	 * @param ctx the parse tree
	 */
	void enterColumn_constraint(@NotNull SQLiteParser.Column_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#column_constraint}.
	 * @param ctx the parse tree
	 */
	void exitColumn_constraint(@NotNull SQLiteParser.Column_constraintContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#database_name}.
	 * @param ctx the parse tree
	 */
	void enterDatabase_name(@NotNull SQLiteParser.Database_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#database_name}.
	 * @param ctx the parse tree
	 */
	void exitDatabase_name(@NotNull SQLiteParser.Database_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(@NotNull SQLiteParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(@NotNull SQLiteParser.Column_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#result_column}.
	 * @param ctx the parse tree
	 */
	void enterResult_column(@NotNull SQLiteParser.Result_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#result_column}.
	 * @param ctx the parse tree
	 */
	void exitResult_column(@NotNull SQLiteParser.Result_columnContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(@NotNull SQLiteParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(@NotNull SQLiteParser.ParseContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void enterTable_constraint(@NotNull SQLiteParser.Table_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void exitTable_constraint(@NotNull SQLiteParser.Table_constraintContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#pragma_name}.
	 * @param ctx the parse tree
	 */
	void enterPragma_name(@NotNull SQLiteParser.Pragma_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#pragma_name}.
	 * @param ctx the parse tree
	 */
	void exitPragma_name(@NotNull SQLiteParser.Pragma_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#analyze_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAnalyze_stmt(@NotNull SQLiteParser.Analyze_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#analyze_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAnalyze_stmt(@NotNull SQLiteParser.Analyze_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#foreign_table}.
	 * @param ctx the parse tree
	 */
	void enterForeign_table(@NotNull SQLiteParser.Foreign_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#foreign_table}.
	 * @param ctx the parse tree
	 */
	void exitForeign_table(@NotNull SQLiteParser.Foreign_tableContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#function_name}.
	 * @param ctx the parse tree
	 */
	void enterFunction_name(@NotNull SQLiteParser.Function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#function_name}.
	 * @param ctx the parse tree
	 */
	void exitFunction_name(@NotNull SQLiteParser.Function_nameContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void enterJoin_clause(@NotNull SQLiteParser.Join_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void exitJoin_clause(@NotNull SQLiteParser.Join_clauseContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#update_stmt_limited}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_stmt_limited(@NotNull SQLiteParser.Update_stmt_limitedContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#update_stmt_limited}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_stmt_limited(@NotNull SQLiteParser.Update_stmt_limitedContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#raise_function}.
	 * @param ctx the parse tree
	 */
	void enterRaise_function(@NotNull SQLiteParser.Raise_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#raise_function}.
	 * @param ctx the parse tree
	 */
	void exitRaise_function(@NotNull SQLiteParser.Raise_functionContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSelect_stmt(@NotNull SQLiteParser.Select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSelect_stmt(@NotNull SQLiteParser.Select_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDelete_stmt(@NotNull SQLiteParser.Delete_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDelete_stmt(@NotNull SQLiteParser.Delete_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SQLiteParser#pragma_value}.
	 * @param ctx the parse tree
	 */
	void enterPragma_value(@NotNull SQLiteParser.Pragma_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#pragma_value}.
	 * @param ctx the parse tree
	 */
	void exitPragma_value(@NotNull SQLiteParser.Pragma_valueContext ctx);
}