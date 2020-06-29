// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;

public class PrimaryKey implements Serializable{
	private static final long serialVersionUID = 5003431166885480820L;
	protected final Attribute[] attributes;
	
	protected PrimaryKey(Attribute[] key) {
		this.attributes = key.clone();
	}
	
	public Attribute[] getAttributes() {
		return this.attributes.clone();
	}
	
	public int getNumberOfAttributes() {
		return this.attributes.length;
	}
	
	public static PrimaryKey create(Attribute[] attributes) {
		return Cache.primaryKey.retrieve(new PrimaryKey(attributes));
	}

}
