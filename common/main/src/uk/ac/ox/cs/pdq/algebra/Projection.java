package uk.ac.ox.cs.pdq.algebra;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * OpenProjection operator.
 *
 * @author Julien Leblay
 */
public class Projection extends UnaryOperator {

	/** The head, the list of terms from the child's output terms list that are projected by this operator. */
	private final List<Term> projected;

	/** Maps each non-constant term's position of the child's output to a new named term. */
	private final Map<Integer, Term> renaming;

	/**
	 * Instantiates a new projection.
	 * @param child LogicalOperator
	 * @param head the array of term to project
	 */
	public Projection(RelationalOperator child, Term... head) {
		this(child, null, Lists.newArrayList(head));
	}

	/**
	 * Instantiates a new projection.
	 * @param child LogicalOperator
	 * @param head the array of term to project
	 */
	public Projection(RelationalOperator child, List<Term> head) {
		this(child, null, head);
	}

	/**
	 * Constructor for Projection.
	 * @param child LogicalOperator
	 * @param naming Map<Integer,Term>
	 */
	public Projection(RelationalOperator child, Map<Integer, Term> naming) {
		this(child, naming, getColumns(naming, child));
	}

	/**
	 * Constructor for Projection.
	 * @param child LogicalOperator
	 * @param naming Map<Integer,Term>
	 * @param head List<Term>
	 */
	public Projection(RelationalOperator child, Map<Integer, Term> naming, Term... head) {
		this(child, naming, Lists.newArrayList(head));
	}

	/**
	 * Constructor for Projection.
	 * @param child LogicalOperator
	 * @param naming Map<Integer,Term> maps all non-typed constant positions in the output of the child to a renamed term
	 * @param head List<Term>
	 */
	public Projection(RelationalOperator child, Map<Integer, Term> naming, List<Term> head) {
		super(inputType(child),
				renameTerms(outputTerms(child), inputTerms(child), naming),
				inferType(head, outputType(child), outputTerms(child)),
				renameTerms(outputTerms(child), head, naming),
				child);
		this.renaming = naming == null ? Maps.<Integer, Term>newHashMap() : naming;
		this.projected = Lists.newArrayList(head);
	}

	/**
	 * Gets the columns.
	 *
	 * @param naming Map<Integer,Term>
	 * @param operator LogicalOperator
	 * @return List<Term>
	 */
	private static List<Term> getColumns(Map<Integer, Term> naming, RelationalOperator operator) {
		List<Term> columns = new ArrayList<>();
		for (Entry<Integer, ? extends Term> entry : naming.entrySet()) {
			columns.add(operator.getColumn(entry.getKey()));
		}
		return columns;
	}

	/**
	 * Infer type.
	 *
	 * @param head the head
	 * @param childType the child type
	 * @param childColumns the child columns
	 * @return the type of the projected term list
	 */
	private static TupleType inferType(List<Term> head, TupleType childType, List<? extends Term> childColumns) {
		Preconditions.checkNotNull(head);
		Type[] result = new Type[head.size()];
		for (int i = 0, l = head.size(); i < l; i++) {
			Term t = head.get(i);
			if (t instanceof TypedConstant) {
				result[i] = ((TypedConstant) t).getType();
				continue;
			}
			int j = childColumns.indexOf(t);
			assert j >= 0 : "Could not infer type from " + head + " and " + childColumns + " culprit: " + t;
			result[i] = childType.getType(j);
		}
		return TupleType.DefaultFactory.create(result);
	}

	/**
	 * Rename terms.
	 *
	 * @param source List<Term>
	 * @param target List<Term>
	 * @param renaming Map<Integer,Term>
	 * @return List<Term>
	 */
	private static List<Term> renameTerms(List<Term> source, List<Term> target, Map<Integer, Term> renaming) {
		Preconditions.checkNotNull(target);
		if (renaming != null && !renaming.isEmpty()) {
			List<Term> result = new ArrayList<>(target.size());
			for (Term t : target) {
				if (t.isVariable() || t.isUntypedConstant()) {
					int pos = source.indexOf(t);
					if (renaming.containsKey(pos)) {
						result.add(renaming.get(pos));
					} else {
						assert false : 
							t + " not found in child attributes list. " + 
							"\nsource: " + source + "\ntarget: " + target + 
							"\nrenaming: " + renaming;
					}
				} else {
					result.add(t);
				}
			}
			return result;
		}
		return target;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#deepCopy()
	 */
	@Override
	public Projection deepCopy() throws RelationalOperatorException {
		if (this.renaming == null) {
			return new Projection(this.child.deepCopy(), this.projected);
		}
		return new Projection(this.child.deepCopy(), this.renaming, this.projected);
	}

	/**
	 * Gets the columns display.
	 *
	 * @return a list of human readable column headers.
	 */
	@Override
	public List<String> getColumnsDisplay() {
		List<String> result = new ArrayList<>();
		for (Term t : this.getColumns()) {
			result.add(t.toString());
		}
		return result;
	}

	/**
	 * Gets the projected.
	 *
	 * @return List<Term>
	 */
	public List<Term> getProjected() {
		return this.projected;
	}

	/**
	 * Gets the renaming.
	 *
	 * @return Map<Integer,Term>
	 */
	public Map<Integer, Term> getRenaming() {
		return this.renaming;
	}

	/**
	 * Equals.
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		return super.equals(o)
				&& this.getClass().isInstance(o)
				&& this.projected.equals(((Projection) o).projected)
				&& this.renaming.equals(((Projection) o).renaming)
				&& this.child.equals(((Projection) o).child)
				;

	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.outputType, this.inputType, this.child,
				this.columns, this.inputTerms, this.projected, this.renaming, 
				this.metadata);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName()).append('[');
		String sep = "";
		for (Term t : this.projected) {
			String s = t.toString();
			int pos = this.child.getColumns().indexOf(t);
			if (this.renaming != null && this.renaming.containsKey(pos)) {
				s += "/" + this.renaming.get(pos);
			}
			result.append(sep).append(s);
			sep = ",";
		}
		result.append(']').append('(').append(this.child.toString()).append(')');
		return result.toString();
	}
}
