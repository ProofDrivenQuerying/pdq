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

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public abstract class DAGAnnotatedPlan extends AnnotatedPlan<DAGPlan> implements DAGConfiguration {

	private final Integer id;

	private static Integer globalId = 0;
	
	/** The depth of this configuration */
	private final Integer high;

	/** The bushiness of this configuration */
	private final Integer bushiness;
	
	/** True if the configuration is a left-deep one*/
	private Boolean isLeftDeep = null;

	/** The configuration's sub-configurations */
	private Collection<DAGConfiguration> subconfigurations = null;
	
	/** The size of this configuration without applying any projections **/
	private BigInteger size = BigInteger.valueOf(Integer.MAX_VALUE);
	
	/** The size of this configuration after applying any projections on the attributes of the input query.
	 * This estimate makes sense if this configuration subsumes the query and contains the query's projected variables. 
	 * **/
	private BigInteger cardinality = BigInteger.valueOf(Integer.MAX_VALUE);
	
	/** The quality of the annotated plan **/
	private Double quality = Double.MAX_VALUE;
	
	/** The adjusted quality of the annotated plan **/
	private Double adjustedQuality = Double.MAX_VALUE;
	
	/**
	 * 
	 * @param state
	 * 		The state of this configuration.
	 * @param output
	 * 		The output constants
	 * @param high
	 * 		The depth of this configuration
	 * @param bushiness
	 * 		The bushiness of this configuration
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

	@Override
	public abstract DAGAnnotatedPlan clone();

	/**
	 * 
	 * @return the constants of this annotated plan that appear on the facts 
	 * used to build up the constituting unary annotated plans.
	 * The new chase constants that are produced during chasing are not returned.  
	 */
	public abstract Collection<Constant> getExportedConstants();
	
	@Override
	public Integer getHeight() {
		return this.high;
	}

	@Override
	public Integer getBushiness() {
		return this.bushiness;
	}

	@Override
	public Integer getId() {
		return this.id;
	}
	
	@Override
	public Collection<DAGConfiguration> getSubconfigurations() {
		if(this.subconfigurations == null) {
			this.subconfigurations = ConfigurationUtility.getSubconfigurations(this);
		}
		return this.subconfigurations;
	}
	
	@Override
	public Boolean isLeftDeep() {
		if(this.isLeftDeep == null) {
			this.isLeftDeep = ConfigurationUtility.isLeftDeep(this);
		}
		return this.isLeftDeep;
	}
	
	@Override
	public int compareTo(Configuration o) {
		return this.getPlan().compareTo(o.getPlan());
	}

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

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	public BigInteger getSize() {
		return this.size;
	}

	public void setSize(BigInteger size) {
		this.size = size;
	}

	public double getQuality() {
		return this.quality;
	}

	public void setQuality(double quality) {
		this.quality = quality;
	}

	public double getAdjustedQuality() {
		return this.adjustedQuality;
	}

	public void setAdjustedQuality(double adjustedQuality) {
		this.adjustedQuality = adjustedQuality;
	}

	public BigInteger getCardinality() {
		return this.cardinality;
	}

	public void setCardinality(BigInteger cardinality) {
		this.cardinality = cardinality;
	}

}
