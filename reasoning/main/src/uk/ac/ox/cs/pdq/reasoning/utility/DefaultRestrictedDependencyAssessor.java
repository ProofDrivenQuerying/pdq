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

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public final class DefaultRestrictedDependencyAssessor implements RestrictedDependencyAssessor{

	private Collection<Predicate> stateFacts = null;

	private final Multimap<String, EGD> egdMap = ArrayListMultimap.create();

	private final Multimap<String, TGD> tgdMap = ArrayListMultimap.create();
	
	private final Collection<Constraint> dependencies;

	public DefaultRestrictedDependencyAssessor(Collection<? extends Constraint> dependencies) {
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
