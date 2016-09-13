package uk.ac.ox.cs.pdq.ui.model;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javafx.beans.property.SimpleObjectProperty;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.CostParameters.CostTypes;
import uk.ac.ox.cs.pdq.db.ReasoningParameters;
import uk.ac.ox.cs.pdq.db.ReasoningParameters.ReasoningTypes;
import uk.ac.ox.cs.pdq.io.xml.PlanWriter;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PlannerTypes;
import uk.ac.ox.cs.pdq.ui.UserInterfaceException;
import uk.ac.ox.cs.pdq.ui.io.xml.ProofWriter;
import uk.ac.ox.cs.pdq.ui.proof.Proof;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Observable representation of a Plan and the resources.settings that were used to 
 * generate it.
 * 
 * @author Julien Leblay
 */
public class ObservablePlan {

	/** The proof file. */
	private final SimpleObjectProperty<File> proofFile =  new SimpleObjectProperty<>(this, "proofFile");
	
	/** The plan file. */
	private final SimpleObjectProperty<File> planFile =  new SimpleObjectProperty<>(this, "planFile");
	
	/** The settings file. */
	private final SimpleObjectProperty<File> settingsFile =  new SimpleObjectProperty<>(this, "settingsFile");
	
	/** The plan. */
	private final SimpleObjectProperty<Plan> plan =  new SimpleObjectProperty<>(this, "plan");
	
	/** The proof. */
	private final SimpleObjectProperty<Proof> proof =  new SimpleObjectProperty<>(this, "proof");
	
	/** The planner type. */
	private final SimpleObjectProperty<PlannerTypes> plannerType = new SimpleObjectProperty<>(this, "plannerType");
	
	/** The chaser type. */
	private final SimpleObjectProperty<ReasoningTypes> chaserType = new SimpleObjectProperty<>(this, "chaserType");
	
	/** The cost type. */
	private final SimpleObjectProperty<CostTypes> costType = new SimpleObjectProperty<>(this, "costType");
	
	/** The timeout. */
	private final SimpleObjectProperty<Double> timeout = new SimpleObjectProperty<>(this, "timeout");
	
	/** The max iterations. */
	private final SimpleObjectProperty<Double> maxIterations = new SimpleObjectProperty<>(this, "maxIterations");
	
	/** The query match interval. */
	//private final SimpleObjectProperty<Integer> blockingInterval = new SimpleObjectProperty<>(this, "blockingInterval");
	private final SimpleObjectProperty<Integer> queryMatchInterval = new SimpleObjectProperty<>(this, "queryMatchInterval");
	
	/** The cost. */
	private final SimpleObjectProperty<Cost> cost = new SimpleObjectProperty<>(this, "cost");

	/**
	 * Default constructor.
	 *
	 * @param settings the settings
	 * @param costParams the cost params
	 * @param reasoningParams the reasoning params
	 */
	public ObservablePlan(PlannerParameters settings, CostParameters costParams, ReasoningParameters reasoningParams) {
		this(null, null, null, null, settings, costParams, reasoningParams, null);
	}

	/**
	 * Default constructor.
	 *
	 * @param p the p
	 * @param settings the settings
	 * @param costParams the cost params
	 * @param reasoningParams the reasoning params
	 */
	public ObservablePlan(Plan p, PlannerParameters settings, CostParameters costParams, ReasoningParameters reasoningParams) {
		this(p, null, null, null, settings, costParams, reasoningParams, null);
	}

	/**
	 * Default constructor.
	 *
	 * @param p the p
	 * @param pr the pr
	 * @param settings the settings
	 * @param costParams the cost params
	 * @param reasoningParams the reasoning params
	 */
	public ObservablePlan(Plan p, Proof pr, PlannerParameters settings, CostParameters costParams, ReasoningParameters reasoningParams) {
		this(p, null, pr, null, settings, costParams, reasoningParams, null);
	}

	/**
	 * Default constructor.
	 *
	 * @param pl the pl
	 * @param planFile the plan file
	 * @param pr the pr
	 * @param proofFile the proof file
	 * @param settings the settings
	 * @param costParams the cost params
	 * @param reasoningParams the reasoning params
	 * @param settingsFile the settings file
	 */
	public ObservablePlan(Plan pl, File planFile, Proof pr, File proofFile, 
			PlannerParameters settings, 
			CostParameters costParams, 
			ReasoningParameters reasoningParams, 
			File settingsFile) {
		Preconditions.checkArgument(settings != null);
		this.plan.set(pl);
		this.planFile.set(planFile);
		this.proof.set(pr);
		this.proofFile.set(proofFile);
		this.settingsFile.set(settingsFile);
		this.plannerType.set(settings.getPlannerType() != null ? settings.getPlannerType() : PlannerTypes.LINEAR_OPTIMIZED);
		this.costType.set(costParams.getCostType() != null ? costParams.getCostType() : CostTypes.BLACKBOX);
		this.timeout.set(settings.getTimeout() != null ? settings.getTimeout().doubleValue() : Double.POSITIVE_INFINITY);
		this.maxIterations.set(settings.getMaxIterations() != null ? settings.getMaxIterations().doubleValue() : Double.POSITIVE_INFINITY);
		this.queryMatchInterval.set(settings.getQueryMatchInterval());
		this.cost.set(pl == null || pl.isEmpty() ? null : pl.getCost());
		this.chaserType.set(reasoningParams.getReasoningType() != null ? reasoningParams.getReasoningType() : ReasoningTypes.RESTRICTED_CHASE);
//		this.blockingInterval.set(reasoningParams.getBlockingInterval());
	}

