package uk.ac.ox.cs.pdq.datasources.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import uk.ac.ox.cs.pdq.datasources.Pipelineable;
import uk.ac.ox.cs.pdq.datasources.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.datasources.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.Table;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;

/**
 * TOCOMMENT this class is copy pasted by InMemoryRelationWrapper. Same questions apply here.
 * 
 * In memory view wrapper. This is the default implementation of a view,
 * where the data associated with a view resides in memory, and does not
 * rely on any external support.
 * 
 * @author Julien Leblay
 */
public class InMemoryViewWrapper extends View implements Pipelineable, RelationAccessWrapper, InMemoryRelation {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3167783211904676965L;

	/**  The underlying data. */
	private Collection<Tuple> data = new ArrayList<>();
	
	/**
	 * Instantiates a new in memory view wrapper.
	 *
	 * @param name String
	 * @param attributes List<Attribute>
	 * @param methods List<AccessMethod>
	 */
	public InMemoryViewWrapper(String name, Attribute[] attributes, AccessMethod[] methods) {
		super(name, attributes, methods);
	}
	
	/**
	 * Instantiates a new in memory view wrapper.
	 *
	 * @param name String
	 * @param attributes List<Attribute>
	 */
	public InMemoryViewWrapper(String name, Attribute[] attributes) {
		super(name, attributes);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.wrappers.InMemoryRelation#load(java.util.Collection)
	 */
	public void load(Collection<Tuple> d) {
		this.data.addAll(d);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.wrappers.InMemoryRelation#clear()
	 */
	public void clear() {
		this.data.clear();
	}
	
	/**
	 * Gets the data tuples.
	 *
	 * @return the data that this relation currently holds in-memory.
	 */
	public Collection<Tuple> getData() {
		return this.data;
	}
	
	/**
	 * TOCOMMENT see comment for InMemoryViewWrapper  
	 * 
	 * Access.
	 *
	 * @param inputHeader the input header
	 * @param inputTuples the input tuples
	 * @return Table
	 * @see uk.ac.ox.cs.pdq.datasources.memory.runtime.RelationAccessWrapper#access(Table)
	 */
	@Override
	public Table access(List<? extends Attribute> inputHeader, ResetableIterator<Tuple> inputTuples) {
		Preconditions.checkArgument(inputHeader != null);
		Preconditions.checkArgument(inputTuples != null);
		
		Table result = new Table(this.attributes);

		ResetableIterator<Tuple> iterator = this.iterator(inputHeader, inputTuples);
		iterator.open();
		while (iterator.hasNext()) {
			result.appendRow(iterator.next());
		}
		return result;
	}

	/**
	 * Access.
	 *
	 * @return the content of the view materialized in memory
	 * @see uk.ac.ox.cs.pdq.datasources.memory.runtime.RelationAccessWrapper#access()
	 */
	@Override
	public Table access() {
		Table result = new Table(this.attributes);
		ResetableIterator<Tuple> iterator = this.iterator();
		iterator.open();
		while (iterator.hasNext()) {
			result.appendRow(iterator.next());
		}
		return result;
	}

	/**
	 * Iterator.
	 *
	 * @param inputAttributes List<? extends Attribute>
	 * @param inputs ResetableIterator<Tuple>
	 * @return ResetableIterator<Tuple>
	 * @see uk.ac.ox.cs.pdq.runtime.wrappers.Pipelineable#iterator(List<? extends Attribute>, ResetableIterator<Tuple>)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResetableIterator<Tuple> iterator(
			List<? extends Attribute> inputAttributes, ResetableIterator<Tuple> inputs) {
		return new AccessIterator((List<Attribute>) inputAttributes, inputs);
	}

	/**
	 * Iterator.
	 *
	 * @return ResetableIterator<Tuple>
	 * @see uk.ac.ox.cs.pdq.datasources.runtime.Pipelineable#iterator()
	 */
	@Override
	public ResetableIterator<Tuple> iterator() {
		return new AccessIterator();
	}

	/**
	 * The class encapsulates the pipelined behaviour of the Wrapper.
	 * 
	 * @author Julien Leblay
	 */
	private class AccessIterator implements ResetableIterator<Tuple> {

		/**  The list of input attributes. */
		private final List<Attribute> inputAttributes;
		
		/** Iterator over a set of the input tuples. */
		private final ResetableIterator<Tuple> inputs;

		/** Iterator over a set of the output tuples. */
		private final ResetableIterator<Tuple> outputs;

		/** The input type. */
		private final TupleType inputType;
		
		/** The filter. */
		private Set<Tuple> filter = new LinkedHashSet<>();
		
		/** The next tuple. */
		private Tuple nextTuple = null;

		/**
		 * Constructor with input tuples, i.e. free access
		 */
		public AccessIterator() {
			this(null, (ResetableIterator<Tuple>) null);
		}
		
		/**
		 * Constructor with input tuple iterator.
		 *
		 * @param inputAttributes List<Attribute>
		 * @param inputTuples ResetableIterator<Tuple>
		 */
		public AccessIterator(List<Attribute> inputAttributes, ResetableIterator<Tuple> inputTuples) {
			this.inputAttributes = inputAttributes;
			this.inputs = inputTuples;
			Table d = new Table(InMemoryViewWrapper.this.getAttributes());
			for(Tuple t: InMemoryViewWrapper.this.getData()) {
				d.appendRow(t);
			}
			this.outputs = d.iterator();
			if (inputAttributes != null) {
				this.inputType = Utility.createFromTyped(inputAttributes.toArray(new Attribute[inputAttributes.size()]));
			} else {
				this.inputType = TupleType.EmptyTupleType;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#open()
		 */
		@Override
		public void open() {
			if (this.inputs != null) {
				this.inputs.open();
				while (this.inputs.hasNext()) {
					this.filter.add(this.inputs.next());
				}
			}
			this.nextTuple();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#reset()
		 */
		@Override
		public void reset() {
			this.outputs.reset();
			this.nextTuple();
		}

		/**
		 * Deep copy.
		 *
		 * @return AccessIterator
		 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#deepCopy()
		 */
		@Override
		public AccessIterator deepCopy() {
			return new AccessIterator(
					this.inputAttributes,
					this.inputs != null ? this.inputs.deepCopy() : null);
		}
		
		/**
		 * Checks for next.
		 *
		 * @return boolean
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			synchronized (this) {
				return this.nextTuple != null;
			}
		}

		/**
		 * Next.
		 *
		 * @return Tuple
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Tuple next() {
			synchronized (this) {
				if (this.nextTuple == null) {
					throw new NoSuchElementException();
				}
				Tuple result = this.nextTuple;
				this.nextTuple();
				return result;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(View.class.getSimpleName()).append('{')
				.append(InMemoryViewWrapper.this.getName()).append('}').append('.')
				.append(this.getClass().getSimpleName()).append('(')
				.append(this.inputs).append(')');
			
			return result.toString();
		}
		
		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		/*
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		/**
		 * Set the next tuple to the following item on the output iterator, 
		 * using the next input tuple if necessary.
		 * The nextTuple is set to null, if the iterator has reached the end.
		 */
		public void nextTuple() {
			this.nextTuple = null;

			Object[] subTuple = new Object[this.inputType.size()];
			while (this.outputs.hasNext()) {
				Tuple t = this.outputs.next();
				int i = 0;
				if (this.inputAttributes != null) {
					for (Attribute att: this.inputAttributes) {
						subTuple[i] = t.getValue(InMemoryViewWrapper.this.getAttributePosition(att.getName()));
						i++;
					}
					if (this.filter.contains(this.inputType.createTuple(subTuple))) {
						this.nextTuple = t;
						return;
					}
				} else if (this.filter.isEmpty()) {
					this.nextTuple = t;
					return;
				} else {
					throw new IllegalStateException();
				}
			}
		}
	}
}
