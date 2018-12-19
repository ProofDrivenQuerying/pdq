package uk.ac.ox.cs.pdq.ui.prefuse.popups;

import javax.swing.JFrame;

import prefuse.visual.NodeItem;

// TODO: Auto-generated Javadoc
/**
 * The Class NodePanel.
 */
public class NodePanel extends JFrame {

	/** The menu. */
	private NodeMenu menu;
	
	/**
	 * Instantiates a new node panel.
	 *
	 * @param frame the frame
	 * @param node the node
	 */
	public NodePanel(JFrame frame, NodeItem node) {
		this.menu = new NodeMenu(frame, node);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.add(this.menu);
		this.setSize(250, 250);
		this.pack();           // layout components in window
		this.setVisible(true); // show the window
	}
}
