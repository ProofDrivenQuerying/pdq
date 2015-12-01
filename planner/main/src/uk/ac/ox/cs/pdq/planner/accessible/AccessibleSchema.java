package uk.ac.ox.cs.pdq.planner.accessible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Accessible schema
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public class AccessibleSchema extends Schema {

	/** The accessible relations*/
	private final List<AccessibleRelation> accessibleRelations;

	/** Mapping from schema relations to the corresponding inferred accessed relations*/
	private final Map<String, InferredAccessibleRelation> infAccessibleRelations;

	/** Mapping from a relation binding pair to an accessibility axioms, not relying on AccessedRelations */
	private final Map<Pair<? extends Relation, AccessMethod>, AccessibilityAxiom> accessibilityAxioms;

	/** Mapping from a dependency to its inferred accessible version*/
	private final Map<Constraint, InferredAccessibleAxiom> infAccessibilityAxioms;

	private final List<InferredAccessibleAxiom> infAccessibleViews = new ArrayList<>();

	/**
	 *
	 * @param relations
	 * 		List of schema relations
	 * @param ics
	 * 		list if schema ICs
	 */
	public AccessibleSchema(List<Relation> relations, List<Constraint> ics, Map<String, TypedConstant<?>> constantsMap) {
		super(relations, ics);
		this.constants = constantsMap;
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

		ImmutableMap.Builder<Constraint, InferredAccessibleAxiom> b6 = ImmutableMap.builder();
		this.accessibleRelations = ImmutableList.of(AccessibleRelation.getInstance());
		this.infAccessibleRelations = b2.build();

		// Inferred accessible axioms the schema ICs
		for (Constraint<?, ?> ic: ics) {
			Map<Predicate, InferredAccessibleRelation> predicateToInfAccessibleRelation = new LinkedHashMap<>();
			for (Predicate p: ic.getPredicates()) {
				predicateToInfAccessibleRelation.put(p, this.infAccessibleRelations.get(p.getName()));
			}
			if (ic instanceof TGD) {
				InferredAccessibleAxiom infAcc = new InferredAccessibleAxiom((TGD) ic, predicateToInfAccessibleRelation);
				b6.put(ic, infAcc);
				if(this.views.contains(ic)) {
					this.infAccessibleViews.add(infAcc);
				}
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
		this(schema.getRelations(), schema.getDependencies(), schema.getConstants());
	}
	
	/**
	 * @return the inferred accessible versions of the schema dependencies
	 */
	public Collection<InferredAccessibleAxiom> getInferredAccessibilityAxioms() {
		return this.infAccessibilityAxioms.values();
	}


	/**
	 * @return List<InferredAccessibleAxiom>
	 */
	public List<InferredAccessibleAxiom> getInferredAccessibleViews() {
		return this.infAccessibleViews;
	}

	/**
	 * @return Collection<AccessibilityAxiom>
	 */
	public Collection<AccessibilityAxiom> getAccessibilityAxioms() {
		return this.accessibilityAxioms.values();
	}

	/**
	 * @param r Input relation
	 * @param b Input binding
	 * @return The accessibility axiom that corresponds to the relation-binding pair
	 */
	public AccessibilityAxiom getAccessibilityAxiom(Relation r, AccessMethod b) {
		return this.accessibilityAxioms.get(Pair.of(r, b));
	}
	
	/**
	 * @return List<Relation>
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
	 * @return AccessibleRelation
	 */
	public AccessibleRelation getAccessibleRelation() {
		return this.accessibleRelations.get(0);
	}

	/**
	 * @param r Relation
	 * @return InferredAccessibleRelation
	 */
	public InferredAccessibleRelation getInferredAccessibleRelation(Relation r) {
		return this.infAccessibleRelations.get(r.getName());
	}

	/**
	 * @return all the accessible schema dependencies including the ones of the original schema
	 */
	@Override
	public List<Constraint> getDependencies() {
		return Lists.newArrayList(
				Iterables.concat(
						this.schemaDependencies,
						this.infAccessibilityAxioms.values()));
	}

	/**
	 * @param accessibleSchema AccessibleSchema
	 * @return the accessible query
	 * @see uk.ac.ox.cs.pdq.fol.Query#accessible(AccessibleSchema)
	 */
	public <Q extends Query<?>> Q accessible(Q query) {
		if (!(query instanceof ConjunctiveQuery)) {
			throw new UnsupportedOperationException("Non CQ not supported yet.");
		}
		List<Predicate> atomicFormulas = new ArrayList<>();
		for (Predicate af: query.getBody().getPredicates()) {
			atomicFormulas.add(
					new Predicate(this.getInferredAccessibleRelation((Relation) af.getSignature()), af.getTerms()));
		}
		return (Q) new ConjunctiveQuery(query.getHead(), Conjunction.of(atomicFormulas));
	}
	
	/**
	 * @param accessibleSchema AccessibleSchema
	 * @return the accessible query
	 * @see uk.ac.ox.cs.pdq.fol.Query#accessible(AccessibleSchema)
	 */
	public <Q extends Query<?>> Q accessible(Q query, Map<Variable, Constant> canonicalMapping) {
		if (!(query instanceof ConjunctiveQuery)) {
			throw new UnsupportedOperationException("Non CQ not supported yet.");
		}
		List<Predicate> atomicFormulas = new ArrayList<>();
		for (Predicate af: query.getBody().getPredicates()) {
			atomicFormulas.add(
					new Predicate(this.getInferredAccessibleRelation((Relation) af.getSignature()), af.getTerms()));
		}
		return (Q) new ConjunctiveQuery(query.getHead(), Conjunction.of(atomicFormulas), canonicalMapping);
	}
	
	/**
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
			for (Constraint ic : this.schemaDependencies) {
				result.append("\n\t\t").append(ic);
			}
			result.append("\n\t}");
		}
		if (!this.accessibilityAxioms.isEmpty()) {
			result.append("\n\t{");
			for (Constraint ic : this.accessibilityAxioms.values()) {
				result.append("\n\t\t").append(ic);
			}
			result.append("\n\t}");
		}
		if (!this.infAccessibilityAxioms.isEmpty()) {
			result.append("\n\t{");
			for (Constraint ic : this.infAccessibilityAxioms.values()) {
				result.append("\n\t\t").append(ic);
			}
			result.append("\n\t}");
		}
		result.append("\n}");
		return result.toString();
	}

	/**
	 *
	 * Accessible relations implementation
	 * @author Efthymia Tsamoura
	 *
	 */
	public static class AccessibleRelation extends Relation {

		/** */
		private static final long serialVersionUID = 5762653825140737301L;
		public static final String PREFIX = "Access";
		private static AccessibleRelation singleton = null;

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
		 * @param constant TypedConstant<?>
		 * @return PredicateFormula
		 */
		public static Predicate getAccessibleFact(TypedConstant<?> constant) {
			return new Predicate(AccessibleRelation.getInstance(), new TypedConstant<>(constant));
		}

		/**
		 * @param a Attribute
		 * @return List<Attribute>
		 */
		private static List<Attribute> asList(Attribute a) {
			List<Attribute> result = new ArrayList<>(1);
			result.add(a);
			return result;
		}

		/**
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
	 *
	 * Inferred accessible relations implementation
	 * @author Efthymia Tsamoura
	 *
	 */
	public static class InferredAccessibleRelation extends Relation {

		/** */
		private static final long serialVersionUID = 5612488076416617646L;
		public static final String PREFIX = "InferredAccessible";
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
		 * @return Relation
		 */
		public Relation getBaseRelation() {
			return this.baseRelation;
		}
	}
}
