package uk.ac.ox.cs.pdq.ui.prefuse.popups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import prefuse.visual.NodeItem;

public class NodeMenu extends JPopupMenu {
	
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
    
    private class CloseListener implements ActionListener {
    	@Override
    	public void actionPerformed(ActionEvent e) {
    	    NodeMenu.this.setVisible(false);
    	}
    }
}

