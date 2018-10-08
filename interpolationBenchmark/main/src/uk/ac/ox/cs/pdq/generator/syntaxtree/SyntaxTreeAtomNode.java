package uk.ac.ox.cs.pdq.generator.syntaxtree;

import java.util.List;

import uk.ac.ox.cs.pdq.fol.Atom;

import com.google.common.collect.Lists;

public class SyntaxTreeAtomNode extends SyntaxTreeNode{
	
	private final Atom atom;
	
	public SyntaxTreeAtomNode(Atom atom) {
		super(atom);
		this.atom = atom;
	}

	@Override
	public List<SyntaxTreeNode> getChildren() {
		return Lists.newArrayList();
	}

	@Override
	public boolean addChild(SyntaxTreeNode node) {
		return false;
	}

	/**
	 * @return the atom
	 */
	public Atom getAtom() {
		return this.atom;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}
}
