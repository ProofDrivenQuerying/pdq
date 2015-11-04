package uk.ac.ox.cs.pdq.generator.reverse;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Term;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * A signature for fact that does not distinguish between fact that 
 * have the same predicate name and sequence of schema constants.
 * 
 * @author Julien Leblay
 */
public class FactSignature implements Comparable<FactSignature> {
	
	final Signature sig;
	
	final List<TypedConstant<?>> constants;
	
	public static FactSignature make(Predicate fact) {
		List<TypedConstant<?>> constants = new LinkedList<>();
		int i = 0;
		for (Term t: fact.getConstants()) {
			if (!t.isVariable() && !t.isSkolem()) {
				constants.add((TypedConstant<?>) t);
			}
			i++;
		}
		return new FactSignature(fact.getSignature(), ImmutableList.copyOf(constants));
	}
	
	public static SortedSet<FactSignature> make(Iterable<Predicate> facts) {
		TreeSet<FactSignature> result = new TreeSet<>();
		for (Predicate f: facts) {
			result.add(make(f));
		}
		return result;
	}
	
	private FactSignature(Signature s, List<TypedConstant<?>> c) {
		Preconditions.checkArgument(s != null);
		Preconditions.checkArgument(c != null);
		this.sig = s;
		this.constants = c;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.sig, this.constants);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.sig.equals(((FactSignature) o).sig)
				&& this.constants.equals(((FactSignature) o).constants);
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.sig.getName()).append('(').append(this.sig.getArity()).append(')');
		result.append(this.constants.toString());
		return result.toString();
	}
	
	public boolean covers(FactSignature o) {
		return this.sig.equals(o.sig)
				&& this.constants.containsAll(o.constants);
	}

	@Override
	public int compareTo(FactSignature o) {
		Preconditions.checkArgument(o != null);
		return this.toString().compareTo(o.toString());
	}
}
