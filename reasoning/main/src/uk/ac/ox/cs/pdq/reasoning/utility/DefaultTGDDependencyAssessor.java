package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Sets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

// TODO: Auto-generated Javadoc
/**
	Finds for each chase round which dependencies
	are most likely to be fired and returns those dependencies.
	It works as follows: after
	each rule firing this class keeps track of the generated facts. After a chase round is
	completed it returns all the dependencies that have in their left-hand side at
	least one atom with predicate that matches one of the predicates in the
	generated facts.
*    
* @author Efthymia Tsamoura
*
*/

public final class DefaultTGDDependencyAssessor implements TGDDependencyAssessor{

	/**  The facts of this database instance*. */
	private Collection<Atom> stateFacts = null;

	/** Maps of predicate names to EGDs. Given an EGD \delta = \sigma --> x_i = x_j, we 
	 * create a new entry in the map for each R in \sigma **/
	private final Multimap<String, EGD> egdMap = ArrayListMultimap.create();

	/** Maps of predicate names to EGDs. Given a TGD \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
	 * we create a new entry in the map for each R in \sigma **/
	private final Multimap<String, TGD> tgdMap = ArrayListMultimap.create();
	
	/**  All schema dependencies *. */
	private final Collection<Dependency> dependencies;

	/**
	 * Instantiates a new default restricted chase dependency assessor.
	 *
	 * @param dependencies the dependencies
	 */
	public DefaultTGDDependencyAssessor(Collection<? extends Dependency> dependencies) {
		Preconditions.checkNotNull(dependencies);
		this.dependencies = Lists.newArrayList();
		List<Dependency> egds = Lists.newArrayList();
		List<Dependency> tgds = Lists.newArrayList();
		
		//Build the dependency map
		for(Dependency dependency:dependencies) {
			for(Atom atom:dependency.getLeft().getAtoms()) {
				Predicate s = atom.getPredicate();
				if(dependency instanceof EGD) {
					this.egdMap.put(s.getName(), (EGD) dependency);
					egds.add(dependency);
				}
				else if(dependency instanceof TGD) {
					this.tgdMap.put(s.getName(), (TGD) dependency);
					tgds.add(dependency);
				}
			}
		}
		this.dependencies.addAll(egds);
		this.dependencies.addAll(tgds);
	}

	/**
	 * Gets the dependencies.
	 *
	 * @param state 		A collection of chase facts
	 * @return 		the dependencies that are most likely to be fired in the next chase round.
	 */
	@Override
	public Collection<? extends Dependency> getDependencies(ChaseInstance state) {
		Collection<Dependency> constraints = Sets.newLinkedHashSet();
		Collection<Atom> newFacts = null;
		if(this.stateFacts == null) {
			newFacts = state.getFacts();
		}
		else {
			newFacts = CollectionUtils.subtract(state.getFacts(), this.stateFacts);
		}
		
		Multimap<String, Atom> newFactsMap = ArrayListMultimap.create();
		for(Atom fact:newFacts) {
			newFactsMap.put(fact.getPredicate().getName(), fact);
		}
		
		Multimap<String, Atom> allFactsMap = ArrayListMultimap.create();
		for(Atom fact:state.getFacts()) {
			allFactsMap.put(fact.getPredicate().getName(), fact);
		}
		
		for(Dependency dependency:this.dependencies) {
			for(Atom atom:dependency.getLeft().getAtoms()) {
				Predicate s = atom.getPredicate();
				if(dependency instanceof TGD && newFactsMap.keySet().contains(s.getName())) {
					constraints.add(dependency);
					break;
				}
				
				if(dependency instanceof EGD && newFactsMap.keySet().contains(s.getName()) && allFactsMap.get(s.getName()).size() > 1) {
					constraints.add(dependency);
					break;
				}
			}
		}
		this.stateFacts = Sets.newLinkedHashSet();
		this.stateFacts.addAll(state.getFacts());
		return constraints;
	}

}
