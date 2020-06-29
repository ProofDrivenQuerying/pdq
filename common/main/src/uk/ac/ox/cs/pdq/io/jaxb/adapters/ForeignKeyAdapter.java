// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedForeignKey;

/**
 * @author Gabor
 *
 */
public class ForeignKeyAdapter extends XmlAdapter<AdaptedForeignKey, ForeignKey> {

	@Override
	public ForeignKey unmarshal(AdaptedForeignKey v) throws Exception {
		try {
			return v.toForeignKey();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	@Override
	public AdaptedForeignKey marshal(ForeignKey v) throws Exception {
		try {
			return new AdaptedForeignKey(v);
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

}
