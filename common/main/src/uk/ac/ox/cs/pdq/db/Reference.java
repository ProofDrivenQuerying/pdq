package uk.ac.ox.cs.pdq.db;

/*
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

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a reference between a column in the local table and a column in
 * another table.
 *
 */
public class Reference implements Cloneable, Serializable {
	/** Unique ID for serialization purposes. */
	private static final long serialVersionUID = 6062467640266171664L;

	/** The sequence value within the key. */
	private int _sequenceValue;

	/** The local column. */
	private Attribute _localAttribute;

	/** The foreign column. */
	private Attribute _foreignAttribute;

	/** The name of the local column. */
	private String _localAttributeName;

	/** The name of the foreign column. */
	private String _foreignAttributeName;

	/**
	 * Creates a new, empty reference.
	 */
	public Reference() {
	}

	/**
	 * Constructor for Reference.
	 * @param rf Reference
	 */
	public Reference(Reference rf) {
	}

	/**
	 * Creates a new reference between the two given columns.
	 *
	 * @param localAttribute
	 *            The local column
	 * @param foreignAttribute
	 *            The remote column
	 */
	public Reference(Attribute localAttribute, Attribute foreignAttribute) {
		this.setLocalAttribute(localAttribute);
		this.setForeignAttribute(foreignAttribute);
	}

	/**
	 * Returns the sequence value within the owning key.
	 *
	 * @return The sequence value
	 */
	public int getSequenceValue() {
		return this._sequenceValue;
	}

	/**
	 * Sets the sequence value within the owning key. Please note that you
	 * should not change the value once the reference has been added to a key.
	 *
	 * @param sequenceValue
	 *            The sequence value
	 */
	public void setSequenceValue(int sequenceValue) {
		this._sequenceValue = sequenceValue;
	}

	/**
	 * Returns the local column.
	 * TOCOMMENT: WHAT IS LOCAL?
	 *
	 * @return The local column
	 */
	public Attribute getLocalAttribute() {
		return this._localAttribute;
	}

	/**
	 *
	 * @param localAttribute
	 *            The local column
	 */
	public void setLocalAttribute(Attribute localAttribute) {
		this._localAttribute = localAttribute;
		this._localAttributeName = (localAttribute == null ? null : localAttribute.getName());
	}

	/**
	 *
	 * @return The foreign column
	 */
	public Attribute getForeignAttribute() {
		return this._foreignAttribute;
	}

	/**
	 * Sets the foreign column.
	 *
	 * @param foreignAttribute
	 *            The foreign column
	 */
	public void setForeignAttribute(Attribute foreignAttribute) {
		this._foreignAttribute = foreignAttribute;
		this._foreignAttributeName = (foreignAttribute == null ? null : foreignAttribute.getName());
	}

	/**
	 * Returns the name of the local column.
	 * TOCOMMENT: WHAT IS THE LOCAL COLUMN?
	 *
	 * @return The column name
	 */
	public String getLocalAttributeName() {
		return this._localAttributeName;
	}

	/**
	 * Sets the name of the local column. Note that you should not use this
	 * method when manipulating the model manually. Rather use the
	 * {@link #setLocalAttribute(Attribute)} method.
	 *
	 * @param localAttributeName
	 *            The column name
	 */
	public void setLocalAttributeName(String localAttributeName) {
		if ((this._localAttribute != null) && !this._localAttribute.getName().equals(localAttributeName)) {
			this._localAttribute = null;
		}
		this._localAttributeName = localAttributeName;
	}

	/**
	 * Returns the name of the foreign column.
	 *
	 * @return The column name
	 */
	public String getForeignAttributeName() {
		return this._foreignAttributeName;
	}

	/**
	 * Sets the name of the remote column. Note that you should not use this
	 * method when manipulating the model manually. Rather use the
	 * {@link #setForeignAttribute(Attribute)} method.
	 *
	 * @param foreignAttributeName
	 *            The column name
	 */
	public void setForeignAttributeName(String foreignAttributeName) {
		if ((this._foreignAttribute != null) && !this._foreignAttribute.getName().equals(foreignAttributeName)) {
			this._foreignAttribute = null;
		}
		this._foreignAttributeName = foreignAttributeName;
	}

	/**
	 * {@inheritDoc}
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	@Override
	public Reference clone() throws CloneNotSupportedException {
		Reference result = (Reference) super.clone();

		result._localAttributeName = this._localAttributeName;
		result._foreignAttributeName = this._foreignAttributeName;

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
		if (obj instanceof Reference) {
			Reference other = (Reference) obj;
			return new EqualsBuilder().append(this._localAttributeName, other._localAttributeName)
					.append(this._foreignAttributeName, other._foreignAttributeName).isEquals();
		}
		return false;
	}

	/**
	 * Compares this reference to the given one while ignoring the case of
	 * identifiers.
	 *
	 * @param otherRef
	 *            The other reference
	 * @return <code>true</code> if this reference is equal (ignoring case) to
	 *         the given one
	 */
	public boolean equalsIgnoreCase(Reference otherRef) {
		return (otherRef != null) && this._localAttributeName.equalsIgnoreCase(otherRef._localAttributeName)
				&& this._foreignAttributeName.equalsIgnoreCase(otherRef._foreignAttributeName);
	}

	/**
	 * {@inheritDoc}
	 * @return int
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(this._localAttributeName).append(this._foreignAttributeName).toHashCode();
	}

	/**
	 * {@inheritDoc}
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append(this.getLocalAttributeName());
		result.append(" -> ");
		result.append(this.getForeignAttributeName());

		return result.toString();
	}
}
