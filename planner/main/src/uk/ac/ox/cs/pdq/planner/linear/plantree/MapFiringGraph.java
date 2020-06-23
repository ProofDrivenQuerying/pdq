// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear.plantree;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;

/**
 * Implementation of a FiringGraph.
 *
 * @author Efthymia Tsamoura
 */

public class MapFiringGraph implements FiringGraph{

	/** Keeps for each dependency, the facts that were used to fire it, as well as, the consequence facts. */
	private final MultiValueMap<Collection<Atom>, Collection<Atom>> map;

	/** Chase graph. */
	private final Graph<Atom, DefaultEdge> graph;

	/**
	 * Fact history map. Associates each chase fact with the facts and the dependency that were last fired to produce this fact
	 */
	private final Map<Atom, Pair<Dependency, Collection<Atom>>> provenance;

	/** Keeps the set of facts that were used to fire each dependency. */
	private final Multimap<Collection<Atom>, Dependency> firings;

	/**
	 * Instantiates a new map firing graph.
	 */
	public MapFiringGraph() {
		this.map = new MultiValueMap<>();
		this.provenance = new LinkedHashMap<>();
		this.firings = LinkedHashMultimap.create();
		this.graph = new SimpleGraph<Atom, DefaultEdge>(DefaultEdge.class);
	}

	/**
	 * Constructor for MapFiringGraph.
	 * @param map MultiValueMap<Collection<PredicateFormula>,Collection<PredicateFormula>>
	 * @param graph Graph<PredicateFormula,DefaultEdge>
	 * @param factProvenance Map<PredicateFormula,Pair<IC,Collection<PredicateFormula>>>
	 * @param firedDependencies Multimap<Collection<PredicateFormula>,IC>
	 */
	private MapFiringGraph(
			MultiValueMap<Collection<Atom>, Collection<Atom>> map,
			Graph<Atom, DefaultEdge> graph,
			Map<Atom, Pair<Dependency, Collection<Atom>>> factProvenance,
			Multimap<Collection<Atom>, Dependency> firedDependencies) {
		this.map = map;
		this.graph = graph;
		this.provenance = factProvenance;
		this.firings = firedDependencies;
	}

	/**
	 * Insert into the graph
	 *
	 * @param dependency IC
	 * @param sources Collection<PredicateFormula>
	 * @param targets Collection<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.chase.FiringGraph#put(IC, Collection<PredicateFormula>, Collection<PredicateFormula>, boolean)
	 */
	@Override
	public void put(Dependency dependency, Collection<Atom> sources, Collection<Atom> targets) {
		for (Atom fact:targets) {
			if (!this.provenance.containsKey(fact)) {
				this.provenance.put(fact, Pair.of(dependency, sources));
			}
		}
		this.firings.put(sources, dependency);
		this.map.put(sources, targets);
		this.updateGraph(sources, targets);
	}

	/**
	 * Variation of put for singleton inputs rather than collections
	 *
	 * @param dependency IC
	 * @param source PredicateFormula
	 * @param target PredicateFormula
	 * @see uk.ac.ox.cs.pdq.planner.linear.plantree.FiringGraph#put(Dependency, Atom, Atom)
	 */
	@Override
	public void put(Dependency dependency, Atom source, Atom target) {
		this.put(dependency, Sets.newHashSet(source), Sets.newHashSet(target));
	}

	/**
	 * Clone.
	 *
	 * @return MapFiringGraph
	 * @see uk.ac.ox.cs.pdq.planner.linear.plantree.FiringGraph#clone()
	 */
	@Override
	public MapFiringGraph clone() {
		Multimap<Collection<Atom>, Dependency> firedDependencies = LinkedHashMultimap.create();
		firedDependencies.putAll(this.firings);
		Map<Atom, Pair<Dependency, Collection<Atom>>> factProvenance = Maps.newLinkedHashMap(this.getFactProvenance());
		return new MapFiringGraph(this.map, this.graph, factProvenance, firedDependencies);
	}

	/**
	 * Gets the graph.
	 *
	 * @return Graph<PredicateFormula,DefaultEdge>
	 * @see uk.ac.ox.cs.pdq.planner.linear.plantree.FiringGraph#getGraph()
	 */
	@Override
	public Graph<Atom, DefaultEdge> getGraph() {
		return this.graph;
	}

