// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.layout;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import prefuse.action.layout.Layout;
import prefuse.util.GraphicsLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;

// TODO: Auto-generated Javadoc
/**
 * Layout algorithm that computes a convex hull surrounding
 * aggregate items and saves it in the "_polygon" field.
 */
public class AggregateLayout extends Layout {

	/** The m_margin. */
	private int m_margin = 5; // convex hull pixel margin
	
	/** The m_pts. */
	private double[] m_pts;   // buffer for computing convex hulls

	/**
	 * Instantiates a new aggregate layout.
	 *
	 * @param aggrGroup the aggr group
	 */
	public AggregateLayout(String aggrGroup) {
		super(aggrGroup);
	}

	/**
	 * Run.
	 *
	 * @param frac the frac
	 * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry, double)
	 */
	public void run(double frac) {
		AggregateTable aggr = (AggregateTable)m_vis.getGroup(m_group);
		// do we have any  to process?
		int num = aggr.getTupleCount();
		if ( num == 0 ) return;

		// update buffers
		int maxsz = 0;
		for ( Iterator aggrs = aggr.tuples(); aggrs.hasNext();  )
			maxsz = Math.max(maxsz, 4*2*
					((AggregateItem)aggrs.next()).getAggregateSize());
		if ( m_pts == null || maxsz > m_pts.length ) {
			m_pts = new double[maxsz];
		}

		// compute and assign convex hull for each aggregate
		Iterator aggrs = m_vis.visibleItems(m_group);
		while ( aggrs.hasNext() ) {
			AggregateItem aitem = (AggregateItem)aggrs.next();

			int idx = 0;
			if ( aitem.getAggregateSize() == 0 ) continue;
			VisualItem item = null;
			Iterator iter = aitem.items();
			while ( iter.hasNext() ) {
				item = (VisualItem)iter.next();
				if ( item.isVisible() ) {
					addPoint(m_pts, idx, item, m_margin);
					idx += 2*4;
				}
			}
			// if no aggregates are visible, do nothing
			if ( idx == 0 ) continue;

			// compute convex hull
			double[] nhull = GraphicsLib.convexHull(m_pts, idx);

			// prepare viz attribute array
			float[]  fhull = (float[])aitem.get(VisualItem.POLYGON);
			if ( fhull == null || fhull.length < nhull.length )
				fhull = new float[nhull.length];
			else if ( fhull.length > nhull.length )
				fhull[nhull.length] = Float.NaN;

			// copy hull values
			for ( int j=0; j<nhull.length; j++ )
				fhull[j] = (float)nhull[j];
			aitem.set(VisualItem.POLYGON, fhull);
			aitem.setValidated(false); // force invalidation
		}
	}

	/**
	 * Adds the point.
	 *
	 * @param pts the pts
	 * @param idx the idx
	 * @param item the item
	 * @param growth the growth
	 */
	private static void addPoint(double[] pts, int idx, VisualItem item, int growth) {
		Rectangle2D b = item.getBounds();
		double minX = (b.getMinX())-growth, minY = (b.getMinY())-growth;
		double maxX = (b.getMaxX())+growth, maxY = (b.getMaxY())+growth;
		pts[idx]   = minX; pts[idx+1] = minY;
		pts[idx+2] = minX; pts[idx+3] = maxY;
		pts[idx+4] = maxX; pts[idx+5] = minY;
		pts[idx+6] = maxX; pts[idx+7] = maxY;
	}

} 