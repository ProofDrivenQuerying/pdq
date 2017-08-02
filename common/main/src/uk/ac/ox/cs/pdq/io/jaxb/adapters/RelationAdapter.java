package uk.ac.ox.cs.pdq.io.jaxb.adapters;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedRelation;

/**
 * @author Gabor
 *
 */
public class RelationAdapter extends XmlAdapter<AdaptedRelation, Relation> implements Serializable {
	private static final long serialVersionUID = -9222721018270749836L;

	@Override
	public Relation unmarshal(AdaptedRelation v) throws Exception {
		try {
			return v.toRelation();
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}

	}

	@Override
	public AdaptedRelation marshal(Relation v) throws Exception {
		try {
			return new AdaptedRelation(v);
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(), t);
			throw t;
		}
	}

}
