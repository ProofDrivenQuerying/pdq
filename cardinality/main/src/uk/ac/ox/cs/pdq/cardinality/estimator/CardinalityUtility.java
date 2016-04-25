/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.estimator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.dag.UnaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseListState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


// TODO: Auto-generated Javadoc
/**
 * The Class CardinalityUtility.
 *
 * @author Efthymia Tsamoura
 */
public class CardinalityUtility {

	/** The log. */
	protected static Logger log = Logger.getLogger(CardinalityUtility.class);
	

	/**
	 * Determines whether the attributes corresponding to
	 * constants in keys form a key in the plan of AnnPlan. We decide this by adding
	 * to the fact annotations in AnnPlan a copy of all fact annotations containing some
	 * constant in keys, with any constant c in keys being renamed to a fresh copy c′. We then
	 * chase with the constraints and see if for every c_i \in keys′, c_i = c′ i is derived.
	 *
	 * @param keys 		Input candidate keys
	 * @param configuration the configuration
	 * @param egd 		Runs the EGD chasing algorithm 
	 * @param detector 		Detects homomorphisms during chasing
	 * @param dependencies 		Dependencies to consider during chasing
	 * @return 		true if the input collection of constants is a key for the input annotated plan
	 */
	public static boolean isKey(Collection<Constant> keys, DAGAnnotatedPlan configuration, Chaser egd, HomomorphismDetector detector, Collection<? extends Dependency> dependencies) {
		Preconditions.checkNotNull(keys);
		Preconditions.checkArgument(!keys.isEmpty());

		//Resort to base tables for unary annotated plans
		if(configuration instanceof UnaryAnnotatedPlan) {
			List<Integer> keyPositions = Lists.newArrayList();
			Relation relation = ((UnaryAnnotatedPlan)configuration).getRelation();
			for(Constant key:keys) {
				keyPositions.addAll(Utility.search(((UnaryAnnotatedPlan) configuration).getFact().getTerms(), key));
			}
			if(!relation.getKeyPositions().isEmpty() && keyPositions.containsAll(relation.getKeyPositions())) {
				return true;
			}
		}	

		//Map to each constant in keys a new name
		Map<Constant,Skolem> map = Maps.newHashMap();

		//Create a copy of the facts in the input configuration with the renamed constants
		Collection<Atom> copiedFacts = Sets.newHashSet();
		for(Atom fact:configuration.getOutputFacts()) {
			List<Term> copiedTerms = Lists.newArrayList();
			for(Term originalTerm:fact.getTerms()) {
				if(originalTerm instanceof Skolem && !keys.contains(originalTerm) && map.get(originalTerm) == null) {
					map.put((Constant) originalTerm, new Skolem("?" + ((Skolem) originalTerm).getName()));
				}
				if(map.get(originalTerm) != null) {
					copiedTerms.add(map.get(originalTerm));
				}
				else {
					copiedTerms.add(originalTerm);
				}
			}
			copiedFacts.add(new Atom(fact.getPredicate(), copiedTerms));
		}

		DatabaseChaseListState state = new DatabaseChaseListState((DatabaseHomomorphismManager)detector, Sets.newLinkedHashSet(CollectionUtils.union(configuration.getOutputFacts(), copiedFacts)));
		egd.reasonUntilTermination(state, dependencies);

		LinkedHashSet<Atom> toDelete = Sets.newLinkedHashSet(CollectionUtils.union(copiedFacts, state.getFacts()));
		toDelete.removeAll(configuration.getOutputFacts());
		((DatabaseHomomorphismManager)detector).deleteFacts(Sets.newLinkedHashSet(toDelete));
		
		if(state.isFailed()) {
			log.trace(keys + " IS NOT KEY FOR " + configuration);
			return false;
		}
		else {
			EqualConstantsClasses classes = state.getConstantClasses();
			if(classes.size() == 0) {
				return false;
			}
			else {
				for(Entry<Constant, Skolem> entry:map.entrySet()) {
					if(classes.getClass(entry.getKey()) == null ||
						classes.getClass(entry.getValue()) == null ||
						!classes.getClass(entry.getKey()).equals(classes.getClass(entry.getValue()))) {
						log.trace(keys + " IS NOT KEY FOR " + configuration);
						return false;
					}
				}
				log.trace(keys + " IS KEY FOR " + configuration);
				return true;	
			}
		}
	}

