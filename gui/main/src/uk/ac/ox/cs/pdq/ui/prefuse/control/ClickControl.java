// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.control;



import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.ui.PlannerController;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;

// TODO: Auto-generated Javadoc
/**
 * The Class ClickControl.
 */
public class ClickControl extends ControlAdapter implements Control {

	/** The data queue. */
	private final ConcurrentLinkedQueue dataQueue;
	
	/**
	 * Instantiates a new click control.
	 *
	 * @param q the q
	 */
	public ClickControl(ConcurrentLinkedQueue<?> q) {
		this.dataQueue = q;
	}
	
	/* (non-Javadoc)
	 * @see prefuse.controls.ControlAdapter#itemClicked(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
	 */
	@Override
	public void itemClicked(VisualItem item, MouseEvent e) {
		if(item instanceof NodeItem) {			
			if (e.getClickCount() == 1) {
				this.dataQueue.add(((NodeItem) item).get("data"));
			} else if (e.getClickCount() == 2) {
				this.collapseExpandNode((NodeItem) item);
			}
			
		} else if(item instanceof EdgeItem) {
			NodeItem s = ((EdgeItem)item).getSourceItem();
			NodeItem t = ((EdgeItem)item).getAdjacentItem(s);
			SearchNode target = (SearchNode) t.get("data");
			
			EdgeTypes type = (EdgeTypes) item.get("type");
			this.dataQueue.add(new PlannerController.SearchEdge(target, type));
		}
	}
	
	/**
	 * Collapse or expands the given depending on its current state .
	 *
	 * @param n the n
	 */
	private void collapseExpandNode(NodeItem n) {
		boolean isCollapsed = (boolean) n.get("isCollapsed");
		this.updateTreeRecursive(n, n, !isCollapsed);
	}

	/**
	 * Recursively propagates the collapsing command to a node's descendants.
	 *
	 * @param root the root
	 * @param n the n
	 * @param isCollapsed the is collapsed
	 */
	private void updateTreeRecursive(NodeItem root, NodeItem n, Boolean isCollapsed) {
		if (n == null) {
			return;
		}
		if (n.equals(root)) {
			Iterator out = n.outEdges();
			while (out.hasNext()) {
				EdgeItem eitem = (EdgeItem)out.next();
				if (eitem.get("type") == EdgeTypes.HIERARCHY) {
					NodeItem nitem = eitem.getAdjacentItem(n);
					eitem.setVisible(isCollapsed);
					nitem.setVisible(isCollapsed);
					n.set("isCollapsed", isCollapsed);
					this.updateTreeRecursive(root, nitem, isCollapsed);
				}
			}
		} else {
			Iterator in = n.inEdges();
			while (in.hasNext()) {
				EdgeItem eitem = (EdgeItem) in.next();
				eitem.setVisible(isCollapsed);
			}
			Iterator out = n.outEdges();
			while (out.hasNext()) {
				EdgeItem eitem = (EdgeItem) out.next();
				eitem.setVisible(isCollapsed);
				if (eitem.get("type") != EdgeTypes.POINTER) {
					NodeItem nitem = eitem.getAdjacentItem(n);
					nitem.setVisible(isCollapsed);
					n.set("isCollapsed", isCollapsed);
					this.updateTreeRecursive(root, nitem, isCollapsed);
				}
			}
			
		}
	}
}
