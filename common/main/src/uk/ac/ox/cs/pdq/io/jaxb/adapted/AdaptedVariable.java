// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import javax.xml.bind.annotation.XmlAttribute;

import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
/**
 * @author Gabor
 *
 */
public class AdaptedVariable {
	private String symbol;
	
	public AdaptedVariable() {
	}
	
	public AdaptedVariable(Variable v) {
		this.setSymbol(v.getSymbol());
	}

	public String toString() {
		return this.getSymbol();
	}
	@XmlAttribute(name="name")
	public String getSymbol() {
		return this.symbol;
	}

	public Variable toVariable() {
		if (getSymbol()==null) {
			return Variable.create("_"+GlobalCounterProvider.getNext("AdaptedVariableName"));
		}
		return Variable.createFromXml(getSymbol());
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
}