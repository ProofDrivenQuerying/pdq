// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.event;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.CreationMetadata;
import uk.ac.ox.cs.pdq.ui.event.PDQShape;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;
import uk.ac.ox.cs.pdq.ui.prefuse.utils.Utils;
import prefuse.data.Graph;

public class PDQEdge extends PDQShape
{
	private SearchNode node;
 	private SearchNode parent;
 	private EdgeTypes edgetype;
	
 	public void drawShape()
 	{
 		Utils.addEdge(this.graph, this.parent, this.node, this.edgetype);
 	}
 	
 	public PDQEdge(Graph g, SearchNode p, SearchNode n, EdgeTypes et)
 	{
 		super(g);
 		parent = p;
 		node = n;
 		edgetype = et;
 	}
}
