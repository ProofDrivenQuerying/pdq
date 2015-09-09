
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

import uk.ac.ox.cs.pdq.io.pretty.AlgebraLikeLinearPlanWriter;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.metadata.BestPlanMetadata;
import uk.ac.ox.cs.pdq.planner.linear.metadata.DominanceMetadata;
import uk.ac.ox.cs.pdq.planner.linear.metadata.DominanceMetadata.PruningTypes;
import uk.ac.ox.cs.pdq.planner.linear.metadata.EquivalenceMetadata;
import uk.ac.ox.cs.pdq.planner.linear.metadata.Metadata;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;


public class NodePropertiesDialog extends JFrame {

	private SearchNode node;


	/** Creates the GUI shown inside the frame's content pane. */
	public NodePropertiesDialog(SearchNode node) {
		super();
		this.node = node;
		this.initialise();
	}

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


	private JPanel createGeneralPanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		AlgebraLikeLinearPlanWriter.to(new PrintStream(bos)).write(this.node.getConfiguration().getPlan());
		PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Type"), this.node.getStatus().toString());
		PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Access command"), bos.toString());
		return titledBorders;
	}


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

	private JPanel createPruningPanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));

		Metadata metadata = node.getMetadata();
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
		}
		return titledBorders;
	}


	private JPanel createEquivalencePanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));

		Metadata metadata = node.getMetadata();
		if(metadata instanceof EquivalenceMetadata) {
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Equivalent node"), node.getPointer().toString());
		}
		else {
			throw new java.lang.IllegalArgumentException();
		}
		return titledBorders;
	}


	private JPanel createSuccessPanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));

		Metadata metadata = node.getMetadata();
		if(metadata instanceof BestPlanMetadata) {
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Found proof"), ((BestPlanMetadata) metadata).getProof().toString());
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Found plan"), ((BestPlanMetadata) metadata).getPlan().toString());
			PopupUtils.addBorderAndTextToContainer(titledBorders, BorderFactory.createTitledBorder("Found plan's cost"), ((BestPlanMetadata) metadata).getPlan().getCost().toString());
		}
		else {
			throw new java.lang.IllegalArgumentException();
		}
		return titledBorders;
	}



	private JPanel createMiscellaneousPanel(SearchNode node, Border border) {
		JPanel titledBorders = new JPanel();
		titledBorders.setBorder(border);
		titledBorders.setLayout(new BoxLayout(titledBorders, BoxLayout.Y_AXIS));

		Metadata metadata = node.getMetadata();

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
		}

	}


}
