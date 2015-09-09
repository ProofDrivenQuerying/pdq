package uk.ac.ox.cs.pdq.ui.prefuse.popups;

import javax.swing.JFrame;

import prefuse.visual.NodeItem;

public class NodePanel extends JFrame {

	private NodeMenu menu;
	
	public NodePanel(JFrame frame, NodeItem node) {
		this.menu = new NodeMenu(frame, node);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.add(this.menu);
		this.setSize(250, 250);
		this.pack();           // layout components in window
		this.setVisible(true); // show the window
	}
}
