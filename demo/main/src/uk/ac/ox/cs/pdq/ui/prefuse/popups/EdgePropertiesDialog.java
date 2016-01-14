
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

import uk.ac.ox.cs.pdq.io.pretty.AlgebraLikeLeftDeepPlanWriter;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;


public class EdgePropertiesDialog extends JFrame {

	private EdgeTypes type;
	private SearchNode node;
	
	/** Creates the GUI shown inside the frame's content pane. */
	public EdgePropertiesDialog(JFrame frame, EdgeTypes type, SearchNode node) {
		this.type = type;
		this.node = node;
		this.initialise();
	}

	private void initialise() {

		Border border = BorderFactory.createEmptyBorder(20,20,5,20);

		//Create the components.
		JPanel planPanel = this.createGeneralPanel(this.node, border);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("General...", null, planPanel); 

		this.add(tabbedPane, BorderLayout.CENTER);

	}

	private JPanel createGeneralPanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		AlgebraLikeLeftDeepPlanWriter.to(new PrintStream(bos)).write(this.node.getConfiguration().getPlan());
		PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Type"), this.type.toString());
		PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Plan"), bos.toString());
		return titledBorders;
	}



}
