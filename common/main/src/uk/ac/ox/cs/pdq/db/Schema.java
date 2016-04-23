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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * A database schema.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Schema {

	/**  Relations indexed based on their name. */
	private final Map<String, Relation> relIndex;
	
	/**  The list of schema relations*. */
	protected final List<Relation> relations;

	/**  Distribution of relations by arity. */
	private final List<Relation>[] arityDistribution;

	/**  The schema dependencies indexed based on their id. */
	private final Map<Integer, Dependency> dependencyIndex;

	/**  The schema dependencies. */
	protected final List<Dependency> schemaDependencies;

	/**  True if the schema contains at least one view. */
	private final boolean containsViews;

	/**  True if the schema contains cycles. */
	protected Boolean isCyclic = null;

	/**  Schema constants. */
	protected Collection<TypedConstant<?>> dependencyConstants = null;

	/**  A map from a constant's name to the constant object. */
	protected Map<String, TypedConstant<?>> constants = new LinkedHashMap<>();
	
	/**  The EGDs of the keys*. */
	protected final Collection<EGD> keyDependencies = Lists.newArrayList();

	/**  The views of the input schema*. */
	protected final List<Dependency> views;

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
		boolean containsViews = false;
		Map<String, Relation> rm = new LinkedHashMap<>();
		this.views = new ArrayList<>();
		for (Relation relation : relations) {
			rm.put(relation.getName(), relation);
			if (maxArity < relation.getArity()) {
				maxArity = relation.getArity();
			}
			containsViews |= relation instanceof View;
			if(relation instanceof View) {
				this.views.add(((View) relation).getDependency());
			}
		}
		this.containsViews = containsViews;

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
				this.keyDependencies.add(EGD.getEGDs(relation, relation.getKey()));
			}
		}
		this.schemaDependencies = ImmutableList.copyOf(this.dependencyIndex.values());
		this.loadConstants();
	}
	
	/**
	 * Extracts the EGDs of the relation keys.
	 */
	public void consolidateKeys() {
		for(Relation relation:this.relations) {
			if(!relation.getKey().isEmpty()) {
				this.keyDependencies.add(EGD.getEGDs(relation, relation.getKey()));
			}
		}
	}

	/**
	 * Gets the views.
	 *
	 * @return 		the schema views
	 */
	public List<Dependency> getViews() {
		return this.views;
	}


	/**
	 * Contains views.
	 *
	 * @return 		true if the schema contains views
	 */
	public boolean containsViews() {
		return this.containsViews;
	}

	/**
	 * Gets the relations by arity.
	 *
	 * @param i the i
	 * @return 		all relations having the input arity
	 */
	public List<Relation> getRelationsByArity(int i) {
		return this.arityDistribution[i];
	}

	/**
	 * Gets the relations.
	 *
	 * @return 		all schema relations
	 */
	public List<Relation> getRelations() {
		return this.relations;
	}

	/**
	 * Gets the max arity.
	 *
	 * @return 		the maximum relation arity
	 */
	public int getMaxArity() {
		return this.arityDistribution.length;
	}

	/**
	 * Gets the dependencies.
	 *
	 * @return 		the schema dependencies
	 */
	public List<Dependency> getDependencies() {
		return this.schemaDependencies;
	}
	
	/**
	 * Gets the key dependencies.
	 *
	 * @return 		the EGDs that come from the relations keys
	 */
	public Collection<EGD> getKeyDependencies() {
		return this.keyDependencies;
	}

	/**
	 * Gets the relation.
	 *
	 * @param name the name
	 * @return 		the relation with the input name
	 */
	public Relation getRelation(String name) {
		return this.relIndex.get(name);
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
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
	 * Checks if is cyclic.
	 *
	 * @return true if the schema contains cycles
	 */
	public boolean isCyclic() {
		if (this.isCyclic == null) {
			DirectedGraph<Atom, DefaultEdge> simpleDepedencyGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
			for (Relation relation:this.relations) {
				simpleDepedencyGraph.addVertex(relation.createAtoms());
			}
			for (Dependency ic:this.schemaDependencies) {
				List<Atom> leftAtoms = ic.getLeft().getAtoms();
				List<Atom> rightAtoms = ic.getRight().getAtoms();
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
			if (atom.getName().equals(vertex.getName())) {
				return vertex;
			}
		}
		return null;
	}

	/**
	 * Gets the dependency constants.
	 *
	 * @return the constants of the schema dependencies
	 */
	public Collection<TypedConstant<?>> getDependencyConstants() {
		if (this.dependencyConstants == null) {
			this.dependencyConstants = new LinkedHashSet<>();
			for (Dependency ic : this.schemaDependencies) {
				this.dependencyConstants.addAll(ic.getSchemaConstants());
			}
		}
		return this.dependencyConstants;
	}

	/**
	 * Creates a map of the constants that appear in the schema dependencies.
	 */
	private void loadConstants() {
		for (TypedConstant<?> constant: this.getDependencyConstants()) {
			this.constants.put(constant.toString(), constant);
		}
	}
	
	/**
	 * Updates the schema constants with the input map.
	 *
	 * @param constants the constants
	 */
	public void updateConstants(Collection<TypedConstant<?>> constants) {
		for (TypedConstant<?> constant: constants) {
			this.constants.put(constant.toString(), constant);
		}
	}

	/**
	 * Gets the constants.
	 *
	 * @return 		the schema constants
	 */
	public Map<String, TypedConstant<?>> getConstants() {
		return this.constants;
	}


	/**
	 * Gets the constant.
	 *
	 * @param name the name
	 * @return 		the constant with the given name
	 */
	public TypedConstant<?> getConstant(String name) {
		return this.constants.get(name);
	}

	/**
	 * Contains.
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
	 * Builder.
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
