package uk.ac.ox.cs.pdq.ui.prefuse.color;

import java.util.Random;

import prefuse.util.ColorLib;

public class Palettes {
	

	
	private Random random = new Random(150);

	private final int totalPlans = 10000;
	
	private final int[] nodePalette = new int[] {
			ColorLib.rgba(255,180,180,250), ColorLib.rgba(153,204,255,250), ColorLib.gray(150), ColorLib.gray(150), 
			ColorLib.gray(50)
	};
	
	private final int[] nodeHighlighPalette = new int[] {
			ColorLib.rgba(200,0,255, 250), ColorLib.rgba(0,0,255, 250), ColorLib.rgba(155,100,0, 250), ColorLib.rgba(155,100,0, 250), 
			ColorLib.rgba(0,0,50,250)
	};
	
	private final int[] pathHighlighPalette = new int[] {
			ColorLib.rgb(0,190,0), ColorLib.rgb(0,0,180), ColorLib.rgb(0,0,0)
	};
	
	private int[] aggegatePalette = new int[totalPlans];
	
	private int[] aggegateHighlightPalette = new int[totalPlans];
	
	
	private final int[] edgeStrokePalette = new int[] { ColorLib.rgb(50, 10, 200), ColorLib.gray(200) };
	
	private final int edgeStrokeHighlightPalette = ColorLib.rgb(255,200,125);
	
	private final int aggregateStrokePalette = ColorLib.gray(200);
	
	private final int aggregateStrokeHighlightPalette = ColorLib.rgb(255,100,100);

	private final int textPalette = ColorLib.gray(0);
	
	public Palettes() {
		this.createAggregatePalettes();
	}

	public int[] getNodePalette() {
		return nodePalette;
	}

	public int[] getNodeHighlighPalette() {
		return nodeHighlighPalette;
	}

	public int[] getPathHighlighPalette() {
		return pathHighlighPalette;
	}

	public int[] getAggegatePalette() {
			return this.aggegatePalette;
	}
	
	public int[] getAggegateHighlightPalette() {
		return this.aggegateHighlightPalette;
	}
	
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

	public int[] getEdgePalette() {
		return edgeStrokePalette;
	}

	public int getTextPalette() {
		return textPalette;
	}

	public int getEdgeHighlightPalette() {
		return edgeStrokeHighlightPalette;
	}

	public int getAggregateStrokePalette() {
		return aggregateStrokePalette;
	}

	public int getAggregateStrokeHighlightPalette() {
		return aggregateStrokeHighlightPalette;
	}
}
