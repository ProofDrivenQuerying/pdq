// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedRelationalTerm;

/**
 * @author Gabor
 *
 */
@XmlRootElement
public class RelationalTermAdapter extends XmlAdapter<AdaptedRelationalTerm, RelationalTerm> implements Serializable {
	
	private static final long serialVersionUID = 1734503933593174613L;
	
	public RelationalTermAdapter() {
	}

	@Override
	public RelationalTerm unmarshal(AdaptedRelationalTerm v) throws Exception {
		if (v==null)
			return null;
		try {
			return v.toRelationalTerm();
		}catch(Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(),t);
			throw t;
		}
	}

	@Override
	public AdaptedRelationalTerm marshal(RelationalTerm v) throws Exception {
		if (v==null)
			return null;
		try {
			return new AdaptedRelationalTerm(v);
		}catch(Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(),t);
			throw t;
		}
	}
}
