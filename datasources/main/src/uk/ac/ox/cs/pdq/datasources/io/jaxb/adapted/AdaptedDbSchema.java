package uk.ac.ox.cs.pdq.datasources.io.jaxb.adapted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ox.cs.pdq.datasources.builder.SchemaDiscoverer;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.Source;
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
@XmlType (propOrder={"sources","relations","dependencies"})
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
			if (sources==null) {
				// not every schema has external sources
				if (getDependencies()!=null && getDependencies().length>0)
					return new Schema(relations,getDependencies());
				Schema s = new Schema(relations);
				return s;
			}
			HashSet<Relation> discoveredRelations = new HashSet<Relation>();
			List<String> discoveredSources = new ArrayList<>();
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
				else if ("uk.ac.ox.cs.pdq.services.ServiceReader".equals(discoverer)) {
					discoverer = "uk.ac.ox.cs.pdq.datasources.services.ServiceReader";
				}
				
				if (s.getDriver()!=null) propertiesClone.setProperty("driver", s.getDriver());
				if (s.getUrl()!=null) propertiesClone.setProperty("url", s.getUrl());
				if (s.getDatabase()!=null) propertiesClone.setProperty("database", s.getDatabase());
				if (s.getUsername()!=null) propertiesClone.setProperty("username", s.getUsername());
				if (s.getPassword()!=null) propertiesClone.setProperty("password", s.getPassword());
				if (s.getName()!=null) propertiesClone.setProperty("name", s.getName());
				if (s.getFile()!=null) propertiesClone.setProperty("file", s.getFile());
				SchemaDiscoverer sd = (SchemaDiscoverer) Class.forName(discoverer).newInstance();
				sd.setProperties(propertiesClone);
				Schema discoveredPartialSchema = sd.discover();
				for (AdaptedRelation r : ars) {
					if (s.getName() != null && s.getName().equals(r.getSource())) {
						//create (String name, Attribute[] attributes, AccessMethod[] accessMethods, ForeignKey[] foreignKeys, boolean isEquality)
						Relation dr = discoveredPartialSchema.getRelation(r.getName());
						discoveredRelations.add(Relation.create(r.getName(), dr.getAttributes(),r.getAccessMethods(),dr.getForeignKeys(),dr.isEquality()));
					} else if (r.getSource()==null) {
						discoveredRelations.add(r.toRelation());
					}
				}
				discoveredSources.add(s.getName());
			}
			if (discoveredRelations.size() != relations.length) {
				throw new IllegalArgumentException("Not every relations were discovered. Discovered sources:" + discoveredSources + " out of : " + Arrays.asList(relations));
			}
			if (getDependencies() != null && getDependencies().length > 0)
				return new Schema(discoveredRelations.toArray(new Relation[discoveredRelations.size()]), getDependencies());
			Schema s = new Schema(discoveredRelations.toArray(new Relation[discoveredRelations.size()]));
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
		if (ars!=null) {
			// we have read an old schema file and now we want to export the new format.
			try {
				Schema s = this.toSchema(null);
				if (s!=null) {
					relations = s.getRelations();
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
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
		if (ars!=null) {
			for (int i = 0; i < t.length; i++) {
				t[i].setSource(ars[i].getSource());
			}
		}
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
