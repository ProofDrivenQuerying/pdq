package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import javax.xml.bind.annotation.XmlAttribute;

import uk.ac.ox.cs.pdq.fol.Variable;
/**
 * @author Gabor
 *
 */
public class AdaptedVariable {
	private static int counter = 0;
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
			counter++;
			return Variable.create("_"+counter);
		}
		return Variable.createFromXml(getSymbol());
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
}