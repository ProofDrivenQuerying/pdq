package uk.ac.ox.cs.pdq.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.io.pretty.PrettyDependencyReader;
import uk.ac.ox.cs.pdq.io.pretty.PrettyDependencyWriter;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

public class DependencyController {

	/** DependencyController logger. */
	private static Logger log = Logger.getLogger(DependencyController.class);

    @FXML private TextArea dependencyTextArea;
    @FXML private Button dependencyEditorButton;
    @FXML private Label dependencyEditorMessage;
    
    private SimpleBooleanProperty invalid = new SimpleBooleanProperty(false);

    @FXML void initialize() {
        assert this.dependencyTextArea != null : "fx:id=\"dependencyTextArea\" was not injected: check your FXML file 'dependency-window.fxml'.";

        this.dependencyEditorButton.disableProperty().bind(this.invalid);
		this.dependencyTextArea.textProperty().addListener(
				(ObservableValue<? extends String> obs, String oldString, String newString) -> {
				DependencyController.this.dependencyEditorMessage.setText("");
				if (DependencyController.this.schema != null) {
					try {
						PrettyDependencyReader reader = new PrettyDependencyReader(DependencyController.this.schema.getSchema());
						DependencyController.this.newDependency = reader.read(new ByteArrayInputStream(newString.getBytes()));
					} catch (ReaderException e) {
						DependencyController.this.dependencyEditorMessage.setText(e.getMessage());
						DependencyController.this.invalid.set(true);
						return;
					}
					DependencyController.this.invalid.set(false);
				}
		});
    }

    private Constraint dependency;
    private Constraint newDependency;
    private ObservableSchema schema;
    private ObservableList<ObservableQuery> queries;
    private ConcurrentLinkedQueue dataQueue;

	public Constraint getDependency() {
		return this.dependency;
	}

	public void setQueue(ConcurrentLinkedQueue q) {
		this.dataQueue = q;
	}

	public void setDependency(Constraint dependency) {
		this.dependency = dependency;
		this.refreshEditor();
	}

	public void setSchema(ObservableSchema s) {
		this.schema = s;
	}

	public void setQueries(ObservableList<ObservableQuery> queries) {
		this.queries = queries;
		this.dependencyTextArea.editableProperty().set(queries.isEmpty());
		this.invalid.set(!queries.isEmpty());
	}

	private void refreshEditor() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrettyDependencyWriter.to(new PrintStream(out)).indented().write(this.dependency);
		this.dependencyTextArea.setText(out.toString());
	}

	@FXML
	void saveAndClose(ActionEvent event) {
	   	if (!event.isConsumed()) {
    		event.consume();
    		if (this.newDependency != null) {
        		SchemaBuilder builder = Schema.builder(this.schema.getSchema());
        		builder.removeDependency(this.dependency);
        		builder.addDependency(this.newDependency);
        		Schema newSchema = builder.build();
        		this.schema.setSchema(newSchema);
        		this.dataQueue.add(this.schema);
    		}
    		Stage stage = (Stage) this.dependencyEditorButton.getScene().getWindow();
    		stage.close();
	   	}
    }
}