	/**
	 * Determines whether there is an “inclusion dependency of left into
	 * right on constants�?. It holds if there is a mapping of the non-schema constants
	 * of right to the non-schema constants of left that is the identity on constants
	 * and which preserves all fact annotations of right.
	 *
	 * @param constants the constants
	 * @param source the source
	 * @param target the target
	 * @param detector 		Detects homomorphisms
	 * @return 		true if there is an inclusion dependency of left into right on constants
	 */
	public static boolean hasID(Collection<Constant> constants, DAGAnnotatedPlan source, DAGAnnotatedPlan target, HomomorphismDetector detector) {
		Preconditions.checkNotNull(constants);
		Preconditions.checkArgument(!constants.isEmpty());

		//Create a query from the left's configuration explicit facts
		Map<Constant,Variable> queryMap = Maps.newHashMap();
		int index = 0;
		for(Term term:target.getExportedConstants()) {
			if(term instanceof Skolem && !queryMap.containsKey(term)) {
				queryMap.put((Constant) term, new Variable("x"+index));
			}
			++index;
		}
		
		List<Atom> queryAtoms = Lists.newArrayList();
		for(UnaryAnnotatedPlan unary:target.getUnaryAnnotatedPlans()) {
			Atom fact = unary.getFact();
			List<Term> newTerms = Lists.newArrayList();
			for(Term term:fact.getTerms()) {
				if(term instanceof Skolem) {
					newTerms.add(queryMap.get(term));
				}
				else {
					newTerms.add(term);
				}
			}
			queryAtoms.add(new Atom(fact.getPredicate(), newTerms));
		}
		
		
		Map<Variable, Constant> mapConstraint = Maps.newHashMap();
		List<Term> headTerms = Lists.newArrayList();
		for(Constant constant:constants) {
			if(queryMap.get(constant) == null) {
				log.trace("The constant " + constant + " is not a representative of its class. Skip this case."
						+ " " + source + " " + target + " " + constants);
				return false;
			}
			headTerms.add(queryMap.get(constant));
			mapConstraint.put(queryMap.get(constant), constant);
		}
		Atom head = new Atom(new Predicate("Q", constants.size()), headTerms);
		ConjunctiveQuery query = new ConjunctiveQuery(head, Conjunction.of(queryAtoms));

		//Create homomorphism constraints that preserve the input constants
		if(source.getState() instanceof DatabaseChaseState) {
			((DatabaseChaseState)source.getState()).setManager((DatabaseHomomorphismManager) detector);
		}
		
		HomomorphismProperty[] c = new HomomorphismProperty[2];
		c[0] = HomomorphismProperty.createMapProperty(mapConstraint);
		c[1] = HomomorphismProperty.createTopKProperty(1);
		boolean hasID = !source.getState().getMatches(query, c).isEmpty();
		
		log.trace("hasID " + source + "-->" + target + " = " + hasID);
		return hasID;
	}
	
	/**
	 * 
	 * @param annotatedPlan
	 * @param constant
	 * @return
	 * 		the <annotatedPlan,attribute> pair with the highest quality for the given input constant
	 */
	protected static Pair<UnaryAnnotatedPlan, Attribute> getHighestQualityAnnotatedPlan(DAGAnnotatedPlan annotatedPlan, Constant constant) {
		double maxQuality = Double.MAX_VALUE;
		UnaryAnnotatedPlan maxQualityPlan = null;
		for(UnaryAnnotatedPlan unary:annotatedPlan.getUnaryAnnotatedPlans()) {
			if(unary.getOutput().contains(constant) && maxQuality > unary.getQuality()) {
				maxQuality = unary.getQuality();
				maxQualityPlan = unary;
			}
		}
		Relation relation = maxQualityPlan.getRelation();
		int indexOf = maxQualityPlan.getFact().getTerms().indexOf(constant);
		return Pair.of(maxQualityPlan, relation.getAttribute(indexOf));
	}
	
