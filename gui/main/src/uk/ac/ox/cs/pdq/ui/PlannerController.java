
package uk.ac.ox.cs.pdq.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import prefuse.controls.FocusControl;
import prefuse.controls.WheelZoomControl;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.BestPlanMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.DominanceMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.Metadata;
//import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.BestPlanMetadata;
//import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.DominanceMetadata;
//import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.Metadata;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.ui.event.PlanSearchVisualizer;
import uk.ac.ox.cs.pdq.ui.event.PrefuseEventHandler;
import uk.ac.ox.cs.pdq.ui.model.ObservablePlan;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;
import uk.ac.ox.cs.pdq.ui.model.ObservableSearchState;
import uk.ac.ox.cs.pdq.ui.prefuse.control.AggregateDragControl;
import uk.ac.ox.cs.pdq.ui.prefuse.control.ClickControl;
import uk.ac.ox.cs.pdq.ui.prefuse.control.HoverControl;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;
import uk.ac.ox.cs.pdq.ui.proof.Proof;
import uk.ac.ox.cs.pdq.ui.util.DecimalConverter;
import uk.ac.ox.cs.pdq.ui.util.LogarithmicAxis;

// TODO: Auto-generated Javadoc
/**
 * The Class PlannerController.
 */
public class PlannerController {

	/** PlannerController's logger. */
	private static Logger log = Logger.getLogger(PlannerController.class);

	/**
	 * The Enum Status.
	 */
	enum Status {/** The not started. */
		NOT_STARTED, /** The started. */
		STARTED, /** The paused. */
		PAUSED, /** The complete. */
		COMPLETE}

	/**  Icon for the pause button. */
	private final Image pauseIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/suspend.gif"));

	/**  Icon for the play button. */
	private final Image playIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/resume.gif"));

	/** The cost reduction chart. */
	// This controller's widgets
	@FXML private LineChart<Number, Number> costReductionChart;

	/** The plan statistics table. */
	@FXML private TableView<ObservableSearchState> planStatisticsTable;

	/** The col stats cost. */
	@FXML private TableColumn<ObservableSearchState, Number> colStatsCost;

	/** The col stats iterations. */
	@FXML private TableColumn<ObservableSearchState, Integer> colStatsIterations;

	/** The col stats time. */
	@FXML private TableColumn<ObservableSearchState, Double> colStatsTime;

	/** The planner pause button. */
	@FXML private Button plannerPauseButton;

	/** The planner start button. */
	@FXML private Button plannerStartButton;

	/** The search space controls. */
	@FXML private VBox searchSpaceControls;

	/** The search space vizualization area. */
	@FXML private AnchorPane searchSpaceVizualizationArea;

	/** The planner messages. */
	@FXML private Label plannerMessages;

	/** The plan view area. */
	@FXML private ListView<Text> planViewArea;

	/** The proof view area. */
	@FXML private TextArea proofViewArea;

	/** The planner tabs. */
	@FXML private TabPane plannerTabs;

	/** The planner tab space. */
	@FXML private Tab plannerTabSpace;

	/** The planner tab cost. */
	@FXML private Tab plannerTabCost;

	/** The planner tab plan. */
	@FXML private Tab plannerTabPlan;

	/** The search space split pane. */
	@FXML private SplitPane searchSpaceSplitPane;

	/** The search space metadata general. */
	@FXML private TextArea searchSpaceMetadataGeneral;

	/** The search space metadata candidates. */
	@FXML private TextArea searchSpaceMetadataCandidates;

	/** The search space metadata dominance. */
	//    @FXML private TextArea searchSpaceMetadataEquivalence;
	@FXML private TextArea searchSpaceMetadataDominance;

	/** The search space metadata success. */
	@FXML private TextArea searchSpaceMetadataSuccess;

	/** The search space metadata tabs. */
	@FXML private TabPane searchSpaceMetadataTabs;

