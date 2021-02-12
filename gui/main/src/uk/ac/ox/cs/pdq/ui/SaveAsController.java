// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;

// TODO: Auto-generated Javadoc
/**
 * Controller class for file imports (schema, queries, etc.)
 * @author Julien Leblay
 *
 */
public class SaveAsController {

	/** ImportController's logger. */
	private static Logger log = Logger.getLogger(SaveAsController.class);

	/**  Default icon for relations. */
	final Image errorIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/error.gif"));

	/**  Init icon for window. */
	final Image infoIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/info.gif"));

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
    
    /** The root pane. */
    @FXML GridPane rootPane;

	/**
	 * Initialize.
	 */
	@FXML
	void initialize() {
		assert this.cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'saveas-dialog.fxml'.";
		assert this.detailsLabel != null : "fx:id=\"detailsLabel\" was not injected: check your FXML file 'saveas-dialog.fxml'.";
		assert this.okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'saveas-dialog.fxml'.";
        assert this.rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'saveas-dialog.fxml'.";

    	this.bundle = ResourceBundle.getBundle("resources.i18n.ui");
		this.okButton.setDisable(false);
		this.detailsImage.setImage(SaveAsController.this.infoIcon);
		this.detailsLabel.setText(this.bundle.getString("application.dialog.saveas.info.label"));
	}

	
	public void saveAs(ObservableQuery query)
	{
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
				try  {
					PDQController.pdqController.addQuery(this.query);
					SaveAsController.this.rootPane.getScene().getWindow().hide();
				} catch (Exception e) {
					SaveAsController.this.detailsImage.setImage(SaveAsController.this.errorIcon);
					SaveAsController.this.detailsLabel.setText(
							SaveAsController.this.bundle.getString(
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
			SaveAsController.this.rootPane.getScene().getWindow().hide();
		}
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
