package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * @author Efthymmia Tsamoura
 *
 */
public class HomomorphismUtility {

	/**
	 * Clusters the input atoms based on their signature
	 * @param atoms
	 * @return
	 */
	public static Map<Predicate, List<Atom>> clusterAtoms(Collection<? extends Atom> atoms) {
		//Cluster the input facts based on their predicate
		Map<Predicate, List<Atom>> clusters = Maps.newHashMap();
		for (Atom atom:atoms) {
			if(clusters.containsKey(atom.getPredicate())) {
				clusters.get(atom.getPredicate()).add(atom);
			}
			else {
				clusters.put(atom.getPredicate(), Lists.newArrayList(atom));
			}
		}
		return clusters;
	}
}
