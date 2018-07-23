package uk.ac.ox.cs.pdq.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * Implementation of a database table, whose tuples are fully loaded in memory.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Table implements Iterable<Tuple> {

	/**  The table's header. */
	private Typed[] header;

	/**  The table's tuples. */
	private List<Tuple> data = new ArrayList<>();

	/**  The type of each table's tuple. */
	private TupleType type = null;
	
	/**
	 * Weak tuple interner. Allows idential tuple to be stored in one place
	 * in memory, while still allowing them to be garbage collected.
	 */
	private Interner<Tuple> interner = Interners.newWeakInterner();

	/** If true, tuple added to the table are first interned. */
	private final boolean internTuples;

	/**
	 * Instantiates a new table.
	 *
	 * @param intern boolean
	 * @param attributes 		The table's header
	 */
	public Table(boolean intern, Typed[] attributes) {
		Preconditions.checkArgument(attributes != null && attributes.length >= 0, "Invalid dynamic table type");
		this.type = TupleType.DefaultFactory.createFromTyped(attributes);
		this.header = attributes;
		this.internTuples = intern;
	}

	/**
	 * Instantiates a new table.
	 *
	 * @param attributes 		The table's header
	 */
	public Table(Typed... attributes) {
		this(false, attributes);
	}

	/**
	 * Gets the type.
	 *
	 * @return the tuple type of the table
	 */
	public TupleType getType() {
		return this.type;
	}

	/**
	 * Appends the tuple to the table.
	 *
	 * @param row 		Input tuple
	 */
	public void appendRow(Tuple row) {
		Preconditions.checkArgument(row != null);
		Preconditions.checkArgument(row.getType().equals(this.type),
				"Tuple type mismatch when append Tuple to DynamicTable " + row + " (" + row.getType() + " vs. " + this.type + ")");
		if (this.internTuples) {
			this.data.add(this.interner.intern(row));
		} else {
			this.data.add(row);
		}
	}

	/**
	 * Appends the input's tuples to the current table.
	 *
	 * @param input 		Input table
	 */
	public void appendRows(Table input) {
		for (Tuple row: input) {
			this.appendRow(row);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.structures.Result#isEmpty()
	 */
	public boolean isEmpty() {
		return this.data.isEmpty() || this.type == TupleType.EmptyTupleType;
	}

	/**
	 * Removes duplicate tuple in the table.
	 */
	public void removeDuplicates() {
		this.data = Lists.newArrayList(Sets.newLinkedHashSet(this.data));
	}

	/**
	 * Gets the column.
	 *
	 * @param <T> the generic type
	 * @param c Column
	 * @return the data of this column
	 */
	public <T> T[] getColumn(int c) {
		@SuppressWarnings("unchecked")
		T[] column = (T[]) new Object[this.data.size()];
		int i = 0;
		for (Tuple t : this.data) {
			column[i++] = t.getValue(c);
		}
		return column;
	}

	/**
	 * Contains.
	 *
	 * @param t Input tuple
	 * @return returns true if the input tuple is contained in the current table
	 */
	public boolean contains(Tuple t) {
		return this.data.contains(t);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.structures.Result#size()
	 */
	public int size() {
		return this.data.size();
	}

	/**
	 * Columns.
	 *
	 * @return the number of columns
	 */
	public Integer columns() {
		return this.type.size();
	}

	/** An iterator that can be set back to the beginning.
	 * @author gabor
	 *
	 */
	public interface ResetableIterator<T> extends Iterator<T> {
		public void open();
		public void reset();
	}
	
	/**
	 * Iterator.
	 *
	 * @return a resetable tuple iterator over the table's tuples.
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public ResetableIterator<Tuple> iterator() {
		ResetableIterator<Tuple> result = new ResetableIterator<Tuple> () {

			Iterator<Tuple> i = Table.this.data.iterator();

			@Override
			public boolean hasNext() {
				return this.i.hasNext();
			}

			@Override
			public Tuple next() {
				return this.i.next();
			}

			@Override
			public void remove() {
				this.i.remove();
			}

			@Override
			public void open() {
				this.i = Table.this.data.iterator();
			}

			@Override
			public void reset() {
				this.i = Table.this.data.iterator();
			}
		};
		//		result.open();
		return result;
	}

	/**
	 * Creates a Spliterator over the elements of the table.
	 */
	@Override
	public Spliterator<Tuple> spliterator() {
		return this.data.spliterator();
	}

    /**
     * Returns a {@code Collector} that accumulates the input elements into a
     * new {@code Table}.
     *
     * @param <T> the type of the input elements
     * @return a {@code Collector} which collects all the input elements into a
     * {@code List}, in encounter order
     */
    public static Collector<Tuple, Table, Table> toTable(Supplier<Table> supplier) {
    	
    	// old: Supplier<Table> supplier = (Supplier<Table>) Table::new;
    	BiConsumer<Table, Tuple> accumulator = (table, tuple) -> table.appendRow(tuple);
    	BinaryOperator<Table> combiner = (table1, table2) -> {
    		table1.appendRows(table2);
    		return table1;
    	};
    	
    	return Collector.of(supplier, accumulator, combiner, 
    			Collector.Characteristics.CONCURRENT, 
    			Collector.Characteristics.IDENTITY_FINISH, 
    			Collector.Characteristics.UNORDERED);
    }
    
	/**
	 * Gets the header.
	 *
	 * @return the table header
	 */
	public Typed[] getHeader() {
		return this.header;
	}

	/**
	 * Sets a new header for the table.
	 *
	 * @param schema the new header
	 */
	public void setHeader(Attribute[] schema) {
		this.header = schema;
	}

	/**
	 * Checks for header.
	 *
	 * @return true, if the table has a non-empty header
	 */
	public boolean hasHeader() {
		return !(this.header.length==0);

	}

	/**
	 * Gets the data.
	 *
	 * @return the data contained in the table as a list of tuples.
	 */
	public List<Tuple> getData() {
		return this.data;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		if (this.header != null) {
			ret.append(this.header).append('\n');
		}
		if (this.data != null) {
			for (Tuple t : this.data) {
				ret.append(t).append('\n');
			}
		}
		return ret.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Table clone() {
		Table dyn = new Table(this.header);
		dyn.appendRows(this);
		return dyn;
	}
}