	/** The search space metadata general tab. */
	@FXML private Tab searchSpaceMetadataGeneralTab;

	/** The search space metadata candidates tab. */
	@FXML private Tab searchSpaceMetadataCandidatesTab;

	/** The search space metadata dominance tab. */
	//    @FXML private Tab searchSpaceMetadataEquivalenceTab;
	@FXML private Tab searchSpaceMetadataDominanceTab;

	/** The search space metadata success tab. */
	@FXML private Tab searchSpaceMetadataSuccessTab;

	/** The plan proof tab. */
	@FXML private Tab planProofTab;

	/** The plan selection. */
	@FXML private ComboBox<String> planSelection = new ComboBox();

	/** The selected plan view area. */
	@FXML private ListView<Text> selectedPlanViewArea;

	/** The selected proof view area. */
	@FXML private TextArea selectedProofViewArea;

	/**  Sorted set of the plans found *. */
	private TreeSet<ObservableSearchState> plansFound = new TreeSet<>(new PlanComparator());

	/**
	 * Controller's widget's initialization.
	 */
	@FXML void initialize() {
		assert this.costReductionChart != null : "fx:id=\"costReductionChart\" was not injected: check your FXML file 'planner-window.fxml'.";
		assert this.planStatisticsTable != null : "fx:id=\"planStatisticsTable\" was not injected: check your FXML file 'planner-window.fxml'.";
		assert this.plannerPauseButton != null : "fx:id=\"plannerPauseButton\" was not injected: check your FXML file 'planner-window.fxml'.";
		assert this.plannerStartButton != null : "fx:id=\"plannerStartButton\" was not injected: check your FXML file 'planner-window.fxml'.";
		assert this.searchSpaceVizualizationArea != null : "fx:id=\"searchSpaceVizualizationArea\" was not injected: check your FXML file 'planner-window.fxml'.";
		assert this.plannerMessages != null : "fx:id=\"plannerMessages\" was not injected: check your FXML file 'planner-window.fxml'.";
		assert this.planSelection != null : "fx:id=\"planSelection\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.selectedPlanViewArea != null : "fx:id=\"selectedPlanViewArea\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.selectedProofViewArea != null : "fx:id=\"selectedProofViewArea\" was not injected: check your FXML file 'root-window.fxml'.";

		this.plannerPauseButton.setGraphic(new ImageView(this.pauseIcon));
		this.plannerStartButton.setGraphic(new ImageView(this.playIcon));

		this.plannerTabCost.setOnSelectionChanged((Event arg0) ->
		PlannerController.this.plannerTabCost.getContent().autosize());
		this.plannerPauseButton.setDisable(true);
		this.planStatisticsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.planStatisticsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.planStatisticsTable.setItems(FXCollections.<ObservableSearchState>observableArrayList());
		this.colStatsTime.setCellValueFactory(new PropertyValueFactory<ObservableSearchState, Double>("time"));
		this.colStatsIterations.setCellValueFactory(new PropertyValueFactory<ObservableSearchState, Integer>("iterations"));
		this.colStatsCost.setCellValueFactory(new PropertyValueFactory<ObservableSearchState, Number>("cost"));
		this.costReductionChart.getData().add(this.costReductionSeries);
		this.costReductionChart.setLegendVisible(false);
		this.costReductionChart.setAnimated(false);
		this.costReductionSeries.getNode().getStyleClass().add("cost-reduction-series");
		((LogarithmicAxis) this.costReductionChart.getYAxis()).setTickLabelFormatter(new DecimalConverter());
		this.planSelection.getItems().clear();
	}

	/**  The parameters to be used during this planning session. */
	private PlannerParameters params;

	/**  The cost parameters to be used by the cost function. */
	private CostParameters costParams;

	/**  Keeps the reasoning parameters. */
	private ReasoningParameters reasoningParams;

	/**  Keeps the reasoning parameters. */
	private DatabaseParameters databaseParams;

