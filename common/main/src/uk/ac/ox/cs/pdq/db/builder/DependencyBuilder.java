package uk.ac.ox.cs.pdq.db.builder;

import java.util.LinkedList;
import java.util.List;

import uk.ac.ox.cs.pdq.builder.Builder;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.TGD;

/**
 * A builder for dependencies (e.g. integrity constraints, axioms, ...).
 * @author Julien Leblay
 */
public class DependencyBuilder implements Builder<TGD> {

	/**  The left hand part of the dependency. */
	private List<Formula> left = new LinkedList<>();

	/**  The right hand part of the dependency. */
	private List<Formula> right = new LinkedList<>();
	
	/**
	 * Adds to the left.
	 *
	 * 
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
	 * Adds to the right.
	 *
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
	 * Adds a conjunction to the left.
	 *
	 * @param p Conjunction of Atoms
	 * @return DependencyBuilder
	 */
	public DependencyBuilder addLeftConjunction(Conjunction p) {
		this.left.addAll(p.getAtoms());
		return this;
	}
	
	/**
	 * Adds a conjunction to the right
	 *
	 * @param p Conjunction of Atom
	 * @return DependencyBuilder
	 */
	public DependencyBuilder addRightConjunction(Conjunction p) {
		this.right.addAll(p.getAtoms());
		return this;
	}
	
	/**
	 * Adds an atom to the left 
	 *
	 * @param p Atom
	 * @return DependencyBuilder
	 */
	public DependencyBuilder addLeftAtom(Atom p) {
		this.left.add(p);
		return this;
	}
	
	/**
	 * Adds an atom to the right 
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
