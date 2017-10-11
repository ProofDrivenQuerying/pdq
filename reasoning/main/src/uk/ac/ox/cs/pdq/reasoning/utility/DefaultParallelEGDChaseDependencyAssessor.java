package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

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
public final class DefaultParallelEGDChaseDependencyAssessor implements ParallelEGDChaseDependencyAssessor{

	/**  The facts of this database instance*. */
	private Collection<Atom> stateFacts = null;

	/** Maps of predicate names to EGDs. Given an EGD \delta = \sigma --> x_i = x_j, we 
	 * create a new entry in the map for each R in \sigma **/
	private final Multimap<String, EGD> egdMap = ArrayListMultimap.create();

	/** Maps of predicate names to EGDs. Given a TGD \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
	 * we create a new entry in the map for each R in \sigma **/
	private final Multimap<String, TGD> tgdMap = ArrayListMultimap.create();
	
	/**  True if it is the first time we will perform an EGD round*. */
	private boolean firstEGDRound = true;
	
	/**  True if it is the first time we will perform a TGD round*. */
	private boolean firstTGDRound = true;
	

	/**
	 * Instantiates a new default parallel egd chase dependency assessor.
	 *
	 * @param dependencies the dependencies
	 */
	public DefaultParallelEGDChaseDependencyAssessor(Dependency[] dependencies) {
		Preconditions.checkNotNull(dependencies);
		//Build the dependency map
		for(Dependency dependency:dependencies) {
			for(int bodyAtomIndex = 0; bodyAtomIndex < dependency.getNumberOfBodyAtoms(); ++bodyAtomIndex) {
				Atom atom = dependency.getBodyAtom(bodyAtomIndex);
				Predicate s = atom.getPredicate();
				if(dependency instanceof EGD) 
					this.egdMap.put(s.getName(), (EGD) dependency);
				else if(dependency instanceof TGD) 
					this.tgdMap.put(s.getName(), (TGD) dependency);
			}
		}
	}
	
	/**
	 * Gets the dependencies.
	 *
	 * @param state 		A collection of chase facts
	 * @param round the round
	 * @return 		the dependencies that are most likely to be fired in the next chase round.
	 */
	@Override
	public Dependency[] getDependencies(ChaseInstance state, EGDROUND round) {
		Collection<Dependency> constraints = Sets.newHashSet();
		Collection<Atom> newFacts = null;
		if(this.stateFacts == null || (round.equals(EGDROUND.EGD) && this.firstEGDRound == true) || 
				(round.equals(EGDROUND.TGD) && this.firstTGDRound == true)) 
			newFacts = state.getFacts();
		else 
			newFacts = CollectionUtils.subtract(state.getFacts(), this.stateFacts);
		
		Multimap<String, Atom> newFactsMap = ArrayListMultimap.create();
		for(Atom fact:newFacts) 
			newFactsMap.put(fact.getPredicate().getName(), fact);
		
		Multimap<String, Atom> allFactsMap = ArrayListMultimap.create();
		for(Atom fact:state.getFacts()) {
			allFactsMap.put(fact.getPredicate().getName(), fact);
		}
		//for each fact
		for(String factName:newFactsMap.keySet()) {
			Collection<TGD> tgds = this.tgdMap.get(factName);
			if(round.equals(EGDROUND.TGD) && tgds!=null) {
				constraints.addAll(tgds);
			}
			Collection<EGD> egds = this.egdMap.get(factName);
			if(round.equals(EGDROUND.EGD) && egds!=null && allFactsMap.get(factName).size() > 1) {
				constraints.addAll(egds);
			}
		}
		
		this.stateFacts = Sets.newLinkedHashSet();
		this.stateFacts.addAll(state.getFacts());
		
		if(round.equals(EGDROUND.EGD)) 
			this.firstEGDRound = false;
		else 
			this.firstTGDRound = false;
		
		return constraints.toArray(new Dependency[constraints.size()]);
	}

}
