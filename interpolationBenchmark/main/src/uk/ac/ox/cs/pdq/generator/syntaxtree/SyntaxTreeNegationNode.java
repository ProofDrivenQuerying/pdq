package uk.ac.ox.cs.pdq.generator.syntaxtree;

import java.util.List;

import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Negation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class SyntaxTreeNegationNode extends SyntaxTreeNode{
	
	private final LogicalSymbols operator = LogicalSymbols.NEGATION;
	private SyntaxTreeNode child;
	
	public SyntaxTreeNegationNode(Formula rootedSubformula) {
		super(rootedSubformula);
		Preconditions.checkArgument(rootedSubformula instanceof Negation);
	}

	@Override
	public List<SyntaxTreeNode> getChildren() {
		return Lists.newArrayList(this.child);
	}

	@Override
	public boolean addChild(SyntaxTreeNode node) {
		if(this.child != null) {
			return false;
		}
		else {
			this.child = node;
			return true;
		}
	}

	/**
	 * @return the operator
	 */
	public LogicalSymbols getOperator() {
		return this.operator;
	}
	
	@Override
	public boolean isLeaf() {
		return false;
	}
	
}
