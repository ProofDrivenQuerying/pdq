package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.fol.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 *
 * A relational schema
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Schema {

	/** Relations */
	private final Map<String, Relation> relIndex;
	protected final List<Relation> relations;

	/** Distribution of relations by arity */
	private final List<Relation>[] arityDistribution;

	/** Tuple generating dependencies */
	private final Map<Integer, Constraint> dependencyIndex;

	protected final List<Constraint> schemaDependencies;

	/** True if the schema contains at least one view */
	private final boolean containsViews;

	/** True if the schema contains cycles*/
	protected Boolean isCyclic = null;

	/** Constants that appear to the schema dependencies*/
	protected Collection<TypedConstant<?>> dependencyConstants = null;

	/** A map of the string representation of a constant to the constant*/
	protected Map<String, TypedConstant<?>> constants = new LinkedHashMap<>();
	
	protected final Collection<EGD> keyDependencies = Lists.newArrayList();

	protected final List<Constraint> views;

	public Schema() {
		this(new ArrayList<Relation>(), new ArrayList<Constraint>());
	}

	/**
	 * Constructor for Schema.
	 * @param relations Collection<Relation>
	 */
	public Schema(Collection<Relation> relations) {
		this(relations, new ArrayList<Constraint>());
	}

	/**
	 * Schema constructor
	 * @param relations
	 * 		The input relations
	 * @param dependencies
	 * 		The input dependencies
	 */
	public Schema(Collection<Relation> relations, Collection<Constraint> dependencies) {
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

		Map<Integer, Constraint> dm = new LinkedHashMap<>();
		for (Constraint ic:dependencies) {
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
	
	public void consolidateKeys() {
		for(Relation relation:this.relations) {
			if(!relation.getKey().isEmpty()) {
				this.keyDependencies.add(EGD.getEGDs(relation, relation.getKey()));
			}
		}
	}

	/**
	 * @return List<IC>
	 */
	public List<Constraint> getViews() {
		return this.views;
	}

	/**
	 * @return boolean
	 */
	public boolean containsViews() {
		return this.containsViews;
	}

	/**
	 * @param i int
	 * @return List<Relation>
	 */
	public List<Relation> getRelationsByArity(int i) {
		return this.arityDistribution[i];
	}

	/**
	 * @return List<Relation>
	 */
	public List<Relation> getRelations() {
		return this.relations;
	}

	/**
	 * @return int
	 */
	public int getMaxArity() {
		return this.arityDistribution.length;
	}

	/**
	 * @return List<IC>
	 */
	public List<Constraint> getDependencies() {
		return this.schemaDependencies;
	}
	
	public Collection<EGD> getKeyDependencies() {
		return this.keyDependencies;
	}

	/**
	 * @param relationName String
	 * @return Relation
	 */
	public Relation getRelation(String relationName) {
		return this.relIndex.get(relationName);
	}

	/**
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
			for (Constraint ic : this.schemaDependencies) {
				result.append("\n\t\t").append(ic);
			}
			result.append("\n\t}");
		}
		result.append("\n}");
		return result.toString();
	}

	/**
	 * @return true if the schema contains cycles
	 */
	public boolean isCyclic() {
		if (this.isCyclic == null) {
			DirectedGraph<Predicate, DefaultEdge> simpleDepedencyGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
			for (Relation relation:this.relations) {
				simpleDepedencyGraph.addVertex(relation.createAtoms());
			}
			for (Constraint ic:this.schemaDependencies) {
				List<Predicate> leftAtoms = ic.getLeft().getPredicates();
				List<Predicate> rightAtoms = ic.getRight().getPredicates();
				for (Predicate left : leftAtoms) {
					for (Predicate right : rightAtoms) {
						Predicate leftVertex = this.searchDependencyGraph(simpleDepedencyGraph, left);
						Predicate rightVertex = this.searchDependencyGraph(simpleDepedencyGraph, right);
						simpleDepedencyGraph.addEdge(leftVertex, rightVertex);
					}
				}
			}
			CycleDetector<Predicate, DefaultEdge> cycleDetector = new CycleDetector<>(simpleDepedencyGraph);
			this.isCyclic = cycleDetector.detectCycles();
		}
		return this.isCyclic;
	}

	/**
	 * @param simpleDepedencyGraph A schema dependency graph
	 * @param atom An input atom
	 * @return the atom which has the same predicate with the input one
	 */
	private Predicate searchDependencyGraph(
			DirectedGraph<Predicate, DefaultEdge> simpleDepedencyGraph,
			Predicate atom) {
		for (Predicate vertex: simpleDepedencyGraph.vertexSet()) {
			if (atom.getName().equals(vertex.getName())) {
				return vertex;
			}
		}
		return null;
	}

	/**
	 * @return the constants appearing in the schema dependencies
	 */
	public Collection<TypedConstant<?>> getDependencyConstants() {
		if (this.dependencyConstants == null) {
			this.dependencyConstants = new LinkedHashSet<>();
			for (Constraint ic : this.schemaDependencies) {
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
	 * Updates the map of dependency constants
	 * @param constants Collection<TypedConstant<?>>
	 */
	public void updateConstants(Collection<TypedConstant<?>> constants) {
		for (TypedConstant<?> constant: constants) {
			this.constants.put(constant.toString(), constant);
		}
	}

	/**
	 * @return Map<String,TypedConstant<?>>
	 */
	public Map<String, TypedConstant<?>> getConstants() {
		return this.constants;
	}

	/**
	 * @param constant String
	 * @return TypedConstant<?>
	 */
	public TypedConstant<?> getConstant(String constant) {
		return this.constants.get(constant);
	}

	/**
	 * @param relation
	 * @return true if the given relation is part of the schema.
	 */
	public boolean contains(String relationName) {
		return this.relIndex.containsKey(relationName);
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
	 * @return a new schema builder
	 */
	public static SchemaBuilder builder() {
		return new SchemaBuilder();
	}

	/**
	 * @param schema
	 * @return a new schema builder containing all the relations and
	 *         dependencies already in the given schema
	 */
	public static SchemaBuilder builder(Schema schema) {
		return new SchemaBuilder(schema);
	}
}
