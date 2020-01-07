package uk.ac.ox.cs.pdq.ui.event;
import prefuse.data.Graph;

public abstract class PDQShape {

	protected Graph graph;
	
	protected PDQShape(Graph g)
	{
		graph = g;
	}

	abstract public void drawShape();
	private void nothing()
	{
	  
	}
}
