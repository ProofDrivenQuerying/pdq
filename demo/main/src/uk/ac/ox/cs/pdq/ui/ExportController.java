package uk.ac.ox.cs.pdq.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.ui.io.ObservableQueryReader;
import uk.ac.ox.cs.pdq.ui.io.ObservableQueryWriter;
import uk.ac.ox.cs.pdq.ui.io.ObservableSchemaReader;
import uk.ac.ox.cs.pdq.ui.io.ObservableSchemaWriter;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

// TODO: Auto-generated Javadoc
/**
 * Controller class for file imports (schema, queries, etc.)
 * @author Julien Leblay
 *
 */
public class ExportController {

	/** ImportController's logger. */
	private static Logger log = Logger.getLogger(ExportController.class);

	/**  Default icon for relations. */
	final Image errorIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/error.gif"));

	/** The bundle. */
	@FXML ResourceBundle bundle;

	/** The details label. */
	@FXML Label detailsLabel;
	
	/** The details image. */
	@FXML ImageView detailsImage;
	
	/** The cancel button. */
	@FXML Button cancelButton;
	
	/** The ok button. */
	@FXML Button okButton;
    
    /** The import choose file button. */
    @FXML Button exportChooseFileButton;
    
    /** The import file field. */
    @FXML TextField exportFileField;
    
    /** The root pane. */
    @FXML GridPane rootPane;

	/**
	 * Initialize.
	 */
	@FXML
	void initialize() {
		assert this.cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'import-dialog.fxml'.";
		assert this.detailsLabel != null : "fx:id=\"detailsLabel\" was not injected: check your FXML file 'import-dialog.fxml'.";
		assert this.okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'import-dialog.fxml'.";
        assert this.exportChooseFileButton != null : "fx:id=\"importChooseFileButton\" was not injected: check your FXML file 'import-dialog.fxml'.";
        assert this.exportFileField != null : "fx:id=\"importFileField\" was not injected: check your FXML file 'import-dialog.fxml'.";
        assert this.rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'import-dialog.fxml'.";

    	this.bundle = ResourceBundle.getBundle("resources.i18n.ui");
		this.okButton.setDisable(true);
		this.exportFileField.textProperty().addListener(this.importValidator);
	}

	
	/** The import validator. */
	private ChangeListener<String> importValidator = new ChangeListener<String>() {
		@Override
		public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
			ExportController.this.okButton.setDisable(true);
			ExportController.this.detailsImage.setImage(ExportController.this.errorIcon);
			if (ExportController.this.exportFileField.getText().trim().isEmpty()) {
				ExportController.this.detailsLabel.setText(
						ExportController.this.bundle.getString(
								"application.dialog.import.message.file-required"));
				return;
			}
			if (new File(ExportController.this.exportFileField.getText()).exists()) {
				ExportController.this.detailsImage.setImage(ExportController.this.errorIcon);
				ExportController.this.detailsLabel.setText(
						ExportController.this.bundle.getString(
								"application.dialog.import.message.file-exists"));
				return;
			}
			ExportController.this.okButton.setDisable(false);
			ExportController.this.detailsLabel.setText("");
			ExportController.this.detailsImage.setImage(null);
		}
	};
	
	/**
	 * Save the current selection and closes the dialog window.
	 *
	 * @param event the event
	 */
	@FXML
	void saveAndClose(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			File f = new File(this.exportFileField.getText());
			try  {
				// MR Object o = null;
				if (this.schema != null) {
					ObservableSchemaWriter writer = new ObservableSchemaWriter();
					writer.write(f, this.schema);
				} /* MR else {
					ObservableQueryWriter writer = new ObservableQueryWriter(this.schema.getSchema());
					writer.write(f, this.schema);
				}*/
				// MR this.dataQueue.add(o);
				ExportController.this.rootPane.getScene().getWindow().hide();
			} catch (Exception e) {
				ExportController.this.detailsImage.setImage(ExportController.this.errorIcon);
				ExportController.this.detailsLabel.setText(
						ExportController.this.bundle.getString(
								"application.dialog.import.message.file-corrupted") + "\n" +
								e.getMessage());
			}
		}
	}

	/**
	 * Close the dialog window without further action.
	 *
	 * @param event the event
	 */
	@FXML
	void cancel(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			ExportController.this.rootPane.getScene().getWindow().hide();
		}
	}

	/**
	 * Opens a file chooser window, and handle the returned selected file.
	 *
	 * @param event the event
	 */
	@FXML
	void chooseFile(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(this.bundle.getString("application.dialog.import.title"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter("XML files", "*.xml"));
		File file = fileChooser.showOpenDialog(this.okButton.getScene().getWindow());
		this.exportFileField.setText(file.getAbsolutePath());
		event.consume();
	}

	/** The schema currently selected. If null, we are importing a schema, other a query. */
	private ObservableSchema schema = null;

	/**  Queue containing the object(s) to import. */
	ConcurrentLinkedQueue dataQueue;

	/** Collection of forbidden names (e.g. already in use) */
	Collection<String> forbiddenNames = new LinkedHashSet<>();
	
	/**
	 * Sets the queue.
	 *
	 * @param q the new queue
	 */
	public void setQueue(ConcurrentLinkedQueue<?> q) {
		this.dataQueue = q;
	}
	
	/**
	 * Sets the forbidden names.
	 *
	 * @param names the new forbidden names
	 */
	public void setForbiddenNames(Collection<String> names) {
		this.forbiddenNames.addAll(names);
	}
	
	/**
	 * Sets the schema.
	 *
	 * @param schema the new schema
	 */
	public void setSchema(ObservableSchema schema) {
		this.schema = schema;
	}
}
