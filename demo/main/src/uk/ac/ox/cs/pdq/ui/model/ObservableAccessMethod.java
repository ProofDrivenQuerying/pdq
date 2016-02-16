package uk.ac.ox.cs.pdq.ui.model;

import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;

// TODO: Auto-generated Javadoc
/**
 * The Class ObservableAccessMethod.
 */
public class ObservableAccessMethod {
	
	/** The name. */
	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	
	/** The type. */
	private final SimpleObjectProperty<Types> type =  new SimpleObjectProperty<>(this, "type");
	
	/** The inputs. */
	private final SimpleListProperty<Integer> inputs =  new SimpleListProperty<>(this, "inputs");
	
	/**
	 * Instantiates a new observable access method.
	 *
	 * @param b the b
	 */
	public ObservableAccessMethod(AccessMethod b) {
		this.name.set(b.getName());
		this.type.set(b.getType());
		this.inputs.set(FXCollections.observableArrayList(b.getInputs()));
	}
	
	/**
	 * Name property.
	 *
	 * @return the property
	 */
	public Property<String> nameProperty() {
		return this.name;
	}
	
	/**
	 * Type property.
	 *
	 * @return the property
	 */
	public Property<Types> typeProperty() {
		return this.type;
	}
	
	/**
	 * Inputs property.
	 *
	 * @return the property
	 */
	public Property<ObservableList<Integer>> inputsProperty() {
		return this.inputs;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name.get();
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public Types getType() {
		return this.type.get();
	}

	/**
	 * Gets the inputs.
	 *
	 * @return the inputs
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
