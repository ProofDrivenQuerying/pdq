// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.io.jaxb;

import java.io.File;
import java.io.FileNotFoundException;
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
import uk.ac.ox.cs.pdq.datasources.simplewebservice.JsonWebService;
import uk.ac.ox.cs.pdq.datasources.simplewebservice.XmlWebService;
import uk.ac.ox.cs.pdq.datasources.sql.SqlAccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;

/**
 * This class represents all executable access methods as xml files. Its main
 * purpose is to have all necessary parameters to be able to create a
 * DbAccessMethod, RestAccessMethod or InMemoryAccessMethod, JsonWebService or XmlWebService.
 * 
 * @author gabor
 *
 */
@XmlRootElement(name = "Access")
@XmlType(propOrder = { "accessType", "accessMethodName", "relationName", "xmlAttributes", "data", "dbProperties", "webServiceUrl","requestTemplates" })
public class XmlExecutableAccessMethod {
	public enum ACCESS_TYPE {
		IN_MEMORY_ACCESS_METHOD, DB_ACCESS_METHOD, XML_WEB_ACCESS_METHOD, JSON_WEB_ACCESS_METHOD
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
	
	/* Simple xml and json WebService AccessMethod */
	private String webServiceUrl;
	// web service push or get method parameters
	private List<PostParameter> postParams = new ArrayList<>();
	
	/* Database AccessMethod */
	private Properties dbProperties;
	private File datafolder;

	public XmlExecutableAccessMethod() {
	}

	public XmlExecutableAccessMethod(ExecutableAccessMethod eam) {
		this(eam,null);
	}
	public XmlExecutableAccessMethod(ExecutableAccessMethod eam,File datafolder) {
		if (datafolder==null) 
			this.datafolder = DbIOManager.CSV_FOLDER;
		else
			this.datafolder = datafolder;
		this.datafolder.mkdirs();
		accessMethodName = eam.getName();
		relationName = eam.getRelation().getName();
		attributes = Arrays.asList(eam.outputAttributes(false));
		inputAttributes = new HashSet<>(Arrays.asList(eam.inputAttributes()));
		attributeMapping = eam.getAttributeMapping(false);
		if (eam instanceof InMemoryAccessMethod) {
			accessType = ACCESS_TYPE.IN_MEMORY_ACCESS_METHOD;
			data = ((InMemoryAccessMethod) eam).getData();
		} else if (eam instanceof SqlAccessMethod) {
			accessType = ACCESS_TYPE.DB_ACCESS_METHOD;
			dbProperties = ((SqlAccessMethod) eam).getProperties();
			
		} else if (eam instanceof JsonWebService) {
			accessType = ACCESS_TYPE.JSON_WEB_ACCESS_METHOD;
			webServiceUrl = ((JsonWebService) eam).getUrl();
			
		} else if (eam instanceof XmlWebService) { 
			accessType = ACCESS_TYPE.XML_WEB_ACCESS_METHOD;
			webServiceUrl = ((XmlWebService) eam).getUrl();
			this.postParams = ((XmlWebService) eam).getRequestTemplates();
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

	public ExecutableAccessMethod toExecutableAccessMethod(Schema s, File parentDir) throws IOException {
		Relation r = getRelationObject(s);
		switch (accessType) {
		case IN_MEMORY_ACCESS_METHOD:
			if (dataFileName==null)
				dataFileName = this.getRelationName() + "_" + this.getAccessMethodName() + ".csv";
			if (!new File(dataFileName).exists()) {
				dataFileName = new File(parentDir,dataFileName).getAbsolutePath();
			}
			if (!new File(dataFileName).exists()) {
				throw new FileNotFoundException("Data file: " + dataFileName + " not found!" );
			}
			InMemoryAccessMethod am = new InMemoryAccessMethod(accessMethodName,
					attributes.toArray(new Attribute[attributes.size()]), inputAttributes, r, attributeMapping);
			am.load(DbIOManager.importTuples(attributes.toArray(new Attribute[attributes.size()]), dataFileName));
			return am;
		case DB_ACCESS_METHOD:
			SqlAccessMethod dam = new SqlAccessMethod(accessMethodName,
					attributes.toArray(new Attribute[attributes.size()]), inputAttributes, r, attributeMapping,
					dbProperties);
			return dam;
		case XML_WEB_ACCESS_METHOD:
			XmlWebService service = new XmlWebService(accessMethodName,
					attributes.toArray(new Attribute[attributes.size()]), inputAttributes, r, attributeMapping);
			service.setUrl(webServiceUrl);
			service.setRequestTemplates(postParams);
			return service;
		case JSON_WEB_ACCESS_METHOD:
			JsonWebService service2 = new JsonWebService(accessMethodName,
					attributes.toArray(new Attribute[attributes.size()]), inputAttributes, r, attributeMapping);
			service2.setUrl(webServiceUrl);
			service2.setRequestTemplates(postParams);
			return service2;
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

	//< url-template pattern= "[http:/somethingsomething/{8}]" where {i} are placeholders
	//  for inputs where {i} means the i^th input  >
	@XmlElement(name = "url-template")
	public String getWebServiceUrl() {
		return webServiceUrl;
	}

	public void setWebServiceUrl(String xmlWebServiceUrl) {
		this.webServiceUrl = xmlWebServiceUrl;
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
					isInput(mapsTo), mapsTo));
		}
		return xmlAttr.toArray(new uk.ac.ox.cs.pdq.datasources.io.jaxb.XmlAttribute[xmlAttr.size()]);
	}
	private boolean isInput(String attributeName) {
		for (Attribute a: inputAttributes)
			if (a.getName()!=null && a.getName().equals(attributeName))
				return true;
		return false;
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
			return DbIOManager.exportTuples(relationName+"_"+this.getAccessMethodName(), this.datafolder, data).getAbsolutePath();
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

	//request templates are given for web services, on a per input attribute basis.
	@XmlElement(name = "post-parameter")
	public List<PostParameter> getRequestTemplates() {
		return postParams;
	}

	public void setRequestTemplates(List<PostParameter> postParams) {
		this.postParams = postParams;
	}
	
	/**
	 * @author gabor
	 *	Name value pairs for the post or get web methods.
	 */
	public static class PostParameter {
		private String name;
		private String value;
		public PostParameter() {
		}
		public PostParameter(String name, String value) {
			this.name = name;
			this.value = value;
		}
		@XmlAttribute(name = "name")
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		@XmlAttribute(name = "value")
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
	}
}
