// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.LinkedHashSet;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;

/**
 *  Configurations represent a partial proof and also a corresponding plan
 *  In the DAG planner they are proofs that may have hypotheses, corresponding to
 *  ``open plans'' that have paramters.  
 * DAG configurations are built up inductively. The base case (unary configuratinos)
 * correspond to firing an accessibility axiom (hence ``applyrule'') at the proof level, 
 * and to  single access at the plan level. The inductive step (binary configurations)
 * corresponds to composing proofs at the proof level, and to dependent join of plans at the plan level
 * 
 * @author Efthymia Tsamoura
 *
 */
public abstract class DAGChaseConfiguration extends ChaseConfiguration implements DAGConfiguration {

	/**
	 * ApplyRules have height 1, other configurations height is the sum of the height of their children.
	 */
	private final Integer height;
	
	/**  True if the configuration is a left-deep one. */
	private Boolean isLeftDeep = null;
	
	/**  The configuration's sub-configurations. */
	private Collection<DAGConfiguration> subconfigurations = null;
	
	/**
	 * Instantiates a new DAG chase configuration.
	 *
	 * @param state 		The state of this configuration.
	 * @param input 		The input constants
	 * @param output 		The output constants
	 * @param height 		The depth of this configuration
	 * @param bushiness 		The bushiness of this configuration
	 */
	public DAGChaseConfiguration(
			AccessibleChaseInstance state, 
			Collection<Constant> input,
			Collection<Constant> output, 
			Integer height
			) {
		super(state, input, output);
		Preconditions.checkNotNull(this.getInput());
		Preconditions.checkNotNull(this.getOutput());
		this.height = height;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration#clone()
	 */
	@Override
	public abstract DAGChaseConfiguration clone();

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#getHeight()
	 */
	@Override
	public Integer getHeight() {
		return this.height;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#getSubconfigurations()
	 */
	@Override
	public Collection<DAGConfiguration> getSubconfigurations() {
		if(this.subconfigurations == null) {
			this.subconfigurations = DAGChaseConfiguration.getSubconfigurations(this);
		}
		return this.subconfigurations;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#isLeftDeep()
	 */
	@Override
	public Boolean isLeftDeep() {
		if(this.isLeftDeep == null) {
			this.isLeftDeep = computeIsLeftDeep(this);
		}
		return this.isLeftDeep;
	}
	/**
	 * Checks if is left deep.
	 *
	 * @param configuration the configuration
	 * @return 		true if the input configuration is a left-deep one
	 */
	private static boolean computeIsLeftDeep(DAGConfiguration configuration) {
		if(configuration instanceof BinaryConfiguration) {
			if (((BinaryConfiguration) configuration).getRight() instanceof BinaryConfiguration) 
				return false;
			return computeIsLeftDeep(((BinaryConfiguration) configuration).getLeft());
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.Configuration#compareTo(uk.ac.ox.cs.pdq.planner.reasoning.Configuration)
	 */
	@Override
	public int compareTo(Configuration o) {
		return this.getCost().compareTo(o.getCost());
	}

	/**
	 * 
	 *
	 * @param configuration the configuration
	 * @return 		the subconfigurations of the input configuration
	 */
	public static Collection<DAGConfiguration> getSubconfigurations(DAGConfiguration configuration) {
		Collection<DAGConfiguration> ret = new LinkedHashSet<>();
		if(configuration instanceof BinaryConfiguration) {
			ret.add(configuration);
			ret.addAll(getSubconfigurations(((BinaryConfiguration) configuration).getLeft()));
			ret.addAll(getSubconfigurations(((BinaryConfiguration) configuration).getRight()));
		}
		else 
			ret.add(configuration);
		return ret;
	}

}
