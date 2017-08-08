package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.io.Serializable;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Relation;

/**
 * @author Gabor
 *
 */
@XmlType(propOrder = { "attributes", "accessMethods", "key" })
public class AdaptedRelation implements Serializable {
	private static final long serialVersionUID = -9222721018270749836L;
	protected Attribute[] attributes;
	protected AccessMethod[] accessMethods;
	protected ForeignKey[] foreignKeys;
	protected PrimaryKey primaryKey;
	protected Properties properties;
	private String name;
	private Boolean isEquality = null;

	/**
	 * Source is needed for the case when the relation is definied in the database.
	 */
	private String source;
	private String size;

	public AdaptedRelation() {
	}

	public AdaptedRelation(Relation r) {
		this.attributes = r.getAttributes();
		this.accessMethods = r.getAccessMethods();
		this.foreignKeys = r.getForeignKeys();
		this.primaryKey = r.getKey();
		this.properties = r.getProperties();
		this.setName(r.getName());
		this.setEquality(r.isEquality());
	}

	@XmlAttribute
	public String getName() {
		return this.name;
	}

	@XmlElement(name = "attribute")
	public Attribute[] getAttributes() {
		return this.attributes;
	}

	public void setAttributes(Attribute[] a) {
		this.attributes = a;
	}

	@XmlElement(name = "access-method")
	public AccessMethod[] getAccessMethods() {
		return this.accessMethods;
	}

	public void setAccessMethods(AccessMethod[] am) {
		this.accessMethods = am;
	}

	public void addForeignKey(ForeignKey foreingKey) {
		try {
			if (this.foreignKeys.length == 0)
				this.foreignKeys = new ForeignKey[] { foreingKey };
			else {
				ForeignKey[] destination = new ForeignKey[this.foreignKeys.length + 1];
				System.arraycopy(this.foreignKeys, 0, destination, 0, this.foreignKeys.length);
				destination[this.foreignKeys.length] = foreingKey;
				this.foreignKeys = destination;
			}
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	public ForeignKey[] getForeignKeys() {
		return this.foreignKeys;
	}

	public PrimaryKey getKey() {
		return this.primaryKey;
	}

	public void setKey(PrimaryKey key) {
		this.primaryKey = key;
	}

	@Override
	public String toString() {
		return this.toRelation().toString();
	}

	public Relation toRelation() {
		Attribute[] attr = getAttributes();
		if (attr == null && source != null) {
			attr = getAttributesFromSrc();
		}
		if (isEquality() != null) {
			return Relation.create(getName(), attr, getAccessMethods(), getForeignKeys(), isEquality());
		}
		if (getForeignKeys() != null) {
			return Relation.create(getName(), attr, getAccessMethods(), getForeignKeys());
		}
		if (getAccessMethods() != null) {
			return Relation.create(getName(), attr, getAccessMethods());
		}
		if (attr != null) {
			return Relation.create(getName(), attr);
		} else {
			return null;
		}
	}

	private Attribute[] getAttributesFromSrc() {

		return new Attribute[] {};
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isEquality() {
		return isEquality;
	}

	public void setEquality(boolean isEquality) {
		this.isEquality = isEquality;
	}

	@XmlAttribute
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@XmlAttribute
	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

}
