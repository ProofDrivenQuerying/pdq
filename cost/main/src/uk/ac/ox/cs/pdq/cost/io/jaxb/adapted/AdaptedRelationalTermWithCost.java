// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.io.jaxb.adapted;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedRelationalTerm;

/**
 * @author Gabor
 *
 */
@XmlRootElement(name = "RelationalTermWithCost")
public class AdaptedRelationalTermWithCost extends AdaptedRelationalTerm implements Serializable {

	private static final long serialVersionUID = 1734503933593174613L;

	private Cost cost;

	public AdaptedRelationalTermWithCost() {
		super();
	}

	public AdaptedRelationalTermWithCost(RelationalTerm v) {
		super(v);
	}

	@XmlElement
	public Cost getCost() {
		return cost;
	}

	public void setCost(Cost cost) {
		this.cost = cost;
	}

}
