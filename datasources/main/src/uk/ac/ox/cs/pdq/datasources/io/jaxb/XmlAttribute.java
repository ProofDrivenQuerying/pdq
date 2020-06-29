// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.io.jaxb;

import java.lang.reflect.Type;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * Represents an AccessMethod attribute with name, type, mapping to relation's attribute and wheather it is an input or not.
 * @author gabor
 *
 */
@XmlType (propOrder={"name","typeName", "input","mapsToRelationAttribute"})
public class XmlAttribute {
	String name;
	Type type;
	boolean isInput;
	String mapsToRelationAttribute;
	
	public XmlAttribute() {
	}
	public XmlAttribute(String name, Type type, boolean isInput,String mapsToRelationAttribute) {
		this.name = name;
		this.type = type;
		this.isInput = isInput;
		this.mapsToRelationAttribute = mapsToRelationAttribute;
	}

	@javax.xml.bind.annotation.XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	protected Type getType() {
		return type;
	}
	
	@javax.xml.bind.annotation.XmlAttribute(name = "type")
	public String getTypeName() {
		return this.type.getTypeName();
	}

	public void setTypeName(String type) throws ClassNotFoundException {
		this.type = Class.forName(type);
	}

	@javax.xml.bind.annotation.XmlAttribute
	public boolean isInput() {
		return isInput;
	}

	public void setInput(boolean isInput) {
		this.isInput = isInput;
	}

	@javax.xml.bind.annotation.XmlAttribute
	public String getMapsToRelationAttribute() {
		return mapsToRelationAttribute;
	}

	public void setMapsToRelationAttribute(String mapsToRelationAttribute) {
		this.mapsToRelationAttribute = mapsToRelationAttribute;
	}
	
}
