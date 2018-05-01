package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.ox.cs.pdq.io.jaxb.adapters.RelationAdapter;

/**
 * The schema of a relation.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @author Gabor
 */
@XmlJavaTypeAdapter(RelationAdapter.class)
public class Relation implements Serializable {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -9222721018270749836L;

	/**  The relation's attributes. */
	protected Attribute[] attributes;

	/**
	 * Maps attribute names to position in the relation. 
	 */
	protected Map<String, Integer> attributePositions;

	/**
	 * The relation's access methods, i.e. the positions of the inputAttributes
	 * which require input values.
	 */
	protected Map<String, AccessMethodDescriptor> accessMethodsMaps;

	protected AccessMethodDescriptor[] accessMethods;

	/**
	 * The relation's foreign keys.
	 */
	protected ForeignKey[] foreignKeys;

	protected PrimaryKey primaryKey;
	
	/**  Predicate name. */
	protected final String name;

	/**  Predicate arity. */
	protected final Integer arity;

	/**  true, if this is the signature for an equality predicate. */
	protected final Boolean isEquality;

	protected final String[] indexedAttributes;
	/**
	 * Gets the name of the predicate.
	 *
	 * @return the name of the predicate.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the arity of the predicate.
	 *
	 * @return the arity of the predicate.
	 */
//	public int getArity() {
//		return this.arity;
//	}
	//@Override
	public int getArity() {
		return this.attributes.length;
	}

	/**
	 * Checks if this is an equality predicate.
	 *
	 * @return true if the signature is of an equality predicate,
	 * false otherwise
	 */
	public boolean isEquality() {
		return this.isEquality;
	}

	

	/** 
	 * Properties associated with this relation; these may be SQL
	 * connection parameters, web service settings, etc. depending on the
	 * underlying implementation.
	 * If no properties are defined, then this an an empty Properties instance. */
	protected final Properties properties = new Properties();
	
	protected Relation(String name, Attribute[] attributes, AccessMethodDescriptor[] accessMethods, ForeignKey[] foreignKeys) {
		this(name, attributes, accessMethods, foreignKeys, false);
	}

	protected Relation(String name, Attribute[] attributes, AccessMethodDescriptor[] accessMethods) {
		this(name, attributes, accessMethods, false);
	}

	protected Relation(String name, Attribute[] attributes, AccessMethodDescriptor[] accessMethods, boolean isEquality) {
		this(name, attributes, accessMethods, new ForeignKey[]{}, isEquality);
	}

	protected Relation(String name, Attribute[] attributes, boolean isEquality) {
		this(name, attributes, new AccessMethodDescriptor[]{}, isEquality);
	}

	protected Relation(String name, Attribute[] attributes) {
		this(name, attributes, new AccessMethodDescriptor[]{}, false);
	}
	protected Relation(String name, Attribute[] attributes, AccessMethodDescriptor[] accessMethods, ForeignKey[] foreignKeys, boolean isEquality) {
		this(name, attributes, accessMethods, foreignKeys, isEquality,null);
	}
	protected Relation(String name, Attribute[] attributes, AccessMethodDescriptor[] accessMethods, ForeignKey[] foreignKeys, boolean isEquality, String[] indexedAttributes) {
		//super(name, attributes.length, isEquality);
		this.name = name;
		arity = attributes.length;
		this.isEquality = isEquality;
		this.attributes = attributes.clone();
		Map<String, Integer> positions = new LinkedHashMap<>();
		for (int attributeIndex = 0; attributeIndex < attributes.length; ++attributeIndex) 
			positions.put(this.attributes[attributeIndex].getName(), attributeIndex);
		this.attributePositions = new LinkedHashMap<>();
		this.attributePositions.putAll(positions);
		if (accessMethods==null) {
			this.accessMethods = new AccessMethodDescriptor[]{};
		} else {
			this.accessMethods = accessMethods.clone();
		}
		this.accessMethodsMaps = new LinkedHashMap<>();
		for(AccessMethodDescriptor accessMethod:this.accessMethods) 
			this.accessMethodsMaps.put(accessMethod.getName(), accessMethod);
		this.foreignKeys = foreignKeys.clone();
		if (indexedAttributes!=null) {
			this.indexedAttributes = indexedAttributes;
		} else {
			this.indexedAttributes = new String[0];
		}
	}


	public Attribute[] getAttributes() {
		return this.attributes.clone();
	}

	public Attribute getAttribute(int index) {
		return this.attributes[index];
	}

	/**
	 * Gets the attribute index.
	 *
	 * @param attributeName the attribute name
	 * @return the index of the attribute whose name is given as parameter,
	 *         returns -1 iff the relation has no such attribute
	 */
	public int getAttributePosition(String attributeName) {
		return this.attributePositions.get(attributeName);
	}

	public Attribute getAttribute(String attributeName) {
		Integer position = this.attributePositions.get(attributeName);
		if (position != null && position >= 0) 
			return this.attributes[position];
		return null;
	}

