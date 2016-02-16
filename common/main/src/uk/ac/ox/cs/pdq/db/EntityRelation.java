package uk.ac.ox.cs.pdq.db;

import java.util.List;

import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
//Efi: This class seems strange to me.
//Consider deleting
/**
 * Entity relation are unary relation that also act as types.
 * @author Julien Leblay
 */
public class EntityRelation extends Relation implements DataType {

	/** The attribute. */
	private final Attribute attribute;
	
	/** The attributes. */
	private final List<Attribute> attributes;
	
	/**
	 * Construction for an inaccessible entity relation.
	 *
	 * @param name the name
	 */
	public EntityRelation(String name) {
		super(name, Lists.<Attribute>newArrayList(new Attribute(EntityRelation.class, "_")),
				ImmutableList.<AccessMethod>of(),
				ImmutableList.<ForeignKey>of(), false);
		this.attribute = new Attribute(this, "_");
		this.attributes = ImmutableList.of(this.attribute);
	}

	/**
	 * Constructor for an accessible rentity relation.
	 *
	 * @param name the name
	 * @param am the am
	 */
	public EntityRelation(String name, Types am) {
		super(name, Lists.<Attribute>newArrayList(new Attribute(EntityRelation.class, "_")),
				(am == Types.FREE
				? ImmutableList.<AccessMethod>of(new AccessMethod("_", Types.FREE, ImmutableList.<Integer>of())) :
				ImmutableList.<AccessMethod>of(new AccessMethod("_", am, ImmutableList.of(1)))),
				ImmutableList.<ForeignKey>of(), false);
		this.attribute = new Attribute(this, "_");
		this.attributes = ImmutableList.of(this.attribute);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.db.Relation#getAttributes()
	 */
	@Override
	public List<Attribute> getAttributes() {
		return this.attributes;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.db.Relation#getAttribute(int)
	 */
	@Override
	public Attribute getAttribute(int i) {
		return this.attributes.get(i);
	}
	
	/**
	 * Convenience method to get the unique attribute of an entity relation.
	 * @return the unique attribute of an entity relation.
	 */
	public Attribute getAttribute() {
		return this.attribute;
	}
	
	/**
	 * Convenience method to get the unique access method of an entity relation.
	 * @return the unique access method of the entity relation, or null if the
	 * relation is inaccessible.
	 */
	public AccessMethod getAccessMethod() {
		return super.getAccessMethod("_");
	}

	/**
	 * Gets the type.
	 *
	 * @return the entity relation's type, i.e. the current instance.
	 */
	public TupleType getType() {
		return TupleType.DefaultFactory.create(this);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.db.DataType#isAssignableFrom(java.lang.Object)
	 */
	@Override
	public boolean isAssignableFrom(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof DatabasePredicate)) {
			return false;
		}
		return this.equals(((DatabasePredicate) o).getSignature());
	}
}