	/**  The schema to be used during this planning session. */
	private Schema schema;

	/**  A copy of the accessible schema to be used during this planning session. */
	private AccessibleSchema accSchema;

	/**  The query to be used during this planning session. */
	private ConjunctiveQuery query;

	/**  The previous plan obtained with the setting of this planning session. */
	private ObservablePlan plan;

	/** Queue containing the plan found. */
	private ConcurrentLinkedQueue<Object> planQueue;

	/** Queue containing the next data points to display in the plan/search views. */
	private ConcurrentLinkedQueue dataQueue = new ConcurrentLinkedQueue<>();

	/** The data series of the cost reduction chart. */
	private final XYChart.Series<Number, Number> costReductionSeries = new Series<>();

	/**  Cost of the first plan found. */
	private Number initialCost = null;

	/**  The pauser. */
	private Pauser pauser;

	/**  The future. */
	private Future<?> future;

	/** The best plan. */
	private Plan bestPlan;

	/** The best proof. */
	private Proof bestProof;

	/**
	 * Default constructor, start the animation timer.
	 */
	public PlannerController() {
		this.prepareTimeline();
	}

	/**
	 * Sets the query backing this planning session.
	 *
	 * @param query the new query
	 */
	void setQuery(ObservableQuery query) {
		if(!(query.getFormula() instanceof ConjunctiveQuery))
			throw new RuntimeException("Only Conjunctive Queries Supported Currently");
		this.query = (ConjunctiveQuery) query.getFormula();
	}

	/**
	 * Sets the schema backing this planning session.
	 *
	 * @param schema the new schema
	 */
	void setSchema(ObservableSchema schema) {
		this.schema = schema.getSchema();
		this.accSchema = new AccessibleSchema(this.schema);
	}

	/**
	 * Sets the original plan if any of this planning session.
	 *
	 * @param q the new plan queue
	 */
	void setPlanQueue(ConcurrentLinkedQueue q) {
		this.planQueue = q;
	}

	/**
	 * Sets the original plan if any of this planning session.
	 *
	 * @param plan the new plan
	 */
	void setPlan(ObservablePlan plan) {
		String homeDir = System.getenv("HOME");
		// If HOME is not set try HOMEPATH (Windows)
		if (homeDir == null) {
			homeDir = System.getenv("HOMEPATH");
		}
		if (homeDir == null) {
			log.warn("No HOME directory defined. Using '.' as default");
			homeDir = ".";
		}
		File workDir = new File(homeDir + '/' + PDQApplication.WORK_DIRECTORY);
		if (workDir.exists()) {
			this.params = new PlannerParameters(new File(workDir.getAbsolutePath() + '/' + PDQApplication.DEFAULT_CONFIGURATION));
			this.costParams = new CostParameters(new File(workDir.getAbsolutePath() + '/' + PDQApplication.DEFAULT_CONFIGURATION));
			this.reasoningParams = new ReasoningParameters(new File(workDir.getAbsolutePath() + '/' + PDQApplication.DEFAULT_CONFIGURATION));
			this.databaseParams = DatabaseParameters.Empty;
		} else {
			log.info("No default configuration file. Initializing demo environment...");
			this.params = new PlannerParameters();
			this.costParams = new CostParameters();
			this.reasoningParams = new ReasoningParameters();
			this.databaseParams = DatabaseParameters.Empty;
		}
		this.plan = plan;
		this.params.setSeed(1);
		this.params.setPlannerType(this.plan.getPlannerType());
		this.params.setTimeout(this.plan.getTimeout());
		this.params.setMaxIterations(this.plan.getMaxIterations());
		this.params.setQueryMatchInterval(this.plan.getQueryMatchInterval());
		this.costParams.setCostType(this.plan.getCostType());
		this.reasoningParams.setReasoningType(this.plan.getReasoningType());
		//		this.reasoningParams.setBlockingInterval(this.plan.getBlockingInterval());
	}

