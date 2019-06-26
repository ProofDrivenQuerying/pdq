package uk.ac.ox.cs.pdq.ui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.Callback;
//import uk.ac.ox.cs.pdq.fol.Query;
//import uk.ac.ox.cs.pdq.io.pretty.AlgebraLikeLeftDeepPlanWriter;
//import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Variable;
//import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters.ExecutorTypes;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;
import uk.ac.ox.cs.pdq.ui.model.ObservablePlan;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

// TODO: Auto-generated Javadoc
/**
 * The Class RuntimeController.
 */
public class RuntimeController {

	/** RuntimeController's logger. */
	private static Logger log = Logger.getLogger(RuntimeController.class);

    /** The runtime plan. */
    @FXML private ListView<Text> runtimePlan;
    
    /** The runtime results. */
    @FXML private TableView<Tuple> runtimeResults;
    
    /** The runtime pause button. */
    @FXML private Button runtimePauseButton;
    
    /** The runtime start button. */
    @FXML private Button runtimeStartButton;
    
    /** The runtime messages. */
    @FXML private Label runtimeMessages;

	/**  Icon for the pause button. */
	private final Image pauseIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/suspend.gif"));
	
	/**  Icon for the play button. */
	private final Image playIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/resume.gif"));
	
	/**  The future. */
	private Future<?> future;

	/**  true, if the execution is complete. */
	private Boolean complete = false;
	
    /**
     * Initialize.
     */
    @FXML
    void initialize() {
        assert this.runtimeMessages != null : "fx:id=\"runtimeMessages\" was not injected: check your FXML file 'runtime-window.fxml'.";
        assert this.runtimePauseButton != null : "fx:id=\"runtimePauseButton\" was not injected: check your FXML file 'runtime-window.fxml'.";
        assert this.runtimePlan != null : "fx:id=\"runtimePlan\" was not injected: check your FXML file 'runtime-window.fxml'.";
        assert this.runtimeResults != null : "fx:id=\"runtimeResults\" was not injected: check your FXML file 'runtime-window.fxml'.";
        assert this.runtimeStartButton != null : "fx:id=\"runtimeStartButton\" was not injected: check your FXML file 'runtime-window.fxml'.";

		this.runtimePauseButton.setGraphic(new ImageView(this.pauseIcon));
        this.runtimeStartButton.setGraphic(new ImageView(this.playIcon));

        this.runtimePauseButton.setDisable(true);
        
 }

	/**
	 * Pauses the runtime thread.
	 *
	 * @param event the event
	 */
   @FXML void pauseRunning(ActionEvent event) {
		Preconditions.checkNotNull(this.pauser);
		this.pauser.pause();
		this.runtimeStartButton.setDisable(false);
		this.runtimePauseButton.setDisable(true);
    }

