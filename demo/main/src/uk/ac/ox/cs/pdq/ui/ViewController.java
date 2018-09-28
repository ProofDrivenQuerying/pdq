package uk.ac.ox.cs.pdq.ui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.util.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.ui.model.ObservableAccessMethod;
import uk.ac.ox.cs.pdq.ui.model.ObservableAttribute;

// TODO: Auto-generated Javadoc
/**
 * Controller class for the relation's viewer/editor window.
 * @author Julien Leblay
 *
 */
public class ViewController {

	/** RelationController logger. */
	private static Logger log = Logger.getLogger(ViewController.class);

    /** The relation attributes. */
    @FXML private TableView<ObservableAttribute> relationAttributes;
    
    /** The col attribute name. */
    @FXML private TableColumn<ObservableAttribute, String> colAttributeName;
    
    /** The col attribute type. */
    @FXML private TableColumn<ObservableAttribute, String> colAttributeType;

    /** The view text area. */
    @FXML private TextArea viewTextArea;
    
    /**
     * Initialize.
     */
    @FXML void initialize() {
        assert this.relationAttributes != null : "fx:id=\"relationAttributes\" was not injected: check your FXML file 'relation-editor.fxml'.";
        this.configureTables();
    }

	/**
	 * Initialisation of the attributes and access methods tables.
	 */
	private void configureTables() {
		this.relationAttributes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	    this.relationAttributes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    this.colAttributeName.setCellValueFactory(new PropertyValueFactory<ObservableAttribute, String>("name"));
	    this.colAttributeType.setCellValueFactory(new PropertyValueFactory<ObservableAttribute, String>("displayType"));

	}

	/**
	 * Selected input attributes.
	 *
	 * @param inputs the inputs
	 */
	private void selectedInputAttributes(List<Integer> inputs) {
		SelectionModel<ObservableAttribute> sm = ViewController.this.relationAttributes.getSelectionModel();
		sm.clearSelection();
		for (Integer i: inputs) {
			sm.select(i - 1);
		}
	}
	
    /** The relation. */
    private View view;

	/**
	 * Gets the relation.
	 *
	 * @return the relation
	 */
	public View getView() {
		return this.view;
	}

	/**
	 * Sets the relation.
	 *
	 * @param relation the new relation
	 */
	public void setView(View view) {
		this.view = view;
		this.refreshTables();
	}

	/**
	 * Refresh tables.
	 */
	private void refreshTables() {
		ObservableList<ObservableAttribute> attributes = this.relationAttributes.getItems();
		attributes.clear();
		for (Attribute att: this.view.getAttributes()) {
			attributes.add(new ObservableAttribute(att));
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PrintStream(out).println(this.view.toString());
		this.viewTextArea.setText(out.toString());
	}
}