	/**
	 * Plan property.
	 *
	 * @return the simple object property
	 */
	public SimpleObjectProperty<Plan> planProperty() {
		return this.plan;
	}

	/**
	 * Proof property.
	 *
	 * @return the simple object property
	 */
	public SimpleObjectProperty<Proof> proofProperty() {
		return this.proof;
	}

	/**
	 * Planner type property.
	 *
	 * @return the simple object property
	 */
	public SimpleObjectProperty<PlannerTypes> plannerTypeProperty() {
		return this.plannerType;
	}
	
	/**
	 * Chaser type property.
	 *
	 * @return the simple object property
	 */
	public SimpleObjectProperty<ReasoningTypes> chaserTypeProperty() {
		return this.chaserType;
	}
	
	/**
	 * Cost type property.
	 *
	 * @return the simple object property
	 */
	public SimpleObjectProperty<CostTypes> costTypeProperty() {
		return this.costType;
	}
	
	/**
	 * Timeout property.
	 *
	 * @return the simple object property
	 */
	public SimpleObjectProperty<Double> timeoutProperty() {
		return this.timeout;
	}
	
	/**
	 * Max iterations property.
	 *
	 * @return the simple object property
	 */
	public SimpleObjectProperty<Double> maxIterationsProperty() {
		return this.maxIterations;
	}
	
//	public SimpleObjectProperty<Integer> blockingIntervalProperty() {
//		return this.blockingInterval;
//	}
	
	/**
 * Query match interval property.
 *
 * @return the simple object property
 */
public SimpleObjectProperty<Integer> queryMatchIntervalProperty() {
		return this.queryMatchInterval;
	}
	
	/**
	 * Cost property.
	 *
	 * @return the simple object property
	 */
	public SimpleObjectProperty<Cost> costProperty() {
		return this.cost;
	}
	
	/**
	 * Gets the plan.
	 *
	 * @return the plan
	 */
	public Plan getPlan() {
		return this.plan.get();
	}
	
	/**
	 * Gets the proof.
	 *
	 * @return the proof
	 */
	public Proof getProof() {
		return this.proof.get();
	}

	/**
	 * Gets the planner type.
	 *
	 * @return the planner type
	 */
	public PlannerTypes getPlannerType() {
		return this.plannerType.get();
	}

	/**
	 * Gets the reasoning type.
	 *
	 * @return the reasoning type
	 */
	public ReasoningTypes getReasoningType() {
		return this.chaserType.get();
	}

	/**
	 * Gets the cost type.
	 *
	 * @return the cost type
	 */
	public CostTypes getCostType() {
		return this.costType.get();
	}

	/**
	 * Gets the timeout.
	 *
	 * @return the timeout
	 */
	public Double getTimeout() {
		return this.timeout.get();
	}

	/**
	 * Gets the max iterations.
	 *
	 * @return the max iterations
	 */
	public Double getMaxIterations() {
		return this.maxIterations.get();
	}

//	public Integer getBlockingInterval() {
//		return this.blockingInterval.get();
//	}

	/**
 * Gets the query match interval.
 *
 * @return the query match interval
 */
public Integer getQueryMatchInterval() {
		return this.queryMatchInterval.get();
	}

	/**
	 * Gets the cost.
	 *
	 * @return the cost
	 */
	public Cost getCost() {
		return this.cost.get();
	}

	/**
	 * Gets the settings.
	 *
	 * @return the settings
	 */
	public PlannerParameters getSettings() {
		PlannerParameters result = new PlannerParameters();
		result.setQueryMatchInterval(this.getQueryMatchInterval());
		result.setMaxIterations(this.getMaxIterations());
		result.setTimeout(this.getTimeout());
		result.setPlannerType(this.getPlannerType());
		return result;
	}

	/**
	 * Gets the cost settings.
	 *
	 * @return the cost settings
	 */
	public CostParameters getCostSettings() {
		CostParameters result = new CostParameters();
		result.setCostType(this.getCostType());
		return result;
	}
	
