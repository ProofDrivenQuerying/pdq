package uk.ac.ox.cs.pdq.planner.dag;

/**
@author Efthymia Tsamoura and Michael Benedikt
 * This package contains classes related to exploring DAG configurations.
 * DAG configurations are built up compositionally from basic plans that
 * perform a single access ("unary plans"), 
 * analogous to the way traditional query plans are built up from accesses.
 * 
 * Unary configurations correspond to single access plans, 
 * while binary configurations correspond to join plans. 
 * They are of the form ApplyRule(R,\vec{b}), where R is an accessibility axiom corresponding to method mt on relation R, 
 * and \vec{b} is a binding of the universally quantified variables
 * of R to chase constants or schema constants. The input constants are all those chase constants in \vec{b} where the
 * corresponding variable of R occurs within the R atoms of R at an input position of method mt. The outputs facts
 * of the configuration are any inferred accessible facts produced
 * be applying R with binding \vec{b}, as well as all facts that are consequences from these under the copy of the integrity
 * constraints. Calculating these output facts requires a consequence closure procedure.
 * 
 * Binary configurations are of the form Binary(x,y), where x and y can be either binary of unary DAG configurations. 
 * Binary(x,y) has input I1 \cup (I2-O1) and output O1 \cup O2, and output facts all facts that are consequences of
 * the union of the facts in x and y under the copy of the integrity constraints on the InfAcc relations. 
 * Similar to unary DAG configurations, calculating the set of facts requires the use of consequence closure.
 * 
 * 
**/
