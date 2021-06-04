// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import org.apache.log4j.Logger;
import uk.ac.ox.cs.pdq.datasources.services.service.Service;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.ui.io.ObservableSchemaWriter;

import java.io.File;
// TODO: Auto-generated Javadoc
/**
 * 
 */
public class ObservableSchema {

	private static Logger log = Logger.getLogger(ObservableSchema.class);

	/**  */
	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	
	/** */
	private final SimpleStringProperty description =  new SimpleStringProperty(this, "description");
	
	/** */
	private final SimpleObjectProperty<File> file =  new SimpleObjectProperty<>(this, "file");
	
	/** */
	private final SimpleObjectProperty<Schema> schema =  new SimpleObjectProperty<>(this, "schema");
	
	private final SimpleObjectProperty<Service[]> services =  new SimpleObjectProperty<>(this, "services");
	
	/**
	 * Instantiates a new observable schema.
	 *
	 * @param name the name
	 * @param description the description
	 * @param schema the schema
	 */
	public ObservableSchema(String name, String description, Schema schema, Service[] services) {
		this(name, description, null, schema, services);
	}
	
	/**
	 * Instantiates a new observable schema.
	 *
	 * @param name the name
	 * @param description the description
	 * @param file the file
	 * @param schema the schema
	 */
	public ObservableSchema(String name, String description, File file, Schema schema, Service[] services) {
		this.name.set(name);
		this.description.set(description);
		this.file.set(file);
		this.schema.set(schema);
		this.services.set(services);		
	}

	/**
	 */
	public ObservableStringValue nameProperty() {
		return this.name;
	}

	/**
	 *
	 */
	public ObservableStringValue descriptionProperty() {
		return this.description;
	}

	/**
	 */
	public ObservableValue<File> fileProperty() {
		return this.file;
	}

	/**
	 */
	public ObservableValue<Schema> schemaProperty() {
		return this.schema;
	}

	/**
	 */
	public String getName() {
		return this.name.getValueSafe();
	}

	/**
	 */
	public String getDescription() {
		return this.description.getValueSafe();
	}

	/**
	 */
	public File getFile() {
		return this.file.get();
	}

	/**
	 */
	public Schema getSchema()
	{
		return this.schema.getValue();
	}

	public Service[] getServices()
	{
		return this.services.getValue();
	}

	/**
	 * Sets the name.
	 *
	 * @param n the new name
	 */
	public void setName(String n) {
		this.name.set(n);
	}

	/**
	 * Sets the description.
	 *
	 * @param d the new description
	 */
	public void setDescription(String d) {
		this.description.set(d);
	}

	/**
	 * Sets the file.
	 *
	 * @param f the new file
	 */
	public void setFile(File f) {
		this.file.set(f);
	}

	/**
	 * Gets the schema.
	 *
	 * @return the schema
	 */
	public void setSchema(Schema schema)
	{
		this.schema.set(schema);
	}
/**
	 */
	public void destroy() {
		if (this.file.isNotNull().get()) {
			this.file.getValue().delete();
		}
	}

	/**
	 * store method used to call the writer and store the xml file into the work directory
	 * .pdq/schemas
	 */
	public void store() {
		if (this.file.isNotNull().get()) {
			ObservableSchemaWriter writer = new ObservableSchemaWriter();
			File f = this.getFile();
				writer.write(f, this);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getName();
	}
}
