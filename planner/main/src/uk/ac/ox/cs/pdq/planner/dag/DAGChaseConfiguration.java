package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
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
public abstract class DAGChaseConfiguration extends ChaseConfiguration<DAGPlan> implements DAGConfiguration {

	/** The id. */
	private final Integer id;

	/** The global id. */
	private static Integer globalId = 0;
	
	/**  The depth of this configuration. */
	private final Integer high;

	/**  The bushiness of this configuration. */
	private final Integer bushiness;
	
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
	 * @param high 		The depth of this configuration
	 * @param bushiness 		The bushiness of this configuration
	 */
	public DAGChaseConfiguration(
			AccessibleChaseState state, 
			Collection<Constant> input,
			Collection<Constant> output, 
			Integer high,
			Integer bushiness
			) {
		super(state, input, output);
		Preconditions.checkNotNull(this.getInput());
		Preconditions.checkNotNull(this.getOutput());
		this.id = globalId++;
		this.high = high;
		this.bushiness = bushiness;
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
		return this.high;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#getBushiness()
	 */
	@Override
	public Integer getBushiness() {
		return this.bushiness;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#getId()
	 */
	@Override
	public Integer getId() {
		return this.id;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#getSubconfigurations()
	 */
	@Override
	public Collection<DAGConfiguration> getSubconfigurations() {
		if(this.subconfigurations == null) {
			this.subconfigurations = ConfigurationUtility.getSubconfigurations(this);
		}
		return this.subconfigurations;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#isLeftDeep()
	 */
	@Override
	public Boolean isLeftDeep() {
		if(this.isLeftDeep == null) {
			this.isLeftDeep = ConfigurationUtility.isLeftDeep(this);
		}
		return this.isLeftDeep;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.Configuration#compareTo(uk.ac.ox.cs.pdq.planner.reasoning.Configuration)
	 */
	@Override
	public int compareTo(Configuration o) {
		return this.getPlan().compareTo(o.getPlan());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.id.equals(((DAGChaseConfiguration) o).getId());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

}
