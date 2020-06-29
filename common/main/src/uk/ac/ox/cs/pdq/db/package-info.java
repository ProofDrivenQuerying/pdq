// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.db;

/**
	@author Mark Ridler

	This db sub-package defines several relational database-related objects including relations, attributes, keys and foreign keys,
	access method descriptors and views.
	
	The sub-package contains:
	
	-- AccessMethodDescriptor, which represents a descriptor for an access method rather than the access method itself
	-- Attribute, which represents a relation's attribute
	-- Cache, which creates and maintains a cache of each object type in this package
	-- ForeignKey, which represents a database foreign key.
	-- Instance, which models a relational database instance. An Instance is a set of Atoms.
	-- Match, which represents a formula or query that will be grounded using an homomorphism
	-- PrimaryKey, which represents a database primary key.
	-- Reference, which represents a reference between a column in the local table and a column in another table.
	-- Relation, which represents the schema of a relation.
	-- Schema, which represents a database schema.
	-- View, which represents a database view.
**/
