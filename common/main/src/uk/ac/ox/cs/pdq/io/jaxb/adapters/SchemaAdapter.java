// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedSchema;

/**
 * @author Gabor
 *
 */
public class SchemaAdapter extends XmlAdapter<AdaptedSchema, Schema> implements Serializable {
	private static final long serialVersionUID = -9029821976935757838L;

	@Override
	public Schema unmarshal(AdaptedSchema s) throws Exception {
		if (s==null)
			return null;
		try {
			return s.toSchema();
		}catch(Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(),t);
			throw t;
		}
	}

	@Override
	public AdaptedSchema marshal(Schema s) throws Exception {
		if (s==null)
			return null;
		try {
			return new AdaptedSchema(s);
		}catch(Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(),t);
			throw t;
		}
	}
	
	
}
