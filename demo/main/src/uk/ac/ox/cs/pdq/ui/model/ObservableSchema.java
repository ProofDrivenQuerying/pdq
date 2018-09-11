package uk.ac.ox.cs.pdq.ui.model;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.ui.UserInterfaceException;
import uk.ac.ox.cs.pdq.ui.io.ObservableSchemaWriter;
import uk.ac.ox.cs.pdq.datasources.services.servicegroup.ServiceGroup;
import uk.ac.ox.cs.pdq.datasources.services.service.Service;
// TODO: Auto-generated Javadoc
/**
 * The Class ObservableSchema.
 */
public class ObservableSchema {

	/** The name. */
	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	
	/** The description. */
	private final SimpleStringProperty description =  new SimpleStringProperty(this, "description");
	
	/** The file. */
	private final SimpleObjectProperty<File> file =  new SimpleObjectProperty<>(this, "file");
	
	/** The schema part 1. */
	private ServiceGroup sgr;
	
	/** The schema part 2. */
	private Service sr;
	/**
	 * Instantiates a new observable schema.
	 *
	 * @param name the name
	 * @param description the description
	 * @param schema the schema
	 */
	public ObservableSchema(String name, String description, ServiceGroup sgr, Service sr) {
		this(name, description, null, sgr, sr);
	}
	
	/**
	 * Instantiates a new observable schema.
	 *
	 * @param name the name
	 * @param description the description
	 * @param file the file
	 * @param schema the schema
	 */
	public ObservableSchema(String name, String description, File file, ServiceGroup sgr, Service sr) {
		this.name.set(name);
		this.description.set(description);
		this.file.set(file);
		this.sgr = sgr;
		this.sr = sr;
	}

	/**
	 * Name property.
	 *
	 * @return the observable string value
	 */
	public ObservableStringValue nameProperty() {
		return this.name;
	}

	/**
	 * Description property.
	 *
	 * @return the observable string value
	 */
	public ObservableStringValue descriptionProperty() {
		return this.description;
	}

	/**
	 * File property.
	 *
	 * @return the observable value
	 */
	public ObservableValue<File> fileProperty() {
		return this.file;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name.getValueSafe();
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return this.description.getValueSafe();
	}

	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
	public File getFile() {
		return this.file.get();
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
	 * Destroy.
	 */
	public void destroy() {
		if (this.file.isNotNull().get()) {
			this.file.getValue().delete();
		}
	}

	/**
	 * Store.
	 */
	public void store() {
		if (this.file.isNotNull().get()) {
			ObservableSchemaWriter writer = new ObservableSchemaWriter();
			File f = this.getFile();
			try (PrintStream o = new PrintStream(f)) {
				if (!f.exists()) {
					f.createNewFile();
				}
				writer.write(o, this);
			} catch (IOException e) {
				throw new UserInterfaceException("Could not write file " + f.getAbsolutePath());
			}
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
