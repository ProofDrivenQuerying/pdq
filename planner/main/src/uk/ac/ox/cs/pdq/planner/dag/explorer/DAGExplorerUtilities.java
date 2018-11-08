package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

public class DAGExplorerUtilities {
	
	/**
	 * Creates the initial configurations.
	 *
	 * @return a list of ApplyRule configurations based on the facts derived after chasing the input schema with the canonical database of the query
	 * @throws SQLException 
	 */
	public static List<DAGChaseConfiguration> createInitialApplyRuleConfigurations(
			PlannerParameters parameters,
			ConjunctiveQuery query, 
			AccessibleSchema accessibleSchema, 
			Chaser chaser, 
			DatabaseManager connection
			) throws SQLException {
		AccessibleDatabaseChaseInstance state = null;
		state = new AccessibleDatabaseChaseInstance(query, accessibleSchema, connection, false);
		chaser.reasonUntilTermination(state, accessibleSchema.getOriginalDependencies());

		List<DAGChaseConfiguration> collection = new ArrayList<>();
		Collection<Pair<AccessibilityAxiom,Collection<Atom>>> groupsOfFacts = state.groupFactsByAccessMethods(accessibleSchema.getAccessibilityAxioms());
		for (Pair<AccessibilityAxiom, Collection<Atom>> groupOfFacts: groupsOfFacts) {
			ApplyRule applyRule = null;
			Collection<Collection<Atom>> groupForGivenAccessMethod = new LinkedHashSet<>();
			switch (parameters.getFollowUpHandling()) {
			case MINIMAL:
				for (Atom p: groupOfFacts.getRight()) {
					groupForGivenAccessMethod.add(Sets.newHashSet(p));
				}
				break;
			default:
				groupForGivenAccessMethod.add(groupOfFacts.getRight());
				break;
			}
			for (Collection<Atom> atoms:groupForGivenAccessMethod) {
				AccessibleChaseInstance newState = (uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance) 
						new AccessibleDatabaseChaseInstance(atoms, connection, false);
				applyRule = new ApplyRule(
						newState,
						groupOfFacts.getLeft(),
						Sets.newHashSet(atoms)
						);
				applyRule.generate(chaser, query, accessibleSchema.getInferredAccessibilityAxioms());
				collection.add(applyRule);
			}
		}
		return collection;
	}
}
