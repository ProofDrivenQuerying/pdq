package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Super class for all constraints that an homomorphism should satisfy
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 *
 */
public abstract class HomomorphismConstraint {

	/**
	 * Limits the number of matches to k
	 * @author Julien Leblay
	 */
	static class TopKConstraint extends HomomorphismConstraint {
		final int k;
		/**
		 * Constructor for TopK.
		 * @param k int
		 */
		private TopKConstraint(int k) {
			this.k = k;
		}
	}

	/**
	 * Limits the matches to a subset of facts.
	 * @author Julien Leblay
	 */
	static class FactConstraint extends HomomorphismConstraint {
		final Conjunction<Predicate> atoms;
		/**
		 * Constructor for AtomCollectionScope.
		 * @param atoms Conjunction<PredicateFormula>
		 */
		private FactConstraint(Conjunction<Predicate> atoms) {
			this.atoms = atoms;
		}
	}

	/**
	 * Limits the matches to those subsuming the given map.
	 * @author Julien Leblay
	 */
	static class MapConstraint extends HomomorphismConstraint {
		final Map<Variable, Constant> mapping;
		/**
		 * Constructor for SuperMap.
		 * @param mapping Map<Variable,Term>
		 */
		private MapConstraint(Map<Variable, Constant> mapping) {
			this.mapping = mapping;
		}
	}
	
	static class EGDHomomorphismConstraint extends HomomorphismConstraint {}

	/**
	 * @param k
	 * @return a fresh top k constraint
	 */
	public static TopKConstraint createTopKConstraint(int k) {
		return new TopKConstraint(k);
	}

	/**
	 * @param atoms
	 * @return a fresh fact collection scope constraint
	 */
	public static FactConstraint createFactConstraint(Conjunction<Predicate> atoms) {
		return new FactConstraint(atoms);
	}

	/**
	 * @param mapping
	 * @return a fresh SuperMap constraint
	 */
	public static MapConstraint createMapConstraint(Map<Variable, Constant> mapping) {
		return new MapConstraint(mapping);
	}
	
	public static EGDHomomorphismConstraint createEGDHomomorphismConstraint() {
		return new EGDHomomorphismConstraint();
	}
}
