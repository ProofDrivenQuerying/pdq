
// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.popups;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

import prefuse.visual.NodeItem;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;


// TODO: Auto-generated Javadoc
/**
 * The Class NodePropertiesItem.
 */
public class NodePropertiesItem extends JMenuItem {

	/** The node. */
	private NodeItem node;
    
    /** The frame. */
    private JFrame frame;
    
    /**
     * Instantiates a new node properties item.
     *
     * @param frame the frame
     * @param node the node
     */
    public NodePropertiesItem(JFrame frame, NodeItem node) {
    	super("Properties...");
    	this.frame = frame;
    	this.node = node;
    	this.setAction(new PropertiesListener(this.node));
    }
	
    /**
     * The listener interface for receiving properties events.
     * The class that is interested in processing a properties
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addPropertiesListener<code> method. When
     * the properties event occurs, that object's appropriate
     * method is invoked.
     *
     * @see PropertiesEvent
     */
    private class PropertiesListener extends AbstractAction {
    	
    	/**
	     * Instantiates a new properties listener.
	     *
	     * @param node the node
	     */
	    public PropertiesListener(NodeItem node) {
    		super("Properties...");
    	}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			SearchNode root = (SearchNode) NodePropertiesItem.this.node.get("data");
            NodePropertiesDialog dialog = new NodePropertiesDialog(root);
            dialog.setLocation((int)NodePropertiesItem.this.node.getX()+ NodePropertiesItem.this.frame.getX(), (int)NodePropertiesItem.this.node.getY()+ NodePropertiesItem.this.frame.getY());
            dialog.setSize(450, 450);
            dialog.setVisible(true);
		}
    }
}
