package uk.ac.ox.cs.pdq.generator.syntaxtree;

import java.util.Map;
import java.util.Map.Entry;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class SyntaxTree {

	private String toString = null;
	private final SyntaxTreeNode root;
	private final Map<Integer,SyntaxTreeNode> nodesMap;

	public SyntaxTree(SyntaxTreeNode root) {
		Preconditions.checkNotNull(root);
		this.root = root;
		this.nodesMap = Maps.newLinkedHashMap();
		createNodesMap(this.root, this.nodesMap);
	}

	/**
	 * @return the root
	 */
	public SyntaxTreeNode getRoot() {
		return this.root;
	}

	/**
	 * @return the nodesMap
	 */
	public Map<Integer,SyntaxTreeNode> getNodesMap() {
		return this.nodesMap;
	}

	public static void createNodesMap(SyntaxTreeNode node, Map<Integer,SyntaxTreeNode> nodesMap) {
		if(node instanceof SyntaxTreeAtomNode) {
			nodesMap.put(node.getId(), node);
		}
		else if(node instanceof SyntaxTreeQuantifiedNode || node instanceof SyntaxTreeNegationNode) {
			nodesMap.put(node.getId(), node);
			createNodesMap(node.getChildren().get(0), nodesMap);
		}
		else if(node instanceof SyntaxTreeBinaryNode) {
			nodesMap.put(node.getId(), node);
			createNodesMap(node.getChildren().get(0), nodesMap);
			createNodesMap(node.getChildren().get(1), nodesMap);
		}
	}

	public static SyntaxTree createSyntaxTree(Formula formula) {
		SyntaxTreeNode root = null;
		if(formula instanceof Atom) {
			root = new SyntaxTreeAtomNode((Atom)formula);
		}
		else if(formula instanceof QuantifiedFormula) {
			root = new SyntaxTreeQuantifiedNode(formula);
			createSyntaxTreeNode(root,formula.getChildren().get(0));
		}
		else if(formula instanceof Negation) {
			root = new SyntaxTreeNegationNode(formula);
			createSyntaxTreeNode(root,formula.getChildren().get(0));
		}
		else if(formula instanceof Implication || formula instanceof Disjunction || formula instanceof Conjunction) {
			root = new SyntaxTreeBinaryNode(formula);
			createSyntaxTreeNode(root, formula.getChildren().get(0));
			createSyntaxTreeNode(root, formula.getChildren().get(1));
		}
		return new SyntaxTree(root);
	}

	public static void createSyntaxTreeNode(SyntaxTreeNode parent, Formula formula) {
		if(formula instanceof Atom) {
			parent.addChild(new SyntaxTreeAtomNode((Atom)formula));
		}
		else if(formula instanceof QuantifiedFormula) {
			SyntaxTreeNode child = new SyntaxTreeQuantifiedNode(formula);
			parent.addChild(child);
			createSyntaxTreeNode(child, formula.getChildren().get(0));
		}
		else if(formula instanceof Negation) {
			SyntaxTreeNode child = new SyntaxTreeNegationNode(formula);
			parent.addChild(child);
			createSyntaxTreeNode(child, formula.getChildren().get(0));
		}
		else if(formula instanceof Implication || formula instanceof Disjunction || formula instanceof Conjunction) {
			SyntaxTreeNode child = new SyntaxTreeBinaryNode(formula);
			parent.addChild(child);
			createSyntaxTreeNode(child, formula.getChildren().get(0));
			createSyntaxTreeNode(child, formula.getChildren().get(1));
		}
	}

	public static void estimateNodeDepth(SyntaxTree tree) {
		estimateNodeDepth(tree.getRoot(), 0);
	}

	public static void estimateNodeDepth(SyntaxTreeNode node, int depth) {
		if(node instanceof SyntaxTreeBinaryNode) {
			node.setDepth(depth++);
			estimateNodeDepth(node.getChildren().get(0), depth);
			estimateNodeDepth(node.getChildren().get(1), depth);
		}
		else if(node instanceof SyntaxTreeNegationNode || node instanceof SyntaxTreeQuantifiedNode) {
			node.setDepth(depth++);
			estimateNodeDepth(node.getChildren().get(0), depth);
		}
		else if(node instanceof SyntaxTreeAtomNode) {
			node.setDepth(depth++);
		}
	}

	@Override
	public String toString() {
		if(this.toString == null) {
			this.toString = SyntaxTreeNode.toString(this.root);
		}
		return this.toString;
	}

	public static int estimateMaxDepth(SyntaxTree tree) {
		int maxDepth = Integer.MIN_VALUE;
		for(Entry<Integer, SyntaxTreeNode> entry:tree.getNodesMap().entrySet()) {
			if(entry.getValue().getDepth() > maxDepth) {
				maxDepth = entry.getValue().getDepth();
			}
		}
		return maxDepth;
	}

	public SyntaxTree replaceNode(SyntaxTreeNode source, SyntaxTreeNode target) {
		return new SyntaxTree(replaceNode(this.root, source, target));
	}

	public SyntaxTreeNode replaceNode(SyntaxTreeNode current, SyntaxTreeNode source, SyntaxTreeNode target) {
		if(current.equals(source)) {
			return target;
		}
		else {
			if(current instanceof SyntaxTreeAtomNode) {
				return current;
			}
			else if(current instanceof SyntaxTreeQuantifiedNode) {
				SyntaxTreeNode childNode = replaceNode(current.getChildren().get(0), source, target);
				QuantifiedFormula formula = new QuantifiedFormula(((QuantifiedFormula)current.getRootedFormula()).getOperator(), 
						((QuantifiedFormula)current.getRootedFormula()).getTopLevelQuantifiedVariables(), childNode.getRootedFormula());
				SyntaxTreeNode node = new SyntaxTreeQuantifiedNode(formula);
				node.addChild(childNode);
				return node;
			}
			else if(current instanceof SyntaxTreeNegationNode) {
				SyntaxTreeNode childNode = replaceNode(current.getChildren().get(0), source, target);
				SyntaxTreeNode node = new SyntaxTreeNegationNode(childNode.getRootedFormula());
				node.addChild(childNode);
				return node;
			}
			else if(current instanceof SyntaxTreeBinaryNode) {
				SyntaxTreeNode left = replaceNode(current.getChildren().get(0), source, target);
				SyntaxTreeNode right = replaceNode(current.getChildren().get(1), source, target);

				Formula formula = null;
				if(((SyntaxTreeBinaryNode) current).getOperator().equals(LogicalSymbols.AND)) {
					formula = new Conjunction(left.getRootedFormula(), right.getRootedFormula());
				}
				else if(((SyntaxTreeBinaryNode) current).getOperator().equals(LogicalSymbols.OR)) {
					formula = new Disjunction(left.getRootedFormula(), right.getRootedFormula());
				}
				else if(((SyntaxTreeBinaryNode) current).getOperator().equals(LogicalSymbols.IMPLIES)) {
					formula = new Implication(left.getRootedFormula(), right.getRootedFormula());
				}
				SyntaxTreeNode node = new SyntaxTreeBinaryNode(formula);
				node.addChild(left);
				node.addChild(right);
				return node;
			}
			else {
				throw new java.lang.RuntimeException("Unknown node type");
			}
		}
	}
}
