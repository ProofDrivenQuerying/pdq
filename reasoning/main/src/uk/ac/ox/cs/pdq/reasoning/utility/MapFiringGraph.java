package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Predicate;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Implementation of a FiringGraph
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */

public class MapFiringGraph implements FiringGraph{

	/**
	 * Keeps for each dependency, the facts that were used to fire it, as well as, the consequence facts
	 */
	private final MultiValueMap<Collection<Predicate>, Collection<Predicate>> map;

	/**
	 * Chase graph
	 */
	private final Graph<Predicate, DefaultEdge> graph;

	/**
	 * Fact history map. Associates each chase fact with the facts and the dependency that were last fired to produce this fact
	 */
	private final Map<Predicate, Pair<Constraint, Collection<Predicate>>> provenance;

	/**
	 * Keeps the set of facts that were used to fire each dependency
	 */
	private final Multimap<Collection<Predicate>, Constraint> firings;

	public MapFiringGraph() {
		this.map = new MultiValueMap<>();
		this.provenance = new LinkedHashMap<>();
		this.firings = LinkedHashMultimap.create();
		this.graph = new SimpleGraph<Predicate, DefaultEdge>(DefaultEdge.class);
	}

	/**
	 * Constructor for MapFiringGraph.
	 * @param map MultiValueMap<Collection<PredicateFormula>,Collection<PredicateFormula>>
	 * @param graph Graph<PredicateFormula,DefaultEdge>
	 * @param factProvenance Map<PredicateFormula,Pair<IC,Collection<PredicateFormula>>>
	 * @param firedDependencies Multimap<Collection<PredicateFormula>,IC>
	 */
	private MapFiringGraph(
			MultiValueMap<Collection<Predicate>, Collection<Predicate>> map,
			Graph<Predicate, DefaultEdge> graph,
			Map<Predicate, Pair<Constraint, Collection<Predicate>>> factProvenance,
			Multimap<Collection<Predicate>, Constraint> firedDependencies) {
		this.map = map;
		this.graph = graph;
		this.provenance = factProvenance;
		this.firings = firedDependencies;
	}

	/**
	 * @param dependency IC
	 * @param sources Collection<PredicateFormula>
	 * @param targets Collection<PredicateFormula>
	 * @param initialState boolean
	 * @see uk.ac.ox.cs.pdq.chase.FiringGraph#put(IC, Collection<PredicateFormula>, Collection<PredicateFormula>, boolean)
	 */
	@Override
	public void put(Constraint dependency, Collection<Predicate> sources, Collection<Predicate> targets) {
		for (Predicate fact:targets) {
			if (!this.provenance.containsKey(fact)) {
				this.provenance.put(fact, Pair.of(dependency, sources));
			}
		}
		this.firings.put(sources, dependency);
		this.map.put(sources, targets);
		this.updateGraph(sources, targets);
	}

	/**
	 * @param dependency IC
	 * @param source PredicateFormula
	 * @param target PredicateFormula
	 * @param initialState boolean
	 * @see uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph#put(Constraint, Predicate, Predicate, boolean)
	 */
	@Override
	public void put(Constraint dependency, Predicate source, Predicate target) {
		this.put(dependency, Sets.newHashSet(source), Sets.newHashSet(target));
	}

	/**
	 * @return MapFiringGraph
	 * @see uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph#clone()
	 */
	@Override
	public MapFiringGraph clone() {
		Multimap<Collection<Predicate>, Constraint> firedDependencies = LinkedHashMultimap.create();
		firedDependencies.putAll(this.firings);
		Map<Predicate, Pair<Constraint, Collection<Predicate>>> factProvenance = Maps.newLinkedHashMap(this.getFactProvenance());
		return new MapFiringGraph(this.map, this.graph, factProvenance, firedDependencies);
	}

	/**
	 * @return Graph<PredicateFormula,DefaultEdge>
	 * @see uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph#getGraph()
	 */
	@Override
	public Graph<Predicate, DefaultEdge> getGraph() {
		return this.graph;
	}