	public AccessMethodDescriptor[] getAccessMethods() {
		return this.accessMethods.clone();
	}
	
	public AccessMethodDescriptor getAccessMethod(String acceessMethodName) {
		return this.accessMethodsMaps.get(acceessMethodName);
	}

	public void addForeignKey(ForeignKey foreingKey) {
		if(this.foreignKeys.length == 0)
			this.foreignKeys = new ForeignKey[]{foreingKey};
		else {
			ForeignKey[] destination = new ForeignKey[this.foreignKeys.length + 1];
			System.arraycopy(this.foreignKeys, 0, destination, 0, this.foreignKeys.length);
			destination[this.foreignKeys.length] = foreingKey;
			this.foreignKeys = destination;
		}
	}

	public ForeignKey[] getForeignKeys() {
		return this.foreignKeys.clone();
	}

	public PrimaryKey getKey() {
		return this.primaryKey;
	}

	public void setKey(PrimaryKey key) {
		this.primaryKey = key;
	}

	public Integer[] getKeyPositions() {
		Integer[] keyPositions = new Integer[primaryKey.getNumberOfAttributes()];
		for(int attributeIndex = 0; attributeIndex < this.primaryKey.getNumberOfAttributes(); ++attributeIndex) 
			keyPositions[attributeIndex] = Arrays.asList(this.attributes).indexOf(this.primaryKey.getAttributes()[attributeIndex]);
		return keyPositions;
	}

	/**
	 * Extend the relation's schema by adding an extra attribute
	 */
	public static Relation appendAttribute(Relation r, Attribute attribute) {
		Attribute[] destination = new Attribute[r.attributes.length + 1];
		System.arraycopy(r.attributes, 0, destination, 0, r.attributes.length);
		destination[r.attributes.length] = attribute;
		return Relation.create(r.name, destination, r.accessMethods, r.foreignKeys, r.isEquality());
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.name);
		
		if (this.attributes.length > 0) {
			char sep = '(';
			for (Attribute attribute:this.attributes) {
				result.append(sep).append(attribute);
				sep = ',';
			}
			result.append(')');
		}

		if (this.foreignKeys.length > 0) {
			char sep = '{';
			for (ForeignKey foreignKey:this.foreignKeys) {
				result.append(sep).append(foreignKey);
				sep = ',';
			}
			result.append('}');
		}
		return result.toString();
	}

	/**
	 * Returns properties associated with this relation, these may be SQL
	 * connection parameters, web service settings, etc. depending on the
	 * underlying implementation.
	 * If no properties are defined, this return an empty Properties instance
	 * (not null).
	 * @return the properties associated with this relation.
	 */
	public Properties getProperties() {
		return this.properties;
	}
	
	public static Relation create(String name, Attribute[] attributes, AccessMethodDescriptor[] accessMethods, ForeignKey[] foreignKeys) {
		return Cache.relation.retrieve(new Relation(name, attributes, accessMethods, foreignKeys){
			private static final long serialVersionUID = -3703847952934804655L;});
	}
	
	public static  Relation create(String name, Attribute[] attributes, AccessMethodDescriptor[] accessMethods) {
		return Cache.relation.retrieve(new Relation(name, attributes, accessMethods){
			private static final long serialVersionUID = -8683688887610525202L;});
	}
	
	public static  Relation create(String name, Attribute[] attributes, AccessMethodDescriptor[] accessMethods, boolean isEquality) {
		return Cache.relation.retrieve(new Relation(name, attributes, accessMethods,isEquality){
			private static final long serialVersionUID = 6919596537308356684L;});
	}
	
	public static  Relation create(String name, Attribute[] attributes, boolean isEquality) {
		return Cache.relation.retrieve(new Relation(name, attributes, isEquality){
			private static final long serialVersionUID = 4962368915083031145L;});
	}
	
	public static  Relation create(String name, Attribute[] attributes) {
		return Cache.relation.retrieve(new Relation(name, attributes){
			private static final long serialVersionUID = -8215821247702132205L;});
	}
	
	public static  Relation create (String name, Attribute[] attributes, AccessMethodDescriptor[] accessMethods, ForeignKey[] foreignKeys, boolean isEquality) {
		return Cache.relation.retrieve(new Relation(name, attributes, accessMethods, foreignKeys, isEquality){
			private static final long serialVersionUID = -8215821247702132205L;});
	}
	public static  Relation create (String name, Attribute[] attributes, AccessMethodDescriptor[] accessMethods, ForeignKey[] foreignKeys, boolean isEquality,String[] indexedAttributes) {
		return Cache.relation.retrieve(new Relation(name, attributes, accessMethods, foreignKeys, isEquality,indexedAttributes){
			private static final long serialVersionUID = -8215821247702132205L;});
	}

	public String[] getIndexedAttributes() {
		return indexedAttributes;
	}

}
