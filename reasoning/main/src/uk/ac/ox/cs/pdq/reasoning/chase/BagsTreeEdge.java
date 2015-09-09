package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.Objects;

import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.db.Constraint;

/**
 * Edge of a bag tree
 *
 * @author Efthymia Tsamoura
 */
public class BagsTreeEdge extends DefaultEdge {
	private static final long serialVersionUID = -7873493688903569561L;

	/** Cached instance hash */
	private int hash = Integer.MIN_VALUE;

	/** Source bag*/
	private final Bag source;
	/** Target bag*/
	private final Bag target;

	/** The dependency that was fired to create the target bag*/
	private final Constraint dependency;

	/**
	 * Constructor for BagsTreeEdge.
	 * @param source Bag
	 * @param target Bag
	 * @param dependency IC
	 */
	public BagsTreeEdge(Bag source, Bag target, Constraint dependency) {
		this.source = source;
		this.target = target;
		this.dependency = dependency;
	}

	/**
	 * @return Bag
	 */
	@Override
	public Bag getSource() {
		return this.source;
	}

	/**
	 * @return Bag
	 */
	@Override
	public Bag getTarget() {
		return this.target;
	}

	/**
	 * @return IC
	 */
	public Constraint getDependency() {
		return this.dependency;
	}

	/**
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.source.equals(((BagsTreeEdge) o).source)
				&& this.target.equals(((BagsTreeEdge) o).target);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		if (this.hash == Integer.MIN_VALUE) {
			this.hash = Objects.hash(this.source, this.target);
		}
		return this.hash;
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.dependency.toString();
	}
}