package uk.ac.ox.cs.pdq.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.distortion.Distortion;
import prefuse.action.distortion.FisheyeDistortion;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.Activity;
import prefuse.controls.Control;
import prefuse.data.Graph;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import uk.ac.ox.cs.pdq.ui.prefuse.color.CustomColorAction;
import uk.ac.ox.cs.pdq.ui.prefuse.color.Palettes;
import uk.ac.ox.cs.pdq.ui.prefuse.control.HighlightButton;
import uk.ac.ox.cs.pdq.ui.prefuse.control.PathHighlightControl;
import uk.ac.ox.cs.pdq.ui.prefuse.layout.AggregateLayout;
import uk.ac.ox.cs.pdq.ui.prefuse.renderer.EdgeShapeRenderer;
import uk.ac.ox.cs.pdq.ui.prefuse.schema.SchemaFactory;

/**
 * Runs regression tests.
 * 
 * @author Julien Leblay
 */
public class PrefuseVisualizer extends JComponent {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(PrefuseVisualizer.class);

	private static String GRAPH = "graph";
	private static String EDGES = "graph.edges";
	private static String NODES = "graph.nodes";
	private static String AGGREGATES = "aggregates";
	private static String AGGREGATE_PREDICATE = "ingroup('aggregates')";
	
	private static String HIGHLIGHT_SUCCESSFUL = "LEFT(type, 1) == 'S'";
	private static String HIGHLIGHT_ONGOING = "LEFT(type, 1) == 'O'";
	private static String HIGHLIGHT_NOCANDIDATES = "LEFT(type, 1) == 'N'";
	private static String HIGHLIGHT_TERMINAL = "LEFT(type, 1) == 'T'";
	private static String HIGHLIGHT_DEAD = "LEFT(type, 1) == 'D'";
	
	private static String COLOR_ACTION = "color";
	private static String LAYOUT_ACTION = "layout";
	
	private final JPanel controls;
	private final Display display;
	private final Graph graph;
	private final Visualization visualization;
	private final AggregateTable aggregateTable;
	private final PathHighlightControl pathHighlightControl;
	
