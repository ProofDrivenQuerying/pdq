package uk.ac.ox.cs.pdq.ui.model;

import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;

// TODO: Auto-generated Javadoc
/**
 * The Class ObservableAccessMethod.
 */
public class ObservableAccessMethod {
	
	/** */
	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	
	/**  */
	private final SimpleObjectProperty<String> type =  new SimpleObjectProperty<>(this, "type");
	
	/**  */
	private final SimpleListProperty<Integer> inputs =  new SimpleListProperty<>(this, "inputs");
	
	/**
	 * Instantiates a new observable access method.
	 *
	 * @param b the b
	 */
	public ObservableAccessMethod(AccessMethodDescriptor b) {
		this.name.set(b.getName());
		this.type.set((b.getNumberOfInputs() == 0) ? "Free" : "Limited");
		this.inputs.set(FXCollections.observableArrayList(b.getInputs()));
	}
	
	/**
	 * 
	 *
	 * TOCOMMENT: What are these properties?
	 */
	public Property<String> nameProperty() {
		return this.name;
	}
	
	/**
	 * 
	 */
	public Property<String> typeProperty() {
		return this.type;
	}
	
	/**
	 
	 */
	public Property<ObservableList<Integer>> inputsProperty() {
		return this.inputs;
	}

	/**
	 * 
	 */
	public String getName() {
		return this.name.get();
	}

	/**
	 
	 */
	public String getType() {
		return this.type.get();
	}

	/**
	
	 */
	public List<Integer> getInputs() {
		return this.inputs.get();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(this.getName() + ":" + this.getType() + "[" + this.getInputs() + "]");
	}
}
