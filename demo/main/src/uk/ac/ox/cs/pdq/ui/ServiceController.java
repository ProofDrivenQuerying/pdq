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

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.util.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.ui.model.ObservableAccessMethod;
import uk.ac.ox.cs.pdq.ui.model.ObservableAttribute;
import uk.ac.ox.cs.pdq.datasources.services.service.RESTExecutableAccessMethodAttributeSpecification;
import uk.ac.ox.cs.pdq.datasources.services.service.RESTExecutableAccessMethodSpecification;
import uk.ac.ox.cs.pdq.datasources.services.service.Service;

// TODO: Auto-generated Javadoc
/**
 * Controller class for the relation's viewer/editor window.
 * @author Julien Leblay, Mark Ridler
 *
 */
public class ServiceController {

	/** RelationController logger. */
	private static Logger log = Logger.getLogger(ServiceController.class);

    /** The relation access methods. */
    @FXML private TableView<ObservableAccessMethod> relationAccessMethods;
    
    /** The relation attributes. */
    @FXML private TableView<ObservableAttribute> relationAttributes;
    
    /** The col access method name. */
    @FXML private TableColumn<ObservableAccessMethod, String> colAccessMethodName;
    
    /** The col access method type. */
    @FXML private TableColumn<ObservableAccessMethod, String> colAccessMethodType;
    
    /** The col attribute name. */
    @FXML private TableColumn<ObservableAttribute, String> colAttributeName;
    
    /** The col attribute relation name. */
    @FXML private TableColumn<ObservableAttribute, String> colAttributeRelationName;
    
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
	    this.colAttributeRelationName.setCellValueFactory(new PropertyValueFactory<ObservableAttribute, String>("relationName"));
	    this.colAttributeType.setCellValueFactory(new PropertyValueFactory<ObservableAttribute, String>("displayType"));

	    this.relationAccessMethods.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	    this.relationAccessMethods.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    this.colAccessMethodName.setCellValueFactory(new PropertyValueFactory<ObservableAccessMethod, String>("name"));
	    this.colAccessMethodType.setCellValueFactory(new PropertyValueFactory<ObservableAccessMethod, String>("type"));
	    
	    this.relationAccessMethods.getSelectionModel().selectedItemProperty().addListener(this.accessMethodSelected);
	    this.relationAccessMethods.addEventHandler(MouseEvent.MOUSE_ENTERED, this.accessMethodMouseEntered);
	}

	/** The access method mouse entered. */
	private EventHandler<MouseEvent> accessMethodMouseEntered = (MouseEvent arg0) -> {
		SelectionModel<ObservableAccessMethod> bsm = ServiceController.this.relationAccessMethods.getSelectionModel();
		ObservableAccessMethod b = bsm.selectedItemProperty().get();
		if (b != null) {
			ServiceController.this.selectedInputAttributes(b.getInputs());
		}
	};

	/** The access method selected. */
	private ChangeListener<ObservableAccessMethod> accessMethodSelected =
		(ObservableValue<? extends ObservableAccessMethod> arg0,
			ObservableAccessMethod oldValue, ObservableAccessMethod newValue) -> {
		if (newValue != null) {
			ServiceController.this.selectedInputAttributes(newValue.getInputs());
		}
	};
	
	/**
	 * Selected input attributes.
	 *
	 * @param inputs the inputs
	 */
	private void selectedInputAttributes(List<Integer> inputs) {
		SelectionModel<ObservableAttribute> sm = ServiceController.this.relationAttributes.getSelectionModel();
		sm.clearSelection();
		for (Integer i: inputs) {
			sm.select(i - 1);
		}
	}
	
    /** The service. */
    private Service service;

	/**
	 * Gets the relation.
	 *
	 * @return the relation
	 */
	public Service getService() {
		return this.service;
	}

	/**
	 * Sets the relation.
	 *
	 * @param relation the new relation
	 */
	public void setService(Service service) {
		this.service = service;
		this.refreshTables();
	}

	/**
	 * Refresh tables.
	 */
	private void refreshTables() {
		ObservableList<ObservableAttribute> attributes = this.relationAttributes.getItems();
		attributes.clear();
		for (RESTExecutableAccessMethodAttributeSpecification reams : this.service.getAccessMethod()[0].getAttributes()) {
			attributes.add(new ObservableAttribute(reams.getName(), reams.getType(), reams.getRelationAttribute()));
		}
		ObservableList<ObservableAccessMethod> accessMethods = this.relationAccessMethods.getItems();
		accessMethods.clear();
		for (RESTExecutableAccessMethodSpecification ream : this.service.getAccessMethod()) {
			AccessMethodDescriptor am = AccessMethodDescriptor.create(ream.getName(), new Integer[0]);
			accessMethods.add(new ObservableAccessMethod(am));
		}
	}
}
