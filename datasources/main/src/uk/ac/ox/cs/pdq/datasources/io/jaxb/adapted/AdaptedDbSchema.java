package uk.ac.ox.cs.pdq.datasources.io.jaxb.adapted;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.ox.cs.pdq.datasources.builder.SchemaDiscoverer;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.Source;
import uk.ac.ox.cs.pdq.datasources.sql.PostgresqlSchemaDiscoverer;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedRelation;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedView;

/**
 * @author Gabor
 *
 */
@XmlRootElement(name = "schema")
public class AdaptedDbSchema {
	private Source[] sources;
	private String name;
	private Relation[] relations;
	private String description;
	private Dependency[] dependencies;
	private AdaptedRelation[] ars;

	public AdaptedDbSchema() {
	}

	public AdaptedDbSchema(Schema s) {
		relations = s.getRelations();
		setDependencies(s.getDependencies());
	}

	public Schema toSchema(Properties properties) throws  ClassNotFoundException, InstantiationException, IllegalAccessException {
		try {
			Relation[] discoveredRelations = new Relation[relations.length];
			List<String> discoveredSources = new ArrayList<>();
			int i = 0;
			for (Source s : sources) {
				Properties propertiesClone = new Properties();
				if (properties!=null)
					propertiesClone = (Properties) properties.clone();
				String discoverer = s.getDiscoverer();
				if ("uk.ac.ox.cs.pdq.sql.PostgresqlSchemaDiscoverer".equals(discoverer)) {
					discoverer = "uk.ac.ox.cs.pdq.datasources.sql.PostgresqlSchemaDiscoverer";
				} else if ("uk.ac.ox.cs.pdq.sql.MySQLSchemaDiscoverer".equals(discoverer)) {
					discoverer = "uk.ac.ox.cs.pdq.datasources.sql.MySQLSchemaDiscoverer";
				}
				propertiesClone.setProperty("driver", s.getDriver());
				propertiesClone.setProperty("url", s.getUrl());
				propertiesClone.setProperty("database", s.getDatabase());
				propertiesClone.setProperty("username", s.getUsername());
				propertiesClone.setProperty("password", s.getPassword());
				SchemaDiscoverer sd = (SchemaDiscoverer) Class.forName(discoverer).newInstance();
				sd.setProperties(propertiesClone);
				Schema discoveredPartialSchema = sd.discover();
				for (AdaptedRelation r : ars) {
					if (s.getName() != null && s.getName().equals(r.getSource())) {
						//create (String name, Attribute[] attributes, AccessMethod[] accessMethods, ForeignKey[] foreignKeys, boolean isEquality)
						Relation dr = discoveredPartialSchema.getRelation(r.getName());
						discoveredRelations[i] = Relation.create(r.getName(), dr.getAttributes(),r.getAccessMethods(),dr.getForeignKeys(),dr.isEquality());
						i++;
					}
				}
				discoveredSources.add(s.getName());
			}
			if (i != relations.length) {
				throw new IllegalArgumentException("Not every relations were discovered. Discovered sources:" + discoveredSources);
			}
			if (getDependencies() != null && getDependencies().length > 0)
				return new Schema(discoveredRelations, getDependencies());
			Schema s = new Schema(discoveredRelations);
			return s;
		} catch ( ClassNotFoundException t) {
			t.printStackTrace();
			throw t;
		}
	}

	@XmlElement(name = "dependency")
	@XmlElementWrapper(name = "dependencies")
	public Dependency[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(Dependency[] dependencies) {
		this.dependencies = dependencies;
	}

	@XmlElementWrapper(name = "relations")
	@XmlElements({ @XmlElement(name = "relation", type = AdaptedRelation.class), @XmlElement(name = "view", type = AdaptedView.class) })
	public AdaptedRelation[] getRelations() {
		List<AdaptedRelation> ret = new ArrayList<>();
		if (relations == null)
			return null;
		for (Relation r : relations) {
			if (r != null && r instanceof View) {
				ret.add(new AdaptedView((View) r));
			} else if (r != null)
				ret.add(new AdaptedRelation(r));
		}
		AdaptedRelation[] t = new AdaptedRelation[ret.size()];
		t = ret.toArray(t);
		return t;
	}

	public void setRelations(AdaptedRelation[] ars) {
		this.ars = ars;
		if (ars == null || ars.length == 0)
			return;
		relations = new Relation[ars.length];
		int i = 0;
		for (AdaptedRelation r : ars) {
			if (r != null && r instanceof AdaptedView) {
				relations[i] = ((AdaptedView) r).toRelation();
			} else if (r != null) {
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

	@XmlElement(name = "source")
	@XmlElementWrapper(name = "sources")
	public Source[] getSources() {
		return sources;
	}

	public void setSources(Source[] sources) {
		this.sources = sources;
	}

	public AdaptedRelation[] getAdaptedRelations() {
		return ars;
	}

}
