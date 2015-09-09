package uk.ac.ox.cs.pdq.ui.model;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.ui.UserInterfaceException;
import uk.ac.ox.cs.pdq.ui.io.ObservableQueryWriter;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryWriter;

/**
 * Encapsulate a query, its name, description and the file it is stored in.
 *  
 * @author Julien Leblay
 */
public class ObservableQuery {

	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	private final SimpleStringProperty description =  new SimpleStringProperty(this, "description");
	private final SimpleObjectProperty<File> file =  new SimpleObjectProperty<>(this, "file");
	private final SimpleObjectProperty<Query<?>> query = new SimpleObjectProperty<>(this, "query");
	
	public ObservableQuery(String name, String description, Query<?> query) {
		this(name, description, null, query);
	}

	public ObservableQuery(String name, String description, File file, Query<?> query) {
		this.name.set(name);
		this.description.set(description);
		this.file.set(file);
		this.query.set(query);
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

	public ObservableValue<Query<?>> queryProperty() {
		return this.query;
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

	public Query getQuery() {
		return this.query.get();
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

	public void setQuery(Query q) {
		this.query.set(q);
	}

	public void destroy() {
		if (this.file.isNotNull().get()) {
			this.file.getValue().delete();
		}
	}

	public void store() {
		if (this.file.isNotNull().get()) {
			ObservableQueryWriter writer = new ObservableQueryWriter();
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
		String result = this.getName();
		if (result == null || result.trim().isEmpty()) {
			result = SQLLikeQueryWriter.convert(this.getQuery()).substring(0, 30) + "...";
		}
		return result;
	}
}
