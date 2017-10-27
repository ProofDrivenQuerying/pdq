package uk.ac.ox.cs.pdq.planner.accessibleschema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.TGD;

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
			new Attribute[]{Attribute.create(String.class, "x0"), Attribute.create(Integer.class, "InstanceID")}, 
			new AccessMethod[]{AccessMethod.create(new Integer[]{})});

	/**  Mapping from a relation-access method pair to an accessibility axioms. */
	private final AccessibilityAxiom[] accessibilityAxioms;

	/**  Mapping from a dependency to its inferred accessible counterpart. */
	private final Dependency[] inferredAccessibilityAxioms;

	private final Dependency[] originalDependencies;

	/**
	 * Instantiates a new accessible schema.
	 *
	 * @param relations 		List of schema relations
	 * @param dependencies 		list if schema dependencies
	 * @param constantsMap 		Map of schema constant names to constants
	 */
	private AccessibleSchema(Relation[] relations, Dependency[] dependencies) {
		super(computeAccessibleSchemaRelations(relations), computeAccessibleSchemaAxioms(relations, dependencies));
		this.originalDependencies = dependencies.clone();
		this.accessibilityAxioms = lastComputedaccessibilityAxioms.clone();
		this.inferredAccessibilityAxioms = lastComputedinferredAccessibilityAxioms.clone();
	}

	/**
	 * Constructor for AccessibleSchema.
	 * @param schema Schema
	 */
	public AccessibleSchema(Schema schema) {
		this(schema.getRelations(), schema.getDependencies());
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

	//TOCOMMENT maybe original and inffacc?
	public Dependency[] getOriginalDependencies() {
		return this.originalDependencies.clone();
	}
	
	public boolean containsInferredAccessibleAxiom(Dependency dependency) {
		return Arrays.asList(this.inferredAccessibilityAxioms).contains(dependency);
	}

	private static TGD createInferredAccessibleAxiom(TGD dependency) {
		return TGD.create(computeInferredAccessibleFormula(dependency.getBody()).getAtoms(), computeInferredAccessibleFormula(dependency.getHead()).getAtoms());
	}

	public static Formula computeInferredAccessibleFormula(Formula f) {
		if (f instanceof Conjunction) {
			Formula[] result = new Formula[f.getNumberOfChildlen()];
			for (int index = 0; index < f.getNumberOfChildlen(); ++index) 
				result[index] = computeInferredAccessibleFormula(f.getChild(index));
			return Conjunction.of(result);
		}
		else if (f instanceof Disjunction) {
			Formula[] result = new Formula[f.getNumberOfChildlen()];
			for (int index = 0; index < f.getNumberOfChildlen(); ++index) 
				result[index] = computeInferredAccessibleFormula(f.getChild(index));
			return Disjunction.of(result);
		}
		else if (f instanceof Negation) {
			return Negation.of(computeInferredAccessibleFormula(f.getChild(0)));
		}
		else if (f instanceof QuantifiedFormula) {
			return QuantifiedFormula.create(((QuantifiedFormula)f).getOperator(), f.getFreeVariables(), computeInferredAccessibleFormula(f.getChild(0)));
		}
		else if (f instanceof Atom) {
			Predicate predicate = null;
			predicate = Predicate.create(inferredAccessiblePrefix + ((Atom)f).getPredicate().getName(), ((Atom)f).getPredicate().getArity());
			return Atom.create(predicate, ((Atom)f).getTerms());
		}
		else 
			throw new RuntimeException("Unknown formula type");
	}

	public static Relation[] computeAccessibleSchemaRelations(Relation[] relations) {
		Collection<Relation> output  = new LinkedHashSet<>();
		output.addAll(Arrays.asList(relations));
		for(Relation relation:relations) 
			output.add(Relation.create(AccessibleSchema.inferredAccessiblePrefix + relation.getName(), relation.getAttributes(), new AccessMethod[]{}, relation.isEquality()));
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

	private static AccessibilityAxiom[] lastComputedaccessibilityAxioms;

	private static Dependency[] lastComputedinferredAccessibilityAxioms;

	public static Dependency[] computeAccessibleSchemaAxioms(Relation[] relations, Dependency[] dependencies) {
		lastComputedaccessibilityAxioms = computeAccessibilityAxioms(relations);
		lastComputedinferredAccessibilityAxioms = computeInferredAccessibleAxioms(dependencies);
		Dependency[] allDependencies = new Dependency[dependencies.length + lastComputedaccessibilityAxioms.length + lastComputedinferredAccessibilityAxioms.length];
		System.arraycopy(dependencies, 0, allDependencies, 0, dependencies.length);
		System.arraycopy(lastComputedaccessibilityAxioms, 0, allDependencies, dependencies.length, lastComputedaccessibilityAxioms.length);
		System.arraycopy(lastComputedinferredAccessibilityAxioms, 0, allDependencies, dependencies.length + lastComputedaccessibilityAxioms.length, lastComputedinferredAccessibilityAxioms.length);
		return allDependencies;
	}
}
