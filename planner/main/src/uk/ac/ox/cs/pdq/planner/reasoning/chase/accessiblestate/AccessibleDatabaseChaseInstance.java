package uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleQuery;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.plantree.FiringGraph;
import uk.ac.ox.cs.pdq.planner.linear.plantree.MapFiringGraph;
import uk.ac.ox.cs.pdq.planner.reasoning.MatchFactory;
import uk.ac.ox.cs.pdq.reasoning.chase.schemaconstantequality.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;

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
public class AccessibleDatabaseChaseInstance extends uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance implements AccessibleChaseInstance {

	/**  The firings that took place in this state. */
	protected FiringGraph graph;
	
	/**  String signatures of
	 *  the inferred accessible facts. */
	private final Collection<Atom> inferredAccessibleAtoms;

	/**  Maps each schema signature (relation) to its chase facts. */
	private final Multimap<Predicate, Atom> atomsMap;

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
	public AccessibleDatabaseChaseInstance(ConjunctiveQuery query, Schema schema, DatabaseManager connection, boolean maintainProvenance) throws SQLException {
		this(
				createCanonicalDatabaseAndAccessibleFactsForSchemaConstants(query, schema), 
				maintainProvenance == true ? new MapFiringGraph() : null,
				new EqualConstantsClasses(),
				DatabaseChaseInstance.createdConstantsMap(createCanonicalDatabaseAndAccessibleFactsForSchemaConstants(query, schema)),
				AccessibleQuery.getInferredAccessibleAtoms(createCanonicalDatabaseAndAccessibleFactsForSchemaConstants(query, schema)),
				AccessibleQuery.createAtomsMap(createCanonicalDatabaseAndAccessibleFactsForSchemaConstants(query, schema)),
				AccessibleQuery.getAllTermsAppearingInAccessibleFacts(createCanonicalDatabaseAndAccessibleFactsForSchemaConstants(query, schema)),
				connection);

	}
	
	public AccessibleDatabaseChaseInstance(Collection<Atom> facts, DatabaseManager connection, boolean maintainProvenance) throws SQLException {
		this(	facts, 
				maintainProvenance == true ? new MapFiringGraph() : null,
				new EqualConstantsClasses(),
				DatabaseChaseInstance.createdConstantsMap(facts),
				AccessibleQuery.getInferredAccessibleAtoms(facts),
				AccessibleQuery.createAtomsMap(facts),
				AccessibleQuery.getAllTermsAppearingInAccessibleFacts(facts),
				connection);
		this.addFacts(facts);
	}

	/**
	 * Creates the initial facts.
	 *
	 * @param query the query
	 * @param schema the schema
	 * @return 		the facts of the canonical query and the Accessible(.) facts of the schema constants
	 */
	private static Collection<Atom> createCanonicalDatabaseAndAccessibleFactsForSchemaConstants(ConjunctiveQuery query, Schema schema) {
		// Gets the canonical database of the query
		List<Atom> facts = new ArrayList<>(); 
		facts.addAll(Arrays.asList(uk.ac.ox.cs.pdq.fol.Formula.applySubstitution(query, ExplorationSetUp.getCanonicalSubstitution().get(query)).getAtoms()));
		// Create the Accessible(.) facts
		// One Accessible(.) is being created for every schema constant
		for (TypedConstant constant:query.getTypedConstants()) 
			facts.add(Atom.create(AccessibleSchema.accessibleRelation, constant));
		return facts;
	}
	
	/**
	 * Instantiates a new accessible database list state.
	 *
	 * @param chaseState the chaseState
	 * @param facts the facts
	 * @param graph the graph
	 * @param constantClasses the constant classes
	 * @param inferredAccessibleAtoms the inferred
	 * @param derivedInferredAccessibleAtoms the derived inferred
	 * @param atomsMap the signature groups
	 * @param accessibleTerms the accessible terms
	 * @throws SQLException 
	 */
	private AccessibleDatabaseChaseInstance(
			Collection<Atom> facts,
			FiringGraph graph,
			EqualConstantsClasses constantClasses,
			Multimap<Constant,Atom> constants,
			Collection<Atom> inferredAccessibleAtoms,
			Multimap<Predicate, Atom> atomsMap,
			Multimap<Term,Atom> accessibleTerms, 
			DatabaseManager connection
			) throws SQLException {
		super(facts, constantClasses, constants, connection);
		
		Preconditions.checkNotNull(inferredAccessibleAtoms);
		Preconditions.checkNotNull(atomsMap);
		Preconditions.checkNotNull(accessibleTerms);
		this.graph = graph;
		this.inferredAccessibleAtoms = inferredAccessibleAtoms;
		this.atomsMap = atomsMap;
		this.accessibleTerms = accessibleTerms;
	}

	/**
	 * Gets the inferred.
	 *
	 * @return Collection<String>
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance#getInferredAccessibleFacts()
	 */
	@Override
	public Collection<Atom> getInferredAccessibleFacts() {
		return this.inferredAccessibleAtoms;
	}

	/**
	 *
	 * @return Multimap<Term,PredicateFormula>
	 */
	protected Multimap<Term,Atom> getAccessibleTerms() {
		return this.accessibleTerms;
	}

	/* 
	 * This method applies chase steps and keeps facts derivation information. 
	 * This information is later used for postpruning linear plans (see classes uk.ac.ox.cs.pdq.planner.linear.explorer.pruning.PostPruning).  
	 * Recall that liner plan postpruning is done in the uk.ac.ox.cs.pdq.planner.linear.explorer.LinearOptimized class, but not in the
	 * uk.ac.ox.cs.pdq.planner.linear.explorer.LinearGeneric class. 
	 * 
	 * The input to the chaseStep method is a collection of matches, where each match consists a formula and a mapping of variables to constants. 
	 * This method was implemented for reasoning with the dependencies of the accessible schema, i.e.,  
	 * accessibility axioms and inferred accessible copies of dependencies.
	 * Currently it does not support reasoning with inferred accessible copies of EGDs.   
	 */
	@Override
	public boolean chaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Atom> newFacts = new LinkedHashSet<>();
		for(Match match:matches) {
			Dependency dependency = (Dependency) match.getFormula();
			Preconditions.checkArgument(dependency instanceof TGD, "EGDs are not allowed inside TGDchaseStep");
			Map<Variable, Constant> mapping = match.getMapping();
			Implication grounded = DatabaseChaseInstance.ground(dependency, mapping, true);
			Formula left = grounded.getChild(0);
			Formula right = grounded.getChild(1);
			for(Atom fact:right.getAtoms()) {
				if(fact.getPredicate().getName().startsWith(AccessibleSchema.inferredAccessiblePrefix)) {
					this.inferredAccessibleAtoms.add(fact);
				}
				else if (fact.getPredicate().getName().equals(AccessibleSchema.accessibleRelation.getName())) 
					this.accessibleTerms.put(fact.getTerm(0), fact);
				else 
					this.atomsMap.put(fact.getPredicate(), fact);
			}
			newFacts.addAll(Arrays.asList(right.getAtoms()));
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
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance#groupFactsByAccessMethods(AccessibilityAxiom[] axioms)
	 * 
	 * Gabor: Not exactly grouping by access methods, rather grouping by unique sets of inputs. 
	 * So for an access method that has no input it will create one group that contains all. 
	 * For an access method that has one input it will create as many groups as many different value exists for that parameter. 
	 * When there are multiple inputs it will create groups for each unique input configuration. 
	 */
	public List<Pair<AccessibilityAxiom, Collection<Atom>>> groupFactsByAccessMethods(AccessibilityAxiom[] axioms) {
		return AccessibleQuery.groupFactsByAccessMethods(axioms, this.atomsMap);
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
		return this.getUnexposedFacts(accessibleSchema, this.atomsMap, this.accessibleTerms, this.getFacts());
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState#generate(uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema, uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom, java.util.Collection)
	 */
	@Override
	public void generate(AccessibilityAxiom axiom, Collection<Atom> facts) {

		Collection<Atom> createdFacts = new LinkedHashSet<>();
		for(Atom fact:facts) {			
			Atom accessedFact = Atom.create(fact.getPredicate(), fact.getTerms());
			createdFacts.add(accessedFact);
			Predicate predicate = null;
				predicate = Predicate.create(AccessibleSchema.inferredAccessiblePrefix + fact.getPredicate().getName(), fact.getPredicate().getArity());
			Atom inferredAccessibleFact = Atom.create(predicate, fact.getTerms());
			createdFacts.add(inferredAccessibleFact);
			this.inferredAccessibleAtoms.add(inferredAccessibleFact);
			if(this.graph != null) 
				this.graph.put(axiom, accessedFact, inferredAccessibleFact);
			for(Term term:fact.getTerms()) 
				createdFacts.add(Atom.create(AccessibleSchema.accessibleRelation, term));
		}
		this.addFacts(createdFacts);
	}


	/**
	 *
	 * @param accessibleSchema the accessible schema
	 * @param atomsMap 		Maps each schema signature (relation) to its chase facts
	 * @param accessibleTerms 		Maps each chase constant the Accessed facts it appears
	 * @param facts the graphs
	 * @return 		the non-fired accessibility axioms of the state and information to fire them
	 */
	private Map<AccessibilityAxiom, List<Match>> getUnexposedFacts(
			AccessibleSchema accessibleSchema,
			Multimap<Predicate, Atom> atomsMap,
			Multimap<Term,Atom> accessibleTerms,
			Collection<Atom> facts) {
		Map<AccessibilityAxiom, List<Match>> ret = new LinkedHashMap<>();
		List<Pair<AccessibilityAxiom,Collection<Atom>>> groups = 
				AccessibleQuery.groupFactsByAccessMethods(accessibleSchema.getAccessibilityAxioms(), atomsMap);
		for(Pair<AccessibilityAxiom, Collection<Atom>> pair: groups) {
			AccessibilityAxiom axiom = pair.getLeft();
			Iterator<Atom> iterator = pair.getRight().iterator();
			while (iterator.hasNext()) {
				Atom fact = iterator.next();
				Predicate predicate =  Predicate.create(AccessibleSchema.inferredAccessiblePrefix + fact.getPredicate().getName(), fact.getPredicate().getArity());
				Atom accessedFact = Atom.create(predicate, fact.getTerms());
				Collection<Term> inputTerms = accessedFact.getTerms(axiom.getAccessMethod().getInputs());
				if(!facts.contains(accessedFact) && accessibleTerms.keySet().containsAll(inputTerms)) {
					Match match = MatchFactory.createMatchForAccessibilityAxiom(pair.getLeft(), fact);
					List<Match> matches = ret.get(pair.getLeft());
					if(matches == null) 
						ret.put(axiom, Lists.newArrayList(match));
					else 
						matches.add(match);
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
		if(this.graph == null) 
			return null;
		return this.graph.getFactProvenance();
	}


	/**
	 *
	 * @param fact PredicateFormula
	 * @return Pair<Constraint,Collection<PredicateFormula>>
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#getProvenance(Atom)
	 */
	@Override
	public Pair<Dependency, Collection<Atom>> getProvenance(Atom fact) {
		if(this.graph == null)
			return null;
		return this.graph.getFactProvenance(fact);
	}

	/**
	 *
	 * @return DatabaseListState
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance#clone()
	 */
	@Override
	public AccessibleDatabaseChaseInstance clone() {
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		constantsToAtoms.putAll(this.constantsToAtoms);
		try {
			return new AccessibleDatabaseChaseInstance(
					Sets.newHashSet(this.getFacts()), 
					this.graph == null ? null : this.graph.clone(),
					this.classes.clone(),
					constantsToAtoms,
					new LinkedHashSet<>(this.inferredAccessibleAtoms),
					LinkedHashMultimap.create(this.atomsMap), 
					LinkedHashMultimap.create(this.accessibleTerms),databaseInstance);
		} catch (SQLException e) {
			throw new RuntimeException("Cloning of AccessibleDatabaseListState failed due to an SQL exception "+e);
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState#merge(uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState)
	 */
	@Override
	public AccessibleChaseInstance merge(AccessibleChaseInstance s) {
		Preconditions.checkState(s instanceof AccessibleDatabaseChaseInstance);
		Collection<Atom> facts =  new LinkedHashSet<>(this.getFacts());
		facts.addAll(s.getFacts());
		
		Collection<Atom> inferred = CollectionUtils.union(this.inferredAccessibleAtoms, ((AccessibleDatabaseChaseInstance)s).inferredAccessibleAtoms);
		Multimap<Predicate, Atom> map = LinkedHashMultimap.create(this.atomsMap);
		map.putAll(((AccessibleDatabaseChaseInstance)s).atomsMap);
		Multimap<Term,Atom> accessibleTerms = LinkedHashMultimap.create(this.accessibleTerms);
		accessibleTerms.putAll(((AccessibleDatabaseChaseInstance)s).accessibleTerms);
		
		EqualConstantsClasses classes = this.classes.clone();
		if(!classes.merge(((DatabaseChaseInstance)s).getConstantClasses())) {
			return null;
		}
		
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		constantsToAtoms.putAll(this.constantsToAtoms);
		constantsToAtoms.putAll(((AccessibleDatabaseChaseInstance)s).constantsToAtoms);

		try {
			AccessibleDatabaseChaseInstance ret = new AccessibleDatabaseChaseInstance(
					facts, 
					this.graph == null ? null : this.graph.merge(((AccessibleDatabaseChaseInstance)s).graph),
					classes,
					constantsToAtoms,
					inferred,
					map, 
					accessibleTerms,
					databaseInstance);
			ret.addFacts(facts);
			return ret;
		} catch (SQLException e) {
				throw new RuntimeException("Merging of AccessibleDatabaseListState failed due to an SQL exception "+e);
			}
	}

}