	/**
	 * Update graph.
	 *
	 * @param sources Collection<PredicateFormula>
	 * @param targets Collection<PredicateFormula>
	 */
	private void updateGraph(Collection<Atom> sources, Collection<Atom> targets) {
		for(Atom source:sources) {
			this.graph.addVertex(source);
			for(Atom target: targets) {
				if(!source.equals(target)) {
					this.graph.addVertex(target);
					try {
						this.graph.addEdge(source, target); 
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Clone.
	 *
	 * @param graph0 Graph<PredicateFormula,DefaultEdge>
	 * @return Graph<PredicateFormula,DefaultEdge>
	 */
	protected Graph<Atom, DefaultEdge> clone(Graph<Atom, DefaultEdge> graph0) {
		Graph<Atom, DefaultEdge> graph = new SimpleGraph<Atom, DefaultEdge>(DefaultEdge.class);
		for(Atom vertex:graph0.vertexSet()) {
			graph.addVertex(vertex);
		}
		for(DefaultEdge edge:graph0.edgeSet()) {
			graph.addEdge(graph0.getEdgeSource(edge), graph0.getEdgeTarget(edge));
		}
		return graph;
	}

	/**
	 * Merge.
	 *
	 * @param graph0 Graph<PredicateFormula,DefaultEdge>
	 * @param graph1 Graph<PredicateFormula,DefaultEdge>
	 * @return Graph<PredicateFormula,DefaultEdge>
	 */
	protected Graph<Atom, DefaultEdge> merge(Graph<Atom, DefaultEdge> graph0, Graph<Atom, DefaultEdge> graph1) {
		Graph<Atom, DefaultEdge> graph = new SimpleGraph<Atom, DefaultEdge>(DefaultEdge.class);
		for(Atom vertex:graph0.vertexSet()) {
			graph.addVertex(vertex);
		}
		for(Atom vertex:graph1.vertexSet()) {
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
	 * Gets the fact's provenance.
	 *
	 * @param fact PredicateFormula
	 * @return Pair<IC,Collection<PredicateFormula>>
	 * @see uk.ac.ox.cs.pdq.planner.linear.plantree.FiringGraph#getFactProvenance(Atom)
	 */
	@Override
	public Pair<Dependency, Collection<Atom>> getFactProvenance(Atom fact) {
		return this.provenance.get(fact);
	}

	/**
	 * Checks if is fired.
	 *
	 * @param dependency IC
	 * @param facts Collection<PredicateFormula>
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.chase.FiringGraph#isFired(IC, Collection<PredicateFormula>)
	 */
	@Override
	public boolean isFired(Dependency dependency, Collection<Atom> facts) {
		return this.firings.get(facts).contains(dependency);
	}

	/**
	 * Merge.
	 *
	 * @param source FiringGraph
	 * @return FiringGraph
	 * @see uk.ac.ox.cs.pdq.planner.linear.plantree.chase.FiringGraph#merge(FiringGraph)
	 */
	@Override
	public FiringGraph merge(FiringGraph source) {
		Preconditions.checkArgument(source instanceof MapFiringGraph);

		Multimap<Collection<Atom>, Dependency> firedDependencies = LinkedHashMultimap.create();
		firedDependencies.putAll(this.firings);
		firedDependencies.putAll(((MapFiringGraph)source).getFiredDependencies());

		Map<Atom, Pair<Dependency, Collection<Atom>>> factProvenance = Maps.newLinkedHashMap(this.getFactProvenance());
		factProvenance.putAll(((MapFiringGraph)source).getFactProvenance());

		return new MapFiringGraph(this.map, this.graph, factProvenance, firedDependencies);
	}

	/**
	 * Gets the map.
	 *
	 * @return MultiValueMap<Collection<PredicateFormula>,Collection<PredicateFormula>>
	 */
	public MultiValueMap<Collection<Atom>, Collection<Atom>> getMap() {
		return this.map;
	}

	/**
	 * Gets the fact's provenance.
	 *
	 * @return Map<PredicateFormula,Pair<IC,Collection<PredicateFormula>>>
	 * @see uk.ac.ox.cs.pdq.planner.linear.plantree.FiringGraph#getFactProvenance()
	 */
	@Override
	public Map<Atom, Pair<Dependency, Collection<Atom>>> getFactProvenance() {
		return this.provenance;
	}

	/**
	 * Gets the fired dependencies.
	 *
	 * @return Multimap<Collection<PredicateFormula>,IC>
	 */
	public Multimap<Collection<Atom>, Dependency> getFiredDependencies() {
		return this.firings;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph#getPreconditions()
	 */
	public Set<Collection<Atom>> getPreconditions() {
		return this.map.keySet();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph#getConsequences(java.util.Collection)
	 */
	public Collection<Collection<Atom>> getConsequences(Collection<Atom> key) {
		return this.map.getCollection(key);
	}

}