	private final JSlider pathHighlightSlider;
	private final Distortion fish;
	
	
	public PrefuseVisualizer() {
		super();

		this.graph = SchemaFactory.createGraph();
		this.visualization = new Visualization();
		this.visualization.addGraph(GRAPH, this.graph);
		this.visualization.setInteractive(EDGES, null, false);
		this.aggregateTable = SchemaFactory.createAggregateTable(this.visualization, AGGREGATES);
		this.pathHighlightControl = new PathHighlightControl();
		this.pathHighlightSlider = new JSlider(JSlider.VERTICAL);
		this.fish = new FisheyeDistortion(3,3);

		this.setupRenderers();
		this.controls = this.setupControls();
		this.display = this.setupDisplay();

		this.visualization.run(COLOR_ACTION);

		this.setLayout(new BorderLayout());
		this.add(this.controls, BorderLayout.WEST);
		this.add(this.display, BorderLayout.CENTER);

		new Timer(100, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrefuseVisualizer.this.visualization.run(COLOR_ACTION);
			}
		}).start();
	}

	private void setupRenderers() {
		LabelRenderer r = new LabelRenderer("id");
		r.setRoundedCorner(8, 8); // round the corners
		
		//---NodeShapeRenderer r = new NodeShapeRenderer();
		

		// draw aggregates as polygons with curved edges
		Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
		((PolygonRenderer)polyR).setCurveSlack(0.15f);

		DefaultRendererFactory rendererFactory = new DefaultRendererFactory(r);
		rendererFactory.add(AGGREGATE_PREDICATE, polyR);

		EdgeShapeRenderer edgeRenderer = new EdgeShapeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_FORWARD);
		rendererFactory.setDefaultEdgeRenderer(edgeRenderer);

		//EdgeRenderer edgeRenderer = new EdgeRenderer(prefuse.Constants.EDGE_TYPE_LINE, prefuse.Constants.EDGE_ARROW_FORWARD);
		//rendererFactory.setDefaultEdgeRenderer(edgeRenderer);

		// create a new default renderer factory
		// return our name label renderer as the default for all non-EdgeItems
		// includes straight line edges for EdgeItems by default
		this.visualization.setRendererFactory(rendererFactory);
	}
	
	private JPanel setupControls() {
		Palettes palettes = new Palettes();


		// map nominal data values to colors using our provided palette
		ColorAction nFill = new CustomColorAction(NODES, "type", Constants.NOMINAL, VisualItem.FILLCOLOR, palettes.getNodePalette());
		
		DataColorAction highlightNode = new DataColorAction(NODES, "pathToSuccess", Constants.NOMINAL, VisualItem.FILLCOLOR, palettes.getPathHighlighPalette());
		nFill.add(VisualItem.HIGHLIGHT, highlightNode);

		AndPredicate filter = new AndPredicate();
		Predicate selectSuccessful = ExpressionParser.predicate(HIGHLIGHT_SUCCESSFUL);
		Predicate selectOngoing = ExpressionParser.predicate(HIGHLIGHT_ONGOING);
		Predicate selectDead = ExpressionParser.predicate(HIGHLIGHT_DEAD);
		Predicate selectTerminal = ExpressionParser.predicate(HIGHLIGHT_TERMINAL);
		Predicate selectNoCandidates = ExpressionParser.predicate(HIGHLIGHT_NOCANDIDATES);
		//--DataColorAction nodeHighlight = new DataColorAction(NODES, "type", Constants.NOMINAL, VisualItem.FILLCOLOR, palettes.getNodeHighlighPalette());
		//--nFill.add(filter, nodeHighlight);
		nFill.add(filter, ColorLib.rgba(255, 0, 0, 255));

		// use black for node text
		ColorAction nText = new ColorAction(NODES, VisualItem.TEXTCOLOR, palettes.getTextPalette());

		// use light grey for edges
		ColorAction eFill = new DataColorAction(EDGES, "type", Constants.NOMINAL, VisualItem.STROKECOLOR, palettes.getEdgePalette());
		eFill.add(VisualItem.HIGHLIGHT, palettes.getEdgeHighlightPalette());
		eFill.add(VisualItem.FIXED, palettes.getEdgeHighlightPalette());

		ColorAction arrow =  new DataColorAction(EDGES, "type", Constants.NOMINAL, VisualItem.FILLCOLOR, palettes.getEdgePalette());

		ColorAction aFill = new DataColorAction(AGGREGATES, "id", Constants.NOMINAL, VisualItem.FILLCOLOR, palettes.getAggegatePalette());
		ColorAction aStroke = new ColorAction(AGGREGATES, VisualItem.STROKECOLOR, palettes.getAggregateStrokePalette());
		aStroke.add(VisualItem.HOVER, palettes.getAggregateStrokeHighlightPalette());

		// create an action list containing all color assignments
		ActionList color = new ActionList();
		color.add(nFill);
		color.add(nText);
		color.add(eFill);
		color.add(arrow);
		color.add(aStroke);
		color.add(aFill);
		//color.add(highlightAggregate);

		// create an action list with an animated layout
		ActionList layout = new ActionList(Activity.INFINITY);
		RadialTreeLayout radialTreeLayout = new RadialTreeLayout(GRAPH);
		radialTreeLayout.setAutoScale(false);
		layout.add(radialTreeLayout);
		layout.add(new AggregateLayout(AGGREGATES));
		layout.add(new RepaintAction());


		// add the actions to the visualization
		this.visualization.putAction(COLOR_ACTION, color);
		this.visualization.putAction(LAYOUT_ACTION, layout);
		
        // fisheye distortion based on the current anchor location
        ActionList distort = new ActionList();
        distort.add(this.fish);
        distort.add(nFill);
        distort.add(new RepaintAction());
        this.visualization.putAction("distort", distort);
		

		HighlightButton successfulButton = new HighlightButton("Successful nodes", filter, selectSuccessful);
		HighlightButton ongoingButton = new HighlightButton("Ongoing nodes", filter, selectOngoing);
//		HighlightButton deadButton = new HighlightButton("Dead nodes", filter, selectDead);
		HighlightButton terminalButton = new HighlightButton("Terminal nodes", filter, selectTerminal);
//		HighlightButton nocandidatesButton = new HighlightButton("Fully explored nodes", filter, selectNoCandidates);
		HighlightButton noneButton = new HighlightButton("Clear highlighting", filter, null);

		ButtonGroup group = new ButtonGroup();
		group.add(successfulButton);
		group.add(ongoingButton);
//		group.add(nocandidatesButton);
		group.add(terminalButton);
//		group.add(deadButton);
		group.add(noneButton);
		
		successfulButton.setBackground(Color.WHITE);
		ongoingButton.setBackground(Color.WHITE);
//		deadButton.setBackground(Color.WHITE);
		terminalButton.setBackground(Color.WHITE);
//		nocandidatesButton.setBackground(Color.WHITE);
		noneButton.setBackground(Color.WHITE);

		JPanel result = new JPanel(new GridBagLayout());
		result.setPreferredSize(new Dimension(200, 400));
		result.setBackground(Color.WHITE);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.anchor=GridBagConstraints.LINE_START;
		result.add(successfulButton, gbc);
		gbc.gridy=1;
		result.add(ongoingButton, gbc);
		gbc.gridy=2;
//		result.add(deadButton, gbc);
//		gbc.gridy=3;
		result.add(terminalButton, gbc);
		gbc.gridy=4;
//		result.add(nocandidatesButton, gbc);
//		gbc.gridy=5;
		result.add(noneButton, gbc);
		gbc.gridy=5;
		
//		successfulButton.setAlignmentX(Component.LEFT_ALIGNMENT);
//		ongoingButton.setAlignmentX(Component.LEFT_ALIGNMENT);
//		deadButton.setAlignmentX(Component.LEFT_ALIGNMENT);
//		terminalButton.setAlignmentX(Component.LEFT_ALIGNMENT);
//		nocandidatesButton.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Path highlight widget
		//JLabel pathsHighlightLabel = new JLabel();
		//pathsHighlightLabel.setText("# paths to highlight");
		//pathsHighlightLabel.setPreferredSize(new Dimension(100, 150));
		//result.add(pathsHighlightLabel);
		
		
		this.pathHighlightSlider.setValue(0);
		this.pathHighlightSlider.setSnapToTicks(true);
		this.pathHighlightSlider.setMaximum(0);
		this.pathHighlightSlider.setMinimum(0);
		this.pathHighlightSlider.setMajorTickSpacing(5);
		this.pathHighlightSlider.setMinorTickSpacing(1);
		this.pathHighlightSlider.setPaintTicks(true);
		this.pathHighlightSlider.setBackground(Color.WHITE);

		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put(new Integer(0), new JLabel("No path") );
		this.pathHighlightSlider.setLabelTable(labelTable);
		this.pathHighlightSlider.setPaintLabels(true);
		
		this.pathHighlightSlider.addChangeListener(new PathDisplayChangeListener(this));
		result.add(this.pathHighlightSlider, gbc);
		return result;
	}
	
	private Display setupDisplay() {
		Display result = new Display(this.visualization);
		result.setHighQuality(true);
		return result;
	}

	public void addControl(Control ctrl) {
		this.display.addControlListener(ctrl);
	}
	
	public static class PathDisplayChangeListener implements ChangeListener {
		
		private final PrefuseVisualizer visualizer;

		public PathDisplayChangeListener(PrefuseVisualizer viz) {
			this.visualizer = viz;
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			int rank = ((JSlider) e.getSource()).getValue();
			this.showOrHideAggregates(this.visualizer.aggregateTable, false);

			int rows = this.visualizer.aggregateTable.getRowCount();
			if (0 < rank && rank <= rows && rank < ((JSlider) e.getSource()).getMaximum()) {
				this.selectPath(rank - 1);
			} else if (rank > 0) {
				this.showOrHideAggregates(this.visualizer.aggregateTable, true);
				for (int row = 0; row < rows; ++row) {
					this.selectPath(row);
				}
			}
			this.visualizer.visualization.run(COLOR_ACTION);
		}
		
		private void selectPath(int rank) {
			AggregateItem aggregate = (AggregateItem) this.visualizer.aggregateTable.getItem(rank);
			if (this.allItemsVisible(aggregate)) {
				aggregate.setVisible(true);
				Iterator<VisualItem> iterator = aggregate.items();
				while ( iterator.hasNext() ) {
					VisualItem item = iterator.next();
					item.setHighlighted(true);
				}
				aggregate.setHover(true);
			}
		}

		private void showOrHideAggregates(AggregateTable aggregateTable, boolean visible) {
			int rows = aggregateTable.getRowCount();
			if(rows > 0) {
				for(int row = 0; row < rows; ++row) {
					AggregateItem aggregate = (AggregateItem) aggregateTable.getItem(row);
					aggregate.setVisible(visible);
					Iterator<VisualItem> iterator = aggregate.items();
					while ( iterator.hasNext() ) {
						VisualItem item = iterator.next();
						item.setHighlighted(visible);
					}
					aggregate.setHover(visible);
				}
			}
		}

		private boolean allItemsVisible(AggregateItem aggregate) {
			Iterator<VisualItem> iterator = aggregate.items();
			while ( iterator.hasNext() ) {
				VisualItem item = iterator.next();
				if(!item.isVisible()) {
					return false;
				}
			}
			return true;
		}
	}

	public Graph getGraph() {
		return this.graph;
	}

	public Visualization getVisualization() {
		return this.visualization;
	}

	public AggregateTable getAggregateTable() {
		return this.aggregateTable;
	}

	public PathHighlightControl getPathHighlightControl() {
		return this.pathHighlightControl;
	}
	
	public JSlider getPathsHighlightBox() {
		return this.pathHighlightSlider;
	}
	
	public Distortion getDistortion() {
		return this.fish;
	}
}
