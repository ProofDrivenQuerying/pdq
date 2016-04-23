/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.estimator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cardinality.dag.BinaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.dag.UnaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.Histogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerJoinCardinalityEstimator;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * The Class DefaultCardinalityEstimator.
 *
 * @author Efthymia Tsamoura
 */
public class DefaultCardinalityEstimator implements CardinalityEstimator {

	/** The log. */
	protected static Logger log = Logger.getLogger(DefaultCardinalityEstimator.class);

	/**  Base statistics *. */
	private final Catalog catalog;

	/**  Stores the keys of each annotated plan*. */
	private final Multimap<DAGAnnotatedPlan, Collection<Constant>> keyIndex;

	/**  Stores the keys of each annotated plan*. */
	private final Multimap<DAGAnnotatedPlan, Collection<Constant>> notkeyIndex;

	/**   Different penalties. */
	private final double nonKeyPenalty = 1;

	/** The selectivity penalty. */
	private final double selectivityPenalty = 1;

	/** The distinct penalty. */
	private final double distinctPenalty = 1;

	/** The projection penalty. */
	private final double projectionPenalty = 1;

	/** The subsumption penalty. */
	private final double subsumptionPenalty = 1;

	/** The histogram penalty. */
	private final double histogramPenalty = 1;

	/**
	 * Instantiates a new default cardinality estimator.
	 *
	 * @param catalog the catalog
	 */
	public DefaultCardinalityEstimator(Catalog catalog) {
		Preconditions.checkNotNull(catalog);
		this.catalog = catalog;
		this.keyIndex = Multimaps.synchronizedListMultimap(ArrayListMultimap.<DAGAnnotatedPlan, Collection<Constant>>create());
		this.notkeyIndex = Multimaps.synchronizedListMultimap(ArrayListMultimap.<DAGAnnotatedPlan, Collection<Constant>>create());
	}

	/**
	 * Instantiates a new default cardinality estimator.
	 *
	 * @param catalog the catalog
	 * @param keyIndex the key index
	 * @param notkeyIndex the notkey index
	 */
	private DefaultCardinalityEstimator(Catalog catalog, Multimap<DAGAnnotatedPlan, Collection<Constant>> keyIndex, 
			Multimap<DAGAnnotatedPlan, Collection<Constant>> notkeyIndex) {
		Preconditions.checkNotNull(catalog);
		Preconditions.checkNotNull(keyIndex);
		Preconditions.checkNotNull(notkeyIndex);
		this.catalog = catalog;
		this.keyIndex = keyIndex;
		this.notkeyIndex = notkeyIndex;
	}

	/**
	 * Size quality of.
	 *
	 * @param configuration the configuration
	 * @return 		the size and the quality of the input annotated plan
	 */
	@Override
	public Pair<BigInteger,Double> sizeQualityOf(UnaryAnnotatedPlan configuration) {
		return Pair.of(CardinalityUtility.sizeOf(configuration.getFact(), this.catalog), this.catalog.getQuality(configuration.getRelation())) ;
	}

