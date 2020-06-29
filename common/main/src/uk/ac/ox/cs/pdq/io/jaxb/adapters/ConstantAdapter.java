// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedConstant;

/**
 * @author Gabor
 *
 */
public class ConstantAdapter extends XmlAdapter<AdaptedConstant, Constant> {

	@Override
	public Constant unmarshal(AdaptedConstant v) throws Exception {
		try {
			return v.toConstant();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

	@Override
	public AdaptedConstant marshal(Constant v) throws Exception {
		try {
			if (v==null)
				return null;
			return new AdaptedConstant(v);
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}
}
