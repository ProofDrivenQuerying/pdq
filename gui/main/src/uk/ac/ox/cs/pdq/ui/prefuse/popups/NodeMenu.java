// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.popups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import prefuse.visual.NodeItem;

// TODO: Auto-generated Javadoc
/**
 * The Class NodeMenu.
 */
public class NodeMenu extends JPopupMenu {
	
    /**
     * Instantiates a new node menu.
     *
     * @param frame the frame
     * @param node the node
     */
    public NodeMenu(JFrame frame, NodeItem node) {
        super("Options...");
        this.add(new CollapseExpandTree(node));
        this.addSeparator();
        this.add(new ShowOrHidePointerEdges(node));
        this.addSeparator();
        this.add(new NodePropertiesItem(frame, node));
        this.addSeparator();
        JMenuItem close = new JMenuItem("Hide Menu");
        close.addActionListener(new CloseListener());
        this.add(close);
        
    }
    
    /**
     * The listener interface for receiving close events.
     * The class that is interested in processing a close
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addCloseListener<code> method. When
     * the close event occurs, that object's appropriate
     * method is invoked.
     *
     * @see CloseEvent
     */
    private class CloseListener implements ActionListener {
    	
	    /* (non-Javadoc)
	     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	     */
	    @Override
    	public void actionPerformed(ActionEvent e) {
    	    NodeMenu.this.setVisible(false);
    	}
    }
}

