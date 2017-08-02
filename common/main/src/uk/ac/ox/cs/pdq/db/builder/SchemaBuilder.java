package uk.ac.ox.cs.pdq.db.builder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.FormulaEquivalence;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.util.Utility;


/**
 * Helper class for build schema. Allows incrementally construction, and 
 * performs various validations and checks upon initialization.
 * @author Julien Leblay
 */
public class SchemaBuilder implements uk.ac.ox.cs.pdq.builder.Builder<Schema> {

	/** The relations. */
	private Map<String, Relation> relations = new LinkedHashMap<>();

	/** The dependencies. */
	private Map<Integer, Dependency> dependencies = new LinkedHashMap<>();

	/** The disable dependencies. */
	private boolean disableDependencies = false;

	/**
	 * Instantiates a schema from an existing one.
	 */
	public SchemaBuilder() {
	}

	/**
	 * Add the given relation to the schema under construction iff no
	 * relation other than TemporaryRelation under the name already exists.
	 *
	 * @param relation the r
	 * @return this builder
	 */
	public SchemaBuilder addRelation(Relation relation) {
		Assert.assertNotNull(relation);
		//Efi: I do not understand this part of the code
//		Relation existing = this.relations.get(relation.getName());
//		if (existing != null
//				&& !(existing instanceof SchemaBuilder.TemporaryRelation)
//				&& relation instanceof SchemaBuilder.TemporaryRelation) {
//			throw new IllegalStateException();
//		}
		this.relations.put(relation.getName(), relation);
		return this;
	}

	/**
	 * Adds the dependency.
	 *
	 * @param dependency IC
	 * @return this builder
	 */
	public SchemaBuilder addDependency(Dependency dependency) {
		for (Dependency ic : this.dependencies.values()) {
			if (FormulaEquivalence.approximateEquivalence((Formula) ic, (Formula) dependency) ) 
				return this;
		}
		this.dependencies.put(((TGD) dependency).getId(), dependency);
		return this;
	}

	/**
	 * Adds the dependencies.
	 *
	 * @param dependencies the ics
	 * @return this builder
	 */
	public SchemaBuilder addDependencies(Dependency[] dependencies) {
		for (Dependency dependency:dependencies) 
			this.addDependency(dependency);
		return this;
	}

	/**
	 * Removes all dependencies from the schema being built.
	 * @return this builder
	 */
	public SchemaBuilder disableDependencies() {
		this.disableDependencies = true;
		return this;
	}

	/**
	 * Gets the relation.
	 *
	 * @param name the name
	 * @return the relation currently held in the builder under the given name
	 */
	public Relation getRelation(String name) {
		return this.relations.get(name);
	}

	/**
	 * Ensure every view has its corresponding definition as constraints.
	 *
	 * @param view the view
	 */
	private void ensureViewDefinition(View view) {
		LinearGuarded d = view.getViewToRelationDependency();
		LinearGuarded t = this.findViewDependency(view);
		if (d != null) {
			TGD inverse = TGD.create(d.getHead() instanceof QuantifiedFormula ? d.getHead().getChild(0) : d.getHead(), d.getBody());
			if (t == null) {
				this.dependencies.put(d.getId(), d);
			}
			TGD i = this.findDependency(inverse);
			if (i == null) {
				this.dependencies.put(inverse.getId(), inverse);
			}
		} else {
			if (t != null) {
				view.setViewToRelationDependency(t);
				TGD inverse = TGD.create(t.getHead() instanceof QuantifiedFormula ? t.getHead().getChild(0) : t.getHead(), t.getBody());
				TGD i = this.findDependency(inverse);
				if (i == null) {
					this.dependencies.put(inverse.getId(), inverse);
				}
			} else {
				throw new IllegalStateException("No linear guarded dependency found for view " + view.getName());
			}
		}
	}

	/**
	 * Ensure every relation's foreign has its corresponding constraints.
	 *
	 * @param relation Relation
	 */
	private void ensureForeignKeyDefinition(Relation relation) {
		for (ForeignKey fkey: relation.getForeignKeys()) {
			LinearGuarded dependency = new LinearGuarded(relation, fkey);
			if (this.findFKDependency(dependency) == null) {
				this.addDependency(dependency);
			}
		}
		for (LinearGuarded dependency: this.findFKDependency(relation)) {
			ForeignKey fk = Utility.createForeignKey(dependency);
			if (!Arrays.asList(relation.getForeignKeys()).contains(fk)) {
				relation.addForeignKey(fk);
			}
		}
	}
	
