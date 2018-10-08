package uk.ac.ox.cs.pdq.generator.syntaxtree;

import java.util.List;

import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class SyntaxTreeQuantifiedNode extends SyntaxTreeNode{
	
	private final LogicalSymbols operator;
	private final List<Variable> variables;
	private SyntaxTreeNode child;
	
	public SyntaxTreeQuantifiedNode(Formula rootedSubformula) {
		super(rootedSubformula);
		Preconditions.checkArgument(rootedSubformula instanceof QuantifiedFormula);
		this.operator = ((QuantifiedFormula)rootedSubformula).getOperator();
		this.variables =  ((QuantifiedFormula)rootedSubformula).getBoundVariables();
	}

	@Override
	public List<SyntaxTreeNode> getChildren() {
		return Lists.newArrayList();
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

	/**
	 * @return the variables
	 */
	public List<Variable> getVariables() {
		return this.variables;
	}
	
	@Override
	public boolean isLeaf() {
		return false;
	}
}
