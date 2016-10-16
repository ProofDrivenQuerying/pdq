package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.metadata.RelationMetadata;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * The schema of a relation.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public abstract class Relation extends Predicate implements Serializable {

	/** The log. */
	protected static Logger log = Logger.getLogger(Relation.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -9222721018270749836L;

	/** A global counter that gives unique id numbers to relations. */
	protected static int globalId = 0;

	/**
	 * The relation's unique identifier.
	 */
	protected final int relationId;

	/** 
	 * Properties associated with this relation; these may be SQL
	 * connection parameters, web service settings, etc. depending on the
	 * underlying implementation.
	 * If no properties are defined, then this an an empty Properties instance. */
	protected final Properties properties = new Properties();

	/**  The relation's attributes. */
	protected final List<Attribute> attributes;

	/**
	 * Maps attribute names to position in the relation. This map is initialized lazily
	 * to minimize memory overhead
	 */
	protected final Map<String, Integer> attributePositions;

	/**
	 * TOCOMMENT what is the key of this map? the relations' names?
	 * The relation's access methods, i.e. the positions of the inputAttributes
	 * which require input values.
	 */
	protected Map<String, AccessMethod> accessMethods;

	/**
	 *  TOCOMMENT what is this?
	 *  The am view. */
	protected List<AccessMethod> amView;

	/**
	 * The relation's foreign keys.
	 */
	protected List<ForeignKey> foreignKeys;

	/** 
	 * TOCOMMENT the primary key?
	 * The key. */
	protected List<Attribute> key = Lists.newArrayList();

	/** The key positions. */
	protected List<Integer> keyPositions = null;

	/** Every relation has associated metadata stored in a separate object **/
	private RelationMetadata metadataRelation;


	/**
	 * Constructs a new Relation.
	 *
	 * @param name Relation's name
	 * @param attributes List<? extends Attribute>
	 * @param accessMethods Relation's binding methods
	 * @param foreignKeys List<ForeignKey>
	 */
	public Relation(String name, List<? extends Attribute> attributes,
			List<AccessMethod> accessMethods, List<ForeignKey> foreignKeys) {
		this(name, attributes, accessMethods, foreignKeys, false);
	}

	/**
	 * Constructs a new relation.
	 *
	 * @param name Relation's name
	 * @param attributes List<? extends Attribute>
	 * @param accessMethods Relation's binding methods
	 * @param foreignKeys List<ForeignKey>
	 * @param isEquality true if this is an equality predicate
	 */
	public Relation(String name, List<? extends Attribute> attributes,
			List<AccessMethod> accessMethods, List<ForeignKey> foreignKeys,
			boolean isEquality) {
		super(name, attributes.size(), isEquality);
		this.relationId = Relation.globalId++;
		this.attributes = ImmutableList.copyOf(attributes);
		Map<String, Integer> positions = new LinkedHashMap<>();
		int i = 0;
		for (Attribute a : this.attributes) {
			positions.put(a.getName(), i++);
		}
		this.attributePositions = ImmutableMap.copyOf(positions);
		this.accessMethods = new LinkedHashMap<>();
		this.amView = new ArrayList<>();
		this.setAccessMethods(accessMethods);
		this.foreignKeys = new ArrayList<>(foreignKeys);
		this.rep = null;
	}

	/**
	 * Constructs a new relation.
	 *
	 * @param name Relation's name
	 * @param attributes List<? extends Attribute>
	 * @param accessMethods Relation's binding methods
	 */
	public Relation(String name, List<? extends Attribute> attributes, List<AccessMethod> accessMethods) {
		this(name, attributes, accessMethods, false);
	}

	/**
	 * Constructs a new relation..
	 *
	 * @param name Relation's name
	 * @param attributes List<? extends Attribute>
	 * @param accessMethods Relation's binding methods
	 * @param isEquality the is equality
	 */
	public Relation(String name, List<? extends Attribute> attributes,
			List<AccessMethod> accessMethods, boolean isEquality) {
		this(name, attributes, accessMethods, new ArrayList<ForeignKey>(), isEquality);
	}

	/**
	 * Constructs a new relation.
	 *
	 * @param name Relation's name
	 * @param attributes List<? extends Attribute>
	 * @param isEquality the is equality
	 */
	public Relation(String name, List<? extends Attribute> attributes, boolean isEquality) {
		this(name, attributes, new ArrayList<AccessMethod>(), isEquality);
	}

	/**
	 * Constructs a new relation.
	 *
	 * @param name Relation's name
	 * @param attributes List<? extends Attribute>
	 */
	public Relation(String name, List<? extends Attribute> attributes) {
		this(name, attributes, new ArrayList<AccessMethod>(), false);
	}

	/**
	 * Copy constructor.
	 *
	 * @param r Relation
	 */
	public Relation(Relation r) {
		this(r.getName(), r.getAttributes(), r.getAccessMethods(),
				r.getForeignKeys(), r.isEquality());
	}

	/**
	 * Gets the name of the relation.
	 *
	 * @return String
	 */
	@Override
	public String getName() {
		return this.name;

	}

	/**
	 * Gets the arity of the relation.
	 *
	 * @return the arity of the relation.
	 */
	@Override
	public int getArity() {
		return this.attributes.size();
	}

	/**
	 * Gets the attributes of the relation.
	 *
	 * @return the relation's inputAttributes
	 */
	public List<Attribute> getAttributes() {
		return this.attributes;
	}

	/**
	 * Gets the type of the attributes of the relation.
	 *
	 * @return the relation's type
	 */
	public TupleType getType() {
		return TupleType.DefaultFactory.createFromTyped(getAttributes());
	}

	/**
	 * TOCOMMENT this seems strange, to have to call a method on a relation given the access method as input, to get the input positiions of the access method
	 * Gets the input attributes.
	 *
	 * @param b an access method of this relation
	 * @return 		the relation's input attributes for input binding
	 */
	public List<Attribute> getInputAttributes(AccessMethod b) {
		Preconditions.checkArgument(this.accessMethods.containsKey(b.getName()));
		List<Attribute> result = new ArrayList<>(b.getInputs().size());
		for (Integer i: b.getInputs()) {
			result.add(this.getAttribute(i - 1));
		}
		return result;
	}

	/**
	 * Gets the attribute at position 'index'.
	 *
	 * @param index int
	 * @return the relation's attribute at position index
	 */
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

	/**
	 * Gets the attribute object given its name.
	 *
	 * @param attributeName the attribute name
	 * @return the index of the attribute whose name is given as parameter,
	 *         returns -1 iff the relation has no such attribute
	 */
	public Attribute getAttribute(String attributeName) {
		Integer position = this.attributePositions.get(attributeName);
		if (position != null && position >= 0) {
			return this.attributes.get(position);
		}
		return null;
	}

	/**
	 * Gets the access methods.
	 *
	 * @return the relation's accessMethods.
	 */
	public List<AccessMethod> getAccessMethods() {
		return this.amView;
	}

	/**
	 * Adds a foreign key to the relation.
	 *
	 * @param foreingKey ForeignKey
	 */
	public void addForeignKey(ForeignKey foreingKey) {
		this.foreignKeys.add(foreingKey);
		this.rep = null;
	}

	/**
	 * Adds a list of foreign keys to the relation.
	 *
	 * @param foreingKeys List<ForeignKey>
	 */
	public void addForeignKeys(List<ForeignKey> foreingKeys) {
		this.foreignKeys.addAll(foreingKeys);
		this.rep = null;
	}

	/**
	 * Gets the foreign key at the specified index.
	 *
	 * @param index int
	 * @return ForeignKey
	 */
	public ForeignKey getForeignKey(int index) {
		return this.foreignKeys.get(index);
	}

	/**
	 * Gets the list of all foreign keys.
	 *
	 * @return List<ForeignKey>
	 */
	public List<ForeignKey> getForeignKeys() {
		return this.foreignKeys;
	}

	/**
	 * Gets the id of the relation.
	 *
	 * @return int
	 */
	public int getId() {
		return this.relationId;
	}

	/**
	 * Adds an access method to this relation.
	 *
	 * @param bm AccessMethod
	 */
	public void addAccessMethod(AccessMethod bm) {
		for (int b : bm.getInputs()) {
			if (!(1 <= b && b <= this.attributes.size())) {
				throw new IllegalArgumentException(
						"Attempting to instantiation a relation with inconsistent binding method.");
			}
		}
		if (this.accessMethods.put(bm.getName(), bm) == null) {
			this.amView.add(bm);
		}
		this.rep = null;
	}

	/**
	 * Sets all access methods of this relation, eliminating all prexisting ones if clearFirst is true. 
	 * If the flag clearFirst is false it just adds the input access methods to the list of access methods.
	 *
	 * @param bindingMethods List<AccessMethod>
	 * @param clearFirst boolean
	 */
	public void setAccessMethods(List<AccessMethod> bindingMethods, boolean clearFirst) {
		if (clearFirst) {
			this.accessMethods.clear();
			this.amView.clear();
			this.rep = null;
		}
		if (bindingMethods != null) {
			for (AccessMethod bm : bindingMethods) {
				this.addAccessMethod(bm);
			}
		}
	}

	/**
	 * TOCOMMMENT this uses a wrong naming convention. Since clearFirst is false, this method *adds* to the list of existing access methods, it does not *set*
	 * Sets the access methods.
	 *
	 * @param bindingMethods List<AccessMethod>
	 */
	public void setAccessMethods(List<AccessMethod> bindingMethods) {
		this.setAccessMethods(bindingMethods, false);
	}

	/**
	 * Gets the access method with the corrsponding name.
	 *
	 * @param name String
	 * @return AccessMethod
	 */
	public AccessMethod getAccessMethod(String name) {
		return this.accessMethods.get(name);
	}

	/**
	 * TOCOMMENT creates predicate (so the name of the method should be Atom- singular), used where??
	 * Creates the atoms.
	 *
	 * @return an atom corresponding to this relation.
	 */
	public DatabasePredicate createAtoms() {
		List<Term> variableTerms = new ArrayList<>();
		for (Attribute attribute : this.attributes) {
			variableTerms.add(new Variable(attribute.getName()));
		}
		return new DatabasePredicate(this, variableTerms);
	}

	/**
	 * TOCOMMENT here we can maintain a field so not search again all access methods
	 * Checks if this relation supports any free access method.
	 *
	 * @return Boolean
	 */
	public Boolean hasFreeAccess() {
		if (this.accessMethods != null) {
			for (AccessMethod binding: this.accessMethods.values()) {
				if (binding.getType() == Types.FREE) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * TOCOMMENT primary? same comment applies in two methods below
	 * Sets the key.
	 *
	 * @param key the new key
	 */
	public void setKey(List<Attribute> key) {
		this.key = key;
	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public List<Attribute> getKey() {
		return this.key;
	}

	/**
	 * Gets the key positions.
	 *
	 * @return the key positions
	 */
	public List<Integer> getKeyPositions() {
		if(this.keyPositions == null) {
			this.keyPositions = Lists.newArrayList();
			for(Attribute key:this.key) {
				this.keyPositions.add(this.attributes.indexOf(key));
			}
		}
		return this.keyPositions;
	}

	/**
	 * TOCOMMENT Two methods are equal if, by using the corresponding equals methods, their names are equals, their attributes are equal, and their amView ???? are equal.
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return Relation.class.isInstance(o)
				&& this.name.equals(((Relation) o).name)
				&& this.attributes.equals(((Relation) o).attributes)
				&& this.amView.equals(((Relation) o).amView);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.attributes, this.amView);
	}

	@Override
	public String toString() {
		if (this.rep == null) {
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
			this.rep = result.toString();
		}
		return this.rep;
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
