package uk.ac.ox.cs.pdq.ui.event;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JSlider;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.eventbus.Subscribe;

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.visual.AggregateTable;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.BestPlanMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.CreationMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.EquivalenceMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.Metadata;
import uk.ac.ox.cs.pdq.ui.prefuse.control.PathHighlightControl;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;
import uk.ac.ox.cs.pdq.ui.prefuse.types.PathTypes;
import uk.ac.ox.cs.pdq.ui.prefuse.utils.Path;
import uk.ac.ox.cs.pdq.ui.prefuse.utils.PathComparator;
import uk.ac.ox.cs.pdq.ui.prefuse.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class PrefuseEventHandler.
 *
 * @author Efi Tsamoura
 */
public class PrefuseEventHandler {

	/** Class' logger. */
	private static Logger log = Logger.getLogger(PrefuseEventHandler.class);

	/** The graph. */
	private Graph graph;
	
	/** The aggregate table. */
	private AggregateTable aggregateTable;
	
	/** The visualization. */
	public Visualization visualization;
	
	/** The path highlight control. */
	private PathHighlightControl pathHighlightControl;
	
	/** The aggregate group. */
	private String aggregateGroup;
	
	/** The node group. */
	private String nodeGroup;
	
	/** The color action. */
	public String colorAction;
	
	/** The layout action. */
	public String layoutAction;
	
	/** The path highlight slider. */
	private JSlider pathHighlightSlider;
	
	/** The paths. */
	public final SortedSet<Path> paths = new TreeSet<Path>(new PathComparator());

	/** The shapes. */
	private ArrayList<PDQShape> shapes = null;

	/**
	 * Instantiates a new prefuse event handler.
	 *
	 * @param graph the graph
	 * @param aggregateTable the aggregate table
	 * @param visualization the visualization
	 * @param aggregateGroup the aggregate group
	 * @param nodeGroup the node group
	 * @param colorAction the color action
	 * @param layoutAction the layout action
	 * @param pathHighlightControl the path highlight control
	 * @param pathsHighlightSlider the paths highlight slider
	 */
	public PrefuseEventHandler(
			Graph graph, AggregateTable aggregateTable, Visualization visualization,
			String aggregateGroup, String nodeGroup, String colorAction, String layoutAction,
			PathHighlightControl pathHighlightControl,
			JSlider pathsHighlightSlider) {

		this.graph = graph;
		this.aggregateTable = aggregateTable;
		this.visualization = visualization;
		this.aggregateGroup = aggregateGroup;
		this.pathHighlightControl = pathHighlightControl;
		this.nodeGroup = nodeGroup;
		this.colorAction = colorAction;
		this.layoutAction = layoutAction;
		this.pathHighlightSlider = pathsHighlightSlider;
		this.shapes = null;
	}

	
	/**
	 * Process node.
	 *
	 * @param node the node
	 */
	@Subscribe
	public void processNode(SearchNode node) {
		
		try
		{

			if (node == null || node.getMetadata() == null || (node.getMetadata() instanceof EquivalenceMetadata && node.getEquivalentNode() == null)) {
				throw new java.lang.IllegalArgumentException();
			}

			Metadata metadata = node.getMetadata();

			if (metadata instanceof EquivalenceMetadata) {
				shapes.add(new PDQEdge(this.graph, node, node.getEquivalentNode(), EdgeTypes.POINTER));
			} 
			else if (metadata instanceof BestPlanMetadata) {
				shapes.add(new PDQPath(this.graph, this, ((BestPlanMetadata) metadata).getBestPathToSuccess(), ((BestPlanMetadata) metadata).getPlan(),  new DoubleCost(0.0)));
			} 
			else if (metadata instanceof CreationMetadata){
				if (((CreationMetadata) metadata).getParent() != null) {
					shapes.add(new PDQNode(this.graph, node));
					shapes.add(new PDQEdge(this.graph, ((CreationMetadata) metadata).getParent(), node, EdgeTypes.HIERARCHY));
				} else {
					shapes.add(new PDQNode(this.graph, node));
				}
			}

			shapes.add(new PDQModify(this.graph, this, node.getId(), "type", node.getStatus()));
			//		if (metadata.getParent() != null) {
			//			Utils.modifyNodeProperty(this.graph, metadata.getParent().getId(), "type", metadata.getParent().getStatus());
			//		}

		}
		catch(Exception e)
		{
			// throw e;
		}
	}
	
	
	/**
	 * Update path highlight control.
	 */
	public void updatePathHighlightControl() {
		Queue<Path> paths = new PriorityQueue<Path>(10, new PathComparator());
		int i = 0;
		Iterator<Path> iterator = this.paths.iterator();
		while(iterator.hasNext()) {
			Path path = iterator.next();
			PathTypes type;
			if(i == 0) {
				type = PathTypes.BESTSUCCESSFULPATH;
			}
			else {
				type = PathTypes.SUCCESSFULPATH;
			}
			Utils.modifyNodeProperty(this.graph,  path.getIntegerPath(), "pathToSuccess", type);
			path.setNodesPath(Utils.toNodeItem(this.visualization, this.nodeGroup, this.graph, path.getIntegerPath()));
			paths.add(path); 
		}
		this.pathHighlightControl.setPaths(paths);
	}
	
	/**
	 * Update path highlight slider.
	 */
	public void updatePathHighlightSlider() {
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put( new Integer( 0 ), new JLabel("0") );
		int i = 0;
		for(i = 0; i < this.paths.size(); ++i) {
			labelTable.put( new Integer( i + 1 ), new JLabel(new Integer(i + 1).toString() ) );
		}
		labelTable.put( new Integer( i + 1), new JLabel("All") );
		this.pathHighlightSlider.setLabelTable(labelTable);
		this.pathHighlightSlider.setValue(0);
		this.pathHighlightSlider.setMinimum(0);
		this.pathHighlightSlider.setMaximum(i + 1);
	}
	
	/**
	 * Update aggregate table.
	 */
	public void updateAggregateTable() {
		List<List<Integer>> integerPaths = new ArrayList<>();
		for(Path path:this.paths) {
			integerPaths.add(path.getIntegerPath());
		}
		Utils.updateAggregateTable(this.visualization, this.nodeGroup, this.graph, this.aggregateTable, integerPaths, false);
	}


	public void initialiseShapes()
	{
		shapes = new ArrayList<PDQShape>();
	}
	
	public void drawShapes()
	{
		for(PDQShape s : shapes)
		{
			s.drawShape();
		}
	}
}
