package uk.ac.ox.cs.pdq.datasources.io.jaxb.adapted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.datasources.builder.SchemaDiscoverer;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.Source;
import uk.ac.ox.cs.pdq.datasources.utility.FormulaEquivalence;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Cache;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
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
		setDependencies(s.getNonEgdDependencies());
	}

	public Schema toSchema(Properties properties) throws  ClassNotFoundException, InstantiationException, IllegalAccessException {
		try {
			if (sources==null) {
				List<Dependency> discoveredDependencies = new ArrayList<>();
				if (dependencies!=null)
					discoveredDependencies.addAll(Arrays.asList(dependencies));
				for (Relation r:relations) {
					if (r instanceof View) {
						ensureViewDefinition((View) r,discoveredDependencies);
					}
				}
				
				
				// not every schema has external sources
				if (discoveredDependencies.size()>0)
					return new Schema(relations,discoveredDependencies.toArray(new Dependency[discoveredDependencies.size()]));
				Schema s = new Schema(relations);
				
				return s;
			}
			HashMap<String,Relation> discoveredRelations = new HashMap<>();
			List<String> discoveredSources = new ArrayList<>();
			List<Dependency> discoveredDependencies = new ArrayList<>();
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
				if (discoveredPartialSchema.getNonEgdDependencies() != null && discoveredPartialSchema.getNonEgdDependencies().length!=0) {
					discoveredDependencies.addAll(Arrays.asList(discoveredPartialSchema.getNonEgdDependencies()));
				}
				
				Cache.reStartCaches();
				for (int i = 0; i <discoveredPartialSchema.getNumberOfRelations(); i++) {
					Relation dr = discoveredPartialSchema.getRelation(i);
					AdaptedRelation xmlRelation = getRelationByName(dr.getName());
					AccessMethodDescriptor[] am = null; 
					if (xmlRelation !=null && xmlRelation.getAccessMethods() !=null && xmlRelation.getAccessMethods().length > 0 ) 
						am = xmlRelation.getAccessMethods();
					if (dr.getAccessMethods() != null && dr.getAccessMethods().length > 0) {
						//throw new IllegalArgumentException("Access method merging is not implemented!");
					}
					if (dr instanceof View) {
						View v = new View(dr.getName(), dr.getAttributes(),am);
						v.setViewToRelationDependency(((View) dr).getViewToRelationDependency());
						discoveredRelations.remove(dr.getName());
						discoveredRelations.put(dr.getName(),v);
					} else {
						if (!discoveredRelations.containsKey(dr.getName())) {
							discoveredRelations.put(dr.getName(), Relation.create(dr.getName(), dr.getAttributes(),am, dr.getForeignKeys(),dr.isEquality()));
						}
					}
				}
				for (Relation r:discoveredRelations.values()) {
					if (!(r instanceof View)) {
						ensureForeignKeyDefinition(r,discoveredDependencies);
					}
				}
				
				for (Relation r:discoveredRelations.values()) {
					if (r instanceof View) {
						ensureViewDefinition((View) r,discoveredDependencies);
					}
				}
				
				discoveredSources.add(s.getName());
			}
			for (Relation r: relations) {
				if (!discoveredRelations.containsKey(r.getName())) {
					discoveredRelations.put(r.getName(), r);
				}
			}
//			if (discoveredRelations.size() < relations.length) {
//				throw new IllegalArgumentException("Not every relations were discovered. Discovered sources:" + discoveredSources + " out of : " + Arrays.asList(relations));
//			}
			if (getDependencies() != null && getDependencies().length > 0) {
				discoveredDependencies.addAll(Arrays.asList(getDependencies()));				
			}
			
			
			if (discoveredDependencies.size() > 0) {
				return new Schema(discoveredRelations.values().toArray(new Relation[discoveredRelations.size()]), discoveredDependencies.toArray(new Dependency[discoveredDependencies.size()]));
			}
			return new Schema(discoveredRelations.values().toArray(new Relation[discoveredRelations.size()])); 
		} catch ( ClassNotFoundException t) {
			t.printStackTrace();
			throw t;
		}
	}
	
	private AdaptedRelation getRelationByName(String name) {
		for (AdaptedRelation r:ars) {
			if (name.equals(r.getName()))
				return r;
		}
		return null;
	}
	/**
	 * Ensure every view has its corresponding definition as constraints.
	 *
	 * @param view the view
	 */
	private void ensureViewDefinition(View view,List<Dependency> allDependencies) {
		LinearGuarded d = view.getViewToRelationDependency();
		LinearGuarded t = findViewDependency(view,allDependencies);		
		if (d != null) {
			TGD inverse = TGD.create(
					d.getHead() instanceof QuantifiedFormula ?
							d.getHead().getChildren()[0].getAtoms() :
								d.getHead().getAtoms(), 
								d.getBody().getAtoms());
			if (t == null) {
				allDependencies.add(d);
			}
			if (!allDependencies.contains(inverse)) {
				allDependencies.add(inverse);
			}
		} else {
			if (t != null) {
				view.setViewToRelationDependency(t);
				TGD inverse = TGD.create(
						t.getHead() instanceof QuantifiedFormula ?
								t.getHead().getChildren()[0].getAtoms():
									t.getHead().getAtoms(), 
									t.getBody().getAtoms());
				if (!allDependencies.contains(inverse)) {
					allDependencies.add(inverse);
				}
			} else {
				throw new IllegalStateException("No linear guarded dependency found for view " + view.getName());
			}
		}
	}
	
	private LinearGuarded findViewDependency(View v, List<Dependency> allDependencies) {
		if (allDependencies != null) {
			for (Dependency dependency:allDependencies) {
				if (dependency.getBody().getAtoms().length == 1) {
					if (dependency.getBody().getAtoms()[0]
							.getPredicate().getName().equals(v.getName())) {
						if (dependency instanceof LinearGuarded)
							return (LinearGuarded) dependency;
						return (LinearGuarded) LinearGuarded.create(dependency.getBodyAtoms()[0],dependency.getHeadAtoms());

					}
				}
			}
		}
		return null;
	}

	private Relation findRelation(String name) {
		for (Relation r : relations) {
			if (r.getName().equals(name)) {
				return r;
			}
		}
		return null;
	}
	
	/**
	 * Ensure every relation's foreign has its corresponding constraints.
	 *
	 * @param relation Relation
	 */
	private void ensureForeignKeyDefinition(Relation relation,List<Dependency> allDependencies) {
		for (ForeignKey fkey: relation.getForeignKeys()) {
			LinearGuarded gd = LinearGuarded.create(createBody(relation),new Atom[] {createHead(relation,fkey)});
			if (this.findFKDependency(gd,allDependencies) == null) {
				allDependencies.add(gd);
			}
		}
		for (LinearGuarded gd: this.findFKDependency(relation, allDependencies)) {
			ForeignKey fk = new ForeignKey("FK_for_LG" + gd.getId());
			Atom left = gd.getBody().getAtoms()[0];
			Atom right = gd.getHead().getAtoms()[0];
			Relation leftRel = getRelation(left.getPredicate());
			Relation rightRel = getRelation(right.getPredicate());
			fk.setForeignRelation(rightRel);
			fk.setForeignRelationName(rightRel.getName());
			for (Variable v:CollectionUtils.intersection(Arrays.asList(left.getVariables()), Arrays.asList(right.getVariables()))) {
				fk.addReference(new Reference(leftRel.getAttribute(Arrays.asList(left.getTerms()).indexOf(v)), rightRel.getAttribute(Arrays.asList(right.getTerms()).indexOf(v))));
			}
			if (!Arrays.asList(relation.getForeignKeys()).contains(fk)) {
				relation.addForeignKey(fk);
			}
		}
	}
	private Relation getRelation(Predicate p) {
		for (Relation r:relations) {
			if (r.getName().equals(p.getName())) {
				return r;
			}
		}
		return null;
	}
	private Collection<LinearGuarded> findFKDependency(Relation r,List<Dependency> allDependencies) {
		Set<LinearGuarded> result = new LinkedHashSet<>();
		if (allDependencies != null) {
			for (Dependency dependency: allDependencies) {
				if (dependency instanceof LinearGuarded
						&& ((LinearGuarded) dependency).getHead().getAtoms().length == 1
						&& ((LinearGuarded) dependency).getGuard().getPredicate().getName().equals(r.getName())) {
					result.add((LinearGuarded) dependency);
				}
			}
		}
		return result;
	}
	
	private TGD findFKDependency(TGD input,List<Dependency> allDependencies) {
		if (allDependencies != null) {
			for (Dependency dependency:allDependencies) {
				if (FormulaEquivalence.approximateEquivalence((Formula) input, (Formula) dependency)) {
					return (TGD)dependency;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @param relation Relation
	 * @return the body formula of a linear guarded dependency for the given relation
	 */
	private static Atom createBody(Relation relation) {
		List<Variable> free = new ArrayList<>();
		int index = 0;
		for (int i = 0, l = relation.getArity(); i < l; i++) {
			Variable v = Variable.create(Variable.DEFAULT_VARIABLE_PREFIX + (index++));
			free.add(v);
		}
		return Atom.create(relation, free.toArray(new Term[free.size()]));
	}

	/**
	 *
	 * @param relation the relation
	 * @param foreignKey the foreign key
	 * @return the head formula of a linear guarded dependency for the given relation and foreign key constraint
	 */
	private static Atom createHead(Relation relation, ForeignKey foreignKey) {
		List<Variable> free = new ArrayList<>();
		int index = 0;
		for (int i = 0, l = relation.getArity(); i < l; i++) {
			Variable v = Variable.create(Variable.DEFAULT_VARIABLE_PREFIX + (index++));
			free.add(v);
		}

		List<Variable> remoteTerms = new ArrayList<>();
		for (int i = 0, l = foreignKey.getForeignRelation().getArity(); i < l; i++) {
			Variable v = Variable.create(Variable.DEFAULT_VARIABLE_PREFIX + (index++));
			remoteTerms.add(v);
		}

		Reference[] references = foreignKey.getReferences();
		for (Reference rf:references) {
			int remoteTermIndex = foreignKey.getForeignRelation().getAttributePosition(rf.getForeignAttributeName());
			int localTermIndex = relation.getAttributePosition(rf.getLocalAttributeName());
			remoteTerms.set(remoteTermIndex, free.get(localTermIndex));
		}
		return Atom.create(foreignKey.getForeignRelation(), remoteTerms.toArray(new Term[remoteTerms.size()]));
	}
	
	@XmlElement(name = "dependency")
	@XmlElementWrapper(name = "dependencies")
	public Dependency[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(Dependency[] dependencies) {
		this.dependencies = convertDependenciesToPointToActualRelations(dependencies);
	}

	protected Dependency[] convertDependenciesToPointToActualRelations(Dependency[] dependencies2) {
		if (dependencies2==null)
			return null;
		Dependency[] newDep = new Dependency[dependencies2.length];
		for (int i = 0; i<dependencies2.length; i++) {
			Dependency d = dependencies2[i];
			if (d instanceof TGD) {
				newDep[i] = TGD.create(convertAtoms(d.getBodyAtoms()), convertAtoms(d.getHeadAtoms()));
			} else if (d instanceof EGD) {
				newDep[i] = EGD.create(convertAtoms(d.getBodyAtoms()), convertAtoms(d.getHeadAtoms()));
			} else {
				throw new IllegalArgumentException("Unsupported type: " + d);
			}

		}
		return newDep;
	}
	
	private Atom[] convertAtoms(Atom[] bodyAtoms) {
		if (bodyAtoms==null)
			return null;
		Atom[] newAtoms = new Atom[bodyAtoms.length];
		for (int i =0; i < bodyAtoms.length; i++) {
			newAtoms[i] = Atom.create(findRelation(bodyAtoms[i].getPredicate().getName()),bodyAtoms[i].getTerms());
		}
		return newAtoms;
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
