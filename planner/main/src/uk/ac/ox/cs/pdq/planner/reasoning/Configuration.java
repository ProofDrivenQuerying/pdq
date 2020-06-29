// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.reasoning;

import java.util.Collection;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;

/**
 * Configurations represent derivation of implicit information using constraints
 * and have a direct correspondence with a query plan. 
 * This interface is generic as it does not bind a reasoning mechanism to the configuration.  
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @param <P> the generic type
 */
public interface Configuration extends Cloneable, Comparable<Configuration>{
	
	/**
	 *
	 * @return 		the plan of this configuration
	 */
	RelationalTerm getPlan();
	
	Cost getCost();
	
	void setCost(Cost cost);
	
	/**
	 *
	 * @param query the query
	 * @return true if the configuration matches the input query.
	 * (Conjunctive query match definition) If Q′ is a conjunctive query and v is a chase configuration
	 * having elements for each free variable of Q′, then a homomorphism of Q′ into v
	 * mapping each free variable into the corresponding element is called a match for Q′ in v.
	 */
	boolean isSuccessful(ConjunctiveQuery query);
	
	
	/**
	 *
	 * @return the configuration's input constants
	 */
	public Collection<Constant> getInput();

	/**
	 *
	 * @return the configuration's output facts
	 */
	public Collection<Constant> getOutput();

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	int compareTo(Configuration o);
}
