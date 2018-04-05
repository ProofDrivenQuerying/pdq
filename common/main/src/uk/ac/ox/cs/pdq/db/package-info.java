package uk.ac.ox.cs.pdq.db;

/**
	@author Efthymia Tsamoura

	This package defines several relational database-related objects including
	-relations
	-attributes
	-keys and foreign keys
	-access methods (an access method defines the positions of a relation's attributes whose values are required to access the relation)
	-views
	-and constraints. 
	
	The constraints that are currently supproted are 
	-TGDS: dependencies of the form \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y}) where \sigma and \tau are conjunctions of atoms.
TOCOMMENT: THIS NEXT SEEMS OUT OF DATE
	-Linear guarded dependencies: dependencies of the form \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y}) where \sigma is a single atim and \tau is a conjunction of atoms.
	-EGDs: dependencies of the form \delta = \forall \vec{x} \rho(\vec{x}) --> x_i = x_j where \rho is a conjunction of atoms
	
**/
