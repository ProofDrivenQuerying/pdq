package uk.ac.ox.cs.pdq.db.builder;

import java.util.LinkedList;
import java.util.List;

import uk.ac.ox.cs.pdq.builder.Builder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
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
					return LinearGuarded.create(
							this.left.get(0),
							Conjunction.of(this.right.toArray(new Formula[this.right.size()])));
				}
				return TGD.create(
						Conjunction.of(this.left.toArray(new Formula[this.left.size()])),
						Conjunction.of(this.right.toArray(new Formula[this.right.size()])));
			}
			throw new IllegalStateException("Left of a dependency cannot be empty.");
		}
		throw new IllegalStateException("Right of a dependency cannot be empty.");
	}
}
