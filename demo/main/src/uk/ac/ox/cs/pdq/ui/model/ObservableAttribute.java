package uk.ac.ox.cs.pdq.ui.model;

import java.lang.reflect.Type;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Types;

public class ObservableAttribute {
	
	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	private final SimpleObjectProperty<Type> type =  new SimpleObjectProperty<>(this, "type");
	private final SimpleStringProperty displayType =  new SimpleStringProperty(this, "displayType");
	
	public ObservableAttribute(Attribute att) {
		this.name.set(att.getName());
		this.type.set(att.getType());
		this.displayType.set(Types.simpleName(att.getType()));
		this.displayType.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				try {
					ObservableAttribute.this.type.set(Class.forName(arg2));
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException("Inconsistent type '" + arg2 + "'");
				}
			}
		});
	}
	
	public Property<String> nameProperty() {
		return this.name;
	}
	
	public Property<Type> typeProperty() {
		return this.type;
	}
	
	public Property<String> displayTypeProperty() {
		return this.displayType;
	}

	public String getName() {
		return this.name.get();
	}

	public Type getType() {
		return this.type.get();
	}

	public String getDisplayType() {
		return this.displayType.get();
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.getName() + ":" + Types.simpleName(this.getType()));
	}
}
