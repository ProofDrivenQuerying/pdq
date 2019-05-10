
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

//import uk.ac.ox.cs.pdq.io.pretty.AlgebraLikeLeftDeepPlanWriter;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;


// TODO: Auto-generated Javadoc
/**
 * The Class NodePropertiesDialog.
 */
public class NodePropertiesDialog extends JFrame {

	/** The node. */
	private SearchNode node;


	/**
	 *  Creates the GUI shown inside the frame's content pane.
	 *
	 * @param node the node
	 */
	public NodePropertiesDialog(SearchNode node) {
		super();
		this.node = node;
		this.initialise();
	}

	/**
	 * Initialise.
	 */
	private void initialise() {

		Border border = BorderFactory.createEmptyBorder(20, 20, 5, 20);
		
		JPanel planPanel = this.createGeneralPanel(this.node, border);
		JPanel candidatesPanel = this.createCandidatesPanel(this.node, border);
		JPanel metaPanel = this.createMiscellaneousPanel(this.node, border);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("General...", null, planPanel); 
		tabbedPane.addTab("Candidates...", null, candidatesPanel);

		if(metaPanel != null) {
			tabbedPane.addTab("Metadata...", null, metaPanel); 
		}

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
		new PrintStream(bos).println(this.node.getConfiguration().getPlan().toString());
		PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Type"), this.node.getStatus().toString());
		PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Access command"), bos.toString());
		return titledBorders;
	}


	/**
	 * Creates the candidates panel.
	 *
	 * @param node the node
	 * @param border the border
	 * @return the j panel
	 */
	private JPanel createCandidatesPanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));

		if(!node.getConfiguration().getCandidates().isEmpty()) {
			int c = 1;
			for(Candidate candidate:node.getConfiguration().getCandidates()) {
				PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Candidate " + c++), candidate.toString());
			}
		}
		else {
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("NO CANDIDATES "), "");
		}

		return titledBorders;
	}

	/**
	 * Creates the pruning panel.
	 *
	 * @param node the node
	 * @param border the border
	 * @return the j panel
	 */
	private JPanel createPruningPanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));

		return titledBorders;
	}


	/**
	 * Creates the equivalence panel.
	 *
	 * @param node the node
	 * @param border the border
	 * @return the j panel
	 */
	private JPanel createEquivalencePanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));

		return titledBorders;
	}


	/**
	 * Creates the success panel.
	 *
	 * @param node the node
	 * @param border the border
	 * @return the j panel
	 */
	private JPanel createSuccessPanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));

		return titledBorders;
	}



	/**
	 * Creates the miscellaneous panel.
	 *
	 * @param node the node
	 * @param border the border
	 * @return the j panel
	 */
	private JPanel createMiscellaneousPanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));

		return null;
	}


}
