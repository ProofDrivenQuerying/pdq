package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * A database schema.
 *
 * @author George K
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Schema {

	/**  Relations indexed based on their name. */
	private final Map<String, Relation> relIndex;
	
	/**  The list of schema relations*. */
	protected final List<Relation> relations;

	/**
	 * For every different arity, this keeps the list of schema relations with this arity
	 *  Distribution of relations by arity. */
	private final List<Relation>[] arityDistribution;

	/**  The schema dependencies indexed based on their id. */
	private final Map<Integer, Dependency> dependencyIndex;

	/**  The schema dependencies. */
	protected final List<Dependency> schemaDependencies;

	/**  
	 * True if the schema dependencies contain cycles. */
	protected Boolean isCyclic = null;

	/**  
	 * Schema constants, are all constants appearing in the dependencies */
	protected Collection<TypedConstant<?>> dependencyConstants = null;

	/**  A map from a constant's name to the constant object. */
	protected Map<String, TypedConstant<?>> typedConstants = new LinkedHashMap<>();
	
	/**  The EGDs of (TOCOMMENT: corresponding to?) the keys*. */
	protected final Collection<EGD> keyDependencies = Lists.newArrayList();

	/**
	 * Empty schema constructor.
	 */
	public Schema() {
		this(new ArrayList<Relation>(), new ArrayList<Dependency>());
	}

	/**
	 * Builds a schema with the input relations.
	 *
	 * @param relations the relations
	 */
	public Schema(Collection<Relation> relations) {
		this(relations, new ArrayList<Dependency>());
	}

	/**
	 * Builds a schema with the input relations	and dependencies.
	 *
	 * @param relations 		The input relations
	 * @param dependencies 		The input dependencies
	 */
	public Schema(Collection<Relation> relations, Collection<Dependency> dependencies) {
		int maxArity = 0;
		Map<String, Relation> rm = new LinkedHashMap<>();
		this.arityDistribution = new List[maxArity + 1];
		for (int i = 0, l = this.arityDistribution.length; i < l; i++) {
			this.arityDistribution[i] = new ArrayList<>();
		}
		for (Relation r:relations) {
			this.arityDistribution[r.getArity()].add(r);
		}

		Map<Integer, Dependency> dm = new LinkedHashMap<>();
		for (Dependency ic:dependencies) {
			if (ic instanceof TGD) {
				dm.put(((TGD) ic).getId(), ic);
			}
		}
		this.relIndex = ImmutableMap.copyOf(rm);
		this.dependencyIndex = ImmutableMap.copyOf(dm);
		this.relations = ImmutableList.copyOf(this.relIndex.values());
		
		for(Relation relation:this.relations) {
			if(!relation.getKey().isEmpty()) {
				this.keyDependencies.add(Utility.getEGDs(relation, relation.getKey()));
			}
		}
		this.schemaDependencies = ImmutableList.copyOf(this.dependencyIndex.values());
		this.loadTypedConstants();
	}
	
	/**
	 * TOCOMMENT I don't find "consolidateKeys" or "EGD.getEGDs" very appropriate names
	 * Extracts the EGDs of the relation keys.
	 */
	public void consolidateKeys() {
		for(Relation relation:this.relations) {
			if(!relation.getKey().isEmpty()) {
				this.keyDependencies.add(Utility.getEGDs(relation, relation.getKey()));
			}
		}
	}

	/**
	 * Gets the relations that have the input arity.
	 *
	 * @param i the i
	 * @return 		all relations having the input arity
	 */
	public List<Relation> getRelationsByArity(int i) {
		return this.arityDistribution[i];
	}

	/**
	 * Gets all schema relations.
	 *
	 * @return 		all schema relations
	 */
	public List<Relation> getRelations() {
		return this.relations;
	}

	/**
	 * Gets the max arity of any relation.
	 *
	 * @return 		the maximum relation arity
	 */
	public int getMaxArity() {
		return this.arityDistribution.length;
	}

	/**
	 * Gets the schema dependencies.
	 *
	 * @return 		the schema dependencies
	 */
	public List<Dependency> getDependencies() {
		return this.schemaDependencies;
	}
	
	/**
	 * Gets the (TOCOMMENT primary) key dependencies.
	 *
	 * @return 		the EGDs that come from the relations keys
	 */
	public Collection<EGD> getKeyDependencies() {
		return this.keyDependencies;
	}

	/**
	 * Gets the relation with the specified name.
	 *
	 * @param name the name
	 * @return 		the relation with the input name
	 */
	public Relation getRelation(String name) {
		return this.relIndex.get(name);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('{');
		if (!this.relations.isEmpty()) {
			result.append("\n\t{");
			for (Relation r : this.relations) {
				result.append("\n\t\t").append(r);
			}
			result.append("\n\t}");
		}
		if (!this.schemaDependencies.isEmpty()) {
			result.append("\n\t{");
			for (Dependency ic : this.schemaDependencies) {
				result.append("\n\t\t").append(ic);
			}
			result.append("\n\t}");
		}
		result.append("\n}");
		return result.toString();
	}

	/**
	 * Checks if is the dependencies are cyclic.
	 *
	 * @return true if the schema dependencies contain cycles
	 */
	public boolean isCyclic() {
		if (this.isCyclic == null) {
			DirectedGraph<Atom, DefaultEdge> simpleDepedencyGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
			for (Relation relation:this.relations) {
				simpleDepedencyGraph.addVertex(relation.createAtoms());
			}
			for (Dependency dependency:this.schemaDependencies) {
				List<Atom> leftAtoms = dependency.getBody().getAtoms();
				List<Atom> rightAtoms = dependency.getHead().getAtoms();
				for (Atom left : leftAtoms) {
					for (Atom right : rightAtoms) {
						Atom leftVertex = this.searchDependencyGraph(simpleDepedencyGraph, left);
						Atom rightVertex = this.searchDependencyGraph(simpleDepedencyGraph, right);
						simpleDepedencyGraph.addEdge(leftVertex, rightVertex);
					}
				}
			}
			CycleDetector<Atom, DefaultEdge> cycleDetector = new CycleDetector<>(simpleDepedencyGraph);
			this.isCyclic = cycleDetector.detectCycles();
		}
		return this.isCyclic;
	}

	/**
	 * TOCOMMENT not an appropriate method name
	 * Search dependency graph.
	 *
	 * @param simpleDepedencyGraph A schema dependency graph
	 * @param atom An input atom
	 * @return the atom which has the same predicate with the input one
	 */
	private Atom searchDependencyGraph(
			DirectedGraph<Atom, DefaultEdge> simpleDepedencyGraph,
			Atom atom) {
		for (Atom vertex: simpleDepedencyGraph.vertexSet()) {
			if (atom.getPredicate().getName().equals(vertex.getPredicate().getName())) {
				return vertex;
			}
		}
		return null;
	}

	/**
	 * Gets the dependency constants.
	 * 
	 * TOCOMMENT what is the difference with getSchemaConstants?
	 *
	 * @return the constants of the schema dependencies
	 */
	public Collection<TypedConstant<?>> getDependencyTypedConstants() {
		if (this.dependencyConstants == null) {
			this.dependencyConstants = new LinkedHashSet<>();
			for (Dependency dependency:this.schemaDependencies) {
				this.dependencyConstants.addAll(Utility.getTypedConstants(dependency));
			}
		}
		return this.dependencyConstants;
	}

	/**
	 * TOCOMMENT I'm not sure what this is but two different constants (say integer 5, and a string with value "5") 
	 * will end up in the same position (one of them will overwrite the entry of the other in this map)
	 * TOCOMMENT Once we figure what this map does, we need to update the comments for 4 methods below.
	 * Creates a map of the constants that appear in the schema dependencies.
	 */
	private void loadTypedConstants() {
		for (TypedConstant<?> constant: this.getDependencyTypedConstants()) {
			this.typedConstants.put(constant.toString(), constant);
		}
	}
	
	/**
	 * Updates the schema constants with the input map.
	 *
	 * @param constants the constants
	 */
	public void updateTypedConstants(Collection<TypedConstant<?>> constants) {
		for (TypedConstant<?> constant: constants) {
			this.typedConstants.put(constant.toString(), constant);
		}
	}

	/**
	 * Gets the constants.
	 *
	 * @return 		the schema constants
	 */
	public Map<String, TypedConstant<?>> getTypedConstants() {
		return this.typedConstants;
	}


	/**
	 * Gets the constant.
	 *
	 * @param name the name
	 * @return 		the constant with the given name
	 */
	public TypedConstant<?> getConstant(String name) {
		return this.typedConstants.get(name);
	}

	/**
	 * Checks if the schema contains a relation.
	 *
	 * @param name the name
	 * @return true if the given relation is part of the schema.
	 */
	public boolean contains(String name) {
		return this.relIndex.containsKey(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.relations.equals(((Schema) o).relations)
				&& this.schemaDependencies.equals(((Schema) o).schemaDependencies);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.relations, this.schemaDependencies);
	}

	/**
	 * Instantiates a new SchemaBuilder.
	 *
	 * @return a new schema builder
	 */
	public static SchemaBuilder builder() {
		return new SchemaBuilder();
	}

	/**
	 * Builder.
	 *
	 * @param schema the schema
	 * @return a new schema builder containing all the relations and
	 *         dependencies already in the given schema
	 */
	public static SchemaBuilder builder(Schema schema) {
		return new SchemaBuilder(schema);
	}
}