	/**
	 * @param sources Collection<PredicateFormula>
	 * @param targets Collection<PredicateFormula>
	 */
	private void updateGraph(Collection<Predicate> sources, Collection<Predicate> targets) {
		for(Predicate source:sources) {
			this.graph.addVertex(source);
			for(Predicate target: targets) {
				this.graph.addVertex(target);
				try {
					this.graph.addEdge(source, target); 
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param graph0 Graph<PredicateFormula,DefaultEdge>
	 * @return Graph<PredicateFormula,DefaultEdge>
	 */
	private Graph<Predicate, DefaultEdge> clone(Graph<Predicate, DefaultEdge> graph0) {
		Graph<Predicate, DefaultEdge> graph = new SimpleGraph<Predicate, DefaultEdge>(DefaultEdge.class);
		for(Predicate vertex:graph0.vertexSet()) {
			graph.addVertex(vertex);
		}
		for(DefaultEdge edge:graph0.edgeSet()) {
			graph.addEdge(graph0.getEdgeSource(edge), graph0.getEdgeTarget(edge));
		}
		return graph;
	}

	/**
	 * @param graph0 Graph<PredicateFormula,DefaultEdge>
	 * @param graph1 Graph<PredicateFormula,DefaultEdge>
	 * @return Graph<PredicateFormula,DefaultEdge>
	 */
	private Graph<Predicate, DefaultEdge> merge(Graph<Predicate, DefaultEdge> graph0, Graph<Predicate, DefaultEdge> graph1) {
		Graph<Predicate, DefaultEdge> graph = new SimpleGraph<Predicate, DefaultEdge>(DefaultEdge.class);
		for(Predicate vertex:graph0.vertexSet()) {
			graph.addVertex(vertex);
		}
		for(Predicate vertex:graph1.vertexSet()) {
			graph.addVertex(vertex);
		}
		for(DefaultEdge edge:graph0.edgeSet()) {
			graph.addEdge(graph0.getEdgeSource(edge), graph0.getEdgeTarget(edge));
		}
		for(DefaultEdge edge:graph1.edgeSet()) {
			graph.addEdge(graph1.getEdgeSource(edge), graph1.getEdgeTarget(edge));
		}
		return graph;
	}

	/**
	 * @param fact PredicateFormula
	 * @return Pair<IC,Collection<PredicateFormula>>
	 * @see uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph#getFactProvenance(Predicate)
	 */
	@Override
	public Pair<Constraint, Collection<Predicate>> getFactProvenance(Predicate fact) {
		return this.provenance.get(fact);
	}

	/**
	 * @param dependency IC
	 * @param facts Collection<PredicateFormula>
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.chase.FiringGraph#isFired(IC, Collection<PredicateFormula>)
	 */
	@Override
	public boolean isFired(Constraint dependency, Collection<Predicate> facts) {
		return this.firings.get(facts).contains(dependency);
	}

	/**
	 * @param source FiringGraph
	 * @return FiringGraph
	 * @see uk.ac.ox.cs.pdq.reasoning.utility.chase.FiringGraph#merge(FiringGraph)
	 */
	@Override
	public FiringGraph merge(FiringGraph source) {
		Preconditions.checkArgument(source instanceof MapFiringGraph);

		Multimap<Collection<Predicate>, Constraint> firedDependencies = LinkedHashMultimap.create();
		firedDependencies.putAll(this.firings);
		firedDependencies.putAll(((MapFiringGraph)source).getFiredDependencies());

		Map<Predicate, Pair<Constraint, Collection<Predicate>>> factProvenance = Maps.newLinkedHashMap(this.getFactProvenance());
		factProvenance.putAll(((MapFiringGraph)source).getFactProvenance());

		return new MapFiringGraph(this.map, this.graph, factProvenance, firedDependencies);
	}

	/**
	 * @return MultiValueMap<Collection<PredicateFormula>,Collection<PredicateFormula>>
	 */
	public MultiValueMap<Collection<Predicate>, Collection<Predicate>> getMap() {
		return this.map;
	}

	/**
	 * @return Map<PredicateFormula,Pair<IC,Collection<PredicateFormula>>>
	 * @see uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph#getFactProvenance()
	 */
	@Override
	public Map<Predicate, Pair<Constraint, Collection<Predicate>>> getFactProvenance() {
		return this.provenance;
	}

	/**
	 * @return Multimap<Collection<PredicateFormula>,IC>
	 */
	public Multimap<Collection<Predicate>, Constraint> getFiredDependencies() {
		return this.firings;
	}

	public Set<Collection<Predicate>> getPreconditions() {
		return this.map.keySet();
	}

	public Collection<Collection<Predicate>> getConsequences(Collection<Predicate> key) {
		return this.map.getCollection(key);
	}

}
