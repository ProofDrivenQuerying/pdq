// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedAccessMethod;

/**
 * @author Gabor
 *
 */
public class AccessMethodAdapter extends XmlAdapter<AdaptedAccessMethod, AccessMethodDescriptor> {

	@Override
	public AccessMethodDescriptor unmarshal(AdaptedAccessMethod v) throws Exception {
		try {
			return v.toAccessMethod();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	@Override
	public AdaptedAccessMethod marshal(AccessMethodDescriptor v) throws Exception {
		try {
			if (v==null)
				return null;
			return new AdaptedAccessMethod(v.getName(), v.getInputs());
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

}
