// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.event;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.ui.event.PDQShape;

import java.util.List;

import uk.ac.ox.cs.pdq.ui.prefuse.utils.Utils;

import prefuse.data.Graph;

public class PDQModify extends PDQShape
{
	private int id;
	private String type;
	private SearchNode.NodeStatus node;
 	private PrefuseEventHandler peh;
	
 	public void drawShape()
 	{
		Utils.modifyNodeProperty(this.graph, id, type, node);
		peh.visualization.run(peh.colorAction);
		peh.visualization.run(peh.layoutAction);
 	}
 	
 	public PDQModify(Graph g, PrefuseEventHandler h, int i, String t, SearchNode.NodeStatus n)
 	{
  		super(g);
		peh = h;
 		id = i;
 		type = t;
 		node = n;
 	}
}
