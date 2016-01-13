package uk.ac.ox.cs.pdq.ui.prefuse.schema;

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;
import uk.ac.ox.cs.pdq.ui.prefuse.types.PathTypes;

public class SchemaFactory {
	
	public static Schema getNodeTableSchema() {
		String[] names = new String[] { "id", "type", "pathToSuccess", "isCollapsed", "hidePointerEdges", "data" };
		Class[] types = new Class[] { int.class, NodeStatus.class, PathTypes.class, boolean.class, boolean.class, SearchNode.class };
		return new Schema(names, types);
	}
	
	public static Schema getEdgeTableSchema() {
		String[] names = new String[] { "type", Graph.DEFAULT_SOURCE_KEY, Graph.DEFAULT_TARGET_KEY };
		Class[] types = new Class[] {
			EdgeTypes.class, int.class, int.class
		};
		return new Schema(names, types);
	}
	
	
	public static Table createTable(Schema schema) {
		Table table = new Table();
		for(int c = 0; c < schema.getColumnCount(); ++c) {
			table.addColumn(schema.getColumnName(c), schema.getColumnType(c));
		}
		return table;
	}

	public static Graph createGraph() {
		Table nodes = createTable(getNodeTableSchema());
		Table edges = createTable(getEdgeTableSchema());
		return new Graph(nodes, edges, true);
	}
	
	public static AggregateTable createAggregateTable(Visualization visualization, String group) {
		if(group == null || visualization == null) {
			throw new java.lang.IllegalArgumentException();
		}
		AggregateTable at = visualization.addAggregates(group);
		at.addColumn(VisualItem.POLYGON, float[].class);
		at.addColumn("id", int.class);
		return at;
	}
	


}
