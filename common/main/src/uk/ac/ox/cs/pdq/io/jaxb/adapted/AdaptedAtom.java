// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * @author Gabor
 *
 */
public class AdaptedAtom {
	private Predicate predicate;
	private Term[] terms;
	private Variable[] freeVariables = null;
	private String predicateName;
	private AdaptedVariable[] terms2;

	public AdaptedAtom(Predicate predicate, Term[] terms) {
		this.predicate = predicate;
		this.terms = terms.clone();
	}

	public AdaptedAtom() {
	}

	/**
	 * Gets the atom's predicate.
	 *
	 * @return the atom's predicate
	 */
	public Predicate getPredicate() {
		return this.predicate;
	}

	@XmlAttribute(name = "name")
	public String getPredicateName() {
		if (predicate != null)
			return predicate.getName();
		return predicateName;
	}

	@XmlElements({ @XmlElement(name = "variable", type = AdaptedVariable.class), @XmlElement(name = "constant", type = AdaptedConstant.class) })
	public AdaptedVariable[] getTerms() {
		try {
			ArrayList<AdaptedVariable> v = new ArrayList<AdaptedVariable>();
			if (terms == null || terms.length == 0) {
				return new AdaptedVariable[0];
			}
			for (Term x : terms) {
				if (x instanceof Variable)
					v.add(new AdaptedVariable((Variable) x));
				else if (x instanceof UntypedConstant)
					v.add(new AdaptedConstant((UntypedConstant) x));
				else if (x instanceof TypedConstant)
					v.add(new AdaptedConstant((TypedConstant) x));
				else
					throw new IllegalArgumentException();
			}
			AdaptedVariable[] t = new AdaptedVariable[v.size()];
			t = v.toArray(t);
			return t;
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	public void setTerms(AdaptedVariable[] terms) {
		terms2 = terms;
	}

	public Formula toFormula() {
		try {
			Relation r = null;
			if (terms2 != null) {
				if (AdaptedSchema.getCurrentSchema()!=null) {
					r = AdaptedSchema.getCurrentSchema().getRelation(predicateName);
				}
				if (predicate==null)
					predicate = Predicate.create(predicateName, terms2.length,"EQUALITY".equals(predicateName));
				Term[] newTerms = new Term[terms2.length];
				for (int i = 0; i < terms2.length; i++) {
					AdaptedVariable v = terms2[i];
					if (v instanceof AdaptedConstant) {
						if (r instanceof Relation) {
							newTerms[i] = ((AdaptedConstant) v).toConstant((r).getAttribute(i).getType());
						} else {
							newTerms[i] = ((AdaptedConstant) v).toConstant();
						}
					} else if (v instanceof AdaptedVariable) {
						newTerms[i] = v.toVariable();
					}
				}
				return Atom.createFromXml(predicate, newTerms);
			}
			if (predicate == null) {
				predicate = Predicate.create(predicateName, freeVariables.length,"EQUALITY".equals(predicateName));
			}
			return Atom.createFromXml(predicate, freeVariables);
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	public void setPredicateName(String predicateName) {
		this.predicateName = predicateName;
	}

}
