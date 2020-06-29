// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.event;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.CreationMetadata;
import uk.ac.ox.cs.pdq.ui.event.PDQShape;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;
import uk.ac.ox.cs.pdq.ui.prefuse.utils.Utils;
import prefuse.data.Graph;

public class PDQNode extends PDQShape
{
	private SearchNode node;
	
 	public void drawShape()
 	{
 		Utils.addNode(this.graph, this.node);
 	}
 	
 	public PDQNode(Graph g, SearchNode n)
 	{
 		super(g);
 		node = n;
 	}
}
