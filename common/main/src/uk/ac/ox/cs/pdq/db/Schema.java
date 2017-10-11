
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

	/** Relations indexed based on their name. */
	private final Map<String, Relation> relationsMap;

	/** The list of schema relations*. */
	protected final Relation[] relations;

	/** The schema dependencies. */
	protected final Dependency[] nonEgdDependencies;

	/** A map from a constant's name to the constant object. */
	protected final Map<String, TypedConstant> constants = new LinkedHashMap<>();

	/** The EGDs of the keys*. */
	protected final EGD[] egdDependencies;

	/**
	 * Builds a schema with the input relations.
	 *
	 * @param relations
	 *            the relations
	 */
	public Schema(Relation[] relations) {
		this(relations, new Dependency[] {});
	}

	/**
	 * Builds a schema with the input relations and dependencies.
	 *
	 * @param relations
	 *            The input relations
	 * @param mixedDependencies
	 *            The input dependencies
	 */
	public Schema(Relation[] relations, Dependency[] mixedDependencies) {
		this.relations = new Relation[relations.length];
		this.relationsMap = new LinkedHashMap<>();
		int relationIndex = 0;
		for (Relation relation : relations) {
			this.relations[relationIndex++] = relation;
			this.relationsMap.put(relation.getName(), relation);
		}

		// Separate mixed dependencies into two groups: EGDs and everything else.
		List<Dependency> nonEgdDependenciesList = new ArrayList<>();
		List<Dependency> egdDependenciesList = new ArrayList<>();
		for (Dependency dependency : mixedDependencies) {
			if (dependency instanceof EGD) {
				egdDependenciesList.add(dependency);
			} else {
				nonEgdDependenciesList.add(dependency);
			}
		}
		if (nonEgdDependenciesList.size() > 0) {
			this.nonEgdDependencies = nonEgdDependenciesList.toArray(new Dependency[nonEgdDependenciesList.size()]);
		} else {
			this.nonEgdDependencies = new EGD[0];
		}
		if (egdDependenciesList.size() > 0) {
			this.egdDependencies = egdDependenciesList.toArray(new EGD[egdDependenciesList.size()]);
		} else {
			this.egdDependencies = new EGD[0];
		}

		// This will be done outside of the class.
		// List<EGD> EGDs = new ArrayList<>();
		// for(Relation relation:this.relations) {
		// if(relation.getKey() != null)
		// EGDs.add(Utility.getEGDs(relation, relation.getKey().getAttributes()));
		// }
		// this.egdDependencies = EGDs.toArray(new EGD[EGDs.size()]);

		for (Dependency dependency : this.nonEgdDependencies) {
			for (TypedConstant constant : Utility.getTypedConstants(dependency)) {
				this.constants.put(constant.toString(), constant);
			}
		}
	}

	/**
	 * Gets all schema relations.
	 *
	 * @return all schema relations
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
	 * @return the schema dependencies
	 */
	public Dependency[] getDependencies() {
		return this.nonEgdDependencies.clone();
	}

	/**
	 * Gets the primary-key dependencies.
	 *
	 * @return the EGDs that come from the relations keys
	 */
	public EGD[] getKeyDependencies() {
		return this.egdDependencies.clone();
	}

	/**
	 * Gets the relation with the specified name.
	 *
	 * @param name
	 *            the name
	 * @return the relation with the input name
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
		if (this.nonEgdDependencies.length > 0 || this.egdDependencies.length > 0) {
			result.append("\n\t{");
			if (this.nonEgdDependencies.length > 0) {
				for (Dependency ic : this.nonEgdDependencies) {
					result.append("\n\t\t").append(ic);
				}
			}
			if (this.egdDependencies.length > 0) {
				for (Dependency ic : this.egdDependencies) {
					result.append("\n\t\t").append(ic);
				}
			}
			result.append("\n\t}");
		}
		result.append("\n}");
		return result.toString();
	}

	/**
	 * Updates the schema constants with the input map.
	 *
	 * @param constants
	 *            the constants
	 */
	public void addConstants(Collection<TypedConstant> constants) {
		for (TypedConstant constant : constants)
			this.constants.put(constant.toString(), constant);
	}

	/**
	 *
	 * @return the schema constants
	 */
	public Map<String, TypedConstant> getConstants() {
		return this.constants;
	}

	/**
	 *
	 * @param name
	 *            the name
	 * @return the constant with the given name
	 */
	public TypedConstant getConstant(String name) {
		return this.constants.get(name);
	}

	/**
	 * Checks if the schema contains a relation.
	 *
	 * @param name
	 *            the name
	 * @return true if the given relation is part of the schema.
	 */
	public boolean contains(String name) {
		return this.relationsMap.containsKey(name);
	}

}
