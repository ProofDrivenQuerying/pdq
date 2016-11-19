package uk.ac.ox.cs.pdq.db;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * 
 * @author Rachel.Eaton
 * Class represents one table of a relational database
 */
public class MainMemoryRelation extends Relation{
	
	protected Set<Atom> tuples = new HashSet<Atom>();
	
		/**
	 * Constructs a new Relation.
	 *
	 * @param name Relation's name
	 * @param attributes List<? extends Attribute>
	 * @param accessMethods Relation's binding methods
	 * @param foreignKeys List<ForeignKey>
	 */
	public MainMemoryRelation(String name, List<? extends Attribute> attributes, List<AccessMethod> accessMethods, List<ForeignKey> foreignKeys) {
		super(name, attributes, accessMethods, foreignKeys);

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
	public MainMemoryRelation(String name, List<? extends Attribute> attributes, List<AccessMethod> accessMethods, List<ForeignKey> foreignKeys,
			boolean isEquality) {
		super(name, attributes,accessMethods, foreignKeys, isEquality);

	}

	/**
	 * Constructs a new relation.
	 *
	 * @param name Relation's name
	 * @param attributes List<? extends Attribute>
	 * @param accessMethods Relation's binding methods
	 */
	public MainMemoryRelation(String name, List<? extends Attribute> attributes, List<AccessMethod> accessMethods) {
		super(name, attributes, accessMethods);
	}

	/**
	 * Constructs a new relation..
	 *
	 * @param name Relation's name
	 * @param attributes List<? extends Attribute>
	 * @param accessMethods Relation's binding methods
	 * @param isEquality the is equality
	 */
	public MainMemoryRelation(String name, List<? extends Attribute> attributes, List<AccessMethod> accessMethods, boolean isEquality) {
		super(name, attributes, accessMethods, isEquality);

	}

	/**
	 * Constructs a new relation.
	 *
	 * @param name Relation's name
	 * @param attributes List<? extends Attribute>
	 * @param isEquality the is equality
	 */
	public MainMemoryRelation(String name, List<? extends Attribute> attributes, boolean isEquality) {
		super(name, attributes, isEquality);

	}

	/**
	 * Constructs a new relation.
	 *
	 * @param name Relation's name
	 * @param attributes List<? extends Attribute>
	 */
	public MainMemoryRelation(String name, List<? extends Attribute> attributes) {
		super(name, attributes);
		
	}

	/**
	 * Copy constructor.
	 *
	 * @param r Relation
	 */
	public MainMemoryRelation(Relation r) {
		super(r.getName(), r.getAttributes(), r.getAccessMethods(),	r.getForeignKeys(), r.isEquality());

	}

	public String getName(  ){
		return this.name;
	}
	/** Adds a tuple to the table 
	 * TODO Add checks to ensure that the tuple matches the table type
	 **/
	public void addTuple(Atom tuple){
		if( tuple.getTerms().size() == this.attributes.size()){
			this.tuples.add(tuple);
		} else {
			throw new IllegalStateException("Arity of tuple does not match arity of relation");
		}
	}
	
	/** Returns set of all tuples in table **/
	public Set<Atom> getTuples(){
		return this.tuples;
	}
	
	/** Drops the tuple specified from the table **/
	public void dropTuple(Atom tuple){
		this.tuples.remove(tuple);
	}
	
	public void dropTuples( List<Atom> tuples){
		for( Atom tuple: tuples){
			this.dropTuple(tuple);
		}
	}
	
	/** Drops all tuples from the table **/
	public void dropAll(){
		this.tuples = null;
	}
	
	public String toString(){
		String temp = new String(this.getName()+"\n");
		for( Atom tuple : this.tuples ){
			temp += tuple.toString()+"\n";
		}
		return temp;
		
	}


}
