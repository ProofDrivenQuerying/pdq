// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.schema;

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;
import uk.ac.ox.cs.pdq.ui.prefuse.types.PathTypes;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating Schema objects.
 */
public class SchemaFactory {
	
	/**
	 * Gets the node table schema.
	 *
	 * @return the node table schema
	 */
	public static Schema getNodeTableSchema() {
		String[] names = new String[] { "id", "type", "pathToSuccess", "isCollapsed", "hidePointerEdges", "data" };
		Class[] types = new Class[] { int.class, NodeStatus.class, PathTypes.class, boolean.class, boolean.class, SearchNode.class };
		return new Schema(names, types);
	}
	
	/**
	 * Gets the edge table schema.
	 *
	 * @return the edge table schema
	 */
	public static Schema getEdgeTableSchema() {
		String[] names = new String[] { "type", Graph.DEFAULT_SOURCE_KEY, Graph.DEFAULT_TARGET_KEY };
		Class[] types = new Class[] {
			EdgeTypes.class, int.class, int.class
		};
		return new Schema(names, types);
	}
	
	
	/**
	 * Creates a new Schema object.
	 *
	 * @param schema the schema
	 * @return the table
	 */
	public static Table createTable(Schema schema) {
		Table table = new Table();
		for(int c = 0; c < schema.getColumnCount(); ++c) {
			table.addColumn(schema.getColumnName(c), schema.getColumnType(c));
		}
		return table;
	}

	/**
	 * Creates a new Schema object.
	 *
	 * @return the graph
	 */
	public static Graph createGraph() {
		Table nodes = createTable(getNodeTableSchema());
		Table edges = createTable(getEdgeTableSchema());
		return new Graph(nodes, edges, true);
	}
	
	/**
	 * Creates a new Schema object.
	 *
	 * @param visualization the visualization
	 * @param group the group
	 * @return the aggregate table
	 */
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
