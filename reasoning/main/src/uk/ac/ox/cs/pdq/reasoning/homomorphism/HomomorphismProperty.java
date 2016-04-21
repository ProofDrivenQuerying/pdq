package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Variable;

// TODO: Auto-generated Javadoc
/**
 * Super class for all constraints that an homomorphism should satisfy.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public abstract class HomomorphismProperty {

	/**
	 * Limits the number of matches to k.
	 *
	 * @author Julien Leblay
	 */
	public static class TopKProperty extends HomomorphismProperty {
		
		/** The k. */
		public final int k;
		/**
		 * Constructor for TopK.
		 * @param k int
		 */
		private TopKProperty(int k) {
			this.k = k;
		}
	}

	/**
	 * Limits the matches to a subset of facts.
	 * @author Julien Leblay
	 */
	public static class FactProperty extends HomomorphismProperty {
		
		/** The atoms. */
		public final Conjunction<Atom> atoms;
		/**
		 * Constructor for AtomCollectionScope.
		 * @param atoms Conjunction<PredicateFormula>
		 */
		private FactProperty(Conjunction<Atom> atoms) {
			this.atoms = atoms;
		}
	}

	/**
	 * Limits the matches to those subsuming the given map.
	 * @author Julien Leblay
	 */
	public static class MapProperty extends HomomorphismProperty {
		
		/** The mapping. */
		public final Map<Variable, Constant> mapping;
		/**
		 * Constructor for SuperMap.
		 * @param mapping Map<Variable,Term>
		 */
		private MapProperty(Map<Variable, Constant> mapping) {
			this.mapping = mapping;
		}
	}
	
	/**
	 * Property to impose non trivial EGDs 
	 */
	public static class EGDHomomorphismProperty extends HomomorphismProperty {}
	
	
	/**
	 * Checks if is active trigger.
	 *
	 * 
	 * (From modern dependency theory notes)
	 * Consider an instance I, a set Base of values, and a TGD
	 * 		\delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
	 * 		A trigger for \delta in I is a homomorphism h of \sigma into I. A trigger is active if it
	 * 		does not extend to a homomorphism h0 into I. Informally, a trigger is a tuple \vec{c}
	 * 		satisfying \sigma, and it is active if there is no witness \vec{y} that makes \tau holds.
	 */
	public static class ActiveTriggerProperty extends HomomorphismProperty {}

	/**
	 * Creates the top k constraint.
	 *
	 * @param k the k
	 * @return a fresh top k constraint
	 */
	public static TopKProperty createTopKProperty(int k) {
		return new TopKProperty(k);
	}

	/**
	 * Creates the fact constraint.
	 *
	 * @param atoms the atoms
	 * @return a fresh fact collection scope constraint
	 */
	public static FactProperty createFactProperty(Conjunction<Atom> atoms) {
		return new FactProperty(atoms);
	}

	/**
	 * Creates the map constraint.
	 *
	 * @param mapping the mapping
	 * @return a fresh SuperMap constraint
	 */
	public static MapProperty createMapProperty(Map<Variable, Constant> mapping) {
		return new MapProperty(mapping);
	}
	
	/**
	 * Creates the egd homomorphism constraint.
	 *
	 * @return the EGD homomorphism constraint
	 */
	public static EGDHomomorphismProperty createEGDHomomorphismProperty() {
		return new EGDHomomorphismProperty();
	}
	
	/**
	 * Creates the egd homomorphism constraint.
	 *
	 * @return the EGD homomorphism constraint
	 */
	public static ActiveTriggerProperty createActiveTriggerProperty() {
		return new ActiveTriggerProperty();
	}
}