	/**
	 * This method uses key foreign-key constraints to derive an accurate cardinality estimation of the input (nary) join.
	 * When this is not possible, the method breaks up the input (nary) join predicate into unary join predicates.
	 * It estimates the output cardinality using the minimum of the cardinalities related to each unary join predicate.   
	 *
	 * @param left the left
	 * @param right the right
	 * @param egd 		Runs the EGD chase algorithm 
	 * @param detector 		Detects homomorphisms during chasing
	 * @param dependencies 		The dependencies to take into account during chasing
	 * @return 		the size and the quality of the annotated plan that is composed by the input annotated plans.
	 */
	@Override
	public Triple<BigInteger,Double, Integer> sizeQualityOf(DAGAnnotatedPlan left, DAGAnnotatedPlan right, Chaser egd, HomomorphismDetector detector, Collection<? extends Dependency> dependencies) {				
		//Find the constants that are shared among the annotated plans
		Collection<Constant> constants = Sets.newHashSet(CollectionUtils.intersection(left.getOutput(), right.getOutput()));
		Boolean rightKey = this.isKey(constants, right, egd, detector, dependencies);
		Boolean leftKey = this.isKey(constants, left, egd, detector, dependencies);
		
		if(!constants.isEmpty() && CardinalityUtility.hasID(constants, left, right, detector) && rightKey) {
			log.trace("ID: " + left + " " + right + "\tSize quality: " + Pair.of(left.getSize(), left.getQuality()));
			//The number of independence assumptions that will be made after joining the input annotated plan is 0
			return Triple.of(left.getSize(), 
					left.getQuality(), 
					0 + left.getIndependenceAssumptions() + right.getIndependenceAssumptions());
		}
		else if(!constants.isEmpty() && CardinalityUtility.hasID(constants, right, left, detector) && leftKey) {
			log.trace("ID: " + right + " " + left + "\tSize quality: " + Pair.of(right.getSize(), right.getQuality()));
			//The number of independence assumptions that will be made after joining the input annotated plan is 0
			return Triple.of(right.getSize(), 
					right.getQuality(), 
					0 + left.getIndependenceAssumptions() + right.getIndependenceAssumptions());
		}

		
		//Overapproximations when the join attributes is a key in either of sides
		Triple<BigInteger,Double, Integer> keyApproximation = null;
		if(rightKey && leftKey) {
			//The number of independence assumptions that will be made after joining the input annotated plan is 0
			keyApproximation = Triple.of(left.getSize().min(right.getSize()), 
					Math.max(left.getQuality(), right.getQuality()), 
					0 + left.getIndependenceAssumptions() + right.getIndependenceAssumptions());
			return keyApproximation;
		}
		else if(rightKey) {
			//The number of independence assumptions that will be made after joining the input annotated plan is 0
			keyApproximation = Triple.of(left.getSize(), 
					left.getQuality() + this.nonKeyPenalty, 
					0 + left.getIndependenceAssumptions() + right.getIndependenceAssumptions());
			return keyApproximation;
		}
		else if(leftKey) {
			//The number of independence assumptions that will be made after joining the input annotated plan is 0
			keyApproximation = Triple.of(right.getSize(), 
					right.getQuality() + this.nonKeyPenalty, 
					0 + left.getIndependenceAssumptions() + right.getIndependenceAssumptions());
			return keyApproximation;
		}

		//Approximations when using histograms or other heuristics for join cardinality estimation 
		Triple<BigInteger, Double, Integer> nonkeyApproximation = null;
		//if there no join constants return the cartesian product of these relations 
		if(constants.isEmpty()) {
			return Triple.of(left.getSize().multiply(right.getSize()), 
					Math.max(left.getQuality(), right.getQuality()), 
					0 + left.getIndependenceAssumptions() + right.getIndependenceAssumptions());
		}
		//If the annotated plans are unary and join on every exported constant then 
		//check whether there exist base histograms to return an join size estimate   
		else if(left instanceof UnaryAnnotatedPlan && right instanceof UnaryAnnotatedPlan &&
				left.getExportedConstants().equals(right.getExportedConstants())) {
			BigInteger estimate = this.intersectionSizeWithHistograms((UnaryAnnotatedPlan)left, (UnaryAnnotatedPlan)right);
			double quality = Math.max(left.getQuality(), right.getQuality()) + this.histogramPenalty;
			//Increase the number of independence assumptions made in the newly created annotated plan
			Integer ids = 1 + left.getIndependenceAssumptions() + right.getIndependenceAssumptions();
			nonkeyApproximation = Triple.of(estimate, quality, ids);
			log.trace("Intersection with histograms: " + left + " " + right + "\tSize quality: " + nonkeyApproximation);
		} 
		//if there is a single join constant, then first check whether there exist base histograms to estimate the cardinality of the join or not.
		//If histograms are not available then resort to base table and attribute cardinalities
		if(nonkeyApproximation == null && constants.size() == 1) {
			Constant constant = constants.iterator().next();
			BigInteger estimate = null;
			if(left instanceof UnaryAnnotatedPlan && right instanceof UnaryAnnotatedPlan) {
				estimate = this.equijoinSizeWithHistograms((UnaryAnnotatedPlan)left, (UnaryAnnotatedPlan)right, constant);
				//Preconditions.checkNotNull(estimate);
				double quality = Math.max(left.getQuality(), right.getQuality()) + this.histogramPenalty;
				//Increase the number of independence assumptions made in the newly created annotated plan
				Integer ids = 1 + left.getIndependenceAssumptions() + right.getIndependenceAssumptions();
				nonkeyApproximation = Triple.of(estimate, quality, ids);
				log.trace("Single join with histograms: " + left + " " + right + "\tSize quality: " + nonkeyApproximation);
			}
			if(estimate == null) {
				log.trace("Single join no histograms: " + left + " " + right + "\tSize quality: " + nonkeyApproximation);
				estimate = this.equijoinSizeWithoutHistograms(left, right, constant);
				Double quality = Math.max(left.getQuality(), right.getQuality()) + this.selectivityPenalty;
				//Increase the number of independence assumptions made in the newly created annotated plan
				Integer ids = 1 + left.getIndependenceAssumptions() + right.getIndependenceAssumptions();
				nonkeyApproximation = Triple.of(estimate, quality, ids);
			}
		}
		//if there are multiple join constants.
		//Then break the join into multiple single attribute joins and take as an upper bound the maximum of each single attribute joins
		//We do not use histograms for cardinality estimation  
		else if(nonkeyApproximation == null && constants.size() > 1) {
			BigInteger bestSize = BigInteger.valueOf(Integer.MAX_VALUE);
			Double bestQuality = Double.MAX_VALUE;
			for(Constant constant:constants) {
				BigInteger estimate = this.equijoinSizeWithoutHistograms(left, right, constant);
				Double quality = Math.max(left.getQuality(), right.getQuality()) + this.selectivityPenalty;
				if(bestSize.compareTo(estimate) > 0 && bestQuality > quality) {
					bestSize = estimate;
					bestQuality = quality;
				}
			}
			log.trace("Multiple joins no histograms: " + left + " " + right);
			nonkeyApproximation = Triple.of(bestSize, 
					bestQuality + this.distinctPenalty, 
					/*The number of independence assumptions that will be made after joining the input annotated plan equals 1*/
					1 + left.getIndependenceAssumptions() + right.getIndependenceAssumptions());
		}

		//Before returning this estimate make sure is less than the key approximation (if any)
		Preconditions.checkNotNull(nonkeyApproximation);
		return nonkeyApproximation;
	}

