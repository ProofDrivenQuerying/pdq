package uk.ac.ox.cs.pdq.ui;

import java.text.NumberFormat;
import java.util.concurrent.Callable;

import javafx.beans.binding.ObjectBinding;

public class PrettyNumberPropertyBinding<N extends Number> implements Callable<String> {
	public ObjectBinding<N> number;

	public PrettyNumberPropertyBinding(ObjectBinding<N> n) {
		this.number = n;
	}
	@Override public String call() throws Exception {
		return this.number.get() == null ? "N/A" : NumberFormat.getIntegerInstance().format(this.number.get());
	}
}