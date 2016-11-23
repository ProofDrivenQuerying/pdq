package uk.ac.ox.cs.pdq.db;

/* TOCOMMENT What is this licence for?
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Represents a database foreign key.
 *
 */
public class ForeignKey implements Cloneable {

	/** The name of the foreign key, may be <code>null</code>. */
	private String _name;

	/** The target table. */
	private Relation _foreignRelation;

	/** The name of the foreign table. */
	private String _foreignRelationName;

	/** The references between local and remote columns. */
	private ListOrderedSet<Reference> _references = new ListOrderedSet<>();

	/** Whether this foreign key has an associated auto-generated index. */
	private boolean _autoIndexPresent;

	/**
	 * Creates a new foreign key object that has no name.
	 */
	public ForeignKey() {

	}

	/**
	 * Creates a new foreign key object.
	 *
	 * @param name
	 *            The name of the foreign key
	 */
	public ForeignKey(String name) {
		this._name = name;
	}

	/**
	 * Creates a new foreign key object.
	 *
	 * @param dep LinearGuarded
	 */
	public ForeignKey(LinearGuarded dep) {
		Atom left = dep.getBody().getAtoms().get(0);
		Atom right = dep.getHead().getAtoms().get(0);
		Relation leftRel = (Relation) left.getPredicate();
		Relation rightRel = (Relation) right.getPredicate();
		this.setForeignRelation(rightRel);
		this.setForeignRelationName(rightRel.getName());
		for (Variable v:CollectionUtils.intersection(left.getVariables(), right.getVariables())) {
			this.addReference(new Reference(leftRel.getAttribute(left.getTerms().indexOf(v)), rightRel.getAttribute(right.getTerms().indexOf(v))));
		}
	}

	/**
	 * Returns the name of this foreign key.
	 *
	 * @return The name
	 */
	public String getName() {
		return this._name;
	}

	/**
	 * Sets the name of this foreign key.
	 *
	 * @param name
	 *            The name
	 */
	public void setName(String name) {
		this._name = name;
	}

	/**
	 * Returns the foreign table.
	 *
	 * @return The foreign table
	 */
	public Relation getForeignRelation() {
		return this._foreignRelation;
	}

	/**
	 * Sets the foreign table.
	 *
	 * @param foreignRelation
	 *            The foreign table
	 */
	public void setForeignRelation(Relation foreignRelation) {
		this._foreignRelation = foreignRelation;
		this._foreignRelationName = (foreignRelation == null ? null : foreignRelation.getName());
	}

	/**
	 * Returns the name of the foreign table.
	 *
	 * @return The table name
	 */
	public String getForeignRelationName() {
		return this._foreignRelationName;
	}

	/**
	 * Sets the name of the foreign table. Please note that you should not use
	 * this method when manually constructing or manipulating the database
	 * model. Rather utilize the {@link #setForeignRelation(Relation)} method.
	 *
	 * @param foreignRelationName
	 *            The table name
	 */
	public void setForeignRelationName(String foreignRelationName) {
		if ((this._foreignRelation != null) && !this._foreignRelation.getName().equals(foreignRelationName)) {
			this._foreignRelation = null;
		}
		this._foreignRelationName = foreignRelationName;
	}

	/**
	 * Returns the number of references.
	 *
	 * @return The number of references
	 */
	public int getReferenceCount() {
		return this._references.size();
	}

	/**
	 * Returns the indicated reference.
	 *
	 * @param idx The index
	 * @return The reference
	 */
	public Reference getReference(int idx) {
		return (Reference) this._references.get(idx);
	}

	/**
	 * Returns the references.
	 *
	 * @return The references
	 */
	public Reference[] getReferences() {
		return (Reference[]) this._references.toArray(new Reference[this._references.size()]);
	}

	/**
	 * Returns the first reference if it exists.
	 *
	 * @return The first reference
	 */
	public Reference getFirstReference() {
		return (Reference) (this._references.isEmpty() ? null : this._references.get(0));
	}

	/**
	 * Adds a reference, ie. a mapping between a local column (in the table that
	 * owns this foreign key) and a remote column.
	 *
	 * @param reference The reference to add
	 */
	public void addReference(Reference reference) {
		if (reference != null) {
			for (int idx = 0; idx < this._references.size(); idx++) {
				Reference curRef = this.getReference(idx);

				if (curRef.getSequenceValue() > reference.getSequenceValue()) {
					this._references.add(idx, reference);
					return;
				}
			}
			this._references.add(reference);
		}
	}

	/**
	 * Removes the given reference.
	 *
	 * @param reference The reference to remove
	 */
	public void removeReference(Reference reference) {
		if (reference != null) {
			this._references.remove(reference);
		}
	}

	/**
	 * Removes the indicated reference.
	 *
	 * @param idx The index of the reference to remove
	 */
	public void removeReference(int idx) {
		this._references.remove(idx);
	}

