// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

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
import uk.ac.ox.cs.pdq.ui.io.ObservableQueryReader;
import uk.ac.ox.cs.pdq.ui.io.ObservableSchemaReader;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

// TODO: Auto-generated Javadoc
/**
 * Controller class for file imports (schema, queries, etc.)
 * @author Julien Leblay
 *
 */
public class ImportController {

	/** ImportController's logger. */
	private static Logger log = Logger.getLogger(ImportController.class);

	/**  Default icon for relations. */
	final Image errorIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/error.gif"));

	private boolean saveAs = false;
	
	private ObservableQuery query = null;
	
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
    @FXML Button importChooseFileButton;
    
    /** The import file field. */
    @FXML TextField importFileField;
        
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
        assert this.importChooseFileButton != null : "fx:id=\"importChooseFileButton\" was not injected: check your FXML file 'import-dialog.fxml'.";
        assert this.importFileField != null : "fx:id=\"importFileField\" was not injected: check your FXML file 'import-dialog.fxml'.";
         assert this.rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'import-dialog.fxml'.";

    	this.bundle = ResourceBundle.getBundle("resources.i18n.ui");
		this.okButton.setDisable(true);
		this.importFileField.textProperty().addListener(this.importValidator);
	}

	
	/** The import validator. */
	private ChangeListener<String> importValidator = new ChangeListener<String>() {
		@Override
		public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
			ImportController.this.okButton.setDisable(true);
			ImportController.this.detailsImage.setImage(ImportController.this.errorIcon);
			if (ImportController.this.importFileField.getText().trim().isEmpty()) {
				ImportController.this.detailsLabel.setText(
						ImportController.this.bundle.getString(
								"application.dialog.import.message.file-required"));
				return;
			}
			if (!new File(ImportController.this.importFileField.getText()).exists()) {
				ImportController.this.detailsImage.setImage(ImportController.this.errorIcon);
				ImportController.this.detailsLabel.setText(
						ImportController.this.bundle.getString(
								"application.dialog.import.message.file-notexists"));
				return;
			}
			ImportController.this.okButton.setDisable(false);
			ImportController.this.detailsLabel.setText("");
			ImportController.this.detailsImage.setImage(null);
		}
	};
	
	public void saveAs(ObservableQuery query)
	{
		this.saveAs = true;
		this.query = query;
	}
	
	/**
	 * Save the current selection and closes the dialog window.
	 *
	 * @param event the event
	 */
	@FXML
	void saveAndClose(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			if(this.saveAs)
			{
				try  {
					File file = new File(this.importFileField.getText());
					this.query.setFile(file);
					this.query.store();
					ImportController.this.rootPane.getScene().getWindow().hide();
				} catch (Exception e) {
					ImportController.this.detailsImage.setImage(ImportController.this.errorIcon);
					ImportController.this.detailsLabel.setText(
							ImportController.this.bundle.getString(
									"application.dialog.import.message.file-corrupted") + "\n" +
									e.getMessage());
				}
			}
			else
			{

				File f = new File(this.importFileField.getText());
				try  {
					Object o = null;
					if (this.schema == null) {
						ObservableSchemaReader reader = new ObservableSchemaReader();
						o = reader.read(f);
					} else {
						ObservableQueryReader reader = new ObservableQueryReader(this.schema.getSchema());
						o = reader.read(f);
					}
					this.dataQueue.add(o);
					ImportController.this.rootPane.getScene().getWindow().hide();
				} catch (Exception e) {
					ImportController.this.detailsImage.setImage(ImportController.this.errorIcon);
					ImportController.this.detailsLabel.setText(
							ImportController.this.bundle.getString(
									"application.dialog.import.message.file-corrupted") + "\n" +
									e.getMessage());
				}
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
			ImportController.this.rootPane.getScene().getWindow().hide();
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
		File file = fileChooser.showOpenDialog(this.okButton.getScene().getWindow());
		this.importFileField.setText(file.getAbsolutePath());
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
