package uk.ac.ox.cs.pdq.planner.reasoning.chase.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.ExtendedBag;
import uk.ac.ox.cs.pdq.reasoning.chase.Bag;
import uk.ac.ox.cs.pdq.reasoning.chase.BagBoundPredicate;
import uk.ac.ox.cs.pdq.reasoning.chase.BagsTree;
import uk.ac.ox.cs.pdq.reasoning.chase.BagsTreeEdge;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.collect.Lists;

/**
 * Implements the MergeTrees interface
 * @author Efthymia Tsamoura
 *
 */
public class MergeTreesImplementation implements MergeTrees {

	/**
	 * Facts propagated to other bags during merging
	 */
	private final Collection<Predicate> copiedFacts = new LinkedHashSet<>();

	/**
	 * Bags which got new facts during merging
	 */
	private final Collection<ExtendedBag> updatedBags = new LinkedHashSet<>();

	/** Queries and updates the facts' database**/
	private final HomomorphismDetector detector;

	/**
	 * Constructor for MergeTreesImplementation.
	 * @param detector HomomorphismDetector
	 */
	public MergeTreesImplementation(HomomorphismDetector detector) {
		this.detector = detector;
	}
	/**
	 *
	 * @param target
	 * 		The target tree
	 * @param targetRoot
	 * 		The root of the target tree
	 * @param source
	 * 		The input tree
	 * @param sourceRoot
	 * 		The root of the input tree
	 * @return
	 * 		The bags of the input tree that were propagated to the target tree
	 */
	private List<ExtendedBag> propagateChildren(BagsTree target,
			ExtendedBag targetRoot,
			BagsTree source,
			ExtendedBag sourceRoot) {
		List<ExtendedBag> propagated = Lists.newArrayList();
		//For each child of sourceRoot
		Set<DefaultEdge> edges = source.outgoingEdgesOf(sourceRoot);
		for(DefaultEdge edge:edges) {
			//Add it to the target tree
			Bag toAdd = source.getEdgeTarget(edge);
			propagated.addAll(this.copySubgraph(target, targetRoot, source, toAdd));
		}
		return propagated;
	}


	/**
	 * Adds the subgraph of source rooted at sourceRoot to the target tree
	 * @param target
	 * @param targetRoot
	 * @param source
	 * @param sourceRoot
	 * @return the list of propagated bags
	 */
	private List<ExtendedBag> copySubgraph(BagsTree target,
			ExtendedBag targetRoot,
			BagsTree source,
			Bag sourceRoot) {
		ExtendedBag clonedRoot  = new ExtendedBag((ExtendedBag) sourceRoot);
		List<ExtendedBag> propagated = Lists.newArrayList(clonedRoot);
		target.addVertex(clonedRoot);
		target.addEdge(targetRoot, clonedRoot, new BagsTreeEdge(targetRoot, clonedRoot, null));

		Set<DefaultEdge> edges = source.outgoingEdgesOf(clonedRoot);
		for(DefaultEdge edge:edges) {
			ExtendedBag toAdd = (ExtendedBag) source.getEdgeTarget(edge);
			propagated.addAll(this.copySubgraph(target, clonedRoot, source, toAdd));
		}
		return propagated;
	}



	/**
	 * @param source Input bag
	 * @param target Target bags (of another tree)
	 * @return the target bags having constants a superset of the constants of the input bag
	 */
	private List<ExtendedBag> getSuperBags(ExtendedBag source, Collection<ExtendedBag> target) {
		List<ExtendedBag> superBags = new ArrayList<>();
		for(ExtendedBag bag:target) {
			if(bag.containsTerms(source.getConstants())) {
				superBags.add(bag);
			}
		}
		return superBags;
	}

	/**
	 * @param target Input bag
	 * @param facts
	 * @return facts bound to the input bag
	 */
	private Collection<BagBoundPredicate> getBagBoundFacts(ExtendedBag target, Collection<Predicate> facts) {
		Collection<BagBoundPredicate> bagBoundFacts = new LinkedHashSet<>();
		for(Predicate fact: facts) {
			bagBoundFacts.add(new BagBoundPredicate(fact, target.getId()));
		}
		return bagBoundFacts;
	}


