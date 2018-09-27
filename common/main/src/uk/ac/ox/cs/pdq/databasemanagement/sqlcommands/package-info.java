package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

/**
 * @author Mark Ridler
 * 
 * The databasemanagement.sqlcommands sub-package implements a range of SQL
 * commands such as CreateTable or DropDatabase. It contains:
 * 
 *  -- BasicSelect, which represents a SQL query
 *  -- BulkInsert, which constructs a single SQL statement that inserts a list of records (facts) into a database table.
 *  -- Command, which is the main superclass of all database commands in this package
 *  -- CreateDatabase, which creates an empty schema with the given databaseName.
 *  -- CreateIndex, which represents a CREATE INDEX sql command
 *  -- CreateTable, which represents a CREATE TABLE sql command
 *  -- Delete, which represents a DELETE FROM sql statement
 *  -- DifferenceQuery, which represents a kind of nested select that will tell the difference between two BasicSelects.
 *  -- DropDatabase, which represents a DROP DATABASE sql command
 *  -- ExplainSelect, which represents a SQL query with an EXPLAIN clause.
 *  -- Insert, which represents an INSERT statement.
 *
 */