package uk.ac.ox.cs.pdq.ui.util;

import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.util.StringConverter;

// TODO: Auto-generated Javadoc
/**
 * The Class DecimalConverter.
 */
public class DecimalConverter extends StringConverter<Number> {

	/** The format. */
	private DecimalFormat format = new DecimalFormat("#0.#E0");
	
	/* (non-Javadoc)
	 * @see javafx.util.StringConverter#fromString(java.lang.String)
	 */
	@Override
	public Number fromString(String str) {
		try {
			return this.format.parse(str);
		} catch (ParseException e) {
			throw new IllegalArgumentException(str + " cannot be parsed to a number.");
		}
	}

	/* (non-Javadoc)
	 * @see javafx.util.StringConverter#toString(java.lang.Object)
	 */
	@Override
	public String toString(Number number) {
		if (number != null && number.intValue() > 1000) {
			return this.format.format(number);
		}
		return String.valueOf(number);
	}

}
