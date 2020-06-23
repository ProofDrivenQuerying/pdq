// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.renderer;

import java.awt.Shape;

import prefuse.Constants;
import prefuse.render.EdgeRenderer;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import uk.ac.ox.cs.pdq.ui.prefuse.types.EdgeTypes;

// TODO: Auto-generated Javadoc
/**
 * The Class EdgeShapeRenderer.
 */
public class EdgeShapeRenderer extends EdgeRenderer{

	/**
	 * Create a new EdgeRenderer.
	 */
	public EdgeShapeRenderer() {
		super();
	}
	
    /**
     * Create a new EdgeRenderer with the given edge type.
     * @param edgeType the edge type, one of
     * {@link prefuse.Constants#EDGE_TYPE_LINE} or
     * {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     */
    public EdgeShapeRenderer(int edgeType) {
        super(edgeType);
    }
    
    /**
     * Create a new EdgeRenderer with the given edge and arrow types.
     * @param edgeType the edge type, one of
     * {@link prefuse.Constants#EDGE_TYPE_LINE} or
     * {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     * @param arrowType the arrow type, one of
     * {@link prefuse.Constants#EDGE_ARROW_FORWARD},
     * {@link prefuse.Constants#EDGE_ARROW_REVERSE}, or
     * {@link prefuse.Constants#EDGE_ARROW_NONE}.
     * @see #setArrowType(int)
     */
    public EdgeShapeRenderer(int edgeType, int arrowType) {
    	super(edgeType, arrowType);
    }

	/**
	 * Gets the raw shape.
	 *
	 * @param item the item
	 * @return the raw shape
	 * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
	 */
	protected Shape getRawShape(VisualItem item) {
		EdgeItem   edge = (EdgeItem)item;
		EdgeTypes type = (EdgeTypes) edge.get("type");
		if(type == null) {
			Thread.yield();
			type = (EdgeTypes) edge.get("type");
		}
		if(type != null)
		{
			switch(type) {
			case HIERARCHY:
				this.m_edgeType = Constants.EDGE_TYPE_LINE;
				this.m_edgeArrow = Constants.EDGE_ARROW_FORWARD;
				this.m_width = 1.0;
				return super.getRawShape(item);
			case POINTER:
				this.m_edgeType = Constants.EDGE_TYPE_CURVE;
				this.m_edgeArrow = Constants.EDGE_ARROW_FORWARD;
				this.m_arrowWidth = 10;
				this.m_width = 1.0;
				return super.getRawShape(item);
			default:
				throw new java.lang.IllegalArgumentException();
			}
		}
		else
		{
			throw new java.lang.IllegalArgumentException();			
		}
	}

}
