package uk.ac.ox.cs.pdq.db.builder;

import java.util.LinkedList;
import java.util.List;

import uk.ac.ox.cs.pdq.builder.Builder;
import uk.ac.ox.cs.pdq.db.LinearGuarded;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * A builder for dependencies (e.g. integrity constraints, axioms, ...).
 * @author Julien Leblay
 */
public class DependencyBuilder implements Builder<TGD> {

	/**  The left hand part of the dependency. */
	private List<Atom> left = new LinkedList<>();

	/**  The right hand part of the dependency. */
	private List<Atom> right = new LinkedList<>();
	
	/**
	 * Adds the left.
	 *
	 * @param f the f
	 * @return DependencyBuilder
	 */
	public DependencyBuilder addLeft(Formula f) {
		if(f instanceof Atom) {
			return this.addLeftAtom((Atom) f);
		}
		if(f instanceof Conjunction) {
			return this.addLeftConjunction((Conjunction) f);
		}
		throw new IllegalArgumentException("Input must be either an atom or conjunction thereof: " + f);
	}
	
	/**
	 * Adds the right.
	 *
	 * @param f the f
	 * @return DependencyBuilder
	 */
	public DependencyBuilder addRight(Formula f) {
		if(f instanceof Atom) {
			return this.addRightAtom((Atom) f);
		}
		if(f instanceof Conjunction) {
			return this.addRightConjunction((Conjunction) f);
		}
		throw new IllegalArgumentException("Input must be either an atom or conjunction thereof: " + f);
	}
	
	/**
	 * Adds the left conjunction.
	 *
	 * @param p Conjunction of Atoms
	 * @return DependencyBuilder
	 */
	public DependencyBuilder addLeftConjunction(Conjunction<Atom> p) {
		this.left.addAll(p.getAtoms());
		return this;
	}
	
	/**
	 * Adds the right conjunction.
	 *
	 * @param p Conjunction of Atom
	 * @return DependencyBuilder
	 */
	public DependencyBuilder addRightConjunction(Conjunction<Atom> p) {
		this.right.addAll(p.getAtoms());
		return this;
	}
	
	/**
	 * Adds the left atom.
	 *
	 * @param p Atom
	 * @return DependencyBuilder
	 */
	public DependencyBuilder addLeftAtom(Atom p) {
		this.left.add(p);
		return this;
	}
	
	/**
	 * Adds the right atom.
	 *
	 * @param p Atom
	 * @return DependencyBuilder
	 */
	public DependencyBuilder addRightAtom(Atom p) {
		this.right.add(p);
		return this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
	 */
	@Override
	public TGD build() {
		if (!this.right.isEmpty()) {
			if (!this.left.isEmpty()) {
				if (this.left.size() == 1) {
					return new LinearGuarded(
							this.left.get(0),
							Conjunction.of(this.right));
				}
				return new TGD(
						Conjunction.of(this.left),
						Conjunction.of(this.right));
			}
			throw new IllegalStateException("Left of a dependency cannot be empty.");
		}
		throw new IllegalStateException("Right of a dependency cannot be empty.");
	}
}
