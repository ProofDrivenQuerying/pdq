// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

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
