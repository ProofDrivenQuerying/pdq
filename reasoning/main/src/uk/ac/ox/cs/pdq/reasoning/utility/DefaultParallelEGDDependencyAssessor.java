package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;

import com.beust.jcommander.internal.Sets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public final class DefaultParallelEGDDependencyAssessor implements ParallelEGDDependencyAssessor{

	private Collection<Predicate> stateFacts = null;

	private final Multimap<String, EGD> egdMap = ArrayListMultimap.create();

	private final Multimap<String, TGD> tgdMap = ArrayListMultimap.create();
	
	private boolean firstEGDRound = true;
	
	private boolean firstTGDRound = true;
	

	public DefaultParallelEGDDependencyAssessor(Collection<? extends Constraint> dependencies) {
		Preconditions.checkNotNull(dependencies);
		//Build the dependency map
		for(Constraint dependency:dependencies) {
			for(Predicate predicate:dependency.getLeft().getPredicates()) {
				Signature s = predicate.getSignature();
				if(dependency instanceof EGD) {
					this.egdMap.put(s.getName(), (EGD) dependency);
				}
				else if(dependency instanceof TGD) {
					this.tgdMap.put(s.getName(), (TGD) dependency);
				}
			}
		}
	}
	
	@Override
	public Collection<? extends Constraint> getDependencies(ChaseState state, EGDROUND round) {
		Collection<Constraint> constraints = Sets.newHashSet();
		Collection<Predicate> newFacts = null;
		if(this.stateFacts == null || (round.equals(EGDROUND.EGD) && this.firstEGDRound == true) || 
				(round.equals(EGDROUND.TGD) && this.firstTGDRound == true)) {
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
		
		if(round.equals(EGDROUND.EGD)) {
			this.firstEGDRound = false;
		}
		else {
			this.firstTGDRound = false;
		}
		
		return constraints;
	}

}