	/**
	 * @param configuration
	 * @return
	 * 		the size of the input atom
	 */
	public static BigInteger sizeOf(Atom fact, Catalog catalog) {
		Relation relation = (Relation)fact.getPredicate();
		int index = 0;
		//Estimate using the catalog the selectivity of the input selection predicates
		List<Double> selectivities = Lists.newArrayList();
		for(Term constant:fact.getTerms()) {
			if(constant instanceof TypedConstant) {
				selectivities.add(catalog.getSelectivity(relation, relation.getAttribute(index), (TypedConstant<?>) constant));
			}
			++index;
		}
		//Detect attribute equality predicates and use default attribute equality selectivities
		List<Term> terms = fact.getTerms();
		for(int i = 0; i < terms.size() - 1; ++i) {
			for(int j = i + 1; j < terms.size(); ++j) {
				Term first = terms.get(i);
				Term second = terms.get(j);
				if(first instanceof Skolem && second instanceof Skolem && first.equals(second)) {
					selectivities.add(SimpleCatalog.DEFAULT_ATTRIBUTE_EQUALITY_SELECTIVITY);
				}
			}
		}

		//Estimate the joint selectivity of the predicates using SQL 2014 server's formula.
		//The formula is described below (from SQL 2014 tutorial):
		//In an effort to assume some correlation, the new CE in SQL Server 2014 lessens the independence assumption slightly for conjunctions of predicates. 
		//The process used to model this correlation is called â€œexponential back-offâ€�. 
		//Note: â€œExponential back-offâ€� is used with disjunctions as well. The system calculates this value by first transforming disjunctions to a negation of conjunctions. 
		//The formula below represents the new CE computation of selectivity for a conjunction of predicates. 
		//â€œP0â€� represents the most selective predicate and is followed by the three most selective predicates:
		//p_0â‹…ã€–p_1ã€—^(1â�„2)â‹…ã€–p_2ã€—^(1â�„4)â‹…ã€–p_3ã€—^(1â�„8)
		//The new CE sorts predicates by selectivity and keeps the four most selective predicates for use in the calculation. 
		//The CE then â€œmoderatesâ€� each successive predicate by taking larger square roots. 
		if(!selectivities.isEmpty()) {
			Collections.sort(selectivities);
			double globalSelectivity = 1;
			for(int i = 0; i < Math.min(selectivities.size(), 4); ++i) {
				globalSelectivity *= Math.pow(selectivities.get(i), 1.0/(Math.pow(2, i)));
			}
			Preconditions.checkArgument((int)(globalSelectivity * catalog.getCardinality(relation)) > 0);
			return BigInteger.valueOf((long) (globalSelectivity * catalog.getCardinality(relation)));
		}
		Preconditions.checkArgument(catalog.getCardinality(relation) > 0);
		return BigInteger.valueOf(catalog.getCardinality(relation));
	}
	
	/**
	 * 
	 * @param configuration
	 * @param query
	 * @return
	 * 		an upper bound of the size of the query after projecting on its free variables.
	 * 		This estimate is derived after assigning each query free variable to a constant (attribute) in a unary annotated plan 
	 * 		and taking the product of the the cardinalities of the constants from the schema catalog. 
	 * 		The cardinality of each constant is reduced by the selectivity of the unary annotated plan it comes from,
	 * 		where the selectivity of the unary annotate plan is estimated as the size of the annotated plan to the size of the base relation.  
	 */
	public static BigInteger sizeOfProjectionIndependentEstimate(DAGAnnotatedPlan configuration, ConjunctiveQuery query, Catalog catalog) {
		if(configuration.getExportedConstants().containsAll(query.getGroundingsProjectionOnFreeVars().values())) {
			if(!query.getFree().isEmpty()) {
				BigDecimal cardinality = BigDecimal.ONE;
				//Estimate the cardinality of each attribute in the final projection of the input query 
				for(Constant constant:query.getGroundingsProjectionOnFreeVars().values()) {
					//Find the relation attribute pair with the highest quality that map to the projected constant  
					Pair<UnaryAnnotatedPlan, Attribute> pair = getHighestQualityAnnotatedPlan(configuration, constant);
					//Find the cardinality of the attribute that maps to the projected constant
					Relation baseRelation = pair.getLeft().getRelation();
					int baseCardinality = catalog.getCardinality(baseRelation, pair.getRight());

					double selectivity = pair.getLeft().getSize().doubleValue() / catalog.getCardinality(baseRelation);
					cardinality = cardinality.multiply(new BigDecimal(baseCardinality)).multiply(new BigDecimal(selectivity));

					log.trace("Configuration: " + configuration + " constant: " + constant + "\n" + 
							"Relation: " + pair.getLeft().getRelation() + " Attribute: " + pair.getRight() + "\n" + 
							"Base cardinality: " + baseCardinality + "\n" + 
							"Selectivity: " + selectivity + "\n" + 
							"Total cardinality: " + cardinality);
				}
				log.trace("Cardinality of configuration " + configuration + " " + cardinality);
				return cardinality.toBigInteger();
			}
			else {
				return configuration.getSize();
			}
		}
		else {
			return BigInteger.valueOf(Integer.MAX_VALUE);
		}
	}

	/**
	 * 
	 * @param configuration
	 * @param query
	 * @return
	 * 		an upper bound of the size of the query after projecting on its free variables.
	 * 		pick an A_i for each free variable of the query v_i, and then say
			that v_i == v_j if A_i = A_j. For each class C, let A_C be the corresponding atom. For each
			equivalence class C, let m_C be the maximum of |A_C| and \pi_{v_i \in C} SizeOf|\pi_{v_i} A_C|. That is,
			we can estimate \pi_{v_i} A_C by the minimum of these two quantities.
			We then estimate SizeOf(p_{\vec{v}}(AnnPlan)), where \vec{v} is the vector of query's free variables by \Pi_C m_C
	 * 
	 */
	public static BigInteger sizeOfProjectionGroupEstimate(DAGAnnotatedPlan configuration, ConjunctiveQuery query, Catalog catalog) {
		if(configuration.getExportedConstants().containsAll(query.getGroundingsProjectionOnFreeVars().values())) {
			if(!query.getFree().isEmpty()) {
				//Try to assign the maximum number of query's variables to a single annotated plan 

				//Keeps the unary annotated plans to query free variables assignments
				Map<UnaryAnnotatedPlan, Collection<Constant>> assignments = Maps.newHashMap();
				Collection<UnaryAnnotatedPlan> unaryPlans = Sets.newHashSet(configuration.getUnaryAnnotatedPlans());
				Collection<Constant> constants = Sets.newHashSet(query.getGroundingsProjectionOnFreeVars().values());
				Iterator<Constant> constantsIterator = constants.iterator();
				while(constantsIterator.hasNext()) {
					UnaryAnnotatedPlan maximalCover = null;
					Collection<Constant> maximallyCovered = Sets.newHashSet();
					//Find the query variables that are covered by the current annotated plan 
					Iterator<UnaryAnnotatedPlan> iterator = unaryPlans.iterator();
					while(iterator.hasNext()) {
						UnaryAnnotatedPlan unaryPlan = iterator.next();
						Collection<Constant> intersection = CollectionUtils.intersection(unaryPlan.getExportedConstants(), constants);	
						if(intersection.size() > maximallyCovered.size()) {
							maximallyCovered = intersection;
							maximalCover = unaryPlan;
						}
					}
					assignments.put(maximalCover, maximallyCovered);
					unaryPlans.remove(maximalCover);
					constants.removeAll(maximallyCovered);
					constantsIterator = constants.iterator();
				}

				BigInteger product = BigInteger.ONE;
				for(Entry<UnaryAnnotatedPlan, Collection<Constant>> entry:assignments.entrySet()) {
					//Estimate the cardinality of each group of variables that are assigned to the same annotated plan
					UnaryAnnotatedPlan unaryPlan = entry.getKey();
					//First estimate: the size of the annotated plan 
					BigInteger sizeEstimate = unaryPlan.getSize();
					//Second estimate: the product of the cardinalities of the variables
					BigDecimal projectionEstimate = BigDecimal.ONE;
					for(Constant constant:entry.getValue()) {
						//Find the relation attribute pair with the highest quality that map to the projected constant  
						Pair<UnaryAnnotatedPlan, Attribute> pair = getHighestQualityAnnotatedPlan(configuration, constant);
						//Find the cardinality of the attribute that maps to the projected constant
						Relation baseRelation = pair.getLeft().getRelation();
						int baseCardinality = catalog.getCardinality(baseRelation, pair.getRight());

						double selectivity = pair.getLeft().getSize().doubleValue() / catalog.getCardinality(baseRelation);
						projectionEstimate = projectionEstimate.multiply(new BigDecimal(baseCardinality)).multiply(BigDecimal.valueOf(selectivity));
						projectionEstimate = projectionEstimate.compareTo(BigDecimal.ONE) < 0 ? BigDecimal.ONE : projectionEstimate; 

						log.trace("Configuration: " + configuration + " constant: " + constant + "\n" + 
								"Relation: " + pair.getLeft().getRelation() + " Attribute: " + pair.getRight() + "\n" + 
								"Base cardinality: " + baseCardinality + "\n" + 
								"Selectivity: " + selectivity + "\n" + 
								"Total cardinality: " + projectionEstimate);
					}
					//Select the smallest estimate
					sizeEstimate = sizeEstimate.compareTo(projectionEstimate.toBigInteger()) < 0 ? sizeEstimate : projectionEstimate.toBigInteger();
					product = product.multiply(sizeEstimate);
				}
				return product;

			}
			else {
				return configuration.getSize();
			}
		}
		else {
			return BigInteger.valueOf(Integer.MAX_VALUE);
		}
	}

