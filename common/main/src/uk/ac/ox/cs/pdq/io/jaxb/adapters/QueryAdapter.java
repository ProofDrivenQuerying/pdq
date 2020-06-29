// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedQuery;

/**
 * @author Gabor
 *
 */
public class QueryAdapter extends XmlAdapter<AdaptedQuery, ConjunctiveQuery> {

	@Override
	public ConjunctiveQuery unmarshal(AdaptedQuery v) throws Exception {
		try {
			return v.toQuery();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

	@Override
	public AdaptedQuery marshal(ConjunctiveQuery v) throws Exception {
		try {
			return new AdaptedQuery(v);
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

}
