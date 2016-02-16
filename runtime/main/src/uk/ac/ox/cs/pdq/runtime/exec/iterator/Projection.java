package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Projection operator.
 * 
 * @author Julien Leblay
 */
public class Projection extends UnaryIterator {

	/** The head, the list of term projected by this operator. */
	public List<Typed> projected;

	/** Maps each variable is the head to a position in the children. */
	public Map<Typed, Integer> positions;

	/** Maps each child term position to its (optional renaming). */
	public Map<Integer, Typed> renaming;

	/**
	 * Instantiates a new projection.
	 * 
	 * @param head the head
	 * @param child TupleIterator
	 */
	public Projection(List<Typed> head, TupleIterator child) {
		this(head, new LinkedHashMap<Integer, Typed>(), child);
	}

	/**
	 * Instantiates a new projection.
	 * 
	 * @param projected the head
	 * @param renaming Map<Integer,Typed>
	 * @param child TupleIterator
	 */
	public Projection(List<Typed> projected, Map<Integer, Typed> renaming, TupleIterator child) {
		super(applyRenaming(inputColumns(child), renaming, outputColumns(child)),
				TupleType.DefaultFactory.createFromTyped(projected),
				applyRenaming(projected, renaming, outputColumns(child)),
				child);
		this.projected = projected != null ? new ArrayList<>(projected) : null;
		this.renaming = renaming;
		this.positions = new LinkedHashMap<>();
		int i = 0;
		for (Typed c : this.child.getColumns()) {
			if (c instanceof Attribute 
					&& containsName(this.projected, (Attribute) c) 
					&& !this.positions.containsKey(c)) {
				this.positions.put(c, i);
			}
			i++;
		}
	}

	/**
	 * Method containsName.
	 * @param projected List<Typed>
	 * @param attribute Attribute
	 * @return boolean
	 */
	private static boolean containsName(List<Typed> projected, Attribute attribute) {
		for (Iterator<Typed> it = projected.iterator(); it.hasNext();) {
			Typed t = it.next();
			if (t instanceof Attribute && ((Attribute) t).getName().equals(attribute.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method applyRenaming.
	 * @param projected List<Typed>
	 * @param renaming Map<Integer,Typed>
	 * @param columns List<Typed>
	 * @return List<Typed>
	 */
	private static List<Typed> applyRenaming(List<Typed> projected, Map<Integer, Typed> renaming, List<Typed> columns) {
		Preconditions.checkArgument(projected != null);
		Preconditions.checkArgument(columns != null);
		if (renaming != null && !renaming.isEmpty()) {
			List<Typed> result = new ArrayList<>(projected.size());
			for (Typed t : projected) {
				if (t instanceof Attribute) {
					int pos = columns.indexOf(t);
					if (renaming.containsKey(pos)) {
						result.add(renaming.get(pos));
					} else {
						throw new IllegalStateException(t + " not found in child attributes list.");
					}
				} else {
					result.add(t);
				}
			}
			return result;
		}
		return projected;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		return !this.interrupted && this.child.hasNext();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		Tuple next = this.child.next();
		if (next == null) {
			throw new NoSuchElementException("End of projection operator reached.");
		}
		Object[] result = new Object[this.projected.size()];
		int i = 0;
		for (Typed t : this.projected) {
			if (t instanceof Attribute) {
				result[i++] = next.getValue(this.positions.get(t));
			} else if (t instanceof TypedConstant) {
				result[i++] = ((TypedConstant) t).getValue();
			}
		}
		return this.outputType.createTuple(result);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#deepCopy()
	 */
	@Override
	public Projection deepCopy() {
		return new Projection(
				this.projected,
				this.renaming,
				this.child.deepCopy());
	}

	/**
	 * Gets the columns display.
	 *
	 * @return a list of human readable column headers.
	 */
	@Override
	public List<String> getColumnsDisplay() {
		List<String> result = new ArrayList<>();
		int i = 0;
		for (Typed t : this.projected) {
			String s = t.toString();
			if (this.renaming.containsKey(i)) {
				s += "/" + this.renaming.get(i);
			}
			result.add(s);
			i++;
		}
		return result;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append(this.getColumnsDisplay());
		result.append('(').append(this.child).append(')');
		return result.toString();
	}
}
