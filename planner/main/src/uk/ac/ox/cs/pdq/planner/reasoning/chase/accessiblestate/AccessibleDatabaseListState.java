package uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.MatchFactory;
import uk.ac.ox.cs.pdq.planner.util.FiringGraph;
import uk.ac.ox.cs.pdq.planner.util.MapFiringGraph;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
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
public class AccessibleDatabaseListState extends uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance implements AccessibleChaseState {

	/**  The firings that took place in this state. */
	protected FiringGraph graph;
	
	/**  String signatures of
	 *  the inferred accessible facts. */
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
	 * @param chaseState the chaseState
	 * @throws SQLException 
	 */
	public AccessibleDatabaseListState(ReasoningParameters resParam,ConjunctiveQuery query, Schema schema, DatabaseConnection connection, boolean maintainProvenance) throws SQLException {
		this(
				createInitialFacts(query, schema), 
				maintainProvenance == true ? new MapFiringGraph() : null,
				new EqualConstantsClasses(),
				createdConstantsMap(createInitialFacts(query, schema)),
				Utility.inferInferred(createInitialFacts(query, schema)),
				Utility.inferDerivedInferred(),
				Utility.inferSignatureGroups(createInitialFacts(query, schema)),
				Utility.inferAccessibleTerms(createInitialFacts(query, schema)),
				connection);
		this.addFacts(this.facts);
	}
	
	public AccessibleDatabaseListState(ReasoningParameters resParam, Collection<Atom> facts, DatabaseConnection connection, boolean maintainProvenance) throws SQLException {
		this(	facts, 
				maintainProvenance == true ? new MapFiringGraph() : null,
				new EqualConstantsClasses(),
				createdConstantsMap(facts),
				Utility.inferInferred(facts),
				Utility.inferDerivedInferred(),
				Utility.inferSignatureGroups(facts),
				Utility.inferAccessibleTerms(facts),
				connection);
		this.addFacts(this.facts);
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
		Collection<Atom> facts = uk.ac.ox.cs.pdq.reasoning.chase.Utility.applySubstitution(query, query.getSubstitutionToCanonicalConstants()).getAtoms();
		// Create the Accessible(.) facts
		// One Accessible(.) is being created for every schema constant
		for (TypedConstant<?> constant : uk.ac.ox.cs.pdq.util.Utility.getTypedConstants(query)) {
			facts.add(AccessibleRelation.getAccessibleFact(constant));
		}
		for (TypedConstant<?> constant:schema.getDependencyTypedConstants()) {
			facts.add(AccessibleRelation.getAccessibleFact(constant));
		}
		return facts;
	}
	
	/**
	 * Instantiates a new accessible database list state.
	 *
	 * @param chaseState the chaseState
	 * @param facts the facts
	 * @param graph the graph
	 * @param constantClasses the constant classes
	 * @param inferred the inferred
	 * @param derivedInferred the derived inferred
	 * @param signatureGroups the signature groups
	 * @param accessibleTerms the accessible terms
	 * @throws SQLException 
	 */
	private AccessibleDatabaseListState(
			Collection<Atom> facts,
			FiringGraph graph,
			EqualConstantsClasses constantClasses,
			Multimap<Constant,Atom> constants,
			Collection<String> inferred,
			Collection<Atom> derivedInferred,
			Multimap<Predicate, Atom> signatureGroups,
			Multimap<Term,Atom> accessibleTerms, 
			DatabaseConnection connection
			) throws SQLException {
		super(facts, constantClasses, constants, connection);
		
		Preconditions.checkNotNull(inferred);
		Preconditions.checkNotNull(derivedInferred);
		Preconditions.checkNotNull(signatureGroups);
		Preconditions.checkNotNull(accessibleTerms);
		this.graph = graph;
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
		Preconditions.checkNotNull(matches);
		Collection<Atom> newFacts = new LinkedHashSet<>();
		for(Match match:matches) {
			Dependency dependency = (Dependency) match.getQuery();
			Preconditions.checkArgument(dependency instanceof TGD, "EGDs are not allowed inside TGDchaseStep");
			Map<Variable, Constant> mapping = match.getMapping();
			Implication grounded = uk.ac.ox.cs.pdq.reasoning.chase.Utility.fire(dependency, mapping, true);
			Formula left = grounded.getChildren().get(0);
			Formula right = grounded.getChildren().get(1);
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
			newFacts.addAll(right.getAtoms());
			if(this.graph != null) {
				//Update the provenance of facts
				this.graph.put(dependency, Sets.newHashSet(left.getAtoms()), Sets.newHashSet(right.getAtoms()));
			}
		}
		//Add the newly created facts to the database
		this.addFacts(newFacts);
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
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#getProvenance()
	 */
	@Override
	public Map<Atom, Pair<Dependency, Collection<Atom>>> getProvenance() {
		if(this.graph == null) {
			return null;
		}
		return this.getFiringGraph().getFactProvenance();
	}


	/**
	 * Gets the provenance.
	 *
	 * @param fact PredicateFormula
	 * @return Pair<Constraint,Collection<PredicateFormula>>
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#getProvenance(Atom)
	 */
	@Override
	public Pair<Dependency, Collection<Atom>> getProvenance(Atom fact) {
		if(this.graph == null) {
			return null;
		}
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
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		constantsToAtoms.putAll(this.constantsToAtoms);
		try {
			return new AccessibleDatabaseListState(
					Sets.newHashSet(this.facts), 
					this.graph == null ? null : this.graph.clone(),
					this.classes.clone(),
					constantsToAtoms,
					new LinkedHashSet<>(this.inferred),
					new LinkedHashSet<>(this.derivedInferred), 
					LinkedHashMultimap.create(this.signatureGroups), 
					LinkedHashMultimap.create(this.accessibleTerms),this.getDatabaseConnection());
		} catch (SQLException e) {
			throw new RuntimeException("Cloning of AccessibleDatabaseListState failed due to an SQL exception "+e);
		}
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
		
		EqualConstantsClasses classes = this.classes.clone();
		if(!classes.merge(((DatabaseChaseInstance)s).getConstantClasses())) {
			return null;
		}
		
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		constantsToAtoms.putAll(this.constantsToAtoms);
		constantsToAtoms.putAll(((AccessibleDatabaseListState)s).getConstantsToAtoms());

		try {
			AccessibleDatabaseListState ret = new AccessibleDatabaseListState(
					facts, 
					this.getFiringGraph() == null ? null : this.getFiringGraph().merge(((AccessibleDatabaseListState)s).getFiringGraph()),
					classes,
					constantsToAtoms,
					inferred,
					derivedInferred, 
					signatureGroups, 
					accessibleTerms,
					this.getDatabaseConnection());
			ret.addFacts(facts);
			return ret;
		} catch (SQLException e) {
				throw new RuntimeException("Merging of AccessibleDatabaseListState failed due to an SQL exception "+e);
			}
	}
	
	//TOCOMMENT: verify that this is constructed correrctly
	public FiringGraph getFiringGraph() {
		return this.graph;
	}
}
