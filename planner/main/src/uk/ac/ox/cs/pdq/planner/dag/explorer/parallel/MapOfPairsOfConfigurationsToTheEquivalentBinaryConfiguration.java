package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClass;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses;

// TODO: Auto-generated Javadoc
/**
 * Map of representatives. For each configuration c = BinConfiguration(c_1,c_2) we create a map from the
 * equivalence classes of c and c' to c''. This map helps us reducing the chasing time, i.e.,
 * if c'_1 and c'_2 are structurally equivalent to c_1 and c_2, respectively, and
 * c = BinConfiguration(c_1,c_2) has already been fully chased,
 * then we copy the state of c to the state of c' = BinConfiguration(c'_1,c'_2).
 * 
 * @author Efthymia Tsamoura
 *
 */
public class MapOfPairsOfConfigurationsToTheEquivalentBinaryConfiguration {
	
	/** The representatives. */
	private final Map<Pair<DAGEquivalenceClass,DAGEquivalenceClass>,DAGChaseConfiguration> representatives  = new ConcurrentHashMap<>();

	/**
	 * Gets the representative.
	 *
	 * @param equivalenceClasses the eclasses
	 * @param left the left
	 * @param right the right
	 * @return the representative
	 */
	public DAGChaseConfiguration getRepresentative(DAGEquivalenceClasses equivalenceClasses, DAGChaseConfiguration left, DAGChaseConfiguration right) {
		//The equivalence class of the left input configuration
		DAGEquivalenceClass rep0 = equivalenceClasses.getEquivalenceClass(left);
		//The equivalence class of the left input configuration
		DAGEquivalenceClass rep1 = equivalenceClasses.getEquivalenceClass(right);
		//A configuration BinConfiguration(c,c'), where c and c' belong to the equivalence classes of
		//the left and right input configuration, respectively.
		return this.representatives.get(Pair.of(rep0, rep1));
	}
	
	/**
	 * Put.
	 *
	 * @param eclasses the eclasses
	 * @param left the left
	 * @param right the right
	 * @param representative the representative
	 */
	public void put(DAGEquivalenceClasses eclasses, DAGChaseConfiguration left, DAGChaseConfiguration right, DAGChaseConfiguration representative) {
		this.representatives.put(Pair.of(eclasses.getEquivalenceClass(left), eclasses.getEquivalenceClass(right)), representative);
	}
}
