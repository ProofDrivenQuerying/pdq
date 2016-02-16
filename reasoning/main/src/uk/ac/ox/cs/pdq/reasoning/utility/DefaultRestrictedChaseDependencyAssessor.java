package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;

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

public final class DefaultRestrictedChaseDependencyAssessor implements RestrictedChaseDependencyAssessor{

	/**  The facts of this database instance*. */
	private Collection<Predicate> stateFacts = null;

	/** Maps of predicate names to EGDs. Given an EGD \delta = \sigma --> x_i = x_j, we 
	 * create a new entry in the map for each R in \sigma **/
	private final Multimap<String, EGD> egdMap = ArrayListMultimap.create();

	/** Maps of predicate names to EGDs. Given a TGD \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
	 * we create a new entry in the map for each R in \sigma **/
	private final Multimap<String, TGD> tgdMap = ArrayListMultimap.create();
	
	/**  All schema dependencies *. */
	private final Collection<Constraint> dependencies;

	/**
	 * Instantiates a new default restricted chase dependency assessor.
	 *
	 * @param dependencies the dependencies
	 */
	public DefaultRestrictedChaseDependencyAssessor(Collection<? extends Constraint> dependencies) {
		Preconditions.checkNotNull(dependencies);
		this.dependencies = Lists.newArrayList();
		List<Constraint> egds = Lists.newArrayList();
		List<Constraint> tgds = Lists.newArrayList();
		
		//Build the dependency map
		for(Constraint dependency:dependencies) {
			for(Predicate predicate:dependency.getLeft().getPredicates()) {
				Signature s = predicate.getSignature();
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
	public Collection<? extends Constraint> getDependencies(ChaseState state) {
		Collection<Constraint> constraints = Sets.newLinkedHashSet();
		Collection<Predicate> newFacts = null;
		if(this.stateFacts == null) {
			newFacts = state.getFacts();
		}
		else {
			newFacts = CollectionUtils.subtract(state.getFacts(), this.stateFacts);
		}
		
		Multimap<String, Predicate> newFactsMap = ArrayListMultimap.create();
		for(Predicate fact:newFacts) {
			newFactsMap.put(fact.getSignature().getName(), fact);
		}
		
		Multimap<String, Predicate> allFactsMap = ArrayListMultimap.create();
		for(Predicate fact:state.getFacts()) {
			allFactsMap.put(fact.getSignature().getName(), fact);
		}
		
		for(Constraint dependency:this.dependencies) {
			for(Predicate predicate:dependency.getLeft().getPredicates()) {
				Signature s = predicate.getSignature();
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