	/**
	 * Gets the reasoning settings.
	 *
	 * @return the reasoning settings
	 */
	public ReasoningParameters getReasoningSettings() {
		ReasoningParameters result = new ReasoningParameters();
		result.setReasoningType(this.getReasoningType());
//		result.setBlockingInterval(this.getBlockingInterval());
		return result;
	}
	
	/**
	 * Gets the plan file.
	 *
	 * @return the plan file
	 */
	public File getPlanFile() {
		return this.planFile.get();
	}

	/**
	 * Gets the proof file.
	 *
	 * @return the proof file
	 */
	public File getProofFile() {
		return this.proofFile.get();
	}

	/**
	 * Gets the settings file.
	 *
	 * @return the settings file
	 */
	public File getSettingsFile() {
		return this.settingsFile.get();
	}
	
	/**
	 * Sets the plan.
	 *
	 * @param p the new plan
	 */
	public void setPlan(Plan p) {
		this.plan.set(p);
	}

	/**
	 * Sets the proof.
	 *
	 * @param p the new proof
	 */
	public void setProof(Proof p) {
		this.proof.set(p);
	}

	/**
	 * Sets the planner type.
	 *
	 * @param o the new planner type
	 */
	public void setPlannerType(PlannerTypes o) {
		this.plannerType.set(o);
	}

	/**
	 * Sets the chaser type.
	 *
	 * @param o the new chaser type
	 */
	public void setChaserType(ReasoningTypes o) {
		this.chaserType.set(o);
	}

	/**
	 * Sets the cost type.
	 *
	 * @param o the new cost type
	 */
	public void setCostType(CostTypes o) {
		this.costType.set(o);
	}

	/**
	 * Sets the timeout.
	 *
	 * @param o the new timeout
	 */
	public void setTimeout(Double o) {
		this.timeout.set(o);
	}

	/**
	 * Sets the max iterations.
	 *
	 * @param o the new max iterations
	 */
	public void setMaxIterations(Double o) {
		this.maxIterations.set(o);
	}

//	public void setBlockingInterval(Integer o) {
//		this.blockingInterval.set(o);
//	}

	/**
 * Sets the query match interval.
 *
 * @param o the new query match interval
 */
public void setQueryMatchInterval(Integer o) {
		this.queryMatchInterval.set(o);
	}

	/**
	 * Sets the cost.
	 *
	 * @param o the new cost
	 */
	public void setCost(Cost o) {
		this.cost.set(o);
	}

	/**
	 * Sets the settings file.
	 *
	 * @param o the new settings file
	 */
	public void setSettingsFile(File o) {
		this.settingsFile.set(o);
	}

	/**
	 * Sets the plan file.
	 *
	 * @param o the new plan file
	 */
	public void setPlanFile(File o) {
		this.planFile.set(o);
	}

	/**
	 * Sets the proof file.
	 *
	 * @param o the new proof file
	 */
	public void setProofFile(File o) {
		this.proofFile.set(o);
	}

	/**
	 * Destroy.
	 */
	public void destroy() {
		if (this.planFile.isNotNull().get()) {
			this.planFile.getValue().delete();
		}
		if (this.proofFile.isNotNull().get()) {
			this.proofFile.getValue().delete();
		}
		if (this.settingsFile.isNotNull().get()) {
			this.settingsFile.getValue().delete();
		}
	}

	/**
	 * Store.
	 */
	public void store() {
		if (this.planFile.isNotNull().get()) {
			File f = this.getPlanFile();
			try (PrintStream o = new PrintStream(f)) {
				if (!f.exists()) {
					f.createNewFile();
				}
				if (this.plan.isNotNull().get()) {
					PlanWriter.to(o).write(this.plan.get());
				}
			} catch (IOException e) {
				throw new UserInterfaceException("Could not write file " + f.getAbsolutePath());
			}
			f = this.getSettingsFile();
			try (PrintStream o = new PrintStream(f)) {
				if (!f.exists()) {
					f.createNewFile();
				}
				o.println(this.getSettings());
			} catch (IOException e) {
				throw new UserInterfaceException("Could not write file " + f.getAbsolutePath());
			}
			if (this.proofFile.isNotNull().get() && this.proof.isNotNull().get()) {
				ProofWriter writer2 = new ProofWriter();
				File f2 = this.getProofFile();
				try (PrintStream o = new PrintStream(f2)) {
					if (!f2.exists()) {
						f2.createNewFile();
					}
					writer2.write(o, this.getProof());
				} catch (IOException e) {
					throw new UserInterfaceException("Could not write file " + f.getAbsolutePath());
				}
			}
		}
	}
	
	/**
	 * Copy.
	 *
	 * @return the observable plan
	 */
	public ObservablePlan copy() {
		return new ObservablePlan(this.plan.get(), this.planFile.get(), 
				this.proof.get(), this.proofFile.get(), 
				this.getSettings(), 
				this.getCostSettings(), 
				this.getReasoningSettings(),
				this.settingsFile.get());
	}
}
