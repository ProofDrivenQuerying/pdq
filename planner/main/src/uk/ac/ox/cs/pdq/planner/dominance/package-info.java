// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dominance;

/**
	@author Efthymia Tsamoura

	The classes of this package implement different notions of configuration and plan dominance:
	<pre>
	 - CostDominance: Closed cost dominance. A closed plan p cost dominates another closed plan p', if p is closed and has cost < the cost of p'.
	 - CostFactDominance: Closed dominance. A closed configuration c dominates a closed configuration c', if c both cost- and fact- dominates c'.
 	 - FactDominance: A configuration c and c' is fact dominated by another configuration c' if there exists an homomorphism from the facts of c to the facts of c' and
 			the input constants are preserved.
 	 - FastFactDominance: Performs fast fact dominance checks. A source configuration is fact dominated
 			by a target configuration if any inferred accessible fact plus in the source
 			configuration also appears in the target configuration. In order to perform
 			this kind of check Skolem constants must be assigned to formula variables
 			during chasing.
	
**/
