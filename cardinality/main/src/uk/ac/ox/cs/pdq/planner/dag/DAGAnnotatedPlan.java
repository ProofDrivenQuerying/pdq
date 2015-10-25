package uk.ac.ox.cs.pdq.planner.dag;

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
	
	private Integer size;
	
	private Double quality;
	
	private Double adjustedQuality;
	
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

	public Integer getSize() {
		return this.size;
	}

	public void setSize(int size) {
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

}
