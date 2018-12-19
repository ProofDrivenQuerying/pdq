package uk.ac.ox.cs.pdq.ui.prefuse.control;



import java.awt.event.MouseEvent;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.ui.prefuse.popups.EdgePropertiesDialog;
import uk.ac.ox.cs.pdq.ui.prefuse.popups.NodeMenu;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;

// TODO: Auto-generated Javadoc
/**
 * The Class RightClickControl.
 */
public class RightClickControl extends ControlAdapter implements Control {

	/** Class' logger. */
	private static Logger log = Logger.getLogger(RightClickControl.class);

	/** The frame. */
	private final JFrame frame;
	
	/**
	 * Instantiates a new right click control.
	 *
	 * @param frame the frame
	 */
	public RightClickControl(JFrame frame) {
		this.frame = frame;
	}
	
	/* (non-Javadoc)
	 * @see prefuse.controls.ControlAdapter#itemClicked(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
	 */
	@Override
	public void itemClicked(VisualItem item, MouseEvent e) {
		log.debug("Clicked " + e);
		if(item instanceof NodeItem) {			
			final NodeMenu menu = new NodeMenu(this.frame, (NodeItem) item);
			menu.setDefaultLightWeightPopupEnabled(true);
			menu.setLocation((int)item.getX()+ this.frame.getX(), (int)item.getY()+ this.frame.getY());
			menu.setSize(50, 50);
			menu.setVisible(true);

		} else if(item instanceof EdgeItem) {
			NodeItem s = ((EdgeItem)item).getSourceItem();
			NodeItem t = ((EdgeItem)item).getAdjacentItem(s);
			SearchNode target = (SearchNode) t.get("data");
			
			EdgeTypes type = (EdgeTypes) item.get("type");
            EdgePropertiesDialog dialog = new EdgePropertiesDialog(this.frame, type, target);
            dialog.setLocation((int)item.getX()+ this.frame.getX(), (int)item.getY()+ this.frame.getY());
            dialog.setSize(450, 450);
            dialog.setVisible(true);
		}
	}
}
