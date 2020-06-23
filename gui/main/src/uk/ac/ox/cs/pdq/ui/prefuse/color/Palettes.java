// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.color;

import java.util.Random;

import prefuse.util.ColorLib;

// TODO: Auto-generated Javadoc
/**
 * The Class Palettes.
 */
public class Palettes {
	

	
	/** The random. */
	private Random random = new Random(150);

	/** The total plans. */
	private final int totalPlans = 10000;
	
	/** The node palette. */
	private final int[] nodePalette = new int[] {
			ColorLib.rgba(255,180,180,250), ColorLib.rgba(153,204,255,250), ColorLib.gray(150), ColorLib.gray(150), 
			ColorLib.gray(50)
	};
	
	/** The node highligh palette. */
	private final int[] nodeHighlighPalette = new int[] {
			ColorLib.rgba(200,0,255, 250), ColorLib.rgba(0,0,255, 250), ColorLib.rgba(155,100,0, 250), ColorLib.rgba(155,100,0, 250), 
			ColorLib.rgba(0,0,50,250)
	};
	
	/** The path highligh palette. */
	private final int[] pathHighlighPalette = new int[] {
			ColorLib.rgb(0,190,0), ColorLib.rgb(0,0,180), ColorLib.rgb(0,0,0)
	};
	
	/** The aggegate palette. */
	private int[] aggegatePalette = new int[totalPlans];
	
	/** The aggegate highlight palette. */
	private int[] aggegateHighlightPalette = new int[totalPlans];
	
	
	/** The edge stroke palette. */
	private final int[] edgeStrokePalette = new int[] { ColorLib.rgb(50, 10, 200), ColorLib.gray(200) };
	
	/** The edge stroke highlight palette. */
	private final int edgeStrokeHighlightPalette = ColorLib.rgb(255,200,125);
	
	/** The aggregate stroke palette. */
	private final int aggregateStrokePalette = ColorLib.gray(200);
	
	/** The aggregate stroke highlight palette. */
	private final int aggregateStrokeHighlightPalette = ColorLib.rgb(255,100,100);

	/** The text palette. */
	private final int textPalette = ColorLib.gray(0);
	
	/**
	 * Instantiates a new palettes.
	 */
	public Palettes() {
		this.createAggregatePalettes();
	}

	/**
	 * Gets the node palette.
	 *
	 * @return the node palette
	 */
	public int[] getNodePalette() {
		return nodePalette;
	}

	/**
	 * Gets the node highligh palette.
	 *
	 * @return the node highligh palette
	 */
	public int[] getNodeHighlighPalette() {
		return nodeHighlighPalette;
	}

	/**
	 * Gets the path highligh palette.
	 *
	 * @return the path highligh palette
	 */
	public int[] getPathHighlighPalette() {
		return pathHighlighPalette;
	}

	/**
	 * Gets the aggegate palette.
	 *
	 * @return the aggegate palette
	 */
	public int[] getAggegatePalette() {
			return this.aggegatePalette;
	}
	
	/**
	 * Gets the aggegate highlight palette.
	 *
	 * @return the aggegate highlight palette
	 */
	public int[] getAggegateHighlightPalette() {
		return this.aggegateHighlightPalette;
	}
	
	/**
	 * Creates the aggregate palettes.
	 */
	private void createAggregatePalettes() {
		
		int bestSuccessPath = ColorLib.rgba(255,200,200,250);
		
		int bestHighlightedSuccessPath = ColorLib.rgba(255,200,200,250);
		
		this.aggegatePalette[0] = bestSuccessPath;
		this.aggegateHighlightPalette[0] = bestHighlightedSuccessPath;
		
		for(int i = 1; i < this.totalPlans; ++i) {
			int r = this.random.nextInt(255);
			int g = this.random.nextInt(255);
			int b = this.random.nextInt(255);
			this.aggegatePalette[i] = ColorLib.rgba(r,g,b,250); 
			this.aggegateHighlightPalette[i] = ColorLib.rgba(r,g,b,250); 
		}
	}

	/**
	 * Gets the edge palette.
	 *
	 * @return the edge palette
	 */
	public int[] getEdgePalette() {
		return edgeStrokePalette;
	}

	/**
	 * Gets the text palette.
	 *
	 * @return the text palette
	 */
	public int getTextPalette() {
		return textPalette;
	}

	/**
	 * Gets the edge highlight palette.
	 *
	 * @return the edge highlight palette
	 */
	public int getEdgeHighlightPalette() {
		return edgeStrokeHighlightPalette;
	}

	/**
	 * Gets the aggregate stroke palette.
	 *
	 * @return the aggregate stroke palette
	 */
	public int getAggregateStrokePalette() {
		return aggregateStrokePalette;
	}

	/**
	 * Gets the aggregate stroke highlight palette.
	 *
	 * @return the aggregate stroke highlight palette
	 */
	public int getAggregateStrokeHighlightPalette() {
		return aggregateStrokeHighlightPalette;
	}
}
