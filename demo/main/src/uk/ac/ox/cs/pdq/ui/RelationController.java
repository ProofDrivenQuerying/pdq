package uk.ac.ox.cs.pdq.ui;

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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.ui.model.ObservableAccessMethod;
import uk.ac.ox.cs.pdq.ui.model.ObservableAttribute;

// TODO: Auto-generated Javadoc
/**
 * Controller class for the relation's viewer/editor window.
 * @author Julien Leblay
 *
 */
public class RelationController {

	/** RelationController logger. */
	private static Logger log = Logger.getLogger(RelationController.class);

    /** The relation access methods. */
    @FXML private TableView<ObservableAccessMethod> relationAccessMethods;
    
    /** The relation attributes. */
    @FXML private TableView<ObservableAttribute> relationAttributes;
    
    /** The col access method name. */
    @FXML private TableColumn<ObservableAccessMethod, String> colAccessMethodName;
    
    /** The col access method type. */
    @FXML private TableColumn<ObservableAccessMethod, Types> colAccessMethodType;
    
    /** The col attribute name. */
    @FXML private TableColumn<ObservableAttribute, String> colAttributeName;
    
    /** The col attribute type. */
    @FXML private TableColumn<ObservableAttribute, String> colAttributeType;

    /**
     * Initialize.
     */
    @FXML void initialize() {
        assert this.relationAccessMethods != null : "fx:id=\"relationAccessMethods\" was not injected: check your FXML file 'relation-editor.fxml'.";
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

	    this.relationAccessMethods.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	    this.relationAccessMethods.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    this.colAccessMethodName.setCellValueFactory(new PropertyValueFactory<ObservableAccessMethod, String>("name"));
	    this.colAccessMethodType.setCellValueFactory(new PropertyValueFactory<ObservableAccessMethod, Types>("type"));
	    
	    this.relationAccessMethods.getSelectionModel().selectedItemProperty().addListener(this.accessMethodSelected);
	    this.relationAccessMethods.addEventHandler(MouseEvent.MOUSE_ENTERED, this.accessMethodMouseEntered);
	}

	/** The access method mouse entered. */
	private EventHandler<MouseEvent> accessMethodMouseEntered = (MouseEvent arg0) -> {
		SelectionModel<ObservableAccessMethod> bsm = RelationController.this.relationAccessMethods.getSelectionModel();
		ObservableAccessMethod b = bsm.selectedItemProperty().get();
		if (b != null) {
			RelationController.this.selectedInputAttributes(b.getInputs());
		}
	};

	/** The access method selected. */
	private ChangeListener<ObservableAccessMethod> accessMethodSelected =
		(ObservableValue<? extends ObservableAccessMethod> arg0,
			ObservableAccessMethod oldValue, ObservableAccessMethod newValue) -> {
		if (newValue != null) {
			RelationController.this.selectedInputAttributes(newValue.getInputs());
		}
	};
	
	/**
	 * Selected input attributes.
	 *
	 * @param inputs the inputs
	 */
	private void selectedInputAttributes(List<Integer> inputs) {
		SelectionModel<ObservableAttribute> sm = RelationController.this.relationAttributes.getSelectionModel();
		sm.clearSelection();
		for (Integer i: inputs) {
			sm.select(i - 1);
		}
	}
	
    /** The relation. */
    private Relation relation;

	/**
	 * Gets the relation.
	 *
	 * @return the relation
	 */
	public Relation getRelation() {
		return this.relation;
	}

	/**
	 * Sets the relation.
	 *
	 * @param relation the new relation
	 */
	public void setRelation(Relation relation) {
		this.relation = relation;
		this.refreshTables();
	}

	/**
	 * Refresh tables.
	 */
	private void refreshTables() {
		ObservableList<ObservableAttribute> attributes = this.relationAttributes.getItems();
		attributes.clear();
		for (Attribute att: this.relation.getAttributes()) {
			attributes.add(new ObservableAttribute(att));
		}
		ObservableList<ObservableAccessMethod> accessMethods = this.relationAccessMethods.getItems();
		accessMethods.clear();
		for (AccessMethod am: this.relation.getAccessMethods()) {
			accessMethods.add(new ObservableAccessMethod(am));
		}
	}
}
