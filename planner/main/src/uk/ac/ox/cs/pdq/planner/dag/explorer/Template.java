package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;

import com.google.common.collect.Sets;

/**
 * 
 * Maps each configuration to its constituting ApplyRule configurations. Used to speed up chasing, i.e.,
 * when we are about to create a new binary configuration c''= BinaryConfiguration(c,c')
 * from c and c' and there exists another configuration c^(3) with ApplyRules the ApplyRules of c and c' 
 * and c^(3) is already chased then we use c^(3)'s state as the state of c''
 * @author Efthymia Tsamoura
 *
 */
public final class Template {

	private final Map<Collection<ApplyRule>,DAGChaseConfiguration> templates = new ConcurrentHashMap<>();
	
	public DAGChaseConfiguration getTemplate(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		Collection<ApplyRule> applyRules = this.getApplyRules(left, right);
		return this.templates.get(applyRules);
	}
	
	public void put(Collection<ApplyRule> rules, DAGChaseConfiguration template) {
		this.templates.put(rules, template);
	}
	
	/**
	 * @param configurations List<DAGChaseConfiguration>
	 */
	public void put(Collection<DAGChaseConfiguration> configurations) {
		for(DAGChaseConfiguration configuration:configurations) {
			this.templates.put(this.getApplyRules(configuration), configuration);
		}
	}

	public void put(DAGChaseConfiguration configuration) {
		this.put(Sets.newHashSet(configuration));
	}
	
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
	
	private Collection<ApplyRule> getApplyRules(DAGChaseConfiguration left) {
		Collection<ApplyRule> applyRules = new HashSet<>();
		for(ApplyRule rule:left.getApplyRules()) {
			applyRules.add(rule);
		}
		return applyRules;
	}
}
