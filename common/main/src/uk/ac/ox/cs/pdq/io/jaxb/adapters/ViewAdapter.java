package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedView;

/**
 * @author Gabor
 *
 */
public class ViewAdapter extends XmlAdapter<AdaptedView, View> implements Serializable {
	private static final long serialVersionUID = -9222721018270749836L;

	@Override
	public View unmarshal(AdaptedView v) throws Exception {
		try {
			return v.toRelation();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

	@Override
	public AdaptedView marshal(View v) throws Exception {
		try {
			return new AdaptedView(v);
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

}