	/**
	 * Determines whether this foreign key uses the given column as a local
	 * column in a reference.
	 *
	 * @param column The column to check

	 * @return <code>true</code> if a reference uses the column as a local column
	 */
	public boolean hasLocalAttribute(Attribute column) {
		for (int idx = 0; idx < this.getReferenceCount(); idx++) {
			if (column.equals(this.getReference(idx).getLocalAttribute())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines whether this foreign key uses the given column as a foreign
	 * column in a reference.
	 *
	 * @param column The column to check
	 * @return <code>true</code> if a reference uses the column as a foreign column
	 */
	public boolean hasForeignAttribute(Attribute column) {
		for (int idx = 0; idx < this.getReferenceCount(); idx++) {
			if (column.equals(this.getReference(idx).getForeignAttribute())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines whether this foreign key has an auto-generated associated
	 * index.
	 *
	 * @return <code>true</code> if an auto-generated index exists
	 */
	public boolean isAutoIndexPresent() {
		return this._autoIndexPresent;
	}

	/**
	 * Specifies whether this foreign key has an auto-generated associated
	 * index.
	 *
	 * @param autoIndexPresent
	 *            <code>true</code> if an auto-generated index exists
	 */
	public void setAutoIndexPresent(boolean autoIndexPresent) {
		this._autoIndexPresent = autoIndexPresent;
	}

	/**
	 * {@inheritDoc}
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		ForeignKey result = (ForeignKey) super.clone();

		result._name = this._name;
		result._foreignRelationName = this._foreignRelationName;
		result._references = new ListOrderedSet<>();

		for (Iterator<Reference> it = this._references.iterator(); it.hasNext();) {
			result._references.add(((Reference) it.next()).clone());
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 * @param obj Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ForeignKey) {
			ForeignKey otherFk = (ForeignKey) obj;

			// Note that this compares case sensitive
			// Note also that we can simply compare the references regardless of
			// their order
			// (which is irrelevant for fks) because they are contained in a set
			EqualsBuilder builder = new EqualsBuilder();

			if ((this._name != null) && (this._name.length() > 0) && (otherFk._name != null) && (otherFk._name.length() > 0)) {
				builder.append(this._name, otherFk._name);
			}
			return builder.append(this._foreignRelationName, otherFk._foreignRelationName)
					.append(this._references, otherFk._references).isEquals();
		}
		return false;
	}

	/**
	 * Compares this foreign key to the given one while ignoring the case of
	 * identifiers.
	 *
	 * @param otherFk The other foreign key
	 * @return <code>true</code> if this foreign key is equal (ignoring case) to
	 *         the given one
	 */
	public boolean equalsIgnoreCase(ForeignKey otherFk) {
		boolean checkName = (this._name != null) && (this._name.length() > 0) && (otherFk._name != null)
				&& (otherFk._name.length() > 0);

		if ((!checkName || this._name.equalsIgnoreCase(otherFk._name))
				&& this._foreignRelationName.equalsIgnoreCase(otherFk._foreignRelationName)) {
			Set<Reference> otherRefs = new LinkedHashSet<>();

			otherRefs.addAll(otherFk._references);
			for (Iterator<Reference> it = this._references.iterator(); it.hasNext();) {
				Reference curLocalRef = (Reference) it.next();
				boolean found = false;

				for (Iterator<Reference> otherIt = otherRefs.iterator(); otherIt.hasNext();) {
					Reference curOtherRef = (Reference) otherIt.next();

					if (curLocalRef.equalsIgnoreCase(curOtherRef)) {
						otherIt.remove();
						found = true;
						break;
					}
				}
				if (!found) {
					return false;
				}
			}
			return otherRefs.isEmpty();
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 * @return int
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(this._name).append(this._foreignRelationName).append(this._references).toHashCode();
	}

	/**
	 * {@inheritDoc}
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("Foreign key [");
		if ((this.getName() != null) && (this.getName().length() > 0)) {
			result.append("name=");
			result.append(this.getName());
			result.append("; ");
		}
		result.append("foreign table=");
		result.append(this.getForeignRelationName());
		result.append("; ");
		result.append(this.getReferenceCount());
		result.append(" references]");

		return result.toString();
	}

	/**
	 * Returns a verbose string representation of this foreign key.
	 *
	 * @return The string representation
	 */
	public String toVerboseString() {
		StringBuffer result = new StringBuffer();

		result.append("ForeignK ky [");
		if ((this.getName() != null) && (this.getName().length() > 0)) {
			result.append("name=");
			result.append(this.getName());
			result.append("; ");
		}
		result.append("foreign table=");
		result.append(this.getForeignRelationName());
		result.append("] references:");
		for (int idx = 0; idx < this.getReferenceCount(); idx++) {
			result.append(' ');
			result.append(this.getReference(idx).toString());
		}

		return result.toString();
	}
}