	/**
	 * Deletes the subsumed bags, moves their children and copies their bags below/to other bags
	 * @param target
	 * 		The target tree
	 * @param children0
	 * 		The bags of the first input tree
	 * @param children1
	 * 		The bags of the second input tree
	 * @return
	 * 		the facts that were propagated to other bags during the subsumption loop.
	 */
	private Pair<Collection<BagBoundPredicate>,Collection<ExtendedBag>> subsume(BagsTree target,
			List<ExtendedBag> children0,
			List<ExtendedBag> children1) {

		Collection<BagBoundPredicate> copiedFacts = new LinkedHashSet<>();
		Collection<ExtendedBag> updatedBags = new LinkedHashSet<>();

		Iterator<ExtendedBag> iterator = children0.iterator();

		int index = 0;
		while(iterator.hasNext()) {
			ExtendedBag b = iterator.next();

			//Let V be the set of bags in the other tree from b which have constants a superset
			//of the constants of bag b,
			List<ExtendedBag> V = this.getSuperBags(b, children1);

			if(!V.isEmpty()) {
				//Choose a v in V whose constants are maximal
				ExtendedBag v = V.get(0);
				//For each fact F of b add F to v
				v.addFacts(b.getFacts());
				copiedFacts.addAll(this.getBagBoundFacts(v, b.getFacts()));
				updatedBags.add(v);

				Iterator<DefaultEdge> edgeIter = target.outgoingEdgesOf(b).iterator();
				while(edgeIter.hasNext()){
					DefaultEdge edge = edgeIter.next();
					//For each child bag c of b;
					ExtendedBag c = (ExtendedBag) target.getEdgeTarget(edge);
					target.removeEdge(edge);
					//Change the parent pointer of c to point to  v
					if(target.getEdge(v, c) == null) {
						target.addEdge(v, c, new DefaultEdge());
					}
					edgeIter = target.outgoingEdgesOf(b).iterator();
				}


				//Propagate copied facts in other tree
				//For each fact F of b
				//For each bag b' in the same tree as v whose guard subsumes F
				//Copy F to b'
				for(int i = 1; i < V.size(); ++i) {
					Collection<BagBoundPredicate> copied = V.get(i).copyFacts(b);
					if(copied.isEmpty()) {
						copiedFacts.addAll(copied);
						updatedBags.add(V.get(i));
					}
				}

				//Delete b
				target.removeVertex(b);
				children0.remove(index);
				iterator = children0.iterator();
				index = 0;

			}
			else {
				++index;
			}
		}
		return Pair.of(copiedFacts, updatedBags);

	}

	/**
	 * @param tree1 BagsTree
	 * @param tree2 BagsTree
	 * @return Pair<BagsTree,ExtendedBag>
	 * @see uk.ac.ox.cs.pdq.chase.state.MergeTrees#merge(BagsTree, BagsTree)
	 */
	@Override
	public Pair<BagsTree, ExtendedBag> merge(
			BagsTree tree1,
			BagsTree tree2
			) {
		//Create a new root R'' by merging the facts in R and R',
		BagsTree target = new BagsTree(DefaultEdge.class);
		ExtendedBag targetRoot = ((ExtendedBag)tree1.getRoot()).merge((ExtendedBag) tree2.getRoot(), this.detector);
		this.updatedBags.add(targetRoot);

		this.copiedFacts.addAll(this.getBagBoundFacts(targetRoot, tree2.getRoot().getFacts()));
		this.copiedFacts.addAll(this.getBagBoundFacts(targetRoot, tree1.getRoot().getFacts()));

		target.addVertex(targetRoot);

		//Delete R and R' and promote their children to be children of R''
		List<ExtendedBag> children0 = this.propagateChildren(target, targetRoot, tree1, (ExtendedBag) tree1.getRoot());
		List<ExtendedBag> children1 = this.propagateChildren(target, targetRoot, tree2, (ExtendedBag) tree2.getRoot());

		//ExtendedBag subsumption loop
		Pair<Collection<BagBoundPredicate>, Collection<ExtendedBag>> pair0 = this.subsume(target,  children0, children1);
		this.copiedFacts.addAll(pair0.getLeft());
		this.updatedBags.addAll(pair0.getRight());

		Pair<Collection<BagBoundPredicate>, Collection<ExtendedBag>> pair1 = this.subsume(target,  children1, children0);
		this.copiedFacts.addAll(pair1.getLeft());
		this.updatedBags.addAll(pair1.getRight());

		//Let U_b be the set of bags in the "other tree" which have guards that subsume some facts of b
		//If U_b <>  \emptyset, then
		//For every bag u in U_b for every fact F that appears in b, and is subsumed by the guard of u
		//Add F to u;
		for(ExtendedBag b0:children0) {
			for(ExtendedBag b1:children1) {
				Collection<BagBoundPredicate> copied = b1.copyFacts(b0);
				if(!copied.isEmpty()) {
					this.copiedFacts.addAll(copied);
					this.updatedBags.add(b1);
				}
			}
		}

		for(ExtendedBag b1:children1) {
			for(ExtendedBag b0:children0) {
				Collection<BagBoundPredicate> copied = b0.copyFacts(b1);
				if(!copied.isEmpty()) {
					this.copiedFacts.addAll(copied);
					this.updatedBags.add(b0);
				}

			}
		}
		return Pair.of(target, targetRoot);
	}

	/**
	 * @return Collection<PredicateFormula>
	 */
	public Collection<Predicate> getCopiedFacts() {
		return this.copiedFacts;
	}

	/**
	 * @return Collection
	 */
	public Collection<ExtendedBag> getUpdatedBags() {
		return this.updatedBags;
	}

}
