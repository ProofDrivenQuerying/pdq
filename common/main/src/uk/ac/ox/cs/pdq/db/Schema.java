package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.SchemaAdapter;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * A database schema.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
@XmlRootElement
@XmlJavaTypeAdapter(SchemaAdapter.class)
public class Schema {

	/**  Relations indexed based on their name. */
	private final Map<String, Relation> relationsMap;

	/**  The list of schema relations*. */
	protected final Relation[] relations;

	/**  The schema dependencies. */
	protected final Dependency[] dependencies;

	/**  A map from a constant's name to the constant object. */
	protected Map<String, TypedConstant> constants;

	/**  The EGDs of the keys*. */
	protected final EGD[] keyDependencies;

	/**
	 * Builds a schema with the input relations.
	 *
	 * @param relations the relations
	 */
	public Schema(Relation[] relations) {
		this(relations, new Dependency[]{});
	}

	/**
	 * Builds a schema with the input relations	and dependencies.
	 *
	 * @param relations 		The input relations
	 * @param dependencies 		The input dependencies
	 */
	public Schema(Relation[] relations, Dependency[] dependencies) {
		this.relations = new Relation[relations.length];
		this.relationsMap = new LinkedHashMap<>();
		int relationIndex = 0;
		for(Relation relation:relations) {
			this.relations[relationIndex++] = relation;
			this.relationsMap.put(relation.getName(), relation);
		}
		
		int dependencyIndex = 0;
		this.dependencies = new Dependency[dependencies.length];
		for(Dependency dependency:dependencies) 
			this.dependencies[dependencyIndex++] = dependency;
		
		List<EGD> EGDs = new ArrayList<>();
		for(Relation relation:this.relations) {
			if(relation.getKey() != null) 
				EGDs.add(Utility.getEGDs(relation, relation.getKey().getAttributes()));
		}
		this.keyDependencies = EGDs.toArray(new EGD[EGDs.size()]);

		for (Dependency dependency:this.dependencies) {
			for (TypedConstant constant: Utility.getTypedConstants(dependency)) {
				if(this.constants == null)
					this.constants = new LinkedHashMap<>();
				this.constants.put(constant.toString(), constant);
			}				
		}
	}

	/**
	 * Gets all schema relations.
	 *
	 * @return 		all schema relations
	 */
	public Relation[] getRelations() {
		return this.relations.clone();
	}
	
	public Relation getRelation(int index) {
		return this.relations[index];
	}
	
	public int getNumberOfRelations() {
		return this.relations.length;
	}

	/**
	 * Gets the schema dependencies.
	 *
	 * @return 		the schema dependencies
	 */
	public Dependency[] getDependencies() {
		return this.dependencies.clone();
	}

	/**
	 * Gets the (TOCOMMENT primary) key dependencies.
	 *
	 * @return 		the EGDs that come from the relations keys
	 */
	public EGD[] getKeyDependencies() {
		return this.keyDependencies.clone();
	}

	/**
	 * Gets the relation with the specified name.
	 *
	 * @param name the name
	 * @return 		the relation with the input name
	 */
	public Relation getRelation(String name) {
		return this.relationsMap.get(name);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('{');
		if (this.relations.length > 0) {
			result.append("\n\t{");
			for (Relation r : this.relations) {
				result.append("\n\t\t").append(r);
			}
			result.append("\n\t}");
		}
		if (this.dependencies.length > 0) {
			result.append("\n\t{");
			for (Dependency ic : this.dependencies) {
				result.append("\n\t\t").append(ic);
			}
			result.append("\n\t}");
		}
		result.append("\n}");
		return result.toString();
	}

	/**
	 * Updates the schema constants with the input map.
	 *
	 * @param constants the constants
	 */
	public void addConstants(Collection<TypedConstant> constants) {
		for (TypedConstant constant: constants) {
			if(this.constants == null)
				this.constants = new LinkedHashMap<>();
			this.constants.put(constant.toString(), constant);
		}
	}

	/**
	 *
	 * @return 		the schema constants
	 */
	public Map<String, TypedConstant> getConstants() {
		return this.constants;
	}

	/**
	 *
	 * @param name the name
	 * @return 		the constant with the given name
	 */
	public TypedConstant getConstant(String name) {
		return this.constants.get(name);
	}

	/**
	 * Checks if the schema contains a relation.
	 *
	 * @param name the name
	 * @return true if the given relation is part of the schema.
	 */
	public boolean contains(String name) {
		return this.relationsMap.containsKey(name);
	}
	
}
