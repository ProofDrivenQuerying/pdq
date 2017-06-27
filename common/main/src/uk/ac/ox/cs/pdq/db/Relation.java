package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import uk.ac.ox.cs.pdq.db.metadata.RelationMetadata;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 * The schema of a relation.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public abstract class Relation extends Predicate implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -9222721018270749836L;

	/** 
	 * Properties associated with this relation; these may be SQL
	 * connection parameters, web service settings, etc. depending on the
	 * underlying implementation.
	 * If no properties are defined, then this an an empty Properties instance. */
	protected final Properties properties = new Properties();

	/**  The relation's attributes. */
	protected List<Attribute> attributes;

	/**
	 * Maps attribute names to position in the relation. 
	 */
	protected Map<String, Integer> attributePositions;

	/**
	 * The relation's access methods, i.e. the positions of the inputAttributes
	 * which require input values.
	 */
	protected Map<String, AccessMethod> accessMethods;

	protected List<AccessMethod> accessMethodsList;

	/**
	 * The relation's foreign keys.
	 */
	protected List<ForeignKey> foreignKeys;

	protected PrimaryKey key;

	/** Every relation has associated metadata stored in a separate object **/
	private RelationMetadata metadataRelation;


	public Relation(String name, List<? extends Attribute> attributes,
			List<AccessMethod> accessMethods, List<ForeignKey> foreignKeys) {
		this(name, attributes, accessMethods, foreignKeys, false);
	}

	public Relation(String name, List<? extends Attribute> attributes, List<AccessMethod> accessMethods, List<ForeignKey> foreignKeys, boolean isEquality) {
		super(name, attributes.size(), isEquality);
		this.attributes = new ArrayList<>();
		this.attributes.addAll(attributes);
		Map<String, Integer> positions = new LinkedHashMap<>();
		int i = 0;
		for (Attribute a : this.attributes) {
			positions.put(a.getName(), i++);
		}
		this.attributePositions = new LinkedHashMap<>();
		this.attributePositions.putAll(positions);
		this.accessMethods = new LinkedHashMap<>();
		this.accessMethodsList = new ArrayList<>();
		this.addAccessMethods(accessMethods);
		this.foreignKeys = new ArrayList<>(foreignKeys);
	}


	public Relation(String name, List<? extends Attribute> attributes, List<AccessMethod> accessMethods) {
		this(name, attributes, accessMethods, false);
	}

	public Relation(String name, List<? extends Attribute> attributes, List<AccessMethod> accessMethods, boolean isEquality) {
		this(name, attributes, accessMethods, new ArrayList<ForeignKey>(), isEquality);
	}

	public Relation(String name, List<? extends Attribute> attributes, boolean isEquality) {
		this(name, attributes, new ArrayList<AccessMethod>(), isEquality);
	}

	public Relation(String name, List<? extends Attribute> attributes) {
		this(name, attributes, new ArrayList<AccessMethod>(), false);
	}

	@Override
	public String getName() {
		return this.name;

	}

	@Override
	public int getArity() {
		return this.attributes.size();
	}


	public List<Attribute> getAttributes() {
		return this.attributes;
	}

	public TupleType getType() {
		return TupleType.DefaultFactory.createFromTyped(getAttributes());
	}


	public Attribute getAttribute(int index) {
		return this.attributes.get(index);
	}

	/**
	 * TOCOMMENT I guss this retruns the attribute's position
	 * Gets the attribute index.
	 *
	 * @param attributeName the attribute name
	 * @return the index of the attribute whose name is given as parameter,
	 *         returns -1 iff the relation has no such attribute
	 */
	public int getAttributeIndex(String attributeName) {
		return this.attributePositions.get(attributeName);
	}

	public Attribute getAttribute(String attributeName) {
		Integer position = this.attributePositions.get(attributeName);
		if (position != null && position >= 0) {
			return this.attributes.get(position);
		}
		return null;
	}

	public List<AccessMethod> getAccessMethods() {
		return this.accessMethodsList;
	}

	public void addForeignKey(ForeignKey foreingKey) {
		this.foreignKeys.add(foreingKey);
	}

	public void addForeignKeys(List<ForeignKey> foreingKeys) {
		this.foreignKeys.addAll(foreingKeys);
	}

	public ForeignKey getForeignKey(int index) {
		return this.foreignKeys.get(index);
	}

	public List<ForeignKey> getForeignKeys() {
		return this.foreignKeys;
	}
	
	public void addAccessMethods(List<AccessMethod> accessMethods) {
		for(AccessMethod accessMethod:accessMethods) {
			this.accessMethods.put(accessMethod.getName(), accessMethod);
			this.accessMethodsList.add(accessMethod);
		}
	}
	
	public void setAccessMethods(List<AccessMethod> accessMethods) {
		this.accessMethods.clear();
		this.accessMethodsList.clear();
		for(AccessMethod accessMethod:accessMethods) {
			this.accessMethods.put(accessMethod.getName(), accessMethod);
			this.accessMethodsList.add(accessMethod);
		}
	}

	public void setKey(PrimaryKey key) {
		this.key = key;
	}

	public PrimaryKey getKey() {
		return this.key;
	}

	public Integer[] getKeyPositions() {
		Integer[] keyPositions = new Integer[key.getNumberOfAttributes()];
		for(int attributeIndex = 0; attributeIndex < this.key.getNumberOfAttributes(); ++attributeIndex) 
			keyPositions[attributeIndex] = this.attributes.indexOf(this.key.getAttributes()[attributeIndex]);
		return keyPositions;
	}

	/**
	 * Extend the relation's schema by adding an extra attribute
	 */
	public void extendByAddingAttribute(Attribute at) {
		//		arity = arity + 1;
		//		super.hash = Objects.hash(this.name, this.arity);
		//		
		List<Attribute> attrs = new ArrayList<Attribute>(this.attributes);
		attrs.add(at);
		this.attributes = ImmutableList.copyOf(attrs);
		Map<String, Integer> positions = new LinkedHashMap<>();
		int i = 0;
		for (Attribute a : this.attributes) {
			positions.put(a.getName(), i++);
		}
		this.attributePositions = ImmutableMap.copyOf(positions);


	}
	//
	//
	//	/**
	//	 * TOCOMMENT Two relations are equal if, by using the corresponding equals methods, their names are equals, their attributes are equal, and their amView ???? are equal.
	//	 *
	//	 * @param o Object
	//	 * @return boolean
	//	 */
	//	@Override
	//	public boolean equals(Object o) {
	//		if (this == o) {
	//			return true;
	//		}
	//		if (o == null) {
	//			return false;
	//		}
	//		return Relation.class.isInstance(o)
	//				&& this.name.equals(((Relation) o).name)
	//				&& this.attributes.equals(((Relation) o).attributes)
	//				&& this.accessMethodsList.equals(((Relation) o).accessMethodsList);
	//	}
	//
	//	@Override
	//	public int hashCode() {
	//		return Objects.hash(this.name, this.attributes, this.accessMethodsList);
	//	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.name).append('(');
		result.append(Joiner.on(",").join(this.attributes)).append(')');

		if (this.accessMethods != null && !this.accessMethods.isEmpty()) {
			char sep = '{';
			if (this.accessMethods != null && !this.accessMethods.isEmpty()) {
				for (AccessMethod c : this.accessMethods.values()) {
					result.append(sep).append(c);
					RelationMetadata md = this.getMetadata();
					if (md != null) {
						result.append('/').append(md.getPerInputTupleCost(c));
					}
					sep = ',';
				}
				result.append('}');
			}
		}

		if (this.foreignKeys != null && !this.foreignKeys.isEmpty()) {
			char sep = '{';
			if (this.foreignKeys != null && !this.foreignKeys.isEmpty()) {
				for (ForeignKey c : this.foreignKeys) {
					result.append(sep).append(c);
					sep = ',';
				}
				result.append('}');
			}
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

	/**
	 * Gets the metadata object of the relation.
	 *
	 * @return RelationMetadata
	 */
	public RelationMetadata getMetadata() {
		return this.metadataRelation;
	}

	/**
	 * Sets the metadata of the Relation.
	 *
	 * @param metadata RelationMetadata
	 */
	public void setMetadata(RelationMetadata metadata) {
		this.metadataRelation = metadata;
	}
}
