package uk.ac.ox.cs.pdq.data;

/**
 * This package represents all permanent or in-memory data storages. The main
 * interface of this package is the DatabaseManager class, that can create
 * databases, connections etc. The DatabaseManager have to be initialised with a
 * DatabaseParameters object that describes the type and all type related
 * configuration parameters. After that the DatabaseManager provides a unified
 * management surface to use this data storage.
 * 
 * Multi-threaded accesses, type of database, translation from PDQ objects to SQL or other languages, all transparently mapped behind the
 * DatabaseManager class.
 */
