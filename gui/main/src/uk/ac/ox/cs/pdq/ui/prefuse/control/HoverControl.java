// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.control;

import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JPopupMenu;

import prefuse.controls.ControlAdapter;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode.NodeStatus;

// TODO: Auto-generated Javadoc
/**
 * Interactive drag control that is "aggregate-aware".
 */
public class HoverControl extends ControlAdapter {

	/**
	 * Creates a new drag control that issues repaint requests as an item
	 * is dragged.
	 *
	 * @param item the item
	 * @param evt the evt
	 */
	@Override
	public void itemEntered(VisualItem item, MouseEvent evt) {
		if ( item instanceof NodeItem ) {

			SearchNode root = (SearchNode) item.get("data");
			String access = "NO ACCESS";
			Collection<Candidate> exposedCandidates = root.getConfiguration().getExposedCandidates();
			if(exposedCandidates != null) {
				access = exposedCandidates.iterator().next().getRelation().getName();
			}
			
			JPopupMenu jpub = new JPopupMenu();
			jpub.add("Access: " + access);
			jpub.add("Type: " + item.get("type"));
			
			if(((NodeStatus)item.get("type")).equals(NodeStatus.SUCCESSFUL)) {
				if (root.getCostOfBestPlanFromRoot() != null) {
					String cost = root.getCostOfBestPlanFromRoot().toString();
					jpub.add("Plan's cost: " + cost);
				}
			}
			
			//jpub.show(evt.getComponent(),(int)item.getX()+ frame.getX(), (int)item.getY()+ frame.getY());
			jpub.show(evt.getComponent(), 0, 0);
			item.setFillColor(item.getStrokeColor());
			item.setStrokeColor(ColorLib.rgb(0,0,0));
			item.getVisualization().repaint();
		}
	}
	
	/* (non-Javadoc)
	 * @see prefuse.controls.ControlAdapter#itemExited(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
	 */
	@Override
	public void itemExited(VisualItem item, MouseEvent evt) {
		if (item instanceof NodeItem) {
			item.setFillColor(item.getEndFillColor());
			item.setStrokeColor(item.getEndStrokeColor());
			item.getVisualization().repaint();
		}
	}
}
