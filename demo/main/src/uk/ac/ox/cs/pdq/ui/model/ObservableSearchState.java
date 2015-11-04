package uk.ac.ox.cs.pdq.ui.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.ui.proof.Proof;

/**
 * Representation a Planner's search space state.
 * 
 * @author Julien Leblay
 *
 */
public class ObservableSearchState {

	/** Time elapsed since the beginning of the search */
	private final SimpleDoubleProperty time =  new SimpleDoubleProperty(this, "time");

	/** Number of iterations since the beginning of the search */
	private final SimpleIntegerProperty iterations =  new SimpleIntegerProperty(this, "iterations");

	/** Cost of the best plan found so far */
	private final SimpleObjectProperty<Number> cost = new SimpleObjectProperty<>(this, "cost");

	/** Best plan found so far */
	private final SimpleObjectProperty<Plan> plan = new SimpleObjectProperty<>(this, "plan");

	/** Best proof found so far */
	private final SimpleObjectProperty<Proof> proof = new SimpleObjectProperty<>(this, "proof");

	/**
	 * Default constructor
	 * @param pl
	 * @param resources.settings
	 */
	public ObservableSearchState(Double time, Integer rounds, Plan pl, Proof pr) {
		this.plan.set(pl);
		this.proof.set(pr);
		this.cost.set(pl == null || pl.isEmpty() ? null : pl.getCost().getValue());
		this.time.set(time);
		this.iterations.set(rounds);
	}

	public ObservableNumberValue timeProperty() {
		return this.time;
	}
	
	public ObservableNumberValue iterationsProperty() {
		return this.iterations;
	}

	public ObservableValue<Plan> planProperty() {
		return this.plan;
	}

	public ObservableValue<Proof> proofProperty() {
		return this.proof;
	}
	
	public ObservableValue<Number> costProperty() {
		return this.cost;
	}
	
	public Plan getPlan() {
		return this.plan.get();
	}
	
	public Proof getProof() {
		return this.proof.get();
	}

	public Number getTime() {
		return this.time.get();
	}

	public Integer getMaxIterations() {
		return this.iterations.get();
	}

	public Number getCost() {
		return this.cost.get();
	}
	
	public void setPlan(Plan p) {
		this.plan.set(p);
	}
	
	public void setProof(Proof p) {
		this.proof.set(p);
	}
	
	public void setCost(Number c) {
		this.cost.set(c);
	}
	
	public void setTime(Double t) {
		this.time.set(t);
	}
	
	public void setIterations(Integer i) {
		this.iterations.set(i);
	}

}
