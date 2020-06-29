// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedDependency;

/**
 * @author Gabor
 *
 */
public class DependencyAdapter extends XmlAdapter<AdaptedDependency, Dependency> {

	@Override
	public Dependency unmarshal(AdaptedDependency v) throws Exception {
		try {
			return v.toDependency();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

	@Override
	public AdaptedDependency marshal(Dependency v) throws Exception {
		try {
			return new AdaptedDependency(v);
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

}
