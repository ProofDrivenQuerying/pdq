package uk.ac.ox.cs.pdq;

/**
	@author Mark Ridler


	This package defines classes used across the whole PDQ application.
	
	The subjects range from relational algebra and first-order logic to database management and database schemas. By its nature, common
	is a collection of common subjects with loose connection between them. This is the library part of the system and the code
	is accordingly well-defined and well-tested.


	Sub-packages include:
	
	-- Algebra, which defines several logical relational operators including selections, projections, joins and unions. 
	
	-- FOL, which defines several constructs of first order logic including Atom, Clause, Conjunction, Dependency, Formula and Predicate
	
	-- DatabaseManagement, which defines Internal and External database managers
	 
    -- DB, which defines database-related classes including Relation, Attribute and AccessMethodDescriptor
    
    -- IO, which defines JAXB-based XML Reader and Writer functionality
    
	-- Util, which defines several utility classes including tuples and tables.
	
**/