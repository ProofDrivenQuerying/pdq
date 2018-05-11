package uk.ac.ox.cs.pdq.datasources.io.jaxb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryAccessMethod;
import uk.ac.ox.cs.pdq.datasources.sql.DatabaseAccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
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
public class XmlExecutableAccessMethod {
	public enum ACCESS_TYPE {IN_MEMORY_ACCESS_METHOD, DB_ACCESS_METHOD, REST_ACCESS_METHOD};
	private ACCESS_TYPE accessType;
	/* Generic ExecutableAccessMethod properties */
	private String accessMethodName;
	private String relationName;
	private List<Attribute> attributes;
	private Set<Attribute> inputAttributes;
	private Map<Attribute,Attribute> attributeMapping;
	
	/* InMemory AccessMethod */ 
	/**  The underlying data in the InMemoryAccessMethod. */
	private Collection<Tuple> data = new ArrayList<>();
	/* Database AccessMethod */
	private Properties dbProperties;
	/* Rest AccessMethod */ 
	// web service url and other web service related parameters. 
	private String restUrl;
	private String restDocumentationUrl;
	private String restProtocol;
	
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
			data = ((InMemoryAccessMethod)eam).getData();
		} else if (eam instanceof DatabaseAccessMethod) {
			accessType = ACCESS_TYPE.DB_ACCESS_METHOD;
			dbProperties = ((DatabaseAccessMethod)eam).getProperties();
/*		} else if (eam instanceof RestAccessMethod) {
			Do rest specific parameters here.
			*/
		} else {
			throw new RuntimeException("Unknown executable access method type! : " + eam);
		}
	}

	public ExecutableAccessMethod toAccessMethod(Schema s) {
		switch (accessType) {
		case IN_MEMORY_ACCESS_METHOD:
			InMemoryAccessMethod am = new InMemoryAccessMethod(accessMethodName, attributes.toArray(new Attribute[attributes.size()]), 
					inputAttributes, s.getRelation(relationName), attributeMapping);
			am.load(data);
			return am;
		case DB_ACCESS_METHOD:
			DatabaseAccessMethod dam = new DatabaseAccessMethod(accessMethodName, attributes.toArray(new Attribute[attributes.size()]), 
					inputAttributes, s.getRelation(relationName), attributeMapping, dbProperties);
			return dam;
		case REST_ACCESS_METHOD:
			return null;
		default:
			return null;
		} 
	}
	public ACCESS_TYPE getAccessType() {
		return accessType;
	}
	public void setAccessType(ACCESS_TYPE accessType) {
		this.accessType = accessType;
	}
	public String getAccessMethodName() {
		return accessMethodName;
	}
	public void setAccessMethodName(String accessMethodName) {
		this.accessMethodName = accessMethodName;
	}
	public String getRelationName() {
		return relationName;
	}
	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}
	public List<Attribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
	public Set<Attribute> getInputAttributes() {
		return inputAttributes;
	}
	public void setInputAttributes(Set<Attribute> inputAttributes) {
		this.inputAttributes = inputAttributes;
	}
	public Map<Attribute, Attribute> getAttributeMapping() {
		return attributeMapping;
	}
	public void setAttributeMapping(Map<Attribute, Attribute> attributeMapping) {
		this.attributeMapping = attributeMapping;
	}
	public Collection<Tuple> getData() {
		return data;
	}
	public void setData(Collection<Tuple> data) {
		this.data = data;
	}
	public Properties getDbProperties() {
		return dbProperties;
	}
	public void setDbProperties(Properties dbProperties) {
		this.dbProperties = dbProperties;
	}
	public String getRestUrl() {
		return restUrl;
	}
	public void setRestUrl(String restUrl) {
		this.restUrl = restUrl;
	}
	public String getRestDocumentationUrl() {
		return restDocumentationUrl;
	}
	public void setRestDocumentationUrl(String restDocumentationUrl) {
		this.restDocumentationUrl = restDocumentationUrl;
	}
	public String getRestProtocol() {
		return restProtocol;
	}
	public void setRestProtocol(String restProtocol) {
		this.restProtocol = restProtocol;
	}
}
