// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedAttribute;

/**
 * @author Gabor
 *
 */
public class AttributeAdapter extends XmlAdapter<AdaptedAttribute, Attribute> {

	@Override
	public Attribute unmarshal(AdaptedAttribute v) throws Exception {
		try {
			return v.toAttribute();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	@Override
	public AdaptedAttribute marshal(Attribute v) throws Exception {
		try {
			return new AdaptedAttribute(v);
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

}
