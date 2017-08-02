package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.io.jaxb.Relations;

/**
 * @author Gabor
 *
 */
@XmlRootElement(name="schema")
@XmlType (propOrder={"relations","dependencies"})
public class AdaptedSchema {
	private String name;
	private String description;
	private Relations relations;
	private Dependency[] dependencies;
	public AdaptedSchema() {
	}

	public AdaptedSchema(Schema s) {
		setRelations(new Relations(s.getRelations()));
		setDependencies(s.getDependencies());
	}

	public Schema toSchema() {
		if (getDependencies()!=null && getDependencies().length>0)
			return new Schema(getRelations().getAll(),getDependencies());
		Schema s = new Schema(getRelations().getAll());
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

	@XmlElement(name="relations")
	public Relations getRelations() {
		return relations;
	}

	public void setRelations(Relations relations) {
		this.relations = relations;
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
