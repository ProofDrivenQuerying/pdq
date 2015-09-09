package uk.ac.ox.cs.pdq.ui.prefuse.popups;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;

import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;
import uk.ac.ox.cs.pdq.ui.prefuse.utils.Utils;



public class CollapseExpandTree extends JCheckBox {

	private final NodeItem node;

	/** Creates a new instance of DeleteVertexMenuItem */
	public CollapseExpandTree(NodeItem node) {
		super("Collapse...");
		this.node = node;
		this.setSelected((boolean)node.get("isCollapsed"));
		this.setAction(new CollapseExpandListener(this.node));
	}


	private class CollapseExpandListener extends AbstractAction {

		private final NodeItem node;

		public CollapseExpandListener(NodeItem node) {
			super("Collapse...");
			this.node = node;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.updateTree(this.node, ((JCheckBox) e.getSource()).isSelected());
		}

		private void updateTree(NodeItem n, Boolean isVisible) {

			Utils.modifyNodeProperty(n.getGraph(), (Integer) n.get("id"), "isCollapsed", isVisible);
			this.updateTreeRecursive(n, !isVisible);

		}

		private void updateTreeRecursive(NodeItem n, Boolean isVisible) {
			if(n == null) {
				return;
			}
			
			
			
			if( n.equals(this.node)) {
				
				Iterator out = n.outEdges();
				
				while ( out.hasNext() ) {
					EdgeItem eitem = (EdgeItem)out.next();
					if( ((EdgeTypes)eitem.get("type")).equals(EdgeTypes.HIERARCHY) ) {
						NodeItem nitem = eitem.getAdjacentItem(n);
						eitem.setVisible(isVisible);
						nitem.setVisible(isVisible);
						this.updateTreeRecursive(nitem, isVisible);
					}
				}
				
			}
			else {
				
				Iterator in = n.inEdges();
				while( in.hasNext()) {
					EdgeItem eitem = (EdgeItem)in.next();
					eitem.setVisible(isVisible);
				}
				
				Iterator out = n.outEdges();
				while ( out.hasNext() ) {
					EdgeItem eitem = (EdgeItem)out.next();
					if( ((EdgeTypes)eitem.get("type")).equals(EdgeTypes.POINTER) ) {
						eitem.setVisible(isVisible);
					}
					else {
						NodeItem nitem = eitem.getAdjacentItem(n);
						eitem.setVisible(isVisible);
						nitem.setVisible(isVisible);
						this.updateTreeRecursive(nitem, isVisible);
					}
				}
				
			}
			
			
//			Iterator out = n.outEdges();
//			
//			Iterator in = n.inEdges();
//			
//			while( in.hasNext()) {
//				EdgeItem eitem = (EdgeItem)in.next();
//				eitem.setVisible(isVisible);
//			}
//			
//			while ( out.hasNext() ) {
//				EdgeItem eitem = (EdgeItem)out.next();
//				if( ((EdgeTypes)eitem.get("type")).equals(EdgeTypes.POINTER) ) {
//					eitem.setVisible(isVisible);
//				}
//				else {
//					NodeItem nitem = eitem.getAdjacentItem(n);
//					eitem.setVisible(isVisible);
//					nitem.setVisible(isVisible);
//					this.updateTreeRecursive(nitem, isVisible);
//				}
//			}
			
			
			//if ( activity != null )
			//	n.getVisualization().run(activity);
		}
	}



}
