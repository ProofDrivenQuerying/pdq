package uk.ac.ox.cs.pdq.ui.prefuse.renderer;



import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import prefuse.render.AbstractShapeRenderer;
import prefuse.visual.VisualItem;

// TODO: Auto-generated Javadoc
/**
 * The Class NodeShapeRenderer.
 */
public class NodeShapeRenderer extends AbstractShapeRenderer
{

	/** The m_box. */
	protected Ellipse2D m_box = new Ellipse2D.Double();

	/* (non-Javadoc)
	 * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
	 */
	@Override
	protected Shape getRawShape(VisualItem item) 
	{	
		
		this.m_box.setFrame(item.getX(), item.getY(), 18, 18);
		return this.m_box;
		
//		if(item instanceof NodeItem) {
//			NodeTypes type = (NodeTypes) item.get("type");
//
//			switch (type) {
//
//			case SUCCESSFUL: 
//				m_box.setFrame(item.getX(), item.getY(), 16, 8);
//				return m_box;
//
//			case TERMINAL: 
//				m_box.setFrame(item.getX(), item.getY(), 6, 3);
//				return m_box;
//
//			case NOCANDIDATES: 
//				m_box.setFrame(item.getX(), item.getY(), 6, 3);
//				return m_box;
//
//			case DEAD: 
//				m_box.setFrame(item.getX(), item.getY(), 6, 3);
//				return m_box;
//
//			case ONGOING: 
//				m_box.setFrame(item.getX(), item.getY(), 12, 6);
//				return m_box;
//
//			default: 
//				throw new java.lang.IllegalArgumentException();
//			}
//		}
//		else {
//			throw new java.lang.IllegalArgumentException();
//		}

	}
}
