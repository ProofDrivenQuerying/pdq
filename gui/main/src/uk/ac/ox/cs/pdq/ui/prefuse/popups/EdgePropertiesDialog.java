
package uk.ac.ox.cs.pdq.ui.prefuse.popups;

import java.awt.BorderLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;


// TODO: Auto-generated Javadoc
/**
 * The Class EdgePropertiesDialog.
 */
public class EdgePropertiesDialog extends JFrame {

	/** The type. */
	private EdgeTypes type;
	
	/** The node. */
	private SearchNode node;
	
	/**
	 *  Creates the GUI shown inside the frame's content pane.
	 *
	 * @param frame the frame
	 * @param type the type
	 * @param node the node
	 */
	public EdgePropertiesDialog(JFrame frame, EdgeTypes type, SearchNode node) {
		this.type = type;
		this.node = node;
		this.initialise();
	}

	/**
	 * Initialise.
	 */
	private void initialise() {

		Border border = BorderFactory.createEmptyBorder(20,20,5,20);

		//Create the components.
		JPanel planPanel = this.createGeneralPanel(this.node, border);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("General...", null, planPanel); 

		this.add(tabbedPane, BorderLayout.CENTER);

	}

	/**
	 * Creates the general panel.
	 *
	 * @param node the node
	 * @param border the border
	 * @return the j panel
	 */
	private JPanel createGeneralPanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		(new PrintStream(bos)).println(this.node.getConfiguration().getPlan().toString());
		PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Type"), this.type.toString());
		PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Plan"), bos.toString());
		return titledBorders;
	}



}
