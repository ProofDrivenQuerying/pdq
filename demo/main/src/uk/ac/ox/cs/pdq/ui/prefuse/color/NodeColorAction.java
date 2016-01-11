package uk.ac.ox.cs.pdq.ui.prefuse.color;

import prefuse.action.assignment.DataColorAction;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;

public class NodeColorAction extends DataColorAction{

	private final int[] nodePalette = new int[] {
			ColorLib.rgba(255,180,180,250), ColorLib.rgba(153,204,255,250), ColorLib.gray(150), ColorLib.gray(150), 
			ColorLib.gray(50)
	};

	public NodeColorAction(String group, String dataField, int dataType, String colorField) {
		super(group, dataField, dataType, colorField);
	}


	@Override
	public int getColor(VisualItem item) {

		if(item instanceof NodeItem) {
			switch((NodeStatus)item.get("type")) {
			case SUCCESSFUL:
				return this.nodePalette[0];
			case ONGOING:
				return this.nodePalette[1];
			case TERMINAL: 
				return this.nodePalette[3];
//			case DEAD:
//				return this.nodePalette[4];
			default:
				return this.m_defaultColor;
			}
		}
		return super.getColor(item);
	}

}
