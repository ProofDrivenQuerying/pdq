package uk.ac.ox.cs.pdq.algebra;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SortOrder;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Represents sorting order of the output of a logical operator, as a list of
 * position mapping to sorted order.
 *
 * @author Julien Leblay
 */
public class Sorting implements Iterable<Pair<Integer, SortOrder>>{

	private final List<Pair<Integer, SortOrder>> positions = new LinkedList<>();

	/**
	 * @return int
	 */
	public int size() {
		return this.positions.size();
	}

	/**
	 * @return Iterator<Pair<Integer,SortOrder>>
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Pair<Integer, SortOrder>> iterator() {
		return this.positions.iterator();
	}

	/**
	 * Add column position and sorted order to the sorting.
	 * This has no effect was already present.
	 * @param col
	 * @param order
	 */
	public void addSorting(int col, SortOrder order) {
		for (Pair<Integer, SortOrder> so: this.positions) {
			if (so.getKey().equals(col)) {
				return;
			}
		}
		this.positions.add(Pair.of(col, order));
	}

	/**
	 * Modifies the sorting order at the given column position.
	 * This has no effect was already present with the same order.
	 * If the column was not already part of the sorting, it will be add last as
	 * a result of the update.
	 * @param col
	 * @param order
	 */
	public void updateSorting(int col, SortOrder order) {
		boolean found = false;
		for (Iterator<Pair<Integer, SortOrder>> i = this.positions.iterator(); i.hasNext(); ) {
			Pair<Integer, SortOrder> so = i.next();
			if (so.getKey().equals(col)) {
				so.setValue(order);
				found = true;
			}
		}
		if (!found) {
			this.positions.add(Pair.of(col, order));
		}
	}

	/**
	 * Remove the column position and order form the sorting.
	 * This has no effect was not already present.
	 * @param col
	 * @param order
	 */
	public void removeSorting(int col, SortOrder order) {
		for (Iterator<Pair<Integer, SortOrder>> i = this.positions.iterator(); i.hasNext(); ) {
			Pair<Integer, SortOrder> so = i.next();
			if (so.getKey().equals(col)) {
				i.remove();
			}
		}
	}
}
