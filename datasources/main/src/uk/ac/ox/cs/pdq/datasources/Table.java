package uk.ac.ox.cs.pdq.datasources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * Implementation of a database table, whose tuples are fully loaded in memory.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Table implements Result, Iterable<Tuple> {

	/**  The table's header. */
	private List<? extends Typed> header = new ArrayList<>();

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
	
	/** The Constant prefix. */
	private final static String prefix = "T"; 
	
	/** The count. */
	private static int count = 0; 
	
	/** The name. */
	private final String name;

	/**
	 * Instantiates a new table.
	 *
	 * @param intern boolean
	 * @param attributes 		The table's header
	 */
	public Table(boolean intern, List<? extends Typed> attributes) {
		Preconditions.checkArgument(
				attributes != null && attributes.size() >= 0,
				"Invalid dynamic table type");
		this.type = Utility.createFromTyped(attributes.toArray(new Attribute[attributes.size()]));
		this.header = Lists.newArrayList(attributes);
		this.internTuples = intern;
		this.name = Table.prefix + Table.count++;
	}

	/**
	 * Instantiates a new table.
	 *
	 * @param intern boolean
	 * @param attributes 		The table's header
	 */
	public Table(boolean intern, Typed... attributes) {
		this(intern, Lists.newArrayList(attributes));
	}

	/**
	 * Instantiates a new table.
	 *
	 * @param attributes 		The table's header
	 */
	public Table(List<? extends Typed> attributes) {
		this(false, attributes);
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
	@Override
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
	 * @see uk.ac.ox.cs.pdq.structures.Result#howDifferent(uk.ac.ox.cs.pdq.structures.Result)
	 */
	@Override
	public Levels howDifferent(Result o) {
		if (this == o) {
			return Levels.IDENTICAL;
		}
		if (Table.class.isInstance(o)) {
			Table small = this;
			Table large = (Table) o;
			boolean same = true;
			Iterator<Tuple> i, j;
			if (small.size() > large.size()) {
				Table tmp = large;
				large = small;
				small = tmp;
			}
			i = large.data.iterator();
			j = small.data.iterator();
			while (j.hasNext()) {
				Tuple l = i.next();
				Tuple s = j.next();
				if (!s.equals(l)) {
					same = false;
					if (!small.contains(l) || !large.contains(s)) {
						return Levels.DIFFERENT;
					}
				}
			}
			while (i.hasNext()) {
				Tuple t1 = i.next();
				if (!small.contains(t1)) {
					return Levels.DIFFERENT;
				}
			}
			if (same && small.size() == large.size()) {
				return Levels.IDENTICAL;
			}
			if (!large.isEmpty() && !small.isEmpty()) {
				return Levels.EQUIVALENT;
			}
		}
		return Levels.DIFFERENT;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.structures.Result#size()
	 */
	@Override
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

			@Override
			public ResetableIterator<Tuple> deepCopy() {
				return Table.this.iterator();
			}
		};
		//		result.open();
		return result;
	}

	/**
	 * Gets the header.
	 *
	 * @return the table header
	 */
	public List<? extends Typed> getHeader() {
		return this.header;
	}

	/**
	 * Sets a new header for the table.
	 *
	 * @param schema the new header
	 */
	public void setHeader(List<? extends Attribute> schema) {
		this.header = Lists.newArrayList(schema);
	}

	/**
	 * Checks for header.
	 *
	 * @return true, if the table has a non-empty header
	 */
	public boolean hasHeader() {
		return !this.header.isEmpty();

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
	 * @see uk.ac.ox.cs.pdq.structures.Result#diff(uk.ac.ox.cs.pdq.structures.Result)
	 */
	@Override
	public String diff(Result o) {
		if (!this.getClass().isInstance(o)) {
			return "Results are not of the same types.";
		}
		Table that = (Table) o;
		Collection<Tuple> large, small;
		if (this.size() > that.size()) {
			large = this.data;
			small = that.data;
		} else {
			large = that.data;
			small = this.data;
		}
		StringBuilder result = new StringBuilder();
		int intersection = small.size();
		boolean containedSmall = true;
		boolean containedLarge = true;
		Iterator<Tuple> j = large.iterator();
		for (Iterator<Tuple> i = small.iterator(); i.hasNext();) {
			Tuple t = i.next();
			j.next();
			if (!large.contains(t)) {
				intersection--;
				containedSmall = false;
			}
		}
		while (j.hasNext()) {
			if (!small.contains(j.next())) {
				containedLarge = false;
			}
		}
		if (containedSmall && containedLarge && this.size() != that.size()) {
			result.append("duplicates found");
		} else if (!containedSmall || !containedLarge) {
			result.append("results not contained in one another |result1|=" + this.size() + ", |result2|=" + that.size() + " |intersection|=" + intersection);
		}
		return result.toString();
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

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
}