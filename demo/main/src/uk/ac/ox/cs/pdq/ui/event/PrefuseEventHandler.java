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

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.visual.AggregateTable;
import uk.ac.ox.cs.pdq.util.EventHandler;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
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

import com.google.common.base.Joiner;
import com.google.common.eventbus.Subscribe;

// TODO: Auto-generated Javadoc
/**
 * The Class PrefuseEventHandler.
 *
 * @author Efi Tsamoura
 */
public class PrefuseEventHandler implements EventHandler {

	/** Class' logger. */
	private static Logger log = Logger.getLogger(PrefuseEventHandler.class);

	/** The graph. */
	private Graph graph;
	
	/** The aggregate table. */
	private AggregateTable aggregateTable;
	
	/** The visualization. */
	private Visualization visualization;
	
	/** The path highlight control. */
	private PathHighlightControl pathHighlightControl;
	
	/** The aggregate group. */
	private String aggregateGroup;
	
	/** The node group. */
	private String nodeGroup;
	
	/** The color action. */
	private String colorAction;
	
	/** The layout action. */
	private String layoutAction;
	
	/** The path highlight slider. */
	private JSlider pathHighlightSlider;
	
	/** The paths. */
	private final SortedSet<Path> paths = new TreeSet<Path>(new PathComparator());

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
	}

	
	/**
	 * Process node.
	 *
	 * @param node the node
	 */
	@Subscribe
	public void processNode(SearchNode node) {

		if (node == null || node.getMetadata() == null || (node.getMetadata() instanceof EquivalenceMetadata && node.getPointer() == null)) {
			throw new java.lang.IllegalArgumentException();
		}

		Metadata metadata = node.getMetadata();

		if (metadata instanceof EquivalenceMetadata) {
			Utils.addEdge(this.graph, node, node.getPointer(), EdgeTypes.POINTER);
		} 
		else if (metadata instanceof BestPlanMetadata) {

			this.paths.add(new Path(((BestPlanMetadata) metadata).getBestPathToSuccess(), ((BestPlanMetadata) metadata).getPlan()));
			log.debug(Joiner.on("\n").join(this.paths));

			this.updatePathHighlightControl();
			this.updatePathHighlightSlider();
			this.updateAggregateTable();
			
		} 
		else if (metadata instanceof CreationMetadata){
			if (((CreationMetadata) metadata).getParent() != null) {
				Utils.addNode(this.graph, node);
				Utils.addEdge(this.graph, ((CreationMetadata) metadata).getParent(), node, EdgeTypes.HIERARCHY);
			} else {
				Utils.addNode(this.graph, node);
			}
		}

		Utils.modifyNodeProperty(this.graph, node.getId(), "type", node.getStatus());
		if (metadata.getParent() != null) {
			Utils.modifyNodeProperty(this.graph, metadata.getParent().getId(), "type", metadata.getParent().getStatus());
		}

		this.visualization.run(this.colorAction);
		this.visualization.run(this.layoutAction);
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
/* MR			Utils.modifyNodeProperty(this.graph,  path.getIntegerPath(), "pathToSuccess", type);
			path.setNodesPath(Utils.toNodeItem(this.visualization, this.nodeGroup, this.graph, path.getIntegerPath()));
			paths.add(path); */
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
// MR			integerPaths.add(path.getIntegerPath());
		}
		Utils.updateAggregateTable(this.visualization, this.nodeGroup, this.graph, this.aggregateTable, integerPaths, false);
	}


}
