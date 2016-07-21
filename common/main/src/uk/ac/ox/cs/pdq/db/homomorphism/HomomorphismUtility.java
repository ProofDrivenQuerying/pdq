package uk.ac.ox.cs.pdq.db.homomorphism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;

import com.google.common.collect.Maps;

/**
 * @author George K
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
				ArrayList<Atom> new_list  = new ArrayList<Atom>();
				new_list.add(atom);
				clusters.put(atom.getPredicate(), new_list);
			}
		}
		return clusters;
	}
}
