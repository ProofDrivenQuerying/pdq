package uk.ac.ox.cs.pdq.materialize.utility;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.materialize.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.materialize.chase.state.DatabaseDiskState;

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
	public Collection<? extends Dependency> getDependencies(ChaseState state) {
		Collection<Dependency> constraints = Sets.newLinkedHashSet();		
		DatabaseDiskState s = (DatabaseDiskState)state;
		if(s.getRecentAtoms().isEmpty()) {
			return this.dependencies;
		}
		else {
			
			for(Dependency dependency:this.dependencies) {
				for(Atom atom:dependency.getLeft().getAtoms()) {
					Predicate predicate = atom.getPredicate();
					if(dependency instanceof TGD && s.getRecentAtoms().contains(predicate)) {
						constraints.add(dependency);
					}
				}
			}
		}
		s.getRecentAtoms().clear();
		return constraints;
	}

}
