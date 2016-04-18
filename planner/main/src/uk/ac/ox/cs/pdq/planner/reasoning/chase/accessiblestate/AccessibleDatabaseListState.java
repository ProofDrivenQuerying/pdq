package uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.planner.reasoning.MatchFactory;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph;
import uk.ac.ox.cs.pdq.reasoning.utility.MapFiringGraph;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * The Class AccessibleDatabaseListState.
 *
 * @author Efthymia Tsamoura
 * 
 * 	Organises the facts during chasing into a list. 
 * 	This type of state is used in terminating chase implementations.
 * 	It also maintains the classes of equal chase constants that are derived after chasing with EGDs.
 * 	This implementation does not store equality facts into the database, but when a class of equal constants is created
 * 	the database facts are updated; update includes replacing every chase constant c, with a constant c' that is equal to c
 * 	under the constraints and c' is a representative.
 * 	The database is cleared from the obsolete facts after a chase step is applied.
 */
public class AccessibleDatabaseListState extends uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseListState implements AccessibleChaseState {

	/**  String signatures of the inferred accessible facts. */
	private final Collection<String> inferred;

	/** The inferred accessible facts that were derived when chasing this.state **/
	protected final Collection<Atom> derivedInferred;

	/**  Maps each schema signature (relation) to its chase facts. */
	private final Multimap<Predicate, Atom> signatureGroups;

	/**  Maps each chase constant the Accessed facts it appears. */
	private final Multimap<Term,Atom> accessibleTerms;

	/**
	 * Instantiates a new accessible database list state.
	 *
	 * @param query the query
	 * @param schema the schema
	 * @param manager the manager
	 */
	public AccessibleDatabaseListState(ConjunctiveQuery query, Schema schema, DBHomomorphismManager manager) {
		this(manager, 
				createInitialFacts(query, schema), 
				new MapFiringGraph(),
				inferEqualConstantsClasses(createInitialFacts(query, schema)),
				Utility.inferInferred(createInitialFacts(query, schema)),
				Utility.inferDerivedInferred(),
				Utility.inferSignatureGroups(createInitialFacts(query, schema)),
				Utility.inferAccessibleTerms(createInitialFacts(query, schema)));
		this.manager.addFacts(facts);
	}

	/**
	 * Creates the initial facts.
	 *
	 * @param query the query
	 * @param schema the schema
	 * @return 		the facts of the canonical query and the Accessible(.) facts of the schema constants
	 */
	private static Collection<Atom> createInitialFacts(ConjunctiveQuery query, Schema schema) {
		// Gets the canonical database of the query
		Collection<Atom> facts = query.ground(query.getGrounding()).getAtoms();
		// Create the Accessible(.) facts
		// One Accessible(.) is being created for every schema constant
		for (TypedConstant<?> constant : query.getSchemaConstants()) {
			facts.add(AccessibleRelation.getAccessibleFact(constant));
		}
		for (TypedConstant<?> constant:schema.getDependencyConstants()) {
			facts.add(AccessibleRelation.getAccessibleFact(constant));
		}
		return facts;
	}


	/**
	 * Instantiates a new accessible database list state.
	 *
	 * @param manager the manager
	 * @param facts the facts
	 * @param graph the graph
	 * @param constantClasses the constant classes
	 * @param inferred the inferred
	 * @param derivedInferred the derived inferred
	 * @param signatureGroups the signature groups
	 * @param accessibleTerms the accessible terms
	 */
	private AccessibleDatabaseListState(
			DBHomomorphismManager manager,
			Collection<Atom> facts,
			FiringGraph graph,
			EqualConstantsClasses constantClasses,
			Collection<String> inferred,
			Collection<Atom> derivedInferred,
			Multimap<Predicate, Atom> signatureGroups,
			Multimap<Term,Atom> accessibleTerms
			) {
		super(manager, facts, graph, constantClasses);
		this.inferred = inferred;
		this.derivedInferred = derivedInferred;
		this.signatureGroups = signatureGroups;
		this.accessibleTerms = accessibleTerms;
	}

	/**
	 * Gets the inferred.
	 *
	 * @return Collection<String>
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState#getInferred()
	 */
	@Override
	public Collection<String> getInferred() {
		return this.inferred;
	}

