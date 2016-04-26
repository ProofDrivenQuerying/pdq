package uk.ac.ox.cs.pdq.algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * Join is a top-level class for all join operators.
 *
 * @author Julien Leblay
 */
public class Join extends NaryOperator implements PredicateBasedOperator {

	/**
	 * The Enum Variants.
	 */
	public static enum Variants {
		
		/** The nested loop. */
		NESTED_LOOP, 
 /** The merge. */
 MERGE, 
 /** The asymmetric hash. */
 ASYMMETRIC_HASH, 
 /** The symmetric hash. */
 SYMMETRIC_HASH
	}

	/** The predicate associated with this join, null if this is a natural join. */
	private final Predicate predicate;

	/** The variant. */
	protected Variants variant = Variants.SYMMETRIC_HASH;

	/**
	 * Instantiates a new join.
	 *
	 * @param children
	 *            the children
	 */
	public Join(RelationalOperator... children) {
		this(Lists.newArrayList(children));
	}

	/**
	 * Instantiates a new join.
	 *
	 * @param children            the children
	 */
	public Join(List<RelationalOperator> children) {
		super(children);
		this.predicate = this.initNaturalJoin();
	}

	/**
	 * Instantiates a new join.
	 *
	 * @param inputTerms List<Term>
	 * @param children the children
	 */
	protected Join(List<Term> inputTerms, List<RelationalOperator> children) {
		super(inputTerms, inferType(children), children);
		this.predicate = this.initNaturalJoin();
	}

	/**
	 * Instantiates a new join.
	 *
	 * @param pred Atom
	 * @param children the children
	 */
	public Join(Predicate pred, List<RelationalOperator> children) {
		super(children);
		Preconditions.checkNotNull(pred);
		this.predicate = pred;
	}

	/**
	 * Instantiates a new join.
	 *
	 * @param pred Atom
	 * @param children the children
	 */
	public Join(Predicate pred, RelationalOperator... children) {
		this(pred, Lists.newArrayList(children));
	}

	/**
	 * Orders the children such that every child has at least one shared
	 * variable with the union of variable in the preceding children.
	 * @param children List<LogicalOperator>
	 * @return List<LogicalOperator>
	 * @throws RelationalOperatorException if a cartesian product is found among the children
	 */
	protected static List<RelationalOperator> orderChildren(
			List<RelationalOperator> children) throws RelationalOperatorException {
		if (children != null) {
			if (children.size() > 1) {
				List<RelationalOperator> children2 = new ArrayList<>(children);
				List<RelationalOperator> orderedChildren = new ArrayList<>();
				List<Term> allTerms = new ArrayList<>();
				orderedChildren.add(children2.get(0));
				allTerms.addAll(children2.get(0).getColumns());
				children2.remove(0);
				while (children2.size() > 0) {
					boolean found = false;
					for (int i = 0; i < children2.size(); i++) {
						if (hasCommonTerms(allTerms, children2.get(i).getColumns())) {
							orderedChildren.add(children2.get(i));
							allTerms.addAll(children2.get(i).getColumns());
							children2.remove(i);
							found = true;
							break;
						}
					}
					if (!found) {
						throw new RelationalOperatorException("Cartesian product found in a join operator");
					}
				}
				return orderedChildren;
			}
			return children;
		}
		return null;
	}

	/**
	 * Gets the variant.
	 *
	 * @return Variants
	 */
	public Variants getVariant() {
		return this.variant;
	}

	/**
	 * Sets the variant.
	 *
	 * @param variant Variants
	 */
	public void setVariant(Variants variant) {
		Preconditions.checkNotNull(variant);
		this.variant = variant;
	}

	/**
	 * Gets the predicate.
	 *
	 * @return Atom
	 * @see uk.ac.ox.cs.pdq.algebra.PredicateBasedOperator#getPredicate()
	 */
	@Override
	public Predicate getPredicate() {
		return this.predicate;
	}

	/**
	 * Checks for predicate.
	 *
	 * @return boolean
	 */
	public boolean hasPredicate() {
		return this.predicate != null
				&& (this.predicate instanceof ConjunctivePredicate ?
						!((ConjunctivePredicate) this.predicate).isEmpty(): true);
	}

	/**
	 * Checks for common terms.
	 *
	 * @param from the from
	 * @param to the to
	 * @return true, if there is at least one common variable between the lists of terms from and to.
	 */
	private static boolean hasCommonTerms(List<? extends Term> from, List<? extends Term> to) {
		List<Term> f = new ArrayList<>(from);
		f.retainAll(to);
		return !f.isEmpty();
	}


	/**
	 * Initialises the join variables.
	 * @return Atom
	 */
	protected Predicate initNaturalJoin() {
		Multimap<Term, Integer> joinVariables = LinkedHashMultimap.create();
		int totalCol = 0;
		// Cluster patterns by variables
		Set<Term> inChild = Sets.newLinkedHashSet();
		for (RelationalOperator child : this.children) {
			inChild.clear();
			for (int i = 0, l = child.getColumns().size(); i < l; i++) {
				Term col = child.getColumns().get(i);
				if (!inChild.contains(col)) {
					joinVariables.put(col, totalCol);
					inChild.add(col);
				}
				totalCol++;
			}
		}

		Collection<AttributeEqualityPredicate> equalities = new ArrayList<>();
		// Remove clusters containing only one pattern
		for (Iterator<Term> keys = joinVariables.keySet().iterator(); keys.hasNext();) {
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

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('_').append(this.variant);
		if (this.predicate != null) {
			result.append(this.predicate);
		}
		result.append('[');
		if (this.children != null) {
			for (RelationalOperator child: this.children) {
				result.append(child.toString()).append(',');
			}
			result.deleteCharAt(result.length() - 1);
		}
		result.append(']');
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.relational.logical.NaryOperator#deepCopy()
	 */
	@Override
	public Join deepCopy() throws RelationalOperatorException {
		List<RelationalOperator> children = new ArrayList<>();
		for (RelationalOperator child: this.children) {
			children.add(child.deepCopy());
		}
		return new Join(children);
	}
}