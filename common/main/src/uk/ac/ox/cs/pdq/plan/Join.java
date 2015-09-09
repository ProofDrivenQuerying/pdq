package uk.ac.ox.cs.pdq.plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class Join implements Command{

	private final Table left;
	
	private final Table right;
	
	private final Table output;
	
	private final Predicate predicates;
	
	/** Caches the constraint that captures this access command **/
	private final TGD command;
	
	public Join(Table left, Table right) {
		this(left, right, initNaturalJoin(left, right));
	}
	
	public Join(Table left, Table right, Predicate predicates) {
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		Preconditions.checkNotNull(predicates);
		this.left = left;
		this.right = right;
		List<Attribute> attributes = Lists.newArrayList();
		attributes.addAll((List<Attribute>)this.left.getHeader());
		attributes.addAll((List<Attribute>)this.right.getHeader());
		this.output = new Table(attributes);
		this.predicates = predicates;
		this.command = new CommandToTGDTranslator().toTGD(this);
	}
	
	private static Predicate initNaturalJoin(Table ltable, Table rtable) {
		Multimap<Typed, Integer> joinVariables = LinkedHashMultimap.create();
		int totalCol = 0;
		// Cluster patterns by variables
		Set<Typed> inChild = Sets.newLinkedHashSet();
		for (Table child : Lists.newArrayList(ltable,rtable)) {
			inChild.clear();
			for (int i = 0, l = child.getHeader().size(); i < l; i++) {
				Typed col = child.getHeader().get(i);
				if (!inChild.contains(col)) {
					joinVariables.put(col, totalCol);
					inChild.add(col);
				}
				totalCol++;
			}
		}
		Collection<AttributeEqualityPredicate> equalities = new ArrayList<>();
		// Remove clusters containing only one pattern
		for (Iterator<Typed> keys = joinVariables.keySet().iterator(); keys.hasNext();) {
			Collection<Integer> cluster = joinVariables.get(keys.next());
			if (cluster.size() < 2) {
				keys.remove();
			} else {
				Iterator<Integer> i = cluster.iterator();
				Integer left = i.next();
				while (i.hasNext()) {
					Integer right = i.next();
					equalities.add(new AttributeEqualityPredicate(left, right));
				}
			}
		}
		return new ConjunctivePredicate<>(equalities);
	}

	@Override
	public Table getOutput() {
		return this.output;
	}
	
	public Table getLeft() {
		return this.left;
	}
	
	public Table getRight() {
		return this.right;
	}

	public Predicate getPredicates() {
		return predicates;
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
		return Join.class.isInstance(o)
				&& this.left.equals(((Join) o).left)
				&& this.right.equals(((Join) o).right)
				&& this.predicates.equals(((Join) o).predicates);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.left, this.right, this.predicates);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.command.toString();
	}
}
