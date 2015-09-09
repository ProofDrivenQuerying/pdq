
package uk.ac.ox.cs.pdq.ui.prefuse.popups;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

import prefuse.visual.NodeItem;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;


public class NodePropertiesItem extends JMenuItem {

	private NodeItem node;
    private JFrame frame;
    
    public NodePropertiesItem(JFrame frame, NodeItem node) {
    	super("Properties...");
    	this.frame = frame;
    	this.node = node;
    	this.setAction(new PropertiesListener(this.node));
    }
	
    private class PropertiesListener extends AbstractAction {
    	
    	public PropertiesListener(NodeItem node) {
    		super("Properties...");
    	}

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
