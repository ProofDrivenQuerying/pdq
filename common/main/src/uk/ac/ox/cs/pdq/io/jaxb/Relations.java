package uk.ac.ox.cs.pdq.io.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.View;

/**
 * Helper object to separate normal relations and view definitions in schemas.
 * 
 * @author Gabor
 *
 */
public class Relations {
	private Relation[] relations;
	private View[] views;

	public Relations() {
	}

	public Relations(Relation[] relationsAndViews) {
		try {
			List<Relation> relations = new ArrayList<>();
			List<View> views = new ArrayList<>();
			for (Relation element : relationsAndViews) {
				if (element instanceof Relation && !(element instanceof View)) {
					relations.add(element);
				}
				if (element instanceof View) {
					views.add((View) element);
				}
			}
			this.relations = new Relation[relations.size()];
			this.relations = relations.toArray(this.relations);
			setViews(new View[views.size()]);
			if (views.size() > 0)
				setViews(views.toArray(getViews()));
		} catch (Throwable t) {
			Logger.getLogger(this.getClass()).error(t.getMessage(),t);
			throw t;
		}

	}

	public Relation[] getAll() {
		if (getViews() == null)
			return this.relations;

		Relation[] ret = new Relation[this.relations.length + getViews().length];
		ret = ArrayUtils.addAll(this.relations, getViews());
		return ret;
	}

	@XmlElement(name = "relation")
	public Relation[] getRelation() {
		return relations;
	}

	public void setRelation(Relation[] relations) {
		this.relations = relations;
	}

	@XmlElement(name = "view")
	public View[] getViews() {
		return views;
	}

	public void setViews(View[] views) {
		this.views = views;
	}
}