	/**
	 * --If AnnPlan is successful and the non-schema constants in the facts are exactly
	 * 		the free variables of Q, we let
	 * 		QueryAdjustedQuality(AnnPlan) = QualityOf(AnnPlan)
	 * 		In this case we know that the number of tuples in the plan output is exactly the
	 * 		size we want.
	 * 
	 * 		--Otherwise if AnnPlan is successful, we let
	 * 		QueryAdjustedQuality(AnnPlan) = QualityOf(AnnPlan)+Kproject
	 * 		where Kproject is a penalty reflecting the fact that the number of tuples in the
	 * 		plan output is an overestimate of the number of tuples in Q, since Q will be a
	 * 		projection of the plan. By default, we could set Kproject = 1 it could also be
	 * 		proportional to the number of attributes that need to be projected out, which are
	 * 		the non-schema constants in AnnPlan that are not free variables of Q.
	 * 
	 * 		--Otherwise, if AnnPlan is not successful but the non-schema constants are exactly
	 * 		the free variables of Q, we set
	 * 		QueryAdjustedQuality(AnnPlan) = QualityOf(AnnPlan)+Ksubsume
	 * 		where Ksubsume is a penalty reflecting the fact that the number of tuples in the
	 * 		plan output is an overestimate of the number of tuples in Q, since Q will be
	 * 		contained in the plan but not vice versa. By default, we could let Ksubsume be 1.
	 * 
	 * 		--Otherwise, if AnnPlan is not successful and the non-schema constants properly
	 * 		contain the free variables of Q, we set
	 * 		QueryAdjustedQuality(AnnPlan) = QualityOf(AnnPlan)+Ksubsume+Kproject
	 * 		reflecting both the considerations above.
	 *
	 * @param configuration the configuration
	 * @param query the query
	 * @param matchesQuery the matches query
	 * @return the double
	 */
	@Override
	public double adjustedQualityOf(DAGAnnotatedPlan configuration, ConjunctiveQuery query, boolean matchesQuery) {		
		if(matchesQuery) {
			if(configuration.getExportedConstants().equals(Sets.newHashSet(query.getGroundingsProjectionOnFreeVars().values()))) {
				return configuration.getQuality();
			}
			else {
				return configuration.getQuality() + this.projectionPenalty;
			}
		}
		else {
			if(configuration.getExportedConstants().equals(Sets.newHashSet(query.getGroundingsProjectionOnFreeVars().values()))) {
				return configuration.getQuality() + this.subsumptionPenalty;
			}
			else if(configuration.getExportedConstants().containsAll(query.getGroundingsProjectionOnFreeVars().values())){
				return configuration.getQuality() + this.subsumptionPenalty + this.projectionPenalty;
			}
		}
		return Double.MAX_VALUE;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DefaultCardinalityEstimator clone() {
		return new DefaultCardinalityEstimator(this.catalog, this.keyIndex, this.notkeyIndex);
	}

	/**
	 * Determines whether the attributes corresponding to constants in keys form a key in the plan of AnnPlan.
	 * It first searches the cache for previously detected keys before resorting to reasoning.
	 *
	 * @param keys 		Input candidate keys
	 * @param configuration the configuration
	 * @param egd 		Runs the EGD chasing algorithm 
	 * @param detector 		Detects homomorphisms during chasing
	 * @param dependencies 		Dependencies to consider during chasing
	 * @return 		true if the input collection of constants is a key for the input annotated plan
	 */
	private boolean isKey(Collection<Constant> keys, DAGAnnotatedPlan configuration, Chaser egd, HomomorphismDetector detector, Collection<? extends Dependency> dependencies) {
		Preconditions.checkNotNull(keys);
		Preconditions.checkArgument(!keys.isEmpty());

		//If the candidate keys are all the output constants of the input configuration
		//then return true
		if(CollectionUtils.containsAll(keys, configuration.getOutput())) {
			this.keyIndex.put(configuration, keys);
			return true;
		}

		//Search the key cache
		//Get all the detected keys for the input configuration
		//If the candidate keys are a superset of the detected keys
		//then return true
		if(!this.keyIndex.get(configuration).isEmpty()) {			
			Collection<Collection<Constant>> observedKeysets = this.keyIndex.get(configuration);
			for(Collection<Constant> keySet:observedKeysets) {
				if (keys.containsAll(keySet)) {
					this.keyIndex.put(configuration, keys);
					log.trace("Cache hit isKey " + keys + "\t" + configuration + " = " + true);
					return true;
				}
			}
		}


		//Search the nonkey cache
		//Get all the detected keys for the input configuration
		//If the candidate keys are a subset of the tried and failed keys
		//then return false
		if(!this.notkeyIndex.get(configuration).isEmpty()) {			
			Collection<Collection<Constant>> observedFailures = this.notkeyIndex.get(configuration);
			for(Collection<Constant> failed:observedFailures) {
				if (failed.containsAll(keys)) {
					log.trace("Cache hit:  " + keys + "\t" + configuration + " = " + false);
					return false;
				}
			}
		}

		//If the input configuration is a binary configuration 
		//then search the key cache for each subconfiguration. 
		//If the input candidate keys are keys for both subconfigurations 
		//and if the join constants are a subset of the candidate keys 
		//then return true
		if(configuration instanceof BinaryAnnotatedPlan) {
			boolean isLeftKey = false;
			boolean isRightKey = false;
			if(!this.keyIndex.get(((BinaryAnnotatedPlan) configuration).getLeft()).isEmpty()) {			
				Collection<Collection<Constant>> observedKeysets = this.keyIndex.get(((BinaryAnnotatedPlan) configuration).getLeft());
				for(Collection<Constant> keySet:observedKeysets) {
					if (keys.containsAll(keySet)) {
						isLeftKey = true;
						break;
					}
				}
			}

			if(!this.keyIndex.get(((BinaryAnnotatedPlan) configuration).getRight()).isEmpty()) {			
				Collection<Collection<Constant>> observedKeysets = this.keyIndex.get(((BinaryAnnotatedPlan) configuration).getRight());
				for(Collection<Constant> keySet:observedKeysets) {
					if (keys.containsAll(keySet)) {
						isRightKey = true;
						break;
					}
				}
			}

			Collection<Constant> joinConstants = CollectionUtils.intersection(((BinaryAnnotatedPlan) configuration).getLeft().getExportedConstants(), ((BinaryAnnotatedPlan) configuration).getRight().getExportedConstants());

			if(isLeftKey && isRightKey && keys.containsAll(joinConstants)) {
				this.keyIndex.put(configuration, keys);
				log.trace("Cache hit: " + keys + "\t" + configuration);
				return true;
			}
		}

		//Otherwise, detect keys through the egd chase algorithm
		log.trace("No cache hit for " + keys + " " + configuration);
		boolean isKey = CardinalityUtility.isKey(keys, configuration, egd, detector, dependencies);
		if(isKey) {
			this.keyIndex.put(configuration, keys);
		}
		else {
			this.notkeyIndex.put(configuration, keys);
		}
		log.trace("isKey " + keys + "\t" + configuration + " = " + isKey);
		return isKey;
	}

	/**
	 * Gets the highest quality score relation.
	 *
	 * @param annotatedPlan the annotated plan
	 * @param constant the constant
	 * @return 		the <relation,attribute> pair with the highest quality for the given input constant
	 */
	private Pair<Relation,Attribute> getHighestQualityScoreRelation(DAGAnnotatedPlan annotatedPlan, Constant constant) {
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
		return Pair.of(relation, relation.getAttribute(indexOf));
	}

	/**
	 * 
	 * Returns the size and the quality of the join of the input annotated plans.
	 * Choose a fact F1 in the annotation of left containing c, such that the quality score
	 * of the relation R1 in F1 is best possible, and similarly choose fact F2
	 * in the annotation of right containing c such that the quality score
	 * of the relation R2 of F2 is best possible. If we assume that for each
	 * attribute in some base relation, we have a good estimate of the number
	 * of distinct values of this attribute, this means we can take F1 and F2 to
	 * be over unary relations.
	 * 
	 * The output size if estimated as 
	 * SizeOf(AnnPlan)= (SizeOf(left) \times SizeOf(right)) \ max{SizeOf(Atomic(F2)),SizeOf(Atomic(F1))}.
	 * 
	 * The output quality if estimated as 
	 * max{QualityOf(left), QualityOf(right)}+ K_{EstimateSelectivity} where K_{EstimateSelectivity} is a penalty.
	 *
	 * @param left the left
	 * @param right the right
	 * @param c 		the join chase constant
	 * @return the size and the quality of the join of the input annotated plans
	 */
	private BigInteger equijoinSizeWithoutHistograms(DAGAnnotatedPlan left, DAGAnnotatedPlan right, Constant c) {
		Pair<Relation, Attribute> leftPair = this.getHighestQualityScoreRelation(left, c);
		Pair<Relation, Attribute> rightPair = this.getHighestQualityScoreRelation(right, c);
		//The cardinality of the join is 1/max(|join attribute from left annotated plan|, |join attribute from right annotated plan|)
		int d = Math.max(this.catalog.getCardinality(leftPair.getLeft(), leftPair.getRight()), 
				this.catalog.getCardinality(rightPair.getLeft(), rightPair.getRight()));
		BigInteger size  = left.getSize().multiply(right.getSize()).divide(BigInteger.valueOf(d));
		return size;
//		double quality = Math.max(left.getQuality(), right.getQuality()) + this.selectivityPenalty;
//		return Pair.of(size, quality);
	}

	/**
	 * Returns the size and the quality of the join of the input annotated plans.
	 * The method covers the following case:
	 * 
	 * 	i.	our annotated plan is of the form Compose(Atomic(F1),Atomic(F2)), where
	 * 		F1 = R1(\vec(c1)) and F2 = R2(\vec(c2)) are facts.
	 * 		ii. there is only one common chase constant c of Atomic(F1) and Atomic(F2)
	 * 		iii.we have a histogram on a position of R1 containing constant c in R1 and the same
	 * 		in R2
	 * 		iv.	the buckets of the histogram are the same, namely B = fb1 : : :bKg
	 * 		In this case we could take
	 * 		SizeOf(AnnPlan)=\Sum{i\leqK} AvgSize(R1[bi]) \times AvgSize(R2[bi]) \times min{NumDistinct(R1[bi]),NumDistinct(R2[bi])}
	 * 		where AvgSize(Rj[bi]) is the average number of tuples in Rj per element in the bucket
	 * 		bi and NumDistinct(Rj[bi]) is the number of distinct values of the position corresponding
	 * 		to chase constant c in bucket bi of Rj.
	 * 		We take QualityOf(AnnPlan) = KHistJoin where KHistJoin is another penalty factor.
	 *
	 * @param left the left
	 * @param right the right
	 * @param c 		the join chase constant
	 * @return the size and the quality of the join of the input annotated plans
	 */
	private BigInteger equijoinSizeWithHistograms(UnaryAnnotatedPlan left, UnaryAnnotatedPlan right, Constant c) {		
		Pair<Relation, Attribute> leftPair = this.getHighestQualityScoreRelation(left, c);
		Pair<Relation, Attribute> rightPair = this.getHighestQualityScoreRelation(right, c);
		//Estimate the cardinality of the join 
		double joinSelectivity = 0;
		Histogram leftHistogram = this.catalog.getHistogram(leftPair.getLeft(), leftPair.getRight());
		Histogram rightHistogram = this.catalog.getHistogram(rightPair.getLeft(), rightPair.getRight());
		if(leftHistogram == null || rightHistogram == null) {
			log.trace("Base histograms are missing for " + leftPair.getLeft() + " " + leftPair.getRight() + " and/or " + rightPair.getLeft() + " " + rightPair.getRight());
			return null;
		}
		if(leftHistogram instanceof SQLServerHistogram && rightHistogram instanceof SQLServerHistogram) {
			SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
			BigInteger cardinality = estimator.estimateSingleJoinAttributeCardinality((SQLServerHistogram)leftHistogram, (SQLServerHistogram)rightHistogram);
			joinSelectivity = cardinality.doubleValue() / 
					(new Double(this.catalog.getCardinality(leftPair.getLeft())) * new Double(this.catalog.getCardinality(rightPair.getLeft())));
		}
		double size  = left.getSize().doubleValue() * right.getSize().doubleValue() * joinSelectivity;
		Preconditions.checkArgument(size > 0);;
		return new BigDecimal(size).toBigInteger();
//		double quality = Math.max(left.getQuality(), right.getQuality()) + this.histogramPenalty;
//		return Pair.of(new BigDecimal(size).toBigInteger(), quality);
	}

	/**
	 * Returns the size and the quality of the join of the input annotated plans.
	 * The method covers the following case:
	 * 
	 * 		i.our annotated plan is of the form Compose(Atomic(F1),Atomic(F2)), where
	 * 		F1 = R1(\vec{c1}) and F2 = R2(\vec{c1}) are facts with exactly the same chase constants.
	 * 		ii.we have a histogram on a position corresponding to constant c in R1 and similarly
	 * 		in R2
	 * 		iii.the buckets of the histogram are the same, namely B = {b1,...,bK}
	 * 		SizeOf(AnnPlan) = \Sum_{i\leqK}
	 * 		min{AvgSize(R1[bi]) \times NumDistinct(R1[bi]),AvgSize(R2[bi]) \time NumDistinct(R2[bi])}
	 * 		QualityOf(AnnPlan) = KHistJoin.
	 *
	 * @param left the left
	 * @param right the right
	 * @return the size and the quality of the join of the input annotated plans
	 */
	private BigInteger intersectionSizeWithHistograms(UnaryAnnotatedPlan left, UnaryAnnotatedPlan right) {
		List<Term> leftTerms = left.getFact().getTerms();
		List<Term> rightTerms = right.getFact().getTerms();
		int indexOf = rightTerms.indexOf(leftTerms.get(0));
		Preconditions.checkArgument(indexOf >= 0);

		Relation leftRelation = left.getRelation();
		Relation rightRelation = right.getRelation();
		Attribute leftAttribute = leftRelation.getAttribute(0);
		Attribute rightAttribute = rightRelation.getAttribute(indexOf);

		//Estimate the cardinality of the join 
		double joinSelectivity = 0;
		Histogram leftHistogram = this.catalog.getHistogram(leftRelation, leftAttribute);
		Histogram rightHistogram = this.catalog.getHistogram(rightRelation, rightAttribute);
		if(leftHistogram == null || rightHistogram == null) {
			log.trace("Base histograms are missing for " + leftRelation + " " + leftAttribute + " and/or " + rightRelation + " " + rightAttribute);
			return null;
		}
		if(leftHistogram instanceof SQLServerHistogram && rightHistogram instanceof SQLServerHistogram) {
			SQLServerJoinCardinalityEstimator estimator = new SQLServerJoinCardinalityEstimator();
			BigInteger cardinality = estimator.estimateIntersectionCardinality((SQLServerHistogram)leftHistogram, (SQLServerHistogram)rightHistogram);
			joinSelectivity = cardinality.doubleValue() / 
					(new Double(this.catalog.getCardinality(leftRelation)) * new Double(this.catalog.getCardinality(rightRelation)));
		}
		double size  = left.getSize().doubleValue() * right.getSize().doubleValue() * joinSelectivity;
		Preconditions.checkArgument(size > 0);
		return new BigDecimal(size).toBigInteger();
//		double quality = Math.max(left.getQuality(), right.getQuality()) + this.histogramPenalty;
//		return Pair.of(new BigDecimal(size).toBigInteger(), quality);
	}


	/**
	 * Cardinality of.
	 *
	 * @param configuration the configuration
	 * @param query the query
	 * @return 		the cardinality of the input annotated plan after applying the projections of the input
	 */
	@Override
	public BigInteger sizeOfProjection(DAGAnnotatedPlan configuration, ConjunctiveQuery query) {
		return CardinalityUtility.sizeOfProjectionGroupEstimate(configuration, query, this.catalog);
		//		if(configuration.getExportedConstants().containsAll(query.getFree2Canonical().values())) {
		//			if(!query.getFree().isEmpty()) {
		//				ConjunctiveQuery cq = AGMBound.transform(configuration, Lists.newArrayList(query.getFree2Canonical().values()));
		//				return AGMBound.estimate(cq, this.catalog);
		//			}
		//			else {
		//				return configuration.getSizeWithoutProjections();
		//			}
		//		}
		//		else {
		//			return BigInteger.valueOf(Integer.MAX_VALUE);
		//		}
	}
}
