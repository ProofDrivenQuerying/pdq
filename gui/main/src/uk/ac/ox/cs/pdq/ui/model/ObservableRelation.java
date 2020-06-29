// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.model;

import uk.ac.ox.cs.pdq.db.Relation;


// TODO: Auto-generated Javadoc
/**
 * The Class ObservableRelation.
 */
public class ObservableRelation extends Relation {

	/** */
	private Relation relation;
	
	/**
	 * Instantiates a new observable relation.
	 *
	 * @param relation the relation
	 */
	 public ObservableRelation(Relation relation) {
		super(relation.getName(), relation.getAttributes());
		this.relation = relation;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.db.Relation#toString()
	 */
	@Override
	public String toString() {
		String result = String.valueOf(this.relation);
		if (result.length() > 128) {
			result = result.substring(125) + "...";
		}
		return result;
	}
}
