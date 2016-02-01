/**
 * This package gathers classes to generated queries know to be answerable wrt
 * some schema, access restrictions and dependencies.
 * 
 * The general idea consists in running is to find queries answerable for some 
 * given schema, access restrictions and dependencies, by running a reasoning
 * procedures on an arbitrarily large query, and extracting relevant sub-queries
 * from each partial proof.
 * 
 * Queries are then filter using a conjunctive set of QuerySelector which
 * rule out queries not satisfying certain properties.

 */
package uk.ac.ox.cs.pdq.generator.reverse; 