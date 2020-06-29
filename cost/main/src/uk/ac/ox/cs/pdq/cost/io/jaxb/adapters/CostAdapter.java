// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.io.jaxb.adapters;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.io.jaxb.adapted.AdaptedCost;

/**
 * @author Gabor
 *
 */
public class CostAdapter extends XmlAdapter<AdaptedCost, Cost> implements Serializable {
	private static final long serialVersionUID = -9222721018270749836L;

	@Override
	public Cost unmarshal(AdaptedCost v) throws Exception {
		try {
			if (v == null)
				return null;
			return v.toCost();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	@Override
	public AdaptedCost marshal(Cost v) throws Exception {
		try {
			if (v == null)
				return null;
			return new AdaptedCost(v);
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

}
