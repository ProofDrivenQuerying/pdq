package uk.ac.ox.cs.pdq.planner.accessibleschema;

/**
 * @author Efthymia Tsamoura and Michael benedikt
 * If S_0 is a schema with relations, access methods, and constraints,
 * then determining whether a query Q can be reformulated over a schema
 * S_0 reduces to a reasoning problem with another schema derived from S_0 , called
 * the "accessible schema for S_0", denoted AcSch(S_0). This package contains classes 
 * related to the transformed schema.
 * AcSch(S_0), 
 * is the schema without any access restrictions, such that:
 * i. The constants are those of S_0.
 * ii.The relations are those of S_0, a copy of each relation R denoted 
 * AccessedR (the "accessible version of R"), a unary
 * relation accessible(x) ("x is an accessible value") plus another copy of each relation R of S0 called InferredAccR
 * " the "inferred accessible version of R". 
 * iii.The constraints are the constraints of S_0 plus inferred accessible copies of the those constraints
 * and accessibility axioms created from the schema access methods. 
 *  
 * For an access method mt on relation R of arity n with input positions j1, ..., jm 
 * an accessibility axiom is a rule of the form
 * accessible(x_{j_1}) \wegde ... \wedge accessible(x_{j_m}) \wedge R(x_1, ..., x_n) -->
 * InferredAccessible R(x_1, ..., x_n) \wedge \Wedge_{j} accessible(x_j)
 */
