// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.utils;

import java.util.Comparator;

// TODO: Auto-generated Javadoc
/**
 * The Class PathComparator.
 */
public class PathComparator implements Comparator<Path>{

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Path o1, Path o2) {
		if(o1.getCost().greaterThan(o2.getCost())) {
			return 1;
		}
		else if(o1.getCost().lessThan(o2.getCost())) {
			return -1;
		}
		return 0;
	}
}
