// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.logging;

import com.google.common.base.CaseFormat;

import uk.ac.ox.cs.pdq.logging.StatKey;


/**
 * Static collection of key to be used in planning statistics collections.
 *
 * @author Julien Leblay
 */
public enum CostStatKeys implements StatKey {

	/** The cost estimation time. */
	COST_ESTIMATION_TIME, /** The cost estimation count. */
 COST_ESTIMATION_COUNT;

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name());
	}
	
}
