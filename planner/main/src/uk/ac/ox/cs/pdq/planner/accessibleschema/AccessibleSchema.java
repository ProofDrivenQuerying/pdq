package uk.ac.ox.cs.pdq.planner.accessibleschema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Given schema S_0, the Accessible Schema for S_0, denoted AcSch(S_0), 
 * is the schema without any access restrictions, such that:
	i. The constants are those of S_0.
	ii.The relations are those of S_0, a copy of each relation R
	denoted AccessedR (the "accessible version of R"), a unary
	relation accessible(x) ("x is an accessible value") plus another
	copy of each relation R of S0 called InferredAccR
	" the "inferred accessible version of R". 
	iii.The constraints are the constraints of S_0 plus inferred accessible copies of the those constraints
	and accessibility axioms created from the schema access methods. 
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class AccessibleSchema extends Schema {

	/**  The accessible relations. */
	private final List<AccessibleRelation> accessibleRelations;

	/**  Mapping from schema relations to the corresponding inferred accessible relations. */
	private final Map<String, InferredAccessibleRelation> infAccessibleRelations;

	/**  Mapping from a relation-access method pair to an accessibility axioms. */
	private final Map<Pair<? extends Relation, AccessMethod>, AccessibilityAxiom> accessibilityAxioms;

	/**  Mapping from a dependency to its inferred accessible counterpart. */
	private final Map<Dependency, InferredAccessibleAxiom> infAccessibilityAxioms;

//	/**  The inferred accessible axioms*. */
//	private final List<InferredAccessibleAxiom> infAccessibleViews = new ArrayList<>();
	
	/**
	 * Instantiates a new accessible schema.
	 *
	 * @param relations 		List of schema relations
	 * @param dependencies 		list if schema dependencies
	 * @param constantsMap 		Map of schema constant names to constants
	 */
	public AccessibleSchema(List<Relation> relations, List<Dependency> dependencies, Map<String, TypedConstant<?>> constantsMap) {
		super(relations, dependencies);
		this.typedConstants = constantsMap;
		ImmutableMap.Builder<String, InferredAccessibleRelation> b2 = ImmutableMap.builder();
		ImmutableMap.Builder<Pair<? extends Relation, AccessMethod>, AccessibilityAxiom> f4 = ImmutableMap.builder();
		for (Relation relation:relations) {
			InferredAccessibleRelation infAcc = new InferredAccessibleRelation(relation);
			b2.put(relation.getName(), infAcc);
			// Accessibility axioms/
			for (AccessMethod bindingMethod:relation.getAccessMethods()) {
				f4.put(Pair.of(relation, bindingMethod), new AccessibilityAxiom(infAcc, bindingMethod));
			}
		}

		ImmutableMap.Builder<Dependency, InferredAccessibleAxiom> b6 = ImmutableMap.builder();
		this.accessibleRelations = ImmutableList.of(AccessibleRelation.getInstance());
		this.infAccessibleRelations = b2.build();

		// Inferred accessible axioms the schema ICs
		for (Dependency dependency: dependencies) {
			Map<Atom, InferredAccessibleRelation> predicateToInfAccessibleRelation = new LinkedHashMap<>();
			for (Atom p: dependency.getAtoms()) {
				predicateToInfAccessibleRelation.put(p, this.infAccessibleRelations.get(p.getPredicate().getName()));
			}
			if (dependency instanceof TGD) {
				InferredAccessibleAxiom infAcc = new InferredAccessibleAxiom((TGD) dependency, predicateToInfAccessibleRelation);
				b6.put(dependency, infAcc);
//				if(this.views.contains(dependency)) {
//					this.infAccessibleViews.add(infAcc);
//				}
			}
		}
		try {
			this.accessibilityAxioms = f4.build();
			this.infAccessibilityAxioms = b6.build();
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Constructor for AccessibleSchema.
	 * @param schema Schema
	 */
	public AccessibleSchema(Schema schema) {
		this(schema.getRelations(), schema.getDependencies(), schema.getTypedConstants());
	}
	
	/**
	 * Gets the inferred accessibility axioms.
	 *
	 * @return the inferred accessible counterparts of the schema dependencies
	 */
	public Collection<InferredAccessibleAxiom> getInferredAccessibilityAxioms() {
		return this.infAccessibilityAxioms.values();
	}

//	/**
//	 * Gets the inferred accessible views.
//	 *
//	 * @return 		the inferred accessible views of this accessible schema
//	 */
//	public List<InferredAccessibleAxiom> getInferredAccessibleViews() {
//		return this.infAccessibleViews;
//	}

	/**
	 * Gets the accessibility axioms.
	 *
	 * @return 		the accessibility axioms of this schema
	 */
	public Collection<AccessibilityAxiom> getAccessibilityAxioms() {
		return this.accessibilityAxioms.values();
	}

	/**
	 * Gets the accessibility axiom.
	 *
	 * @param r Input relation
	 * @param b Input access method
	 * @return The accessibility axiom that corresponds to the relation-access method pair
	 */
	public AccessibilityAxiom getAccessibilityAxiom(Relation r, AccessMethod b) {
		return this.accessibilityAxioms.get(Pair.of(r, b));
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.db.Schema#getRelations()
	 */
	@Override
	public List<Relation> getRelations() {
		return Lists.newArrayList(
				Iterables.concat(
						this.relations,
						this.infAccessibleRelations.values(),
						this.accessibleRelations));
	}

	/**
	 * Gets the relation.
	 *
	 * @param relationName String
	 * @return Relation
	 */
	@Override
	public Relation getRelation(String relationName) {
		Relation result = super.getRelation(relationName);
		if (result == null && this.accessibleRelations.get(0).getName().equals(relationName)) {
			result = this.accessibleRelations.get(0);
		}
		if (result == null) {
			result = this.infAccessibleRelations.get(relationName);
		}
		return result;
	}

	/**
	 * Gets the accessible relation.
	 *
	 * @return AccessibleRelation
	 */
	public AccessibleRelation getAccessibleRelation() {
		return this.accessibleRelations.get(0);
	}

	/**
	 * Gets the inferred accessible relation.
	 *
	 * @param r Relation
	 * @return InferredAccessibleRelation
	 */
	public InferredAccessibleRelation getInferredAccessibleRelation(Relation r) {
		return this.infAccessibleRelations.get(r.getName());
	}

	/**
	 * Gets the dependencies.
	 *
	 * @return all the accessible schema dependencies including the ones of the original schema
	 */
	@Override
	public List<Dependency> getDependencies() {
		return Lists.newArrayList(
				Iterables.concat(
						this.schemaDependencies,
						this.infAccessibilityAxioms.values()));
	}

	/**
	 * Accessible.
	 *
	 * @param <Q> the generic type
	 * @param query the query
	 * @return the accessible query
	 * @see uk.ac.ox.cs.pdq.fol.Query#accessible(AccessibleSchema)
	 */
	public ConjunctiveQuery accessible(ConjunctiveQuery query) {
		List<Formula> atoms = new ArrayList<>();
		for (Atom af: query.getAtoms()) {
			atoms.add(
					new Atom(this.getInferredAccessibleRelation((Relation) af.getPredicate()), af.getTerms()));
		}
		return new ConjunctiveQuery(query.getFreeVariables(), (Conjunction) Conjunction.of(atoms));
	}
	
	/**
	 * Accessible.
	 *
	 * @param <Q> the generic type
	 * @param query the query
	 * @param canonicalMapping the canonical mapping
	 * @return the accessible query
	 * @see uk.ac.ox.cs.pdq.fol.Query#accessible(AccessibleSchema)
	 */
	public ConjunctiveQuery accessible(ConjunctiveQuery query, Map<Variable, Constant> canonicalMapping) {
		List<Formula> atoms = new ArrayList<>();
		for (Atom af: query.getAtoms()) {
			atoms.add(
					new Atom(this.getInferredAccessibleRelation((Relation) af.getPredicate()), af.getTerms()));
		}
		return new ConjunctiveQuery(query.getFreeVariables(), (Conjunction) Conjunction.of(atoms), canonicalMapping);
	}
	
	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('{');
		if (!this.relations.isEmpty()) {
			result.append("\n\t{");
			for (Relation r : this.relations) {
				result.append("\n\t\t").append(r);
			}
			result.append("\n\t}");
		}
		if (!this.schemaDependencies.isEmpty()) {
			result.append("\n\t{");
			for (Dependency ic : this.schemaDependencies) {
				result.append("\n\t\t").append(ic);
			}
			result.append("\n\t}");
		}
		if (!this.accessibilityAxioms.isEmpty()) {
			result.append("\n\t{");
			for (Dependency ic : this.accessibilityAxioms.values()) {
				result.append("\n\t\t").append(ic);
			}
			result.append("\n\t}");
		}
		if (!this.infAccessibilityAxioms.isEmpty()) {
			result.append("\n\t{");
			for (Dependency ic : this.infAccessibilityAxioms.values()) {
				result.append("\n\t\t").append(ic);
			}
			result.append("\n\t}");
		}
		result.append("\n}");
		return result.toString();
	}

	/**
	 * Accessible relations implementation.
	 *
	 * @author Efthymia Tsamoura
	 */
	public static class AccessibleRelation extends Relation {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 5762653825140737301L;
		
		/** The Constant PREFIX. */
		public static final String PREFIX = "Access";
		
		/** The singleton. */
		private static AccessibleRelation singleton = null;

		/**
		 * Instantiates a new accessible relation.
		 */
		private AccessibleRelation() {
			super(PREFIX, asList(new Attribute(String.class, "x0")),
					Lists.newArrayList(
							new AccessMethod(
									AccessMethod.DEFAULT_PREFIX +
									AccessMethod.Types.FREE + PREFIX,
									AccessMethod.Types.FREE,
									Lists.<Integer>newArrayList())));

		}

		/**
		 * Gets the accessible fact.
		 *
		 * @param constant TypedConstant<?>
		 * @return PredicateFormula
		 */
		public static Atom getAccessibleFact(TypedConstant<?> constant) {
			return new Atom(AccessibleRelation.getInstance(), new TypedConstant<>(constant));
		}

		/**
		 * As list.
		 *
		 * @param a Attribute
		 * @return List<Attribute>
		 */
		private static List<Attribute> asList(Attribute a) {
			List<Attribute> result = new ArrayList<>(1);
			result.add(a);
			return result;
		}

		/**
		 * Gets the single instance of AccessibleRelation.
		 *
		 * @return AccessibleRelation
		 */
		public static AccessibleRelation getInstance() {
			if (singleton == null) {
				singleton = new AccessibleRelation();
			}
			return singleton;
		}
	}

	/**
	 * Inferred accessible relations implementation.
	 *
	 * @author Efthymia Tsamoura
	 */
	public static class InferredAccessibleRelation extends Relation {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 5612488076416617646L;
		
		/** The Constant PREFIX. */
		public static final String PREFIX = "InferredAccessible";
		
		/** The base relation. */
		private final Relation baseRelation;

		/**
		 * Constructor for InferredAccessibleRelation.
		 * @param r Relation
		 */
		public InferredAccessibleRelation(Relation r) {
			super(PREFIX + r.getName(), r.getAttributes(),
					Lists.newArrayList(
							new AccessMethod(
									AccessMethod.DEFAULT_PREFIX +
									AccessMethod.Types.FREE + r.getName(),
									AccessMethod.Types.FREE,
									Lists.<Integer>newArrayList())));
			this.baseRelation = r;
		}

		/**
		 * Constructor for InferredAccessibleRelation.
		 * @param view View
		 */
		public InferredAccessibleRelation(View view) {
			super(PREFIX + view.getName(),
					Utility.canonicalAttributes(view.getAttributes()),
					Lists.newArrayList(
							new AccessMethod(
									AccessMethod.DEFAULT_PREFIX +
									AccessMethod.Types.FREE + view.getName(),
									AccessMethod.Types.FREE,
									Lists.<Integer>newArrayList())));
			this.baseRelation = view;
		}

		/**
		 * Gets the base relation.
		 *
		 * @return Relation
		 */
		public Relation getBaseRelation() {
			return this.baseRelation;
		}
	}
}