	/**
	 * Sets the search space visualizer.
	 *
	 * @param planner the new search space visualizer
	 */
	private void setSearchSpaceVisualizer(final ExplorationSetUp planner) {
		final SwingNode swingNode = new SwingNode();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				PrefuseVisualizer visualizer = new PrefuseVisualizer();
				visualizer.addControl(new AggregateDragControl());
				visualizer.addControl(new FocusControl()); 
				visualizer.addControl(new WheelZoomControl());
				visualizer.addControl(visualizer.getPathHighlightControl());
				visualizer.addControl(new HoverControl());
				visualizer.addControl(new ClickControl(PlannerController.this.dataQueue));
				planner.registerEventHandler(
						new PrefuseEventHandler(visualizer.getGraph(),
								visualizer.getAggregateTable(),  visualizer.getVisualization(), 
								"aggregates", "graph.nodes", "color", "layout",
								visualizer.getPathHighlightControl(),
								visualizer.getPathsHighlightBox()));
				swingNode.setContent(visualizer);
			}
		});
		this.searchSpaceVizualizationArea.getChildren().add(swingNode);
		AnchorPane.setTopAnchor(swingNode, 0.);
		AnchorPane.setBottomAnchor(swingNode, 0.);
		AnchorPane.setRightAnchor(swingNode, 0.);
		AnchorPane.setLeftAnchor(swingNode, 0.);
	}

	/**
	 * Starts or resumes the search thread.
	 *
	 * @param event the event
	 */
	@FXML void startPlanning(ActionEvent event) {
		Preconditions.checkNotNull(this.params);
		Preconditions.checkNotNull(this.schema);
		Preconditions.checkNotNull(this.query);
		if (this.pauser == null) {

			final ExplorationSetUp planner = new ExplorationSetUp(this.params, this.costParams, this.reasoningParams, this.databaseParams, this.schema);
			registerEvents(planner);
			this.pauser = new Pauser(this.dataQueue, 99999);
			ExecutorService executor = Executors.newFixedThreadPool(2);
			executor.execute(this.pauser);
			this.future = executor.submit(() -> {
				try {
					log.debug("Searching plan...");
					Map.Entry<RelationalTerm, Cost> bestPlan = planner.search(this.query);
					if(bestPlan != null)
					{
						PlannerController.this.bestPlan = bestPlan.getKey();
						log.debug("Best plan: " + bestPlan);
						ObservablePlan p = PlannerController.this.plan.copy();
						p.setPlan(bestPlan.getKey());
						p.setProof(PlannerController.this.bestProof);
						p.setCost(bestPlan.getValue());
						p.store();
						PlannerController.this.planQueue.add(p);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					throw new IllegalStateException();
				}
				PlannerController.this.dataQueue.add(Status.COMPLETE);
			});
		} else {
			this.pauser.resume();
		}
		this.plannerStartButton.setDisable(true);
		this.plannerPauseButton.setDisable(false);
	}

	/**
	 * Registers the 2 event subscribers. And waits for them to finish.
	 *
	 * @param planner the planner
	 */
private void registerEvents(final ExplorationSetUp planner) {
		this.setSearchSpaceVisualizer(planner);
		planner.registerEventHandler(new PlanSearchVisualizer(this.dataQueue, 1));
		try
		{
			Thread.sleep(500);
		}
		catch(InterruptedException e)
		{
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
		this.plannerStartButton.setDisable(false);
		this.plannerPauseButton.setDisable(true);
	}

	/**
	 * Interrupt planning threads.
	 */
	public void interruptPlanningThreads() {
		if (this.future != null) {
			this.future.cancel(true);
		}
	}

	/**
	 * Update the plan/search views.
	 */
	private void udpateWidgets() {
		while (this.dataQueue != null && !this.dataQueue.isEmpty()) {
			Object o  = this.dataQueue.poll();
			if (o == Status.COMPLETE) {
				this.displayStatusComplete();
				continue;
			}
			if (o instanceof SearchNode) {
				this.displaySearchNodeInfo((SearchNode) o);
				continue;
			}
			if (o instanceof SearchEdge) {
				this.displaySearchEdgeInfo((SearchEdge) o);
				continue;
			}
			if (o instanceof ObservableSearchState) {
				ObservableSearchState state = (ObservableSearchState) o;
				// Update the table & chart
				this.planStatisticsTable.getItems().add(state);
				this.updateCostTab(state);
				this.updatePlanTab(state.getPlan());
				this.updateProofTab(state.getProof());
				this.updatePlansFound(state);
			}
		}
	}

	/**
	 * Update plans found.
	 *
	 * @param p the p
	 */
	void updatePlansFound(ObservableSearchState p) {
		if(!this.plansFound.contains(p)) {
			this.plansFound.add(p);
			int size = this.planSelection.getItems().size();
			this.planSelection.getItems().add("Plan " + new Integer(size + 1).toString());
			this.planSelection.getSelectionModel().selectedIndexProperty().addListener(this.viewPlan);
		}
	}

	/**
	 * Behaviour triggered when a plan is selected.
	 */
	private final ChangeListener<? super Number> viewPlan =
			(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) -> {
						Iterator<ObservableSearchState> iterator = this.plansFound.iterator();
						int i = 0;
						while(iterator.hasNext() && i < newValue.intValue()) {
							iterator.next();
							++i;
						}
						ObservableSearchState p = iterator.next();	
						this.displayPlan(this.selectedPlanViewArea, p.getPlan());
						this.displayProof(this.selectedProofViewArea, p.getProof());
					};

					/**
					 * Update plan tab.
					 *
					 * @param pplan the pplan
					 */
					private void updatePlanTab(Plan pplan) {
						this.planViewArea.getItems().clear();
						if (pplan != null && pplan instanceof Plan) {
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							PrintStream pbos = new PrintStream(bos);
							PDQController.displayPlanSubtype(pbos, pplan, 0);
							this.planViewArea.getItems().add(new Text(bos.toString()));
						} else if (pplan != null) {
							log.warn("Display of " + pplan.getClass().getSimpleName() + " plans not yet supported.");
							this.planViewArea.getItems().add(new Text("<Non linear plan selected>"));
						} else {
							this.planViewArea.getItems().add(new Text("<No plan>"));
						}
					}

					/**
					 * Update proof tab.
					 *
					 * @param pr the pr
					 */
					private void updateProofTab(Proof pr) {
						this.proofViewArea.clear();
						if (pr != null) {
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							new PrintStream(bos).println(pr.toString());
							this.proofViewArea.setText(bos.toString());
							this.bestProof = pr;
						}
					}

					/**
					 * Update cost tab.
					 *
					 * @param state the state
					 */
					private void updateCostTab(ObservableSearchState state) {
						Number cost = state.getCost();
						if (cost != null) {
							NumberAxis xAxis = (NumberAxis) this.costReductionChart.getXAxis();
							LogarithmicAxis yAxis = (LogarithmicAxis) this.costReductionChart.getYAxis(); 
							if (this.initialCost == null) {
								this.initialCost = cost;
								xAxis.setLowerBound(state.getTime().doubleValue()); 
								yAxis.setUpperBound(cost.doubleValue() + 10);
								yAxis.setLowerBound(cost.doubleValue() - 10);
							}
							if (cost.doubleValue() > yAxis.getUpperBound()) {
								yAxis.setUpperBound(cost.doubleValue());
							}
							if (cost.doubleValue() < yAxis.getLowerBound()) {
								yAxis.setLowerBound(cost.doubleValue());
							}
							this.costReductionSeries.getData().add(
									new XYChart.Data<>(state.getTime(), cost));
						}
					}

					/**
					 * Display plan.
					 *
					 * @param area the area
					 * @param p the p
					 */
					void displayPlan(ListView<Text> area, Plan p) {
						area.getItems().clear();
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							PrintStream pbos = new PrintStream(bos);
							PDQController.displayPlanSubtype(pbos, p, 0);
							area.getItems().add(new Text(bos.toString()));
					}

					/**
					 * Display proof.
					 *
					 * @param area the area
					 * @param p the p
					 */
					void displayProof(TextArea area, Proof p) {
						area.clear();
						if (p != null) {
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							new PrintStream(bos).println(p.toString());
							area.setText(bos.toString());
						} 					
					}

					/**
					 * Display status complete.
					 */
					private void displayStatusComplete() {
						this.plannerMessages.setText("Plan search complete.");
						this.plannerPauseButton.setDisable(true);
						this.searchSpaceMetadataCandidatesTab.setDisable(true);
						PlannerController.this.updatePlanTab(this.bestPlan);
						PlannerController.this.updateProofTab(PlannerController.this.bestProof);
					}

					/**
					 * Display search node info.
					 *
					 * @param n the n
					 */
					private void displaySearchNodeInfo(SearchNode n) {
						this.searchSpaceSplitPane.setDividerPositions(.66);
						this.updateGeneralMetadata(n);
						this.updateCandidatesMetadata(n);
						//		this.updateEquivalenceMetadata(n);
						this.updatePruningMetadata(n);
						this.updateSuccessMetadata(n);
						this.searchSpaceMetadataTabs.getSelectionModel().select(0);
					}

					/**
					 * Display search edge info.
					 *
					 * @param e the e
					 */
					private void displaySearchEdgeInfo(SearchEdge e) {
						this.searchSpaceSplitPane.setDividerPositions(.66);
						this.updateEdgeMetadata(e.edge, e.node);
						this.searchSpaceMetadataTabs.getSelectionModel().select(0);
					}

					/**
					 * Update general metadata.
					 *
					 * @param node the node
					 */
					public void updateGeneralMetadata(SearchNode node) {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						new PrintStream(bos).println(node.getConfiguration().getPlan().toString());
						this.searchSpaceMetadataGeneral.setText(
								"Type: " + node.getStatus() + "\n\n" + 
										"Middleware query commands:\n" + bos);
					}

					/**
					 * Update candidates metadata.
					 *
					 * @param node the node
					 */
					public void updateCandidatesMetadata(SearchNode node) {
						if (node != null && node.getStatus() == NodeStatus.ONGOING) {
							if (!node.getConfiguration().getCandidates().isEmpty()) {
								this.searchSpaceMetadataCandidatesTab.setDisable(false);
								int c = 1;
								String s = "";
								for(Candidate candidate: node.getConfiguration().getCandidates()) {
									s += "Candidate " + (c++) + ":\n" + candidate;
								}
								this.searchSpaceMetadataCandidates.setText(s);
								return;
							}
						}
						this.searchSpaceMetadataCandidatesTab.setDisable(true);
						this.searchSpaceMetadataCandidates.setText("No candidate");
					}

					/**
					 * Update success metadata.
					 *
					 * @param node the node
					 */
					public void updateSuccessMetadata(SearchNode node) {
						Metadata m = node.getMetadata();
						if (m instanceof BestPlanMetadata && ((BestPlanMetadata) m).getPlan() != null) {
							BestPlanMetadata metadata = (BestPlanMetadata) m;
							ByteArrayOutputStream prBos = new ByteArrayOutputStream();
							ByteArrayOutputStream plBos = new ByteArrayOutputStream();
							new PrintStream(plBos).println(metadata.getPlan().toString());
							new PrintStream(prBos).println(Proof.toProof(metadata.getConfigurations()).toString());
							this.searchSpaceMetadataSuccessTab.setDisable(false);
							this.searchSpaceMetadataSuccess.setText(
									"*******************\n* Proof \n*******************\n" + prBos + "\n\n" +
											"*******************\n* Plan\n*******************\n" + plBos + "\n\n" +
											"*******************\n* Cost\n*******************\n" + "0.0\n\n");
						} else {
							this.searchSpaceMetadataSuccess.setText("");
							this.searchSpaceMetadataSuccessTab.setDisable(true);
						}
					} 

					/**
					 * Update edge metadata.
					 *
					 * @param type the type
					 * @param node the node
					 */
					public void updateEdgeMetadata(EdgeTypes type, SearchNode node) {
						String str = "Type: " + type + "\n";
						if (type != EdgeTypes.POINTER) {
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							new PrintStream(bos).println(node.getConfiguration().getPlan().toString());
							str += "\nMiddlewarecommands:\n" + bos;
						}
						this.searchSpaceMetadataGeneral.setText(str);
						this.searchSpaceMetadataGeneralTab.setDisable(false);
						this.searchSpaceMetadataCandidatesTab.setDisable(true);
						this.searchSpaceMetadataDominanceTab.setDisable(true);
						this.searchSpaceMetadataSuccessTab.setDisable(true);
					}


					/**
					 * Update pruning metadata.
					 *
					 * @param node the node
					 */
					public void updatePruningMetadata(SearchNode node) {
						Metadata metadata = node.getMetadata();
						if (metadata instanceof DominanceMetadata) {
							this.searchSpaceMetadataDominanceTab.setDisable(false);
							ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
							new PrintStream(bos1).println(((DominanceMetadata) metadata).getDominatedPlan().toString());
							ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
							new PrintStream(bos2).println(((DominanceMetadata) metadata).getDominancePlan().toString());
							switch (((DominanceMetadata) metadata).getType()) {
							case DOMINANCE:
								this.searchSpaceMetadataDominance.setText(
										"Dominator node\n" + ((DominanceMetadata) metadata).getDominance() + "\n\n" + 
												"Dominated plan\n" + bos1 + "\n\n" +
												"Cost of dominated plan\n" + "0.0" + "\n\n"+
												"Dominator's plan\n" + bos2 + "\n\n" +
												"Cost of dominator plan\n" + "0.0" + "\n\n");
								break;
							case COST:	
								this.searchSpaceMetadataDominance.setText(
										"Plan\n" + bos1 + "\n\n" +
												"Plan's cost\n" + "0.0" + "\n\n"+
												"Best plan\n" + bos2 + "\n\n" +
												"Best plan's cost\n" + "0.0" + "\n\n");
								break;
							}
						} else {
							this.searchSpaceMetadataDominance.setText("");
							this.searchSpaceMetadataDominanceTab.setDisable(true);
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
								PlannerController.this.udpateWidgets();
							}
						}.start();
					}

					/**
					 * A Edge - node pair, use to pass information to the search space 
					 * visualizer.
					 * @author Julien Leblay
					 *
					 */
					public static class SearchEdge {

						/** The node. */
						final SearchNode node;

						/** The edge. */
						final EdgeTypes edge;

						/**
						 * Instantiates a new search edge.
						 *
						 * @param node the node
						 * @param edge the edge
						 */
						public SearchEdge(SearchNode node, EdgeTypes edge) {
							this.node = node;
							this.edge = edge;
						}
					}

					/**
					 * The Class PlanComparator.
					 */
					public static class PlanComparator implements Comparator<ObservableSearchState>{

						/* (non-Javadoc)
						 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
						 */
						@Override
						public int compare(ObservableSearchState o1, ObservableSearchState o2) {
							if(o1.getCost().doubleValue() > o2.getCost().doubleValue()) {
								return 1;
							}
							else if(o1.getCost().doubleValue() < o2.getCost().doubleValue()) {
								return -1;
							}
							else {
								return 0;
							}
						}
					}

}
