// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.control;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;

import prefuse.controls.ControlAdapter;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import uk.ac.ox.cs.pdq.ui.prefuse.types.PathTypes;
import uk.ac.ox.cs.pdq.ui.prefuse.utils.Path;


// TODO: Auto-generated Javadoc
/**
 * <p>
 * A ControlListener that sets the highlighted status (using the
 * {@link prefuse.visual.VisualItem#setHighlighted(boolean)
 * VisualItem.setHighlighted} method) for nodes neighboring the node 
 * currently under the mouse pointer. The highlight flag might then be used
 * by a color function to change node appearance as desired.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class PathHighlightControl extends ControlAdapter {

	/** The paths. */
	private Queue<Path> paths = null;

	/** The activity. */
	private String activity = null;
	
	/** The highlight with invisible edge. */
	private boolean highlightWithInvisibleEdge = false;

	/**
	 * Creates a new highlight control.
	 */
	public PathHighlightControl() {
		this(null);
	}

	/**
	 * Creates a new highlight control that runs the given activity
	 * whenever the neighbor highlight changes.
	 * @param activity the update Activity to run
	 */
	public PathHighlightControl(String activity) {
		this.activity = activity;
	}

	/**
	 * Item entered.
	 *
	 * @param item the item
	 * @param e the e
	 * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
	 */
	public void itemEntered(VisualItem item, MouseEvent e) {
		if ( item instanceof NodeItem )
			setNeighborHighlight((NodeItem)item, true);
	}

	/**
	 * Item exited.
	 *
	 * @param item the item
	 * @param e the e
	 * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
	 */
	public void itemExited(VisualItem item, MouseEvent e) {
		if ( item instanceof NodeItem )
			setNeighborHighlight((NodeItem)item, false);
	}

	/**
	 * Set the highlighted state of the neighbors of a node.
	 * @param n the node under consideration
	 * @param state the highlighting state to apply to neighbors
	 */
	protected void setNeighborHighlight(NodeItem n, boolean state) {
		if(this.paths != null && 
				!((PathTypes)n.get("pathToSuccess")).equals(PathTypes.NOSUCCESSFULPATH) ) {
			Collection<NodeItem> toHighlight = new HashSet<>();
			Iterator<Path> iterator = this.paths.iterator();
			while(iterator.hasNext()) {
				Path path = iterator.next();
				if(path.contains(n)) {
					toHighlight.addAll(path.getNodesPath());
					break;
				}
			}
			toHighlight.add(n);
			setNeighborHighlight(toHighlight, state);
		}
	}

	/**
	 * Sets the neighbor highlight.
	 *
	 * @param toHighlight the to highlight
	 * @param state the state
	 */
	protected void setNeighborHighlight(Collection<NodeItem> toHighlight, boolean state) {
		for(NodeItem n:toHighlight) {
			n.setHighlighted(state);
			Iterator iter = n.edges();
			while ( iter.hasNext() ) {
				EdgeItem eitem = (EdgeItem)iter.next();
				NodeItem nitem = eitem.getAdjacentItem(n);
				Boolean highlight = !((PathTypes)nitem.get("pathToSuccess")).equals(PathTypes.NOSUCCESSFULPATH) &&
						toHighlight.contains(nitem);
				if (highlight && (eitem.isVisible() || this.highlightWithInvisibleEdge)) {
					eitem.setHighlighted(state);
				}
			}
			if (this.activity != null)
				n.getVisualization().run(this.activity);
		}
	}


	/**
	 * Indicates if neighbor nodes with edges currently not visible still
	 * get highlighted.
	 * @return true if neighbors with invisible edges still get highlighted,
	 * false otherwise.
	 */
	public boolean isHighlightWithInvisibleEdge() {
		return this.highlightWithInvisibleEdge;
	}

	/**
	 * Determines if neighbor nodes with edges currently not visible still
	 * get highlighted.
	 * @param highlightWithInvisibleEdge assign true if neighbors with invisible
	 * edges should still get highlighted, false otherwise.
	 */
	public void setHighlightWithInvisibleEdge(boolean highlightWithInvisibleEdge) {
		this.highlightWithInvisibleEdge = highlightWithInvisibleEdge;
	}
	
	/**
	 * Sets the paths.
	 *
	 * @param paths the new paths
	 */
	public void setPaths(Queue<Path> paths) {
		this.paths = paths;
	}

} // end of class NeighborHighlightControl
