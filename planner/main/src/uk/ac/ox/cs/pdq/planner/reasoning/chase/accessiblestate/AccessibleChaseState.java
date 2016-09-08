package uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;

// TODO: Auto-generated Javadoc
/**
 * Extends the state of a chase configuration with methods specific to the accessible schema.
 *
 * @author Efthymia Tsamoura
 */
public interface AccessibleChaseState extends uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance {

	/**
	 * For each input accessibility axiom, it
	 * groups the corresponding facts based on the chase constants assigned to their input positions.
	 * This method is called when creating the initial ApplyRule configurations
	 *
	 * @param axioms the axioms
	 * @return 		pairs of accessibility axioms to chase facts
	 */
	Collection<Pair<AccessibilityAxiom, Collection<Atom>>> groupByBinding(Collection<AccessibilityAxiom> axioms);

	/**
	 * Adds the accessible and inferred accessible facts associated with each input fact.
	 *
	 * @param schema
	 * 			Input accessible schema
	 * @param axiom
	 * 			Input accessibility axiom
	 * @param facts
	 * 			Input facts
	 */
	void generate(AccessibleSchema schema, AccessibilityAxiom axiom, Collection<Atom> facts);

	/**
	 * Gets the unexposed facts.
	 *
	 * @param accessibleSchema the accessible schema
	 * @return 		the unexposed facts and information to expose them
	 */
	Map<AccessibilityAxiom, List<Match>> getUnexposedFacts(AccessibleSchema accessibleSchema);
	
	/**
	 * Gets the inferred.
	 *
	 * @return 		the inferred accessible facts of the state
	 */
	Collection<String> getInferred();

	/**
	 * Gets the derived inferred.
	 *
	 * @return 		the inferred accessible facts that were derived when chasing the state
	 */
	Collection<Atom> getDerivedInferred();
	
	/**
	 * Gets the provenance.
	 *
	 * @return 		the rule firings that took place.
	 */
	Map<Atom, Pair<Dependency, Collection<Atom>>> getProvenance();

	/**
	 * Gets the provenance.
	 *
	 * @param fact the fact
	 * @return 		the firing that has produced the input fact
	 */
	Pair<Dependency, Collection<Atom>> getProvenance(Atom fact);
	
	/**
	 * Merge.
	 *
	 * @param s 		An input chase state
	 * @return 		a state that is the union of this state and the input one
	 */
	AccessibleChaseState merge(AccessibleChaseState s);
	

	AccessibleChaseState clone();
	
}
