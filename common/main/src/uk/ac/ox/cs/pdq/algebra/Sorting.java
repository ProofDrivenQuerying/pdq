package uk.ac.ox.cs.pdq.algebra;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SortOrder;

import org.apache.commons.lang3.tuple.Pair;


/**
 * TOCOMMENT The comment precisely explains what is happening but I am not sure I get it
 * Represents a sorting order of the output of a logical operator, as a list of
 * positions mapped to a sorted order.
 *
 * @author Julien Leblay
 */
public class Sorting implements Iterable<Pair<Integer, SortOrder>>{

	/** The positions. */
	private final List<Pair<Integer, SortOrder>> positions = new LinkedList<>();

	/**
	 * Size of the sorting order.
	 *
	 * @return int
	 */
	public int size() {
		return this.positions.size();
	}


	@Override
	public Iterator<Pair<Integer, SortOrder>> iterator() {
		return this.positions.iterator();
	}

	/**
	 * TOCOMMENT 
	 * Adds a column position and sorted order to the sorting.
	 * This has no effect was already present.
	 *
	 * @param col the col
	 * @param order the order
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
	 * 
	 * Modifies the sorting order at the given column position.
	 * TOCOMMENT This has no effect was already present with the same order.
	 * If the column was not already part of the sorting, it will be add last as
	 * a result of the update.
	 *
	 * @param col the col
	 * @param order the order
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
	 * TOCOMMENT This has no effect was not already present.
	 *
	 * @param col the col
	 * @param order the order
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
