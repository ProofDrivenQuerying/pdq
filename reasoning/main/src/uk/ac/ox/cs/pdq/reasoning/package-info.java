// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoning;

/**
	@author Efthymia Tsamoura and Mark Ridler and Michael Benedikt

	This package contains classes for doing inference on sets of facts.
        The reasoning process can be used stand-alone -- e.g. for seeing whether
        a query follows from a set of facts and constraints. It can also be used
        as a component of planning.
	
	The initiation process consists of the following steps:
		-selection of the appropriate reasoning mechanism. The only reasoning mechanism that is supported currently is the chase.
		-selection of the appropriate mechanism to store facts inferred
                during reasoning and to detect homomorphisms during chasing or to detect query matches. 
		Homomorphism detection works as follows: the chase facts are stored in a database. 
		Every time we check if there is an homomorphism of a formula F to the facts of a chase instance, 
		we create an SQL query from F's atoms and submit it to a database engine. The database engine returns all the facts that 
		are homomorphic to F. The database engine that are supported are Internal and Postgres.
		
	The following chase algorithms are supported:
	
		-Restricted chase: Runs the chase algorithm applying only active triggers. 
 		Consider an instance I, a set Base of values, and a TGD
		\delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
		a trigger for \delta in I is a homomorphism h of \sigma into I. A trigger is active if it
		does not extend to a homomorphism h0 into I. Informally, a trigger is a tuple \vec{c}
		satisfying \sigma, and it is active if there is no witness \vec{y} that makes \tau holds.
		A chase step appends to I additional facts that were produced during grounding \delta. 
		The output of the chase step is a new instance in which h is no longer an active trigger.
		The facts that are generated during chasing are stored in a list.
		
		-Parallel EGD chase: Runs EGD chase using parallel chase steps.
	 	(From modern dependency theory notes)
 	 	A trigger for and EGD \delta = \sigma --> x_i = x_j in I is again a homomorphism h in
	 	\sigma into I. A trigger is active if it does not extend to a homomorphism h0 into I.
	 	Given trigger h
	 	for \delta in I, a chase pre-step marks the pair h(x_i) and h(x_j) as equal. Formally,
	 	it appends the pair h(x_i), h(x_j) to a set of pairs MarkedEqual.
	 	An EGD parallel chase step on instance I for a set of constraints C is performed
	 	as follows.
	 	i. A chase pre-step is performed for every constraint \delta in C and every active
		trigger h in I.
	 	ii. The resulting set of marked pairs is closed under reflexivity and transitivity
	 	to get an equivalence relation.
	 	iii. If we try to equate two different schema constants, then the chase fails. 
	 	The facts that are generated during chasing are stored in a list.
	 
	 	-Bounded chase and KTermination chase: Run the chase for k rounds.
	 	
	 The top-level classes are as follows:
	 - Reason, which is the main entry point for the reasoning package
	 - ReasonerFactory, which creates reasoners based on the input arguments
	 - ReasoningParameters, which hold the parameters of a reasoning session
	
**/
