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

// TODO: Auto-generated Javadoc
/**
 * Encapsulate a query, its name, description and the file it is stored in.
 *  
 * @author Julien Leblay
 */
public class ObservableQuery {

	/** The name. */
	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	
	/** The description. */
	private final SimpleStringProperty description =  new SimpleStringProperty(this, "description");
	
	/** The file. */
	private final SimpleObjectProperty<File> file =  new SimpleObjectProperty<>(this, "file");
	
	/** The query. */
	private final SimpleObjectProperty<Query<?>> query = new SimpleObjectProperty<>(this, "query");
	
	/**
	 * Instantiates a new observable query.
	 *
	 * @param name the name
	 * @param description the description
	 * @param query the query
	 */
	public ObservableQuery(String name, String description, Query<?> query) {
		this(name, description, null, query);
	}

	/**
	 * Instantiates a new observable query.
	 *
	 * @param name the name
	 * @param description the description
	 * @param file the file
	 * @param query the query
	 */
	public ObservableQuery(String name, String description, File file, Query<?> query) {
		this.name.set(name);
		this.description.set(description);
		this.file.set(file);
		this.query.set(query);
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
	 * Query property.
	 *
	 * @return the observable value
	 */
	public ObservableValue<Query<?>> queryProperty() {
		return this.query;
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
	 * Gets the query.
	 *
	 * @return the query
	 */
	public Query getQuery() {
		return this.query.get();
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
	 * Sets the query.
	 *
	 * @param q the new query
	 */
	public void setQuery(Query q) {
		this.query.set(q);
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = this.getName();
		if (result == null || result.trim().isEmpty()) {
			result = SQLLikeQueryWriter.convert(this.getQuery()).substring(0, 30) + "...";
		}
		return result;
	}
}
