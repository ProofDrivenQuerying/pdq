package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedAccessMethod;

/**
 * @author Gabor
 *
 */
public class AccessMethodAdapter extends XmlAdapter<AdaptedAccessMethod, AccessMethod> {

	@Override
	public AccessMethod unmarshal(AdaptedAccessMethod v) throws Exception {
		try {
			return v.toAccessMethod();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	@Override
	public AdaptedAccessMethod marshal(AccessMethod v) throws Exception {
		try {
			return new AdaptedAccessMethod(v.getName(), v.getInputs());
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

}
