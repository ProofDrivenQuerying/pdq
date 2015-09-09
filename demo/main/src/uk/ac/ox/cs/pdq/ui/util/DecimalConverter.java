package uk.ac.ox.cs.pdq.ui.util;

import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.util.StringConverter;

public class DecimalConverter extends StringConverter<Number> {

	private DecimalFormat format = new DecimalFormat("#0.#E0");
	
	@Override
	public Number fromString(String str) {
		try {
			return this.format.parse(str);
		} catch (ParseException e) {
			throw new IllegalArgumentException(str + " cannot be parsed to a number.");
		}
	}

	@Override
	public String toString(Number number) {
		if (number != null && number.intValue() > 1000) {
			return this.format.format(number);
		}
		return String.valueOf(number);
	}

}
