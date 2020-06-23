// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedAtom;

/**
 * @author Gabor
 *
 */
public class AtomAdapter extends XmlAdapter<AdaptedAtom, Formula> {

	@Override
	public Formula unmarshal(AdaptedAtom v) throws Exception {
		try {
			return v.toFormula();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

	@Override
	public AdaptedAtom marshal(Formula v) throws Exception {
		try {
			return new AdaptedAtom(((Atom) v).getPredicate(), v.getTerms());
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

}
