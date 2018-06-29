package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Relation;

/**
 * @author Gabor
 *
 */
@XmlType(propOrder = { "attributes", "accessMethods", "primaryKey", "foreignKeys", "equality","indexedAttributes"})
public class AdaptedRelation implements Serializable {
	private static final long serialVersionUID = -9222721018270749836L;
	protected Attribute[] attributes;
	protected AccessMethodDescriptor[] accessMethods;
	protected ForeignKey[] foreignKeys;
	protected String[] primaryKey; // one or more attribute names that form the primary key.
	protected Properties properties;
	private String name;
	private Boolean isEquality = null;
	private String[] indexedAttributes;

	/**
	 * Source is needed for the case when the relation is definied in the database.
	 */
	private String source;
	private String size;

	public AdaptedRelation() {
		accessMethods = new AccessMethodDescriptor[0];
		foreignKeys = new ForeignKey[0];
	}

	public AdaptedRelation(Relation r) {
		this.attributes = r.getAttributes();
		this.accessMethods = r.getAccessMethods();
		this.primaryKey = new String[r.getForeignKeys().length];
		if (r.getPrimaryKey()!=null) {
			for (int i = 0; i < r.getPrimaryKey().getNumberOfAttributes(); i++) {
				this.primaryKey[i] = r.getPrimaryKey().getAttributes()[i].getName();
			}
		}
		this.foreignKeys = r.getForeignKeys();
		this.setName(r.getName());
		this.setEquality(r.isEquality());
		this.setIndexedAttributes(r.getIndexedAttributes());
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
	public AccessMethodDescriptor[] getAccessMethods() {
		return this.accessMethods;
	}

	public void setAccessMethods(AccessMethodDescriptor[] am) {
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

	@XmlElement(name = "foreign-key")
	public ForeignKey[] getForeignKeys() {
		return this.foreignKeys;
	}
	public void setForeignKeys(ForeignKey[]keys) {
		this.foreignKeys = keys;
	}

	@XmlElement(name = "primaryKey")
	public String[] getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(String[] key) {
		this.primaryKey = key;
	}

	@Override
	public String toString() {
		return this.toRelation().toString();
	}

	public Relation toRelation() {
		Relation r = null;
		Attribute[] attr = getAttributes();
 		if (attr == null && source != null) {
			attr = getAttributesFromSrc();
		}
		if (isEquality() == null && "EQUALITY".equals(name)) {
			setEquality(true);
		}
		if (indexedAttributes!= null) {
			Boolean equality = isEquality();
			if (equality==null)
				equality = false;
			r = Relation.create(getName(), attr, getAccessMethods(), getForeignKeys(), equality, indexedAttributes);
		} else
		if (isEquality() != null) {
			r = Relation.create(getName(), attr, getAccessMethods(), getForeignKeys(), isEquality());
		} else
		if (getForeignKeys() != null) {
			r = Relation.create(getName(), attr, getAccessMethods(), getForeignKeys());
		} else
		if (getAccessMethods() != null) {
			r = Relation.create(getName(), attr, getAccessMethods());
		} else 
		if (attr != null) {
			r =Relation.create(getName(), attr);
		} else {
			return null;
		}
		if (getPrimaryKey()!=null && getPrimaryKey().length!=0) {
			Map<String,Attribute> allAttributesMap = new HashMap<>();
			for (Attribute a: getAttributes()) {
				allAttributesMap.put(a.getName(), a);
			}
			
			Attribute keyAttributes[] = new Attribute[getPrimaryKey().length];
			for (int i = 0; i < keyAttributes.length; i++) {
				keyAttributes[i] = allAttributesMap.get(getPrimaryKey()[i]); 
			}
			r.setPrimaryKey(PrimaryKey.create(keyAttributes));
		}
		return r;
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

	@XmlAttribute(name="isEquality")
	public Boolean isEquality() {
		if (isEquality==null || !isEquality)
			return null; // the optional parameter only displayed when it is set to true
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

	@XmlElement(name="IndexedAttribute")
	public String[] getIndexedAttributes() {
		return indexedAttributes;
	}

	public void setIndexedAttributes(String[] indexedAttributes) {
		this.indexedAttributes = indexedAttributes;
	}

}
