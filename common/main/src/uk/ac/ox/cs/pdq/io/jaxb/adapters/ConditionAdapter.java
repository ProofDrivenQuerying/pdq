// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedCondition;

/**
 * @author Gabor
 *
 */
public class ConditionAdapter extends XmlAdapter<AdaptedCondition, Condition> {

	@Override
	public Condition unmarshal(AdaptedCondition v) throws Exception {
		try {
			return v.toCondition();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	@Override
	public AdaptedCondition marshal(Condition v) throws Exception {
		try {
			if (v==null)
				return null;
			return new AdaptedCondition(v);
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
		
	}

}
