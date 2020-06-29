// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * @author Gabor
 *
 */
@XmlRootElement(name = "query")
public class AdaptedQuery {

	private Variable[] freeVariables;
	private Atom[] atoms;
	private String type;

	public AdaptedQuery() {
	}

	public AdaptedQuery(ConjunctiveQuery v) {
		freeVariables = v.getFreeVariables();
		atoms = v.getAtoms();
		type = "unset";
		if (v instanceof ConjunctiveQuery) {
			type = "conjunctive";
		}
	}

	public ConjunctiveQuery toQuery() {
		try {
			List<Atom> newAtoms = new ArrayList<>();
			
			if(atoms != null)
			{
				for (Atom a: atoms) {
					List<Term> terms = new ArrayList<>();
					for(Term t: a.getTerms()) {
						if (t instanceof UntypedConstant) {
							terms.add(TypedConstant.create(((UntypedConstant) t).getSymbol()));
						} else {
							terms.add(t);
						}
					}
					newAtoms.add(Atom.create(a.getPredicate(),terms.toArray(new Term[terms.size()])));
				}
				return ConjunctiveQuery.create(freeVariables, newAtoms.toArray(new Atom[newAtoms.size()]));	
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@XmlAttribute
	public String getType() {
		return type;
	}

	@XmlElement(name = "atom")
	@XmlElementWrapper(name = "body")
	public Atom[] getAtoms() {
		return atoms;
	}

	@XmlElement(name = "variable")
	@XmlElementWrapper(name = "head")
	public Variable[] getHead() {
		return null;
	}

	public void setHead(Variable[] freeVariables) {
		this.freeVariables = freeVariables;
	}

	@XmlElement(name = "variable")
	@XmlElementWrapper(name = "free-variables")
	public Variable[] getFreeVariables() {
		return this.freeVariables;
	}

	public void setFreeVariables(Variable[] freeVariables) {
		this.freeVariables = freeVariables;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setAtoms(Atom[] atoms) {
		this.atoms = atoms;
	}

}
