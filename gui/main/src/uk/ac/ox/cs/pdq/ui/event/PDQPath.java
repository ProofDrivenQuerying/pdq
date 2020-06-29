// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.event;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.CreationMetadata;
import uk.ac.ox.cs.pdq.ui.event.PDQShape;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;
import uk.ac.ox.cs.pdq.ui.prefuse.utils.Path;
import uk.ac.ox.cs.pdq.ui.prefuse.utils.Utils;

import java.util.List;

import prefuse.data.Graph;

public class PDQPath extends PDQShape
{
	private Plan plan;
 	private DoubleCost cost;
 	private List<Integer> success;
 	private PrefuseEventHandler peh;
	
 	public void drawShape()
 	{
 		Path path = new Path(success, plan, cost);
		peh.paths.add(path);
//		log.debug(Joiner.on("\n").join(this.paths));

		peh.updatePathHighlightControl();
		peh.updatePathHighlightSlider();
		peh.updateAggregateTable();
 	}
 	
 	public PDQPath(Graph g, PrefuseEventHandler h, List<Integer> s, Plan p, DoubleCost c)
 	{
  		super(g);
		peh = h;
 		success = s;
 		plan = p;
 		cost = c;
 	}
}