	/**
	 * TODO Why back-chase?
	 * Intuitively coverage is an index of how close if the annotated plan's size to the size of the input query
	 * for each subquery of size k 
	 * 		We back-chase the input annotated plan and we see if the subquery is contained in the input annotated plan
	 * 		if it is contained we increase the coverage by 1. 
	 * 
	 * We consider all subqueries of size two that have at least one join variable.
	 * We need to consider the coverage only of annotated plans that contain the input query.
	 * 
	 * @param configuration
	 * @param query
	 * @return
	 * 		the coverage of the input annotated plan w.r.t. the input query
	 */
	public synchronized static int coverage(DAGAnnotatedPlan configuration, ConjunctiveQuery query, HomomorphismDetector detector) {		
		int coverage = 0;
		List<Term> freeVariables = query.getFree();
		List<Conjunction<Atom>> conjunctions = Lists.newArrayList();
		//Create all conjunctions of size 1
		for(Atom atom:query.getBody().getAtoms()) {
			conjunctions.add(Conjunction.of(atom));
		}
		
		//Create all conjunctions of size 2 where there is at least one join condition
		List<Atom> atoms = query.getBody().getAtoms();
		for(int i = 0; i < atoms.size()-1; ++i) {
			for(int j = i + 1; j < atoms.size(); ++j) {
				if(!CollectionUtils.intersection(atoms.get(i).getTerms(), atoms.get(j).getTerms()).isEmpty()) {
					conjunctions.add(Conjunction.of(atoms.get(i), atoms.get(j)));
				}
				
			}
		}
		//For each subquery
		for(Conjunction<Atom> conjunction:conjunctions) {
			//Find its free variables
			Collection<Term> f = CollectionUtils.intersection(freeVariables, conjunction.getTerms());
			Atom head = new Atom(new Predicate("Q", f.size()), f);
			ConjunctiveQuery subquery = new ConjunctiveQuery(head, conjunction);
			//Create homomorphism constraints that preserve the input constants
			if(configuration.getState() instanceof DatabaseChaseState) {
				((DatabaseChaseState)configuration.getState()).setManager((DatabaseHomomorphismManager) detector);
			}
			
			Map<Variable, Constant> mapConstraint = Maps.newHashMap(query.getGroundingsProjectionOnFreeVars());
			Iterator<Variable> iterator = mapConstraint.keySet().iterator();
			while(iterator.hasNext()) {
				if(!f.contains(iterator.next())) {
					iterator.remove();
				}
			}
			//The detected matches must preserve the free variables
			HomomorphismProperty[] c = new HomomorphismProperty[2];
			c[0] = HomomorphismProperty.createMapProperty(mapConstraint);
			c[1] = HomomorphismProperty.createTopKProperty(1);
			
			boolean matchesQuery = !configuration.getState().getMatches(subquery, c).isEmpty();
			if(matchesQuery) {
				coverage += 1;				
			}		
		}
		return coverage;
	}


}
