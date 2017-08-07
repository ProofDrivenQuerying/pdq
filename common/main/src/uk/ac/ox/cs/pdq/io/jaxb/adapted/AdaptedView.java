package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;

/**
 * @author Gabor
 *
 */
@XmlType(propOrder = { "attributes", "accessMethods", "dependency", "foreignKeys", "primaryKey" })
public class AdaptedView extends AdaptedRelation {
	private static final long serialVersionUID = -734207998605158179L;
	
	private LinearGuarded dependency;
	protected Attribute[] attributes;
	protected AccessMethod[] accessMethods;
	protected ForeignKey[] foreignKeys;
	protected PrimaryKey primaryKey;

	public AdaptedView() {
	}

	public AdaptedView(View r) {
		this.attributes = r.getAttributes().clone();
		this.accessMethods = r.getAccessMethods().clone();
		this.foreignKeys = r.getForeignKeys().clone();
		this.primaryKey = r.getKey();
		this.setName(r.getName());
	}

	public View toRelation() {
		if (getDependency() != null) {
			return new View(getDependency(), getAccessMethods());
		}
		if (getAccessMethods() != null) {
			return new View(getName(), getAttributes(), getAccessMethods());
		}
		if (getAttributes() != null) {
			return new View(getName(), getAttributes());
		} else {
			return null;
		}
	}

	@XmlElement
	public LinearGuarded getDependency() {
		return this.dependency;
	}

	public void setDependency(LinearGuarded dependency) {
		this.dependency = dependency;
	}

	@XmlElement(name = "attribute")
	public Attribute[] getAttributes() {
		return attributes;
	}

	public void setAttributes(Attribute[] attributes) {
		this.attributes = attributes;
	}

	@XmlElement(name = "access-method")
	public AccessMethod[] getAccessMethods() {
		return accessMethods;
	}

	public void setAccessMethods(AccessMethod[] accessMethods) {
		this.accessMethods = accessMethods;
	}

	public ForeignKey[] getForeignKeys() {
		return foreignKeys;
	}

	public void setForeignKeys(ForeignKey[] foreignKeys) {
		this.foreignKeys = foreignKeys;
	}

	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

}