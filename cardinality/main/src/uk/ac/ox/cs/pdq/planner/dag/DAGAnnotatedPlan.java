/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.dag;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.AnnotatedPlan;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * The Class DAGAnnotatedPlan.
 *
 * @author Efthymia Tsamoura
 */
public abstract class DAGAnnotatedPlan extends AnnotatedPlan<DAGPlan> implements DAGConfiguration {

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
	
	/**  The size of this configuration without applying any projections *. */
	private BigInteger size = BigInteger.valueOf(Integer.MAX_VALUE);
	
	/** The size of this configuration after applying any projections on the attributes of the input query.
	 * This estimate makes sense if this configuration subsumes the query and contains the query's projected variables. 
	 * **/
	private BigInteger cardinality = BigInteger.valueOf(Integer.MAX_VALUE);
	
	/**  The quality of the annotated plan *. */
	private Double quality = Double.MAX_VALUE;
	
	/**  The adjusted quality of the annotated plan *. */
	private Double adjustedQuality = Double.MAX_VALUE;
	
	/**
	 * Instantiates a new DAG annotated plan.
	 *
	 * @param state 		The state of this configuration.
	 * @param output 		The output constants
	 * @param high 		The depth of this configuration
	 * @param bushiness 		The bushiness of this configuration
	 */
	public DAGAnnotatedPlan(
			ChaseState state, 
			Collection<Constant> output, 
			Integer high,
			Integer bushiness
			) {
		super(state, output);
		Preconditions.checkNotNull(this.getOutput());
		this.id = globalId++;
		this.high = high;
		this.bushiness = bushiness;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.AnnotatedPlan#clone()
	 */
	@Override
	public abstract DAGAnnotatedPlan clone();

	/**
	 * Gets the exported constants.
	 *
	 * @return the constants of this annotated plan that appear on the facts
	 * used to build up the constituting unary annotated plans.
	 * The new chase constants that are produced during chasing are not returned.
	 */
	public abstract Collection<Constant> getExportedConstants();
	
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
				&& this.id.equals(((DAGAnnotatedPlan) o).getId());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public BigInteger getSize() {
		return this.size;
	}

	/**
	 * Sets the size.
	 *
	 * @param size the new size
	 */
	public void setSize(BigInteger size) {
		this.size = size;
	}

	/**
	 * Gets the quality.
	 *
	 * @return the quality
	 */
	public double getQuality() {
		return this.quality;
	}

	/**
	 * Sets the quality.
	 *
	 * @param quality the new quality
	 */
	public void setQuality(double quality) {
		this.quality = quality;
	}

	/**
	 * Gets the adjusted quality.
	 *
	 * @return the adjusted quality
	 */
	public double getAdjustedQuality() {
		return this.adjustedQuality;
	}

	/**
	 * Sets the adjusted quality.
	 *
	 * @param adjustedQuality the new adjusted quality
	 */
	public void setAdjustedQuality(double adjustedQuality) {
		this.adjustedQuality = adjustedQuality;
	}

	/**
	 * Gets the cardinality.
	 *
	 * @return the cardinality
	 */
	public BigInteger getCardinality() {
		return this.cardinality;
	}

	/**
	 * Sets the cardinality.
	 *
	 * @param cardinality the new cardinality
	 */
	public void setCardinality(BigInteger cardinality) {
		this.cardinality = cardinality;
	}

}
