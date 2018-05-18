package uk.ac.ox.cs.pdq.datasources.io.jaxb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryAccessMethod;
import uk.ac.ox.cs.pdq.datasources.sql.DatabaseAccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * This class represents all executable access methods as xml files. Its main
 * purpose is to have all necessary parameters to be able to create a
 * DbAccessMethod, RestAccessMethod or InMemoryAccessMethod.
 * 
 * @author gabor
 *
 */
@XmlRootElement(name = "Access")
@XmlType(propOrder = { "accessType", "accessMethodName", "relationName", "xmlAttributes", "data", "dbProperties" })
public class XmlExecutableAccessMethod {
	public enum ACCESS_TYPE {
		IN_MEMORY_ACCESS_METHOD, DB_ACCESS_METHOD, REST_ACCESS_METHOD
	};

	private ACCESS_TYPE accessType;
	/* Generic ExecutableAccessMethod properties */
	private String accessMethodName;
	private String relationName;
	/**
	 * Attributes of the access method
	 */
	private List<Attribute> attributes;
	private Set<Attribute> inputAttributes;
	private Map<Attribute, Attribute> attributeMapping;

	/* InMemory AccessMethod */
	/** The underlying data in the InMemoryAccessMethod. */
	private Collection<Tuple> data = new ArrayList<>();
	private String dataFileName;

	/* Database AccessMethod */
	private Properties dbProperties;

	public XmlExecutableAccessMethod() {
	}

	public XmlExecutableAccessMethod(ExecutableAccessMethod eam) {
		accessMethodName = eam.getName();
		relationName = eam.getRelation().getName();
		attributes = Arrays.asList(eam.outputAttributes(false));
		inputAttributes = new HashSet<>(Arrays.asList(eam.inputAttributes()));
		attributeMapping = eam.getAttributeMapping(false);
		if (eam instanceof InMemoryAccessMethod) {
			accessType = ACCESS_TYPE.IN_MEMORY_ACCESS_METHOD;
			data = ((InMemoryAccessMethod) eam).getData();
		} else if (eam instanceof DatabaseAccessMethod) {
			accessType = ACCESS_TYPE.DB_ACCESS_METHOD;
			dbProperties = ((DatabaseAccessMethod) eam).getProperties();
			/*
			 * } else if (eam instanceof RestAccessMethod) { Do rest specific parameters
			 * here.
			 */
		} else {
			throw new RuntimeException("Unknown executable access method type! : " + eam);
		}
	}

	private Relation getRelationObject(Schema s) {
		Relation r = null;
		if (s != null)
			r = s.getRelation(relationName);
		else
			r = Relation.create(relationName,
					attributeMapping.values().toArray(new Attribute[attributeMapping.values().size()]));
		return r;
	}

	public ExecutableAccessMethod toExecutableAccessMethod(Schema s) {
		Relation r = getRelationObject(s);
		switch (accessType) {
		case IN_MEMORY_ACCESS_METHOD:
			InMemoryAccessMethod am = new InMemoryAccessMethod(accessMethodName,
					attributes.toArray(new Attribute[attributes.size()]), inputAttributes, r, attributeMapping);
			am.load(DbIOManager.importTuples(attributes.toArray(new Attribute[attributes.size()]), dataFileName));
			return am;
		case DB_ACCESS_METHOD:
			DatabaseAccessMethod dam = new DatabaseAccessMethod(accessMethodName,
					attributes.toArray(new Attribute[attributes.size()]), inputAttributes, r, attributeMapping,
					dbProperties);
			return dam;
		case REST_ACCESS_METHOD:
			/* we need to implement this case */
			return null;
		default:
			throw new RuntimeException("Unknown accessType! : " + accessType);
		}
	}

	@XmlAttribute(name = "access-type")
	public String getAccessType() {
		return accessType.name();
	}

	public void setAccessType(String accessType) {
		for (ACCESS_TYPE type : ACCESS_TYPE.values()) {
			if (type.name().equalsIgnoreCase(accessType)) {
				this.accessType = type;
				return;
			}
		}
		throw new RuntimeException("Invalid access type: \"" + accessType + "\".");
	}

	@XmlAttribute(name = "name")
	public String getAccessMethodName() {
		return accessMethodName;
	}

	public void setAccessMethodName(String accessMethodName) {
		this.accessMethodName = accessMethodName;
	}

	@XmlAttribute(name = "relation-name")
	public String getRelationName() {
		return relationName;
	}

	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}

	@XmlElement(name = "attribute")
	public uk.ac.ox.cs.pdq.datasources.io.jaxb.XmlAttribute[] getXmlAttributes() {
		List<uk.ac.ox.cs.pdq.datasources.io.jaxb.XmlAttribute> xmlAttr = new ArrayList<>();
		if (attributes == null)
			return null;
		for (Attribute a : attributes) {
			String mapsTo = null;
			if (this.attributeMapping.get(a) != null) {
				mapsTo = this.attributeMapping.get(a).getName();
			}
			xmlAttr.add(new uk.ac.ox.cs.pdq.datasources.io.jaxb.XmlAttribute(a.getName(), a.getType(),
					inputAttributes.contains(a), mapsTo));
		}
		return xmlAttr.toArray(new uk.ac.ox.cs.pdq.datasources.io.jaxb.XmlAttribute[xmlAttr.size()]);
	}

	public void setXmlAttributes(uk.ac.ox.cs.pdq.datasources.io.jaxb.XmlAttribute[] attributes) {
		this.attributes = new ArrayList<>();
		;
		if (this.inputAttributes == null)
			this.inputAttributes = new HashSet<>();
		if (this.attributeMapping == null)
			this.attributeMapping = new HashMap<>();
		for (uk.ac.ox.cs.pdq.datasources.io.jaxb.XmlAttribute a : attributes) {
			Attribute newAttribute = Attribute.create(a.getType(), a.getName());
			this.attributes.add(newAttribute);
			if (a.isInput) {
				this.inputAttributes.add(newAttribute);
			}
			if (a.getMapsToRelationAttribute() != null) // not every attribute maps to a relation attribute
				attributeMapping.put(newAttribute, Attribute.create(a.getType(), a.getMapsToRelationAttribute()));
		}
	}

	@XmlElement(name = "data-scv-file")
	public String getData() throws IOException {
		if (accessType == ACCESS_TYPE.IN_MEMORY_ACCESS_METHOD) {
			return DbIOManager.exportTuples(relationName, DbIOManager.CSV_FOLDER, data).getAbsolutePath();
		}
		return null;
	}

	public void setData(String filePath) {
		this.dataFileName = filePath;
	}

	@XmlElement(name = "database-properties")
	public Properties getDbProperties() {
		if (accessType == ACCESS_TYPE.DB_ACCESS_METHOD) {
			return dbProperties;
		}
		return null;
	}

	public void setDbProperties(Properties dbProperties) {
		this.dbProperties = dbProperties;
	}
}
