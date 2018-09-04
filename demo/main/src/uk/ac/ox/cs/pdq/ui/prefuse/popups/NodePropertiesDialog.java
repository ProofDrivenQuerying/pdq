
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
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
//import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.BestPlanMetadata;
//import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.DominanceMetadata;
//import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.EquivalenceMetadata;
//import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.Metadata;
//import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.DominanceMetadata.PruningTypes;
import uk.ac.ox.cs.pdq.ui.proof.Proof;


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
// MR		AlgebraLikeLeftDeepPlanWriter.to(new PrintStream(bos)).write(this.node.getConfiguration().getPlan());
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

/* MR		Metadata metadata = node.getMetadata();
		if(metadata instanceof DominanceMetadata) {
			if(((DominanceMetadata) metadata).getType().equals(PruningTypes.DOMINANCE)) {
				PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Dominance node"), ((DominanceMetadata) metadata).getDominance().toString());
			}
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Dominated plan"), 
					((DominanceMetadata) metadata).getDominancePlan().toString());
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Dominated plan's cost"), 
					((DominanceMetadata) metadata).getDominatedPlan().getCost().toString());
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Dominance plan"), ((DominanceMetadata) metadata).getDominancePlan().toString());
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Dominance plan's cost"), 
					((DominanceMetadata) metadata).getDominancePlan().getCost().toString());
		}	
		else {
			throw new java.lang.IllegalArgumentException();
		}*/
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

/* MR		Metadata metadata = node.getMetadata();
		if(metadata instanceof EquivalenceMetadata) {
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Equivalent node"), node.getPointer().toString());
		}
		else {
			throw new java.lang.IllegalArgumentException();
		}*/
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

/* MR		Metadata metadata = node.getMetadata();
		if(metadata instanceof BestPlanMetadata) {
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Found proof"), Proof.toProof(((BestPlanMetadata) metadata).getConfigurations()).toString());
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Found plan"), ((BestPlanMetadata) metadata).getPlan().toString());
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Found plan's cost"), ((BestPlanMetadata) metadata).getPlan().getCost().toString());
		}
		else {
			throw new java.lang.IllegalArgumentException();
		}*/
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

/* MR		Metadata metadata = node.getMetadata();

		if(metadata instanceof DominanceMetadata) {
			return this.createPruningPanel(node, border);
		}
		else if(metadata instanceof EquivalenceMetadata) {
			return this.createEquivalencePanel(node, border);
		}
		else if(metadata instanceof BestPlanMetadata) {
			return this.createSuccessPanel(node, border);
		}
		else {
			return null;
		}*/
		return null;
	}


}
