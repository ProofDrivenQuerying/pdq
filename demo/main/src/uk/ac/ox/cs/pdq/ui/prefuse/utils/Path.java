package uk.ac.ox.cs.pdq.ui.prefuse.utils;

import java.util.List;

import prefuse.visual.NodeItem;
//import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.cost.Cost;

import com.google.common.base.Joiner;

// TODO: Auto-generated Javadoc
/**
 * The Class Path.
 */
public class Path {
	
	/** The ipath. */
	private final List<Integer> ipath;
	
	/** The npath. */
	private List<NodeItem> npath;
	
	/** The plan. */
   private final Plan plan;

	private Cost cost = null;
	
	/**
	 * Instantiates a new path.
	 *
	 * @param ipath the ipath
	 * @param plan the plan
	 */
	public Path(List<Integer> ipath,  Plan plan, Cost cost) {
		this.ipath = ipath;
		this.plan = plan;
		this.cost  = cost;
	}
	
	public Path(List<Integer> ipath,  Plan plan) {
		this.ipath = ipath;
		this.plan = plan;
	}
	
	/**
	 * Contains.
	 *
	 * @param node the node
	 * @return true, if successful
	 */
	public boolean contains(NodeItem node) {
		return this.npath.contains(node);
	}
	
	/**
	 * Contains.
	 *
	 * @param node the node
	 * @return true, if successful
	 */
	public boolean contains(Integer node) {
		return this.ipath.contains(node);
	}
	
	/**
	 * Gets the nodes path.
	 *
	 * @return the nodes path
	 */
	public List<NodeItem> getNodesPath() {
		return this.npath;
	}
	
	/**
	 * Gets the integer path.
	 *
	 * @return the integer path
	 */
	public List<Integer> getIntegerPath() {
		return this.ipath;
	}
	
	/**
	 * Gets the plan.
	 *
	 * @return the plan
	 */
	public Plan getPlan(){
		return this.plan;
	}
	
	/**
	 * Sets the nodes path.
	 *
	 * @param npath the new nodes path
	 */
	public void setNodesPath(List<NodeItem> npath) {
		this.npath = npath;
	}
	
	public Cost getCost() {
		return cost;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Joiner.on(",").join(this.ipath) + " " + cost;
	}
}