	/**
	 * Remove dependencies that refer to relation that are not part of the schema.
	 */
	private void removeOrphanDependencies() {
		for (Iterator<Integer> i = this.dependencies.keySet().iterator(); i.hasNext();) {
			Dependency dependency = this.dependencies.get(i.next());
			for (Atom p: dependency.getBody().getAtoms()) {
				if (this.relations.get(p.getPredicate().getName()) == null) {
					i.remove();
					break;
				}
			}
		}
		for (Iterator<Integer> i = this.dependencies.keySet().iterator(); i.hasNext();) {
			Dependency ic = this.dependencies.get(i.next());
			for (Atom p: ic.getHead().getAtoms()) {
				if (this.relations.get(p.getPredicate().getName()) == null) {
					i.remove();
					break;
				}
			}
		}
	}

	/**
	 * Derives the dependencies that correspond to the schema views.
	 */
	private void consolidateDependencies() {
		for (Relation r:this.relations.values()) {
			if (r instanceof View) {
				this.ensureViewDefinition((View) r);
			} else {
				this.ensureForeignKeyDefinition(r);
			}
		}
		this.removeOrphanDependencies();
	}

	/**
	 * Find view dependency.
	 *
	 * @param v the v
	 * @return the linear guarded dependency currently held in the builder
	 *         for the given view. null if no such dependency was found.
	 */
	private LinearGuarded findViewDependency(View v) {
		if (this.dependencies != null) {
			for (Dependency dependency:this.dependencies.values()) {
				if (dependency.getNumberOfBodyAtoms() == 1) {
					if (dependency.getBodyAtom(0).getPredicate().getName().equals(v.getName())) 
						return (LinearGuarded) dependency;
				}
			}
		}
		return null;
	}

	/**
	 * Find dependency.
	 *
	 * @param input TGD
	 * @return the dependency currently held in the builder that is equal
	 *         (modulo the ID) to the given dependency.
	 */
	private TGD findDependency(TGD input) {
		if (this.dependencies != null) {
			for (Dependency dependency:this.dependencies.values()) {
				if (FormulaEquivalence.approximateEquivalence((Formula) dependency, (Formula) input)) {
					return (TGD) dependency;
				}
			}
		}
		return null;
	}

	/**
	 * Find fk dependency.
	 *
	 * @param input the gd
	 * @return the guarded dependency that is equal to the given one.
	 */
	private TGD findFKDependency(TGD input) {
		if (this.dependencies != null) {
			for (Dependency dependency:this.dependencies.values()) {
				if (FormulaEquivalence.approximateEquivalence((Formula) input, (Formula) dependency)) {
					return (TGD)dependency;
				}
			}
		}
		return null;
	}

	/**
	 * Find fk dependency.
	 *
	 * @param r Relation
	 * @return the guarded dependency that is equal to the given one.
	 */
	private Collection<LinearGuarded> findFKDependency(Relation r) {
		Set<LinearGuarded> result = new LinkedHashSet<>();
		if (this.dependencies != null) {
			for (Dependency dependency: this.dependencies.values()) {
				if (dependency instanceof LinearGuarded
						&& ((LinearGuarded) dependency).getHead().getAtoms().length == 1
						&& ((LinearGuarded) dependency).getGuard().getPredicate().equals(r)) {
					result.add((LinearGuarded) dependency);
				}
			}
		}
		return result;
	}

	/**
	 * Builds a new instance of Schema containing all the relation and
	 * dependencies added so far, plus all dependencies derivable from view
	 * definitions and foreign keys.
	 *
	 * @return a new instance of Schema
	 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
	 */
	@Override
	public Schema build() {
		if (!this.disableDependencies) 
			this.consolidateDependencies();
		else 
			this.dependencies.clear();
		return new Schema(this.relations.values().toArray(new Relation[this.relations.values().size()]), 
				this.dependencies.values().toArray(new Dependency[this.dependencies.values().size()]));
	}

//	/**
//	 * A relation that temporarily hold signature related information in
//	 * preparation for instantiatiating a relation.
//	 * @author Julien Leblay
//	 */
//	private static class TemporaryRelation extends Relation {
//
//		/** The Constant serialVersionUID. */
//		private static final long serialVersionUID = 7049363904713889121L;
//
//		/**
//		 * Constructor for TemporaryRelation.
//		 * @param name String
//		 * @param attributes List<Attribute>
//		 * @param isEq true if the relation acts as an equality
//		 */
//		public TemporaryRelation(String name, Attribute[] attributes, boolean isEq) {
//			super(name, attributes, isEq);
//		}
//	}
}