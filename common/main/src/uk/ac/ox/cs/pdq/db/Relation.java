package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.RelationAdapter;

/**
 * The schema of a relation.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
@XmlJavaTypeAdapter(RelationAdapter.class)
public abstract class Relation extends Predicate implements Serializable {
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
	protected Map<String, AccessMethod> accessMethodsMaps;

	protected AccessMethod[] accessMethods;

	/**
	 * The relation's foreign keys.
	 */
	protected ForeignKey[] foreignKeys;

	protected PrimaryKey primaryKey;

	/** 
	 * Properties associated with this relation; these may be SQL
	 * connection parameters, web service settings, etc. depending on the
	 * underlying implementation.
	 * If no properties are defined, then this an an empty Properties instance. */
	protected final Properties properties = new Properties();
	
	protected Relation(String name, Attribute[] attributes, AccessMethod[] accessMethods, ForeignKey[] foreignKeys) {
		this(name, attributes, accessMethods, foreignKeys, false);
	}

	protected Relation(String name, Attribute[] attributes, AccessMethod[] accessMethods) {
		this(name, attributes, accessMethods, false);
	}

	protected Relation(String name, Attribute[] attributes, AccessMethod[] accessMethods, boolean isEquality) {
		this(name, attributes, accessMethods, new ForeignKey[]{}, isEquality);
	}

	protected Relation(String name, Attribute[] attributes, boolean isEquality) {
		this(name, attributes, new AccessMethod[]{}, isEquality);
	}

	protected Relation(String name, Attribute[] attributes) {
		this(name, attributes, new AccessMethod[]{}, false);
	}

	protected Relation(String name, Attribute[] attributes, AccessMethod[] accessMethods, ForeignKey[] foreignKeys, boolean isEquality) {
		super(name, attributes.length, isEquality);
		this.attributes = attributes.clone();
		Map<String, Integer> positions = new LinkedHashMap<>();
		for (int attributeIndex = 0; attributeIndex < attributes.length; ++attributeIndex) 
			positions.put(this.attributes[attributeIndex].getName(), attributeIndex);
		this.attributePositions = new LinkedHashMap<>();
		this.attributePositions.putAll(positions);
		if (accessMethods==null) {
			this.accessMethods = new AccessMethod[]{};
		} else {
			this.accessMethods = accessMethods.clone();
		}
		this.accessMethodsMaps = new LinkedHashMap<>();
		for(AccessMethod accessMethod:this.accessMethods) 
			this.accessMethodsMaps.put(accessMethod.getName(), accessMethod);
		this.foreignKeys = foreignKeys.clone();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getArity() {
		return this.attributes.length;
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

	public AccessMethod[] getAccessMethods() {
		return this.accessMethods.clone();
	}
	
	public AccessMethod getAccessMethod(String acceessMethodName) {
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
	//TODO this method breaks immutability it should certainly be removed
	public void appendAttribute(Attribute attribute) {
		Attribute[] destination = new Attribute[this.attributes.length + 1];
		System.arraycopy(this.attributes, 0, destination, 0, this.attributes.length);
		destination[this.attributes.length] = attribute;
		this.attributes = destination;
		this.attributePositions.put(attribute.getName(), this.attributes.length);
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
	
	public static Relation create(String name, Attribute[] attributes, AccessMethod[] accessMethods, ForeignKey[] foreignKeys) {
		return Cache.relation.retrieve(new Relation(name, attributes, accessMethods, foreignKeys){
			private static final long serialVersionUID = -3703847952934804655L;});
	}
	
	public static  Relation create(String name, Attribute[] attributes, AccessMethod[] accessMethods) {
		return Cache.relation.retrieve(new Relation(name, attributes, accessMethods){
			private static final long serialVersionUID = -8683688887610525202L;});
	}
	
	public static  Relation create(String name, Attribute[] attributes, AccessMethod[] accessMethods, boolean isEquality) {
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
	
	public static  Relation create (String name, Attribute[] attributes, AccessMethod[] accessMethods, ForeignKey[] foreignKeys, boolean isEquality) {
		return Cache.relation.retrieve(new Relation(name, attributes, accessMethods, foreignKeys, isEquality){
			private static final long serialVersionUID = -8215821247702132205L;});
	}

}
