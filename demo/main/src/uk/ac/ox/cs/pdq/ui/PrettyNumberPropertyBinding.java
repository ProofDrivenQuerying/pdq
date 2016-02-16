package uk.ac.ox.cs.pdq.ui;

import java.text.NumberFormat;
import java.util.concurrent.Callable;

import javafx.beans.binding.ObjectBinding;

// TODO: Auto-generated Javadoc
/**
 * The Class PrettyNumberPropertyBinding.
 *
 * @param <N> the number type
 */
public class PrettyNumberPropertyBinding<N extends Number> implements Callable<String> {
	
	/** The number. */
	public ObjectBinding<N> number;

	/**
	 * Instantiates a new pretty number property binding.
	 *
	 * @param n the n
	 */
	public PrettyNumberPropertyBinding(ObjectBinding<N> n) {
		this.number = n;
	}
	
	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override public String call() throws Exception {
		return this.number.get() == null ? "N/A" : NumberFormat.getIntegerInstance().format(this.number.get());
	}
}