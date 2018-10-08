package uk.ac.ox.cs.pdq.generator.syntaxtree;

import java.util.List;

import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;

import com.google.common.collect.Lists;

public class SyntaxTreeBinaryNode extends SyntaxTreeNode{
	
	private final LogicalSymbols operator;
	private List<SyntaxTreeNode> children = Lists.newArrayList();
	
	public SyntaxTreeBinaryNode(Formula rootedSubformula) {
		super(rootedSubformula);
		if(rootedSubformula instanceof Conjunction) {
			this.operator = LogicalSymbols.AND;
		}
		else if(rootedSubformula instanceof Disjunction) {
			this.operator = LogicalSymbols.OR;
		}
		else if(rootedSubformula instanceof Implication) {
			this.operator = LogicalSymbols.IMPLIES;
		}
		else {
			throw new java.lang.RuntimeException("The only binary operators that are supported are conjunction, disjunction and implication");
		}
		
	}

	@Override
	public List<SyntaxTreeNode> getChildren() {
		return this.children;
	}

	@Override
	public boolean addChild(SyntaxTreeNode node) {
		if(this.children.size()==2) {
			return false;
		}
		else {
			this.children.add(node);
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
