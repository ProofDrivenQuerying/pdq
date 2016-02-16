package uk.ac.ox.cs.pdq.ui.model;

import java.lang.reflect.Type;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Types;

// TODO: Auto-generated Javadoc
/**
 * The Class ObservableAttribute.
 */
public class ObservableAttribute {
	
	/** The name. */
	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	
	/** The type. */
	private final SimpleObjectProperty<Type> type =  new SimpleObjectProperty<>(this, "type");
	
	/** The display type. */
	private final SimpleStringProperty displayType =  new SimpleStringProperty(this, "displayType");
	
	/**
	 * Instantiates a new observable attribute.
	 *
	 * @param att the att
	 */
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
	public Property<Type> typeProperty() {
		return this.type;
	}
	
	/**
	 * Display type property.
	 *
	 * @return the property
	 */
	public Property<String> displayTypeProperty() {
		return this.displayType;
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
	public Type getType() {
		return this.type.get();
	}

	/**
	 * Gets the display type.
	 *
	 * @return the display type
	 */
	public String getDisplayType() {
		return this.displayType.get();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(this.getName() + ":" + Types.simpleName(this.getType()));
	}
}
