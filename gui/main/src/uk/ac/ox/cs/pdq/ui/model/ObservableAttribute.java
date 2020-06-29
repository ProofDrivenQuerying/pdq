// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.model;

import java.lang.reflect.Type;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import uk.ac.ox.cs.pdq.datasources.services.RESTAccessMethodGenerator;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Types;

// TODO: Auto-generated Javadoc
/**
 * The Class ObservableAttribute.
 */
public class ObservableAttribute {
	
	/** */
	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	
	/** The relation attribute name. */
	private final SimpleStringProperty relationName =  new SimpleStringProperty(this, "relationName");
	
	/** The type of the attribute. */
	private final SimpleObjectProperty<Type> type =  new SimpleObjectProperty<>(this, "type");
	
	/** The display type of the attribute. */
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
		
	public ObservableAttribute(String name, String type, String relationattributename) {
			this.name.set(name);
			this.relationName.set(relationattributename);
			this.type.set(RESTAccessMethodGenerator.typeType(type));
			this.displayType.set(Types.simpleName(RESTAccessMethodGenerator.typeType(type)));
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
	 *
	 */
	public Property<String> nameProperty() {
		return this.name;
	}
	
	/**
	 */
	public Property<String> relationNameProperty() {
		return this.relationName;
	}
	
	/**
	 * 
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
	 * 
	 */
	public String getName() {
		return this.name.get();
	}

	/**
	 */
	public String getRelationName() {
		return this.relationName.get();
	}

	/**
	 */
	public Type getType() {
		return this.type.get();
	}

	/**

	 */
	public String getDisplayType() {
		return this.displayType.get();
	}
	
}
