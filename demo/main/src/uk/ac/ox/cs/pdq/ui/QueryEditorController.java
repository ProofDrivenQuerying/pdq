package uk.ac.ox.cs.pdq.ui;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryReader;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryWriter;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

public class QueryEditorController {

	/** QueryEditorController's logger. */
	private static Logger log = Logger.getLogger(QueryEditorController.class);

	// This controller's widgets
	@FXML private TextArea  queryEditArea;
	@FXML private Label     statusLabel;
	@FXML private Button    okButton;
	@FXML private TextField queryNameTextField;
	
	private ObservableQuery  query;
	private ObservableSchema schema;
	private ObservableList<ObservableQuery> queriesList;
	
	public QueryEditorController() { }

	/**
	 * Controller's widget's initialization
	 */
	@FXML void initialize() {
		this.queryNameTextField.setEditable(true);
		this.queryNameTextField.textProperty().addListener(
				(ObservableValue<? extends String> obs, String oldValue, String newValue) -> {
				if (QueryEditorController.this.schema != null) {
					if (QueryEditorController.this.validateQuery() && 
							QueryEditorController.this.validateQueryName(newValue)) {
						QueryEditorController.this.okButton.setDisable(false);
					} else {
						QueryEditorController.this.okButton.setDisable(true);
					}
				}
		});
		this.queryEditArea.textProperty().addListener(
			(ObservableValue<? extends String> obs, String oldValue, String newValue) -> {
			if (QueryEditorController.this.schema != null) {
				if (QueryEditorController.this.validateQuery()
						&& QueryEditorController.this.validateQueryName()) {
					QueryEditorController.this.okButton.setDisable(false);
				} else {
					QueryEditorController.this.okButton.setDisable(true);
				}
			}
		});
		
		log.debug("queryNameTextField.editable = " + this.queryNameTextField.isEditable());
		
	}
	
	public void setQuery(ObservableQuery query) {
		if (query == null) {
			this.queryEditArea.setText("Error. No query currently selected");
			this.okButton.setDisable(true);
			return;
		}
		this.query = query;
		this.queryNameTextField.setText(query.getName());
		this.queryEditArea.setText(SQLLikeQueryWriter.convert(this.query.getQuery()));
	}
	
	public void setSchema(ObservableSchema schema) {
		this.schema = schema;
	}
	
	public void setQueriesListView(ListView<ObservableQuery> queriesView) {
		this.queriesList = queriesView.getItems();
	}
	
	private boolean validateQuery() {
		SQLLikeQueryReader queryReader = new SQLLikeQueryReader(this.schema.getSchema());
		
		Query<?> outputQuery = null;
		try {
			outputQuery = queryReader.fromString(this.queryEditArea.getText());
		} catch (Exception e) {
			log.warn("parseButtonPressed: error parsing query " + e.getClass().getSimpleName() + " " + e.getMessage());
		}
		
		if (outputQuery != null) {
			this.statusLabel.setText("Parse succeeded!");
			this.query.setQuery(outputQuery);
			return true;
		}
		this.statusLabel.setText("Parse failed.");
		return false;
	}
	
	private boolean validateQueryName() {
		String name = this.queryNameTextField.getText();
		if( name == null || name.length() == 0 ) {
			log.error("validateQueryName. Empty name!");
			return false;
		}
		this.query.setName(name);
		return true;
	}
	
	private boolean validateQueryName(String name) {
		if( name == null || name.length() == 0 ) {
			log.error("validateQueryName. Empty name!");
			return false;
		}
		// Check if the query list has a query with the same name.
		for (ObservableQuery query : this.queriesList) {
			if( query.getName().equals(name) ) {
				return false;
			}
		}
		this.query.setName(name);
		return true;
	}
	
	@FXML void okButtonPressed(ActionEvent event) {
		Stage stage = (Stage) this.okButton.getScene().getWindow();
		stage.close();
	}
}
