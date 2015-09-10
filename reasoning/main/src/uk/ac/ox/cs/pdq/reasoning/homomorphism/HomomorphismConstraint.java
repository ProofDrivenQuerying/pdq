package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.Bag;

/**
 * Super class for all constraints that can be passed for homomorphism detection
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
	static class TopK extends HomomorphismConstraint {
		final int k;
		/**
		 * Constructor for TopK.
		 * @param k int
		 */
		private TopK(int k) {
			this.k = k;
		}
	}

	/**
	 * Limits the matches to a subset of facts.
	 * @author Julien Leblay
	 */
	static class FactScope extends HomomorphismConstraint {
		final Conjunction<Predicate> atoms;
		/**
		 * Constructor for AtomCollectionScope.
		 * @param atoms Conjunction<PredicateFormula>
		 */
		private FactScope(Conjunction<Predicate> atoms) {
			this.atoms = atoms;
		}
	}

	/**
	 * Limits the matches to a subset of bags.
	 * @author Julien Leblay
	 */
	static class BagScope extends HomomorphismConstraint {
		final Bag[] bags;
		final boolean singleBag;
		/**
		 * Constructor for BagScope.
		 * @param singleBag boolean
		 * @param bags Bag[]
		 */
		private BagScope(boolean singleBag, Bag... bags) {
			this.bags = bags;
			this.singleBag = singleBag;
		}
	}

	/**
	 * Limits the matches to those subsuming the given map.
	 * @author Julien Leblay
	 */
	static class SuperMap extends HomomorphismConstraint {
		final Map<Variable, Constant> mapping;
		/**
		 * Constructor for SuperMap.
		 * @param mapping Map<Variable,Term>
		 */
		private SuperMap(Map<Variable, Constant> mapping) {
			this.mapping = mapping;
		}
	}
	
	/**
	 * Limits the matches to those subsuming the given map.
	 * @author Efthymia Tsamoura
	 */
	static class ParametrisedMatch extends HomomorphismConstraint {
		final Collection<Variable> variables;
		final Collection<Constant> constants;
		final boolean isStrong;
		/**
		 * 
		 * @param variables
		 * @param constants
		 */
		private ParametrisedMatch(Collection<Variable> variables, Collection<Constant> constants) {
			this.variables = variables;
			this.constants = constants;
			this.isStrong = true;
		}

		/**
		 * 
		 * @param variables
		 * @param constants
		 * @param isStrong
		 */
		private ParametrisedMatch(Collection<Variable> variables, Collection<Constant> constants, boolean isStrong) {
			this.variables = variables;
			this.constants = constants;
			this.isStrong = isStrong;
		}
	}
	
	/**
	 * True if we want to find only active triggers
	 * @author Efthymia Tsamoura
	 */
	static class ActiveTrigger extends HomomorphismConstraint {
		final Boolean active;
		/**
		 * 
		 * @param active
		 * 		True if we want to find only active triggers
		 */
		private ActiveTrigger(Boolean active) {
			this.active = active;
		}
	}

	/**
	 * @param k
	 * @return a fresh top k constraint
	 */
	public static TopK topK(int k) {
		return new TopK(k);
	}

	/**
	 * @param atoms
	 * @return a fresh fact collection scope constraint
	 */
	public static FactScope factScope(Conjunction<Predicate> atoms) {
		return new FactScope(atoms);
	}

	/**
	 * @param bags
	 * @param singleBag boolean
	 * @return a fresh bag collection scope constraint
	 */
	public static BagScope bagScope(boolean singleBag, Bag... bags) {
		return new BagScope(singleBag, bags);
	}

	/**
	 * @param mapping
	 * @return a fresh SuperMap constraint
	 */
	public static SuperMap satisfies(Map<Variable, Constant> mapping) {
		return new SuperMap(mapping);
	}
	
	/**
	 * @param mapping
	 * @return a fresh ActiveTrigger constraint
	 */
	public static ActiveTrigger isActiveTrigger(Boolean active) {
		return new ActiveTrigger(active);
	}
	
	/**
	 * @param mapping
	 * @return a fresh StrongMatch constraint
	 */
	public static ParametrisedMatch isParametrisedMatch(Collection<Variable> variables, Collection<Constant> constants, boolean isStrong) {
		return new ParametrisedMatch(variables, constants, isStrong);
	}
	
}
