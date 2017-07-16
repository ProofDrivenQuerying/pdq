package uk.ac.ox.cs.pdq.planner.accessibleschema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.TGD;

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

	public static final String inferredAccessiblePrefix = "InferredAccessible";

	/**  The accessible relations. */
	public static final Relation accessibleRelation = Relation.create("Accessible", 
			new Attribute[]{Attribute.create(String.class, "x0"), Attribute.create(Integer.class, "FactID")}, 
			new AccessMethod[]{AccessMethod.create(new Integer[]{})});

	/**  Mapping from a relation-access method pair to an accessibility axioms. */
	private final AccessibilityAxiom[] accessibilityAxioms;

	/**  Mapping from a dependency to its inferred accessible counterpart. */
	private final Dependency[] inferredAccessibilityAxioms;

	private static AccessibilityAxiom[] lastComputedaccessibilityAxioms;
	private static Dependency[] lastComputedinferredAccessibilityAxioms;

	/**
	 * Instantiates a new accessible schema.
	 *
	 * @param relations 		List of schema relations
	 * @param dependencies 		list if schema dependencies
	 * @param constantsMap 		Map of schema constant names to constants
	 */
	private AccessibleSchema(Relation[] relations, Dependency[] dependencies, Map<String, TypedConstant> constantsMap) {
		super(computeAccessibleSchemaRelations(relations), computeAccessibleSchemaAxioms(relations, dependencies));
		this.constants = constantsMap;
		this.accessibilityAxioms = lastComputedaccessibilityAxioms;
		this.inferredAccessibilityAxioms = lastComputedinferredAccessibilityAxioms;
	}

	public static Relation[] computeAccessibleSchemaRelations(Relation[] relations) {
		Collection<Relation> output  = new LinkedHashSet<>();
		output.addAll(Arrays.asList(relations));
		for(Relation relation:relations) 
			output.add(Relation.create(AccessibleSchema.inferredAccessiblePrefix + relation.getName(), relation.getAttributes(), relation.getAccessMethods(), relation.getForeignKeys()));
		return output.toArray(new Relation[output.size()]);
	}

	public static AccessibilityAxiom[] computeAccessibilityAxioms(Relation[] relations) {
		List<AccessibilityAxiom> accessibilityAxioms = new LinkedList<>();
		for (Relation relation:relations) {
			for (AccessMethod method:relation.getAccessMethods()) 
				accessibilityAxioms.add(new AccessibilityAxiom(relation, method));
		}
		return accessibilityAxioms.toArray(new AccessibilityAxiom[accessibilityAxioms.size()]);
	}

	public static Dependency[] computeInferredAccessibleAxioms(Dependency[] dependencies) {
		List<Dependency> inferredAccessibleDependencies = new ArrayList<>();
		for (Dependency dependency: dependencies)
			if (dependency instanceof TGD) 
				inferredAccessibleDependencies.add(createInferredAccessibleAxiom((TGD)dependency));
		return inferredAccessibleDependencies.toArray(new Dependency[inferredAccessibleDependencies.size()]);
	}

	public static Dependency[] computeAccessibleSchemaAxioms(Relation[] relations, Dependency[] dependencies) {
		lastComputedaccessibilityAxioms = computeAccessibilityAxioms(relations);
		lastComputedinferredAccessibilityAxioms = computeInferredAccessibleAxioms(dependencies);
		Dependency[] allDependencies = new Dependency[dependencies.length + lastComputedaccessibilityAxioms.length + lastComputedinferredAccessibilityAxioms.length];
		System.arraycopy(dependencies, 0, allDependencies, 0, dependencies.length);
		System.arraycopy(lastComputedaccessibilityAxioms, 0, allDependencies, dependencies.length, lastComputedaccessibilityAxioms.length);
		System.arraycopy(lastComputedinferredAccessibilityAxioms, 0, allDependencies, dependencies.length + lastComputedaccessibilityAxioms.length, lastComputedinferredAccessibilityAxioms.length);
		return allDependencies;
	}


	/**
	 * Constructor for AccessibleSchema.
	 * @param schema Schema
	 */
	public AccessibleSchema(Schema schema) {
		this(schema.getRelations(), schema.getDependencies(), schema.getConstants());
	}

	/**
	 * Gets the inferred accessibility axioms.
	 *
	 * @return the inferred accessible counterparts of the schema dependencies
	 */
	public Dependency[] getInferredAccessibilityAxioms() {
		return this.inferredAccessibilityAxioms.clone();
	}


	/**
	 * Gets the accessibility axioms.
	 *
	 * @return 		the accessibility axioms of this schema
	 */
	public AccessibilityAxiom[] getAccessibilityAxioms() {
		return this.accessibilityAxioms.clone();
	}

	/**
	 * Creates an inferred accessible axiom.
	 *
	 * @param dependency the dependency
	 * @param predToInfAcc the pred to inf acc
	 */
	private static TGD createInferredAccessibleAxiom(TGD dependency) {
		return TGD.create(substitute(dependency.getBody()), substitute(dependency.getHead()));
	}

	/**
	 * Substitute.
	 *
	 * @param f Formula
	 * @return Formula
	 */
	private static Formula substitute(Formula f) {
		if (f instanceof Conjunction) {
			return substitute((Conjunction) f);
		}
		else if (f instanceof Disjunction) {
			return substitute((Disjunction) f);
		}
		else if (f instanceof Negation) {
			return substitute((Negation) f);
		}
		else if (f instanceof QuantifiedFormula) {
			return substitute((QuantifiedFormula) f);
		}
		else if (f instanceof Atom) {
			return substitute((Atom) f);
		}
		return f;
	}

	/**
	 * Substitute.
	 *
	 * @param conjunction Conjunction<Formula>
	 * @return Conjunction<Formula>
	 */
	private static Formula substitute(Conjunction conjunction) {
		List<Formula> result = new LinkedList<>();
		for (Formula f:conjunction.getChildren()) 
			result.add(substitute(f));
		return Conjunction.of(result.toArray(new Formula[result.size()]));
	}

	/**
	 * Substitute.
	 *
	 * @param disjunction Disjunction<Formula>
	 * @return Disjunction<Formula>
	 */
	private static Formula substitute(Disjunction disjunction) {
		List<Formula> result = new LinkedList<>();
		for (Formula f: disjunction.getChildren()) 
			result.add(substitute(f));
		return Disjunction.of(result.toArray(new Formula[result.size()]));
	}

	/**
	 * Substitute.
	 *
	 * @param negation Negation<Formula>
	 * @return Negation<Formula>
	 */
	private static Negation substitute(Negation negation) {
		return Negation.of(substitute(negation.getChildren()[0]));
	}

	private static QuantifiedFormula substitute(QuantifiedFormula quantifiedFormula) {
		return QuantifiedFormula.create(quantifiedFormula.getOperator(), 
				quantifiedFormula.getFreeVariables(), quantifiedFormula.getChildren()[0]);
	}

	/**
	 * Substitute.
	 *
	 * @param atom PredicateFormula
	 * @return PredicateFormula
	 */
	private static Atom substitute(Atom atom) {
		return Atom.create(Predicate.create(inferredAccessiblePrefix + atom.getPredicate().getName(), atom.getPredicate().getArity()), atom.getTerms());
	}
}