	/**
	 * Gets the derived inferred.
	 *
	 * @return Collection<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState#getDerivedInferred()
	 */
	@Override
	public Collection<Atom> getDerivedInferred() {
		return this.derivedInferred;
	}

	/**
	 * Gets the signature groups.
	 *
	 * @return Multimap<Predicate,PredicateFormula>
	 */
	protected Multimap<Predicate, Atom> getSignatureGroups() {
		return this.signatureGroups;
	}

	/**
	 * Gets the accessible terms.
	 *
	 * @return Multimap<Term,PredicateFormula>
	 */
	protected Multimap<Term,Atom> getAccessibleTerms() {
		return this.accessibleTerms;
	}

	/**
	 * Updates this.state
	 *
	 * @param match the match
	 * @return true, if successful
	 */
	@Override
	public boolean chaseStep(Match match) {
		return this.chaseStep(Sets.newHashSet(match));
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseListState#chaseStep(java.util.Collection)
	 */
	@Override
	public boolean chaseStep(Collection<Match> matches) {
		super.chaseStep(matches);
		for(Match match:matches) {
			//The dependency to fire
			Constraint dependency = (Constraint) match.getQuery();
			Map<Variable, Constant> mapping = match.getMapping();
			//The grounded left-hand side of the input dependency
			Constraint grounded = dependency.fire(mapping, this.canonicalNames);
			//The grounded right-hand side of the input dependency
			Formula right = grounded.getRight();
			for(Atom fact:right.getAtoms()) {
				if(fact.getPredicate() instanceof InferredAccessibleRelation) {
					this.derivedInferred.add(fact);
				}
				if (!(fact.getPredicate() instanceof AccessibleRelation) && 
						!(fact.getPredicate() instanceof InferredAccessibleRelation)) {
					this.signatureGroups.put(fact.getPredicate(), fact);
				}
				if (fact.getPredicate() instanceof AccessibleRelation) {
					this.accessibleTerms.put(fact.getTerm(0), fact);
				}
				if (fact.getPredicate() instanceof InferredAccessibleRelation) {
					this.inferred.add(fact.toString());
				}
			}
		}
		return true;
	}

	/**
	 * For each input accessibility axiom, it
	 * groups the corresponding state facts based on the chase constants assigned to their input positions.
	 * This function is called when creating the initial ApplyRule configurations
	 *
	 * @param axioms the axioms
	 * @return 		pairs of accessibility axioms to chase facts
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#groupByBinding(Collection<AccessibilityAxiom>)
	 */
	@Override
	public List<Pair<AccessibilityAxiom, Collection<Atom>>> groupByBinding(Collection<AccessibilityAxiom> axioms) {
		return new Utility().groupByBinding(axioms, this.getSignatureGroups());
	}

	/**
	 * Gets the unexposed facts.
	 *
	 * @param accessibleSchema the accessible schema
	 * @return 		the unexposed facts and information to expose them
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#getUnExposedFacts(AccessibleSchema, Collection<AccessibilityAxiom>)
	 */
	@Override
	public Map<AccessibilityAxiom, List<Match>> getUnexposedFacts(AccessibleSchema accessibleSchema) {
		return this.getUnexposedFacts(accessibleSchema, this.getSignatureGroups(), this.getAccessibleTerms(), this.getFiringGraph());
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState#generate(uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema, uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom, java.util.Collection)
	 */
	@Override
	public void generate(AccessibleSchema schema, AccessibilityAxiom axiom, Collection<Atom> facts) {
		Collection<Atom> generatedFacts = new Utility().generateFacts(schema, axiom, facts, this.getInferred(), this.getDerivedInferred(), this.getFiringGraph());
		this.addFacts(generatedFacts);
	}


	/**
	 * Gets the unexposed facts.
	 *
	 * @param accessibleSchema the accessible schema
	 * @param signatureGroups 		Maps each schema signature (relation) to its chase facts
	 * @param accessibleTerms 		Maps each chase constant the Accessed facts it appears
	 * @param graph the graph
	 * @return 		the non-fired accessibility axioms of the state and information to fire them
	 */
	private Map<AccessibilityAxiom, List<Match>> getUnexposedFacts(
			AccessibleSchema accessibleSchema,
			Multimap<Predicate, Atom> signatureGroups,
			Multimap<Term,Atom> accessibleTerms,
			FiringGraph graph) {
		Map<AccessibilityAxiom, List<Match>> ret = new LinkedHashMap<>();
		List<Pair<AccessibilityAxiom,Collection<Atom>>> groups = 
				new Utility().groupByBinding(accessibleSchema.getAccessibilityAxioms(), signatureGroups);
		for(Pair<AccessibilityAxiom, Collection<Atom>> pair: groups) {
			AccessibilityAxiom axiom = pair.getLeft();
			Iterator<Atom> iterator = pair.getRight().iterator();
			while (iterator.hasNext()) {
				Atom fact = iterator.next();
				Atom accessedFact = new Atom(accessibleSchema.getInferredAccessibleRelation((Relation) fact.getPredicate()), fact.getTerms());
				Collection<Term> inputTerms = accessedFact.getTerms(axiom.getAccessMethod().getZeroBasedInputs());
				if(graph.getFactProvenance(accessedFact) == null && accessibleTerms.keySet().containsAll(inputTerms)) {
					Match matching = MatchFactory.getMatch(pair.getLeft(), fact);
					List<Match> matchings = ret.get(pair.getLeft());
					if(matchings == null) {
						ret.put(axiom, Lists.newArrayList(matching));
					}
					else {
						matchings.add(matching);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Gets the provenance.
	 *
	 * @return Map<PredicateFormula,Pair<Constraint,Collection<PredicateFormula>>>
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getProvenance()
	 */
	@Override
	public Map<Atom, Pair<Constraint, Collection<Atom>>> getProvenance() {
		return this.getFiringGraph().getFactProvenance();
	}


	/**
	 * Gets the provenance.
	 *
	 * @param fact PredicateFormula
	 * @return Pair<Constraint,Collection<PredicateFormula>>
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getProvenance(Atom)
	 */
	@Override
	public Pair<Constraint, Collection<Atom>> getProvenance(Atom fact) {
		return this.getFiringGraph().getFactProvenance(fact);
	}

	/**
	 * Clone.
	 *
	 * @return DatabaseListState
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState#clone()
	 */
	@Override
	public AccessibleDatabaseListState clone() {
		return new AccessibleDatabaseListState(this.manager, Sets.newHashSet(this.facts), 
				this.graph.clone(),
				this.constantClasses.clone(),
				new LinkedHashSet<>(this.inferred),
				new LinkedHashSet<>(this.derivedInferred), 
				LinkedHashMultimap.create(this.signatureGroups), 
				LinkedHashMultimap.create(this.accessibleTerms));
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState#merge(uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState)
	 */
	@Override
	public AccessibleChaseState merge(AccessibleChaseState s) {
		Preconditions.checkState(s instanceof AccessibleDatabaseListState);
		Collection<Atom> facts =  new LinkedHashSet<>(this.facts);
		facts.addAll(s.getFacts());
		
		Collection<String> inferred = CollectionUtils.union(this.inferred, ((AccessibleDatabaseListState)s).inferred);
		Collection<Atom> derivedInferred = CollectionUtils.union(this.derivedInferred, ((AccessibleDatabaseListState)s).derivedInferred);
		Multimap<Predicate, Atom> signatureGroups = LinkedHashMultimap.create(this.signatureGroups);
		signatureGroups.putAll(((AccessibleDatabaseListState)s).signatureGroups);
		Multimap<Term,Atom> accessibleTerms = LinkedHashMultimap.create(this.accessibleTerms);
		accessibleTerms.putAll(((AccessibleDatabaseListState)s).accessibleTerms);
		
		EqualConstantsClasses classes = this.constantClasses.clone();
		if(!classes.merge(((DatabaseChaseListState)s).getConstantClasses())) {
			return null;
		}
		return new AccessibleDatabaseListState(
				this.getManager(),
				facts, 
				this.getFiringGraph().merge(s.getFiringGraph()),
				classes,
				inferred,
				derivedInferred, 
				signatureGroups, 
				accessibleTerms);
	}
}
