package uk.ac.ox.cs.pdq.algebra;

import java.util.Map;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;

/**
 * This class describes a RelationalTerm object with logic objects. Contains two
 * things: a formula phi and a mapping M from output attributes of the
 * relational term to either variables or constants of the formula.
 * 
 * @author gabor
 *
 */
public class RelationalTermAsLogic {
	/**
	 * Formula representing a RelationalTerm object
	 */
	private Formula formula;
	/**
	 * Maps the output Attributes to variables or constants.
	 */
	private Map<Attribute,Term> mapping;

	/** Constructs this descriptor object.
	 * @param phi
	 * @param mapping
	 */
	public RelationalTermAsLogic(Formula phi, Map<Attribute,Term> mapping) {
		this.formula = phi;
		this.mapping = mapping;
	}

	public Formula getFormula() {
		return formula;
	}

	public Map<Attribute, Term> getMapping() {
		return mapping;
	}

	public void setMapping(Map<Attribute, Term> mapping) {
		this.mapping = mapping;
	}

	public String toString() {
		return "Formula: " + formula + "\nMapping: " + mapping;
	}
}
