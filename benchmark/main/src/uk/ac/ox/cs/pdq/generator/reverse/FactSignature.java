package uk.ac.ox.cs.pdq.generator.reverse;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

// TODO: Auto-generated Javadoc
/**
 * A signature for fact that does not distinguish between fact that 
 * have the same predicate name and sequence of schema constants.
 * 
 * @author Julien Leblay
 */
public class FactSignature implements Comparable<FactSignature> {
	
	/** The signature. */
	final Predicate sig;
	
	/** The constants. */
	final List<TypedConstant<?>> constants;
	
	/**
	 * Makes a fact signature from a given fact.
	 *
	 * @param fact the fact
	 * @return the fact signature
	 */
	public static FactSignature make(Atom fact) {
		List<TypedConstant<?>> constants = new LinkedList<>();
		for (Term t: Utility.getTypedAndUntypedConstants(fact)) {
			if (!t.isVariable() && !t.isUntypedConstant()) {
				constants.add((TypedConstant<?>) t);
			}
		}
		return new FactSignature(fact.getPredicate(), ImmutableList.copyOf(constants));
	}
	
	/**
	 * Makes a sorted set of fact signatures from a given iterable of facts.
	 *
	 * @param facts the facts
	 * @return the sorted set
	 */
	public static SortedSet<FactSignature> make(Iterable<Atom> facts) {
		TreeSet<FactSignature> result = new TreeSet<>();
		for (Atom f: facts) {
			result.add(make(f));
		}
		return result;
	}
	
	/**
	 * Instantiates a new fact signature.
	 *
	 * @param s a signature
	 * @param c a list of typed constants
	 */
	private FactSignature(Predicate s, List<TypedConstant<?>> c) {
		Preconditions.checkArgument(s != null);
		Preconditions.checkArgument(c != null);
		this.sig = s;
		this.constants = c;
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.sig, this.constants);
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.sig.equals(((FactSignature) o).sig)
				&& this.constants.equals(((FactSignature) o).constants);
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.sig.getName()).append('(').append(this.sig.getArity()).append(')');
		result.append(this.constants.toString());
		return result.toString();
	}
	
	/**
	 * Checks if the given signature cover the current one, i.e. they have the 
	 * same name, and the constants of the given are included in this fact
	 * constants. 
	 *
	 * @param o the o
	 * @return true, if successful
	 */
	public boolean covers(FactSignature o) {
		return this.sig.equals(o.sig)
				&& this.constants.containsAll(o.constants);
	}

	/**
	 * Compares two signatures based on the lexicographical ordering of their
	 * String representations.
	 * 
	 * {@inheritDoc}
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FactSignature o) {
		Preconditions.checkArgument(o != null);
		return this.toString().compareTo(o.toString());
	}
}