	/**
	 * Starts or resumes the runtime thread.
	 *
	 * @param event the event
	 */
    @FXML void startRunning(ActionEvent event) {
		Preconditions.checkNotNull(this.plan);
		if (this.pauser == null) {
			final uk.ac.ox.cs.pdq.runtime.Runtime runtime =
					new uk.ac.ox.cs.pdq.runtime.Runtime(this.params, this.schema);
			this.configureColumns();
			
			ExecutorService executor = Executors.newFixedThreadPool(2);
			this.pauser = new Pauser(this.dataQueue, (int) 1e3);

			executor.execute(this.pauser);
			this.future = executor.submit(() -> {
				RuntimeController.this.executionStart = System.nanoTime();
				try {
					RelationalTerm rt = null;
					if (RuntimeController.this.plan instanceof RelationalTerm) 
						rt = (RelationalTerm)RuntimeController.this.plan; 
					else
						rt = (RelationalTerm)((ExecutablePlan)RuntimeController.this.plan).getDecoratedPlan();
					Table res = runtime.evaluatePlan(rt);
					List<Tuple> list = res.getData();
				   	for(Tuple tuple : list)
					{
						this.runtimeResults.getItems().add(tuple);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					throw new IllegalStateException();
				}
				RuntimeController.this.complete = true;
			});
		} else {
			this.pauser.resume();
		}
		this.runtimeStartButton.setDisable(true);
		this.runtimePauseButton.setDisable(false);
    }

	/**
	 * Interrupt runtime threads.
	 */
	public void interruptRuntimeThreads() {
		log.info("Interrupting runtime.");
		if (this.future != null) {
			this.future.cancel(true);
		}
	}

    /**
     * Configure columns.
     */
    private void configureColumns() {
    	ObservableList<TableColumn<Tuple, ?>> columns = this.runtimeResults.getColumns();
    	for (int i = 0, l = query.getFreeVariables().length; i < l; i++) {
    		TableColumn<Tuple, Object> column = new TableColumn<>(query.getFreeVariables()[i].toString());
    		column.setCellValueFactory(new TupleCellFactoryCallback(i));
    		columns.add(column);

    	}
    }
    
	/**
	 * Pauses the search thread.
	 *
	 * @param event the event
	 */
	@FXML void pausePlanning(ActionEvent event) {
		Preconditions.checkNotNull(this.pauser);
		this.pauser.pause();
		this.runtimeStartButton.setDisable(false);
		this.runtimePauseButton.setDisable(true);
	}
	
	/**  The schema to be used during this runtime session. */
	private Schema schema;
	
	/**  The query to be used during this runtime session. */
	private ConjunctiveQuery query;

	/**  The plan to run. */
	private Plan plan;

	/**  The parameters to run the plan on. */
	private RuntimeParameters params = new RuntimeParameters();
	
	/**  The runtime session thread executor. */
	private Pauser pauser;
	
	/** The execution start. */
	private Long executionStart = 0l;
	
	/** Queue containing the next tuples to display in the result views. */
	private ConcurrentLinkedQueue<Tuple> dataQueue = new ConcurrentLinkedQueue<>();

	/**
	 * Default constructor, start the animation timer.
	 */
	public RuntimeController() {
		this.prepareTimeline();
	}

	/**
	 * Sets the query backing this runtime session.
	 *
	 * @param query the new query
	 */
	void setQuery(ObservableQuery query) {
		this.query = ConjunctiveQuery.create(query.getQuery().getFreeVariables(), query.getQuery().getAtoms());
		Preconditions.checkNotNull(this.query);
		Preconditions.checkState(this.query instanceof ConjunctiveQuery); 
		this.runtimeResults.getColumns().clear();
	}

	/**
	 * Sets the schema backing this runtime session.
	 *
	 * @param schema the new schema
	 */
	void setSchema(ObservableSchema schema) {
		this.schema = schema.getSchema();
		Preconditions.checkNotNull(this.schema);
	}

	/**
	 * Sets the plan backing this runtime session.
	 *
	 * @param p the new plan
	 */
	void setPlan(ObservablePlan p) {
		Preconditions.checkArgument(p != null);
	this.plan = p.getPlan();
		Preconditions.checkNotNull(this.plan);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		new PrintStream(bos).println(this.plan.toString());
		for (String c : bos.toString().split("\n")) {
			this.runtimePlan.getItems().add(new Text(c));
		}
		
		try {
			final uk.ac.ox.cs.pdq.runtime.Runtime runtime =
					new uk.ac.ox.cs.pdq.runtime.Runtime(this.params, this.schema);
			RelationalTerm rt = null;
			if (RuntimeController.this.plan instanceof RelationalTerm) 
				rt = (RelationalTerm)RuntimeController.this.plan; 
			else
				rt = (RelationalTerm)((ExecutablePlan)RuntimeController.this.plan).getDecoratedPlan();
			ExecutablePlan ep = runtime.decoratePlan(rt);
		}
		catch(Exception e)
		{
			   System.out.println("Runtime has exceptions");
		       this.runtimeStartButton.setDisable(true);
		}
}

	/**
	 * Sets the executor type to use this runtime session.
	 *
	 * @param type the new executor type
	 */
	void setExecutorType(ExecutorTypes type) {
		Preconditions.checkArgument(type != null);
		this.params.setExecutorType(type);
	}
    

	/**
	 * Sets the number of output tuples to return.
	 *
	 * @param limit the new tuples limit
	 */
	void setTuplesLimit(Integer limit) {
		if(limit == null || limit < 0) {
		}
		else {
		}
	}

	/**
	 * Update the plan/search views.
	 */
	void udpateWidgets() {
		while (this.dataQueue != null && !this.dataQueue.isEmpty()) {
			Tuple tuple = this.dataQueue.poll();
			// Update the result table
			this.runtimeResults.getItems().add(tuple);
			RuntimeController.this.runtimeMessages.setText(this.runtimeResults.getItems().size() + " results in " + (System.nanoTime() - this.executionStart) / 1e6 + "ms.");
		}
		// When the data is set to null, the execution is over.
		if (this.dataQueue.isEmpty() && this.complete) {
			this.runtimeStartButton.setDisable(true);
			this.runtimePauseButton.setDisable(true);
		}
	}

	/**
	 * Animation timer, required to update plan/search views from the main JavaFX thread.
	 */
	private void prepareTimeline() {
		// Every frame to take any dataQueue from queue and add to chart
		new AnimationTimer() {
			@Override
			public void handle(long now) {
				RuntimeController.this.udpateWidgets();
			}
		}.start();
	}
	
	/**
	 * Custom table cell value factory for arbitrary tuples.
	 * 
	 * @author Julien Leblay
	 */
	private static class TupleCellFactoryCallback
				implements Callback<CellDataFeatures<Tuple, Object>, ObservableValue<Object>> {

		/**  Index of the value to display within the tuple. */
		private final int index;
		
		/**
		 * Default constructor.
		 *
		 * @param i the i
		 */
		public TupleCellFactoryCallback(int i) {
			this.index = i;
		}
		
		/**
		 * Called be the table render to display the value of a table cell.
		 *
		 * @param t the t
		 * @return the observable value
		 */
		@Override
		public ObservableValue<Object> call(CellDataFeatures<Tuple, Object> t) {
			return new ReadOnlyObjectWrapper<>(t.getValue().getValue(this.index));
		}
		
	}
}
