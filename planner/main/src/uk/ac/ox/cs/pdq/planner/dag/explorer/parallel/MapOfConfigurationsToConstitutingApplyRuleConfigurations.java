package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;

/**
 * 
 * Maps each configuration to its constituting ApplyRule configurations. Used to speed up chasing, i.e.,
 * when we are about to create a new binary configuration c''= BinaryConfiguration(c,c')
 * from c and c' and there exists another configuration c^(3) with ApplyRules the ApplyRules of c and c' 
 * and c^(3) is already chased then we use c^(3)'s state as the state of c''
 * @author Efthymia Tsamoura
 *
 */
public final class MapOfConfigurationsToConstitutingApplyRuleConfigurations {

	/** The templates. */
	private final Map<Collection<ApplyRule>,DAGChaseConfiguration> templates = new ConcurrentHashMap<>();
	
	/**
	 * Gets the template.
	 *
	 * @param left the left
	 * @param right the right
	 * @return the template
	 */
	public DAGChaseConfiguration getTemplate(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		Collection<ApplyRule> applyRules = this.getApplyRules(left, right);
		return this.templates.get(applyRules);
	}
	
	/**
	 * Put.
	 *
	 * @param rules the rules
	 * @param template the template
	 */
	public void put(Collection<ApplyRule> rules, DAGChaseConfiguration template) {
		this.templates.put(rules, template);
	}
	
	/**
	 * Put.
	 *
	 * @param configurations List<DAGChaseConfiguration>
	 */
	public void put(Collection<DAGChaseConfiguration> configurations) {
		for(DAGChaseConfiguration configuration:configurations) {
			this.templates.put(this.getApplyRules(configuration), configuration);
		}
	}

	/**
	 * Put.
	 *
	 * @param configuration the configuration
	 */
	public void put(DAGChaseConfiguration configuration) {
		this.put(Sets.newHashSet(configuration));
	}
	
	/**
	 * Gets the apply rules.
	 *
	 * @param left the left
	 * @param right the right
	 * @return the apply rules
	 */
	private Collection<ApplyRule> getApplyRules(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		Collection<ApplyRule> applyRules = new HashSet<>();
		for(ApplyRule rule:left.getApplyRules()) {
			applyRules.add(rule);
		}
		for(ApplyRule rule:right.getApplyRules()) {
			applyRules.add(rule);
		}
		return applyRules;
	}
	
	/**
	 * Gets the apply rules.
	 *
	 * @param left the left
	 * @return the apply rules
	 */
	private Collection<ApplyRule> getApplyRules(DAGChaseConfiguration left) {
		Collection<ApplyRule> applyRules = new HashSet<>();
		for(ApplyRule rule:left.getApplyRules()) {
			applyRules.add(rule);
		}
		return applyRules;
	}
}
