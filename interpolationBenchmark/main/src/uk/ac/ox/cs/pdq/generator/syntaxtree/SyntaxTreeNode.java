package uk.ac.ox.cs.pdq.generator.syntaxtree;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.fol.Formula;

public abstract class SyntaxTreeNode {
	public static Integer globalId=0;
	protected final Integer id;
	protected final Formula rootedSubformula;
	protected int depth;
	protected String toString;
	
	public SyntaxTreeNode(Formula rootedSubformula) {
		Preconditions.checkArgument(rootedSubformula != null);
		this.rootedSubformula = rootedSubformula;
		this.id = globalId++;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public void setDepth(int depth) {
		Preconditions.checkArgument(depth >= 0);
		this.depth = depth;
	}

	public int getDepth() {
		return this.depth;
	}

	public Formula getRootedFormula() {
		return this.rootedSubformula;
	}
	
	@Override
	public String toString() {
		if(this.toString == null) {
			this.toString = toString(this);
		}
		return this.toString;
	}
	
	
	protected static String toString(SyntaxTreeNode node) {
		String outputString = "";
		if(node instanceof SyntaxTreeBinaryNode) {
			outputString += "(" + toString(node.getChildren().get(0)) + ((SyntaxTreeBinaryNode)node).getOperator() + toString(node.getChildren().get(1)) + ")";
		}
		else if(node instanceof SyntaxTreeQuantifiedNode) {
			outputString += "(" + 
						((SyntaxTreeQuantifiedNode)node).getOperator() + 
						"[" + Joiner.on(",").join(((SyntaxTreeQuantifiedNode)node).getVariables()) + "]" + 
						toString(node.getChildren().get(0)) + 
					")";
		}
		else if(node instanceof SyntaxTreeNegationNode) {
			outputString += "(" + ((SyntaxTreeNegationNode)node).getOperator() + toString(node.getChildren().get(0)) + ")";
		}
		else if(node instanceof SyntaxTreeAtomNode) {
			outputString += ((SyntaxTreeAtomNode)node).getAtom().toString();
		}
		return outputString;
	}
	
	public abstract List<SyntaxTreeNode> getChildren();
	public abstract boolean addChild(SyntaxTreeNode node);
	public abstract boolean isLeaf();
}
