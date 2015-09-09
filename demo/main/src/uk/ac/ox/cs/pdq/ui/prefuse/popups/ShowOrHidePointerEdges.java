package uk.ac.ox.cs.pdq.ui.prefuse.popups;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;

import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;
import uk.ac.ox.cs.pdq.ui.prefuse.utils.Utils;



public class ShowOrHidePointerEdges extends JCheckBox {

	private final NodeItem node;

	/** Creates a new instance of DeleteVertexMenuItem */
	public ShowOrHidePointerEdges(NodeItem node) {
		super("Hide pointer edges...");
		this.node = node;
		this.setSelected((boolean)node.get("hidePointerEdges"));
		this.setAction(new CollapseExpandListener(this.node));
	}


	private class CollapseExpandListener extends AbstractAction {

		private final NodeItem node;

		public CollapseExpandListener(NodeItem node) {
			super("Hide pointer edges...");
			this.node = node;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.updateTree(this.node, ((JCheckBox) e.getSource()).isSelected());
		}

		private void updateTree(NodeItem n, Boolean isVisible) {
			Utils.modifyNodeProperty(n.getGraph(), (Integer) n.get("id"), "hidePointerEdges", isVisible);
			this.ShowOrHidePointerEdges(n, !isVisible);
		}

		private void ShowOrHidePointerEdges(NodeItem n, Boolean isVisible) {
			if(n == null) {
				return;
			}
			Iterator iter = n.edges();
			while ( iter.hasNext() ) {
				EdgeItem eitem = (EdgeItem)iter.next();

				if( ((EdgeTypes)eitem.get("type")).equals(EdgeTypes.POINTER) ) {
					eitem.setVisible(isVisible);
				}
				
			}
			//if ( activity != null )
			//	n.getVisualization().run(activity);
		}
	}



}
