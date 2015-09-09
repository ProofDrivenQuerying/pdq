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

public class ObservableSchema {

	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	private final SimpleStringProperty description =  new SimpleStringProperty(this, "description");
	private final SimpleObjectProperty<File> file =  new SimpleObjectProperty<>(this, "file");
	private final SimpleObjectProperty<Schema> schema = new SimpleObjectProperty<>(this, "schema");
	
	public ObservableSchema(String name, String description, Schema schema) {
		this(name, description, null, schema);
	}
	
	public ObservableSchema(String name, String description, File file, Schema schema) {
		this.name.set(name);
		this.description.set(description);
		this.file.set(file);
		this.schema.set(schema);
	}

	public ObservableStringValue nameProperty() {
		return this.name;
	}

	public ObservableStringValue descriptionProperty() {
		return this.description;
	}

	public ObservableValue<File> fileProperty() {
		return this.file;
	}

	public ObservableValue<Schema> schemaProperty() {
		return this.schema;
	}

	public String getName() {
		return this.name.getValueSafe();
	}

	public String getDescription() {
		return this.description.getValueSafe();
	}

	public File getFile() {
		return this.file.get();
	}

	public Schema getSchema() {
		return this.schema.get();
	}

	public void setName(String n) {
		this.name.set(n);
	}

	public void setDescription(String d) {
		this.description.set(d);
	}

	public void setFile(File f) {
		this.file.set(f);
	}

	public void setSchema(Schema s) {
		this.schema.set(s);
	}

	public void destroy() {
		if (this.file.isNotNull().get()) {
			this.file.getValue().delete();
		}
	}

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

	@Override
	public String toString() {
		return this.getName();
	}
}
