package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Dependency;

/**
 * @author Gabor
 *
 */
@XmlRootElement(name="schema")
@XmlType (propOrder={"relations","dependencies"})
public class AdaptedSchema {
	private String name;
	private Relation[] relations;
	private String description;
	private Dependency[] dependencies;
	public AdaptedSchema() {
	}

	public AdaptedSchema(Schema s) {
		relations = s.getRelations();
		List<Dependency> dependencies = new ArrayList<>();
		dependencies.addAll(Arrays.asList(s.getDependencies()));
		dependencies.addAll(Arrays.asList(s.getKeyDependencies()));
		setDependencies(dependencies.toArray(new Dependency[dependencies.size()]));
	}

	public Schema toSchema() {
		if (getDependencies()!=null && getDependencies().length>0)
			return new Schema(relations,getDependencies());
		Schema s = new Schema(relations);
		return s;
	}

	@XmlElement(name="dependency")
	@XmlElementWrapper(name="dependencies")	
	public Dependency[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(Dependency[] dependencies) {
		this.dependencies = dependencies;
	}

	@XmlElementWrapper(name="relations")	
	//@XmlElement(name = "relation")
	@XmlElements({ @XmlElement(name = "relation", type = AdaptedRelation.class), @XmlElement(name = "view", type = AdaptedView.class) })
	public AdaptedRelation[] getRelations() {
		List<AdaptedRelation> ret = new ArrayList<>();
		if (relations==null)
			return null;
		for (Relation r:relations) {
			if (r!=null && r instanceof View) {
				ret.add(new AdaptedView((View)r));
			} else if (r!=null) 
				ret.add(new AdaptedRelation(r));
		}
		AdaptedRelation[] t = new AdaptedRelation[ret.size()];
		t = ret.toArray(t);
		return t;		
	}
	public void setRelations(AdaptedRelation[] ars) {
		if (ars==null || ars.length==0)
			return;
		relations = new Relation[ars.length];
		int i = 0;
		for (AdaptedRelation r:ars) {
			if (r!=null && r instanceof AdaptedView) {
				relations[i] = ((AdaptedView)r).toRelation();
			} else if (r!=null) { 
				relations[i] = r.toRelation();
			}
			i++;
		}
	}

	@XmlAttribute
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
