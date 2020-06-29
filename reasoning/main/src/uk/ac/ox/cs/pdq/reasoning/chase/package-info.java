// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoning.chase;

/**
	@author Efthymia Tsamoura

	This package contains several implementations of the chase algorithm: 
	
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
	
	
**/