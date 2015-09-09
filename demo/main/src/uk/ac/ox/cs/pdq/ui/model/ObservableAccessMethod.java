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

public class ObservableAccessMethod {
	
	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	private final SimpleObjectProperty<Types> type =  new SimpleObjectProperty<>(this, "type");
	private final SimpleListProperty<Integer> inputs =  new SimpleListProperty<>(this, "inputs");
	
	public ObservableAccessMethod(AccessMethod b) {
		this.name.set(b.getName());
		this.type.set(b.getType());
		this.inputs.set(FXCollections.observableArrayList(b.getInputs()));
	}
	
	public Property<String> nameProperty() {
		return this.name;
	}
	
	public Property<Types> typeProperty() {
		return this.type;
	}
	
	public Property<ObservableList<Integer>> inputsProperty() {
		return this.inputs;
	}

	public String getName() {
		return this.name.get();
	}

	public Types getType() {
		return this.type.get();
	}

	public List<Integer> getInputs() {
		return this.inputs.get();
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.getName() + ":" + this.getType() + "[" + this.getInputs() + "]");
	}
}
