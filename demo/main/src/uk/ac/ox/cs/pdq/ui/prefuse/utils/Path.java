package uk.ac.ox.cs.pdq.ui.prefuse.utils;

import java.util.List;

import prefuse.visual.NodeItem;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.base.Joiner;

public class Path {
	
	private final List<Integer> ipath;
	private List<NodeItem> npath;
	private final Plan plan;
	
	public Path(List<Integer> ipath,  Plan plan) {
		this.ipath = ipath;
		this.plan = plan;
	}
	
	public boolean contains(NodeItem node) {
		return this.npath.contains(node);
	}
	
	public boolean contains(Integer node) {
		return this.ipath.contains(node);
	}
	
	public List<NodeItem> getNodesPath() {
		return this.npath;
	}
	
	public List<Integer> getIntegerPath() {
		return this.ipath;
	}
	
	public Plan getPlan(){
		return this.plan;
	}
	
	public void setNodesPath(List<NodeItem> npath) {
		this.npath = npath;
	}
	@Override
	public String toString() {
		return Joiner.on(",").join(this.ipath) + " " + plan.getCost().toString();
	}
}