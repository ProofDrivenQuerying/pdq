// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.collections4.set.ListOrderedSet;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;

/**
 * @author Gabor
 *
 */
public class AdaptedForeignKey extends ForeignKey implements Serializable {
	private static final long serialVersionUID = -2103116468417078713L;

	/** The name of the foreign key, may be <code>null</code>. */
	private String name;

	/** The name of the foreign table. */
	private String foreignRelationName;

	/** The references between local and remote columns. */
	private ListOrderedSet<ForeignKeyAdapter> references = new ListOrderedSet<>();

	/** Whether this foreign key has an associated auto-generated index. */
	private boolean autoIndexPresent;

	public AdaptedForeignKey() {
	}

	public AdaptedForeignKey(ForeignKey v) {
		this.name = v.getName();
		this.foreignRelationName = v.getForeignRelationName();
		this.autoIndexPresent = v.isAutoIndexPresent();
		for (Reference r:v.getReferences()) {
			references.add(new ForeignKeyAdapter(r.getLocalAttributeName(), r.getForeignAttributeName()));
		}
	}

	@XmlAttribute(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute(name = "foreignRelationName")
	public String getForeignRelationName() {
		return this.foreignRelationName;
	}

	public void setForeignRelationName(String foreignRelationName) {
		this.foreignRelationName = foreignRelationName;
	}
	
	@XmlAttribute(name = "autoIndexPresent")
	public boolean isAutoIndexPresent() {
		return this.autoIndexPresent;
	}
	@XmlElement(name = "References")
	public ListOrderedSet<ForeignKeyAdapter> getReferencesList() {
		return this.references;
	}
	public void setReferencesList(ListOrderedSet<ForeignKeyAdapter> list) {
		this.references = list;
	}

	public void setAutoIndexPresent(boolean autoIndexPresent) {
		this.autoIndexPresent = autoIndexPresent;
	}
	
	public ForeignKey toForeignKey() {
		return this;
	}

	public ForeignKey convertToForeignKey(Relation parentRelation, Relation[] allRelations) {
		ForeignKey fk = new ForeignKey(name);
		fk.setForeignRelationName(this.foreignRelationName);
		fk.setAutoIndexPresent(isAutoIndexPresent());
		int sequence = 0;
		for (ForeignKeyAdapter fka:references) {
			fk.addReference(fka.toReference(this,parentRelation,allRelations,sequence++));
		}
		return fk;
	}

	public static class ForeignKeyAdapter {
		private String localAttributeName;
		private String foreignAttributeName;
		
		public ForeignKeyAdapter() {
		}
		
		public ForeignKeyAdapter(String local, String remote) {
			localAttributeName = local;
			foreignAttributeName = remote;
		}
		@XmlAttribute(name = "ForeignAttributeName")		
		public String getForeignAttributeName() {
			return foreignAttributeName;
		}
		public void setForeignAttributeName(String foreignAttributeName) {
			this.foreignAttributeName = foreignAttributeName;
		}
		@XmlAttribute(name = "LocalAttributeName")		
		public String getLocalAttributeName() {
			return localAttributeName;
		}
		public void setLocalAttributeName(String localAttributeName) {
			this.localAttributeName = localAttributeName;
		}
		
		public Reference toReference(AdaptedForeignKey adaptedForeignKey, Relation parentRelation, Relation[] relations, int sequence) {
			Attribute localAttribute = parentRelation.getAttribute(localAttributeName);
			Attribute remoteAttribute = getRelationByName(relations,adaptedForeignKey.foreignRelationName).getAttribute(foreignAttributeName);
			Reference r = new Reference(localAttribute,remoteAttribute);
			r.setSequenceValue(sequence);
			return r;
		}

		private Relation getRelationByName(Relation[] relations, String foreignRelationName) {
			for (Relation r: relations) {
				if (foreignRelationName.equals(r.getName())) {
					return r;
				}
			}
			return null;
		}
		
	}
}
