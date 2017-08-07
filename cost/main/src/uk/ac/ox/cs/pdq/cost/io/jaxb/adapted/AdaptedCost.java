package uk.ac.ox.cs.pdq.cost.io.jaxb.adapted;

import javax.xml.bind.annotation.XmlAttribute;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;

/**
 * Xml adapter for the Cost class.
 * 
 * @author Gabor
 *
 */
public final class AdaptedCost {

	private Double value = null;
	private String type;

	public AdaptedCost() {
	}

	public AdaptedCost(double cost) {
		this.value = cost;
	}

	public AdaptedCost(Cost cost) {
		if (cost instanceof DoubleCost) {
			this.value = ((DoubleCost) cost).getCost();
			this.type = "DoubleCost";
		}
	}

	public Cost toCost() {
		if ("DoubleCost".equals(type)) {
			return new DoubleCost(value);
		} else
			throw new IllegalArgumentException("Unknown or unset Cost type: " + type);
	}

	@XmlAttribute(name = "value")
	public String getCost() {
		return String.valueOf(this.value);
	}

	@XmlAttribute
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setCost(String value) {
		this.value = Double.valueOf(value);
	}
}
