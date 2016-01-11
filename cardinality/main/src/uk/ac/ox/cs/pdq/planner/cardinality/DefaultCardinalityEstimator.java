package uk.ac.ox.cs.pdq.planner.cardinality;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.Histogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerHistogram;
import uk.ac.ox.cs.pdq.cost.statistics.SQLServerJoinCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.planner.dag.BinaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.UnaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.beust.jcommander.internal.Maps;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class DefaultCardinalityEstimator implements CardinalityEstimator {

	protected static Logger log = Logger.getLogger(DefaultCardinalityEstimator.class);

	/** Base statistics **/
	private final Catalog catalog;

	/** Stores the keys of each annotated plan**/
	private final Multimap<DAGAnnotatedPlan, Collection<Constant>> keyIndex;

	/** Stores the keys of each annotated plan**/
	private final Multimap<DAGAnnotatedPlan, Collection<Constant>> notkeyIndex;

	/** 
	 * Different penalties
	 */
	private final double nonKeyPenalty = 1;

	private final double selectivityPenalty = 1;

	private final double distinctPenalty = 1;

	private final double projectionPenalty = 1;

	private final double subsumptionPenalty = 1;

	private final double histogramPenalty = 1;

	public DefaultCardinalityEstimator(Catalog catalog) {
		Preconditions.checkNotNull(catalog);
		this.catalog = catalog;
		this.keyIndex = Multimaps.synchronizedListMultimap(ArrayListMultimap.<DAGAnnotatedPlan, Collection<Constant>>create());
		this.notkeyIndex = Multimaps.synchronizedListMultimap(ArrayListMultimap.<DAGAnnotatedPlan, Collection<Constant>>create());
	}

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
	 * 
	 * @param configuration
	 * @return
	 * 		the size and the quality of the input annotated plan
	 */
	@Override
	public Pair<BigInteger,Double> sizeQualityOf(UnaryAnnotatedPlan configuration) {
		Relation relation = configuration.getRelation();
		int index = 0;
		//Estimate using the catalog the selectivity of the input selection predicates
		List<Double> selectivities = Lists.newArrayList();
		for(Term constant:configuration.getFact().getTerms()) {
			if(constant instanceof TypedConstant) {
				selectivities.add(this.catalog.getSelectivity(relation, relation.getAttribute(index), (TypedConstant<?>) constant));
			}
			++index;
		}
		//Detect attribute equality predicates and use default attribute equality selectivities
		List<Term> terms = configuration.getFact().getTerms();
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
		//The process used to model this correlation is called “exponential back-off”. 
		//Note: “Exponential back-off” is used with disjunctions as well. The system calculates this value by first transforming disjunctions to a negation of conjunctions. 
		//The formula below represents the new CE computation of selectivity for a conjunction of predicates. 
		//“P0” represents the most selective predicate and is followed by the three most selective predicates:
		//p_0⋅〖p_1〗^(1⁄2)⋅〖p_2〗^(1⁄4)⋅〖p_3〗^(1⁄8)
		//The new CE sorts predicates by selectivity and keeps the four most selective predicates for use in the calculation. 
		//The CE then “moderates” each successive predicate by taking larger square roots. 
		if(!selectivities.isEmpty()) {
			Collections.sort(selectivities);
			double globalSelectivity = 1;
			for(int i = 0; i < Math.min(selectivities.size(), 4); ++i) {
				globalSelectivity *= Math.pow(selectivities.get(i), 1.0/(Math.pow(2, i)));
			}
			Preconditions.checkArgument((int)(globalSelectivity * this.catalog.getCardinality(relation)) > 0);
			return Pair.of(BigInteger.valueOf((long) (globalSelectivity * this.catalog.getCardinality(relation))), this.catalog.getQuality(relation));
		}
		Preconditions.checkArgument(this.catalog.getCardinality(relation) > 0);
		return Pair.of(BigInteger.valueOf(this.catalog.getCardinality(relation)), this.catalog.getQuality(relation));
	}

	/**
	 * This method uses key foreign-key constraints to derive an accurate cardinality estimation of the input (nary) join.
	 * When this is not possible, the method breaks up the input (nary) join predicate into unary join predicates.
	 * It estimates the output cardinality using the minimum of the cardinalities related to each unary join predicate.   
	 * @param left
	 * @param right
	 * @param egd
	 * 		Runs the EGD chase algorithm 
	 * @param detector
	 * 		Detects homomorphisms during chasing
	 * @param dependencies
	 * 		The dependencies to take into account during chasing
	 * @return
	 * 		the size and the quality of the annotated plan that is composed by the input annotated plans.
	 * 		
	 */
	@Override
	public Pair<BigInteger,Double> sizeQualityOf(DAGAnnotatedPlan left, DAGAnnotatedPlan right, Chaser egd, HomomorphismDetector detector, Collection<? extends Constraint> dependencies) {
		//Find constants that are shared among the annotated plans
		Collection<Constant> constants = Sets.newHashSet(CollectionUtils.intersection(left.getOutput(), right.getOutput()));
		Boolean rightKey = this.isKey(constants, right, egd, detector, dependencies);
		Boolean leftKey = this.isKey(constants, left, egd, detector, dependencies);
		
		if(!constants.isEmpty() && CardinalityUtility.hasID(constants, left, right, detector) && rightKey) {
			log.trace("ID: " + left + " " + right + "\tSize quality: " + Pair.of(right.getSize(), right.getQuality()));
			return Pair.of(left.getSize(), left.getQuality());
		}
		else if(!constants.isEmpty() && CardinalityUtility.hasID(constants, right, left, detector) && leftKey) {
			log.trace("ID: " + left + " " + right + "\tSize quality: " + Pair.of(right.getSize(), right.getQuality()));
			return Pair.of(right.getSize(), right.getQuality());
		}

		Pair<BigInteger,Double> keyApproximation = null;
		//Overapproximations when the join attributes is a key in either of sides
		if(rightKey && leftKey) {
			keyApproximation = Pair.of(left.getSize().min(right.getSize()), Math.max(left.getQuality(), right.getQuality()));
		}
		else if(rightKey) {
			keyApproximation = Pair.of(left.getSize(), left.getQuality() + this.nonKeyPenalty);
		}
		else if(leftKey) {
			keyApproximation = Pair.of(right.getSize(), right.getQuality() + this.nonKeyPenalty);
		}

		Pair<BigInteger, Double> nonkeyApproximation = null;
		//if there no join constants return the cartesian product of these relations 
		if(constants.isEmpty()) {
			return Pair.of(left.getSize().multiply(right.getSize()), Math.max(left.getQuality(), right.getQuality()));
		}
		//If the annotated plans are unary and join on every exported constant then 
		//check whether there exist base histograms to return an join size estimate   
		else if(left instanceof UnaryAnnotatedPlan && right instanceof UnaryAnnotatedPlan &&
				left.getExportedConstants().equals(right.getExportedConstants())) {
			nonkeyApproximation = this.intersectionSizeQualityWithHistograms((UnaryAnnotatedPlan)left, (UnaryAnnotatedPlan)right);
			//Before returning this estimate make sure is less than the key approximation (if any)
			if(nonkeyApproximation != null) {
				log.trace("Intersection with histograms: " + left + " " + right + "\tSize quality: " + nonkeyApproximation);
			}
		} 
		//if there is a single join constant, then first check whether there exist base histograms to estimate the cardinality of the join or not.
		//If histograms are not available then resort to base table and attribute cardinalities
		if(nonkeyApproximation == null && constants.size() == 1) {
			Constant constant = constants.iterator().next();
			if(left instanceof UnaryAnnotatedPlan && right instanceof UnaryAnnotatedPlan) {
				nonkeyApproximation = this.singleEquijoinSizeQualityWithHistograms((UnaryAnnotatedPlan)left, (UnaryAnnotatedPlan)right, constant);
				//Before returning this estimate make sure is less than the key approximation (if any)
				if(nonkeyApproximation != null) {
					log.trace("Single join with histograms: " + left + " " + right + "\tSize quality: " + nonkeyApproximation);
				}
			}
			if(nonkeyApproximation == null) {
				log.trace("Single join no histograms: " + left + " " + right + "\tSize quality: " + nonkeyApproximation);
				nonkeyApproximation = this.singleEquijoinSizeQualityWithoutHistograms(left, right, constant);
			}
		}
		//if there are multiple join constants.
		//Then break the join into multiple single attribute joins and take as an upper bound the maximum of each single attribute joins
		//We do not use histograms for cardinality estimation  
		else if(nonkeyApproximation == null && constants.size() > 1) {
			nonkeyApproximation = Pair.of(BigInteger.valueOf(Integer.MAX_VALUE), Double.MAX_VALUE);
			for(Constant constant:constants) {
				Pair<BigInteger, Double> estimate = this.singleEquijoinSizeQualityWithoutHistograms(left, right, constant);
				if(nonkeyApproximation.getLeft().compareTo(estimate.getLeft()) > 0 && nonkeyApproximation.getRight() > estimate.getRight()) {
					nonkeyApproximation = estimate;
				}
			}
			log.trace("Multiple joins no histograms: " + left + " " + right + "\tSize quality: " + Pair.of(nonkeyApproximation.getLeft(), nonkeyApproximation.getRight() + this.distinctPenalty));
			nonkeyApproximation = Pair.of(nonkeyApproximation.getLeft(), nonkeyApproximation.getRight() + this.distinctPenalty);
		}

		
		//Before returning this estimate make sure is less than the key approximation (if any)
		Preconditions.checkNotNull(nonkeyApproximation);
		if(keyApproximation == null || nonkeyApproximation.getLeft().compareTo(keyApproximation.getLeft()) < 0) {
			return nonkeyApproximation;
		}
		else {
			log.trace("The key approximation " + keyApproximation + " has lower size than the non key estimate " + nonkeyApproximation);
			return keyApproximation;
		}
	}

	/**
	 * --If AnnPlan is successful and the non-schema constants in the facts are exactly
		the free variables of Q, we let
		QueryAdjustedQuality(AnnPlan) = QualityOf(AnnPlan)
		In this case we know that the number of tuples in the plan output is exactly the
		size we want.

		--Otherwise if AnnPlan is successful, we let
		QueryAdjustedQuality(AnnPlan) = QualityOf(AnnPlan)+Kproject
		where Kproject is a penalty reflecting the fact that the number of tuples in the
		plan output is an overestimate of the number of tuples in Q, since Q will be a
		projection of the plan. By default, we could set Kproject = 1 it could also be
		proportional to the number of attributes that need to be projected out, which are
		the non-schema constants in AnnPlan that are not free variables of Q.

		--Otherwise, if AnnPlan is not successful but the non-schema constants are exactly
		the free variables of Q, we set
		QueryAdjustedQuality(AnnPlan) = QualityOf(AnnPlan)+Ksubsume
		where Ksubsume is a penalty reflecting the fact that the number of tuples in the
		plan output is an overestimate of the number of tuples in Q, since Q will be
		contained in the plan but not vice versa. By default, we could let Ksubsume be 1.

		--Otherwise, if AnnPlan is not successful and the non-schema constants properly
		contain the free variables of Q, we set
		QueryAdjustedQuality(AnnPlan) = QualityOf(AnnPlan)+Ksubsume+Kproject
		reflecting both the considerations above.
	 */
	@Override
	public double adjustedQualityOf(DAGAnnotatedPlan configuration, Query<?> query, boolean matchesQuery) {		
		if(matchesQuery) {
			if(configuration.getExportedConstants().equals(Sets.newHashSet(query.getFree2Canonical().values()))) {
				return configuration.getQuality();
			}
			else {
				return configuration.getQuality() + this.projectionPenalty;
			}
		}
		else {
			if(configuration.getExportedConstants().equals(Sets.newHashSet(query.getFree2Canonical().values()))) {
				return configuration.getQuality() + this.subsumptionPenalty;
			}
			else if(configuration.getExportedConstants().containsAll(query.getFree2Canonical().values())){
				return configuration.getQuality() + this.subsumptionPenalty + this.projectionPenalty;
			}
		}
		return Double.MAX_VALUE;
	}

	@Override
	public DefaultCardinalityEstimator clone() {
		return new DefaultCardinalityEstimator(this.catalog, this.keyIndex, this.notkeyIndex);
	}

	/**
	 * Determines whether the attributes corresponding to constants in keys form a key in the plan of AnnPlan.
	 * It first searches the cache for previously detected keys before resorting to reasoning.
	 * 
	 * @param keys
	 * 		Input candidate keys
	 * @param configuration
	 * @param egd
	 * 		Runs the EGD chasing algorithm 
	 * @param detector
	 * 		Detects homomorphisms during chasing
	 * @param dependencies
	 * 		Dependencies to consider during chasing
	 * @return
	 * 		true if the input collection of constants is a key for the input annotated plan
	 */
	private boolean isKey(Collection<Constant> keys, DAGAnnotatedPlan configuration, Chaser egd, HomomorphismDetector detector, Collection<? extends Constraint> dependencies) {
		Preconditions.checkNotNull(keys);
		Preconditions.checkArgument(!keys.isEmpty());

		if(CollectionUtils.containsAll(keys, configuration.getOutput())) {
			this.keyIndex.put(configuration, keys);
			return true;
		}

		//Search the key cache
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
				log.trace("Cache hit isKey " + keys + "\t" + configuration);
				return true;
			}
		}

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
	 * 
	 * @param annotatedPlan
	 * @param constant
	 * @return
	 * 		the <relation,attribute> pair with the highest quality for the given input constant
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
	 * @param annotatedPlan
	 * @param constant
	 * @return
	 * 		the <annotatedPlan,attribute> pair with the highest quality for the given input constant
	 */
	private Pair<UnaryAnnotatedPlan, Attribute> getHighestQualityAnnotatedPlan(DAGAnnotatedPlan annotatedPlan, Constant constant) {
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
	 * @param left
	 * @param right
	 * @param c
	 * 		the join chase constant
	 * @return the size and the quality of the join of the input annotated plans
	 */
	private Pair<BigInteger, Double> singleEquijoinSizeQualityWithoutHistograms(DAGAnnotatedPlan left, DAGAnnotatedPlan right, Constant c) {
		Pair<Relation, Attribute> leftPair = this.getHighestQualityScoreRelation(left, c);
		Pair<Relation, Attribute> rightPair = this.getHighestQualityScoreRelation(right, c);
		//The cardinality of the join is 1/max(|join attribute from left annotated plan|, |join attribute from right annotated plan|)
		int d = Math.max(this.catalog.getCardinality(leftPair.getLeft(), leftPair.getRight()), 
				this.catalog.getCardinality(rightPair.getLeft(), rightPair.getRight()));
		BigInteger size  = left.getSize().multiply(right.getSize()).divide(BigInteger.valueOf(d));
		double quality = Math.max(left.getQuality(), right.getQuality()) + this.selectivityPenalty;
		return Pair.of(size, quality);
	}

	/**
	 * Returns the size and the quality of the join of the input annotated plans.
	 * The method covers the following case:
	 * 
	 * 	i.	our annotated plan is of the form Compose(Atomic(F1),Atomic(F2)), where
		F1 = R1(\vec(c1)) and F2 = R2(\vec(c2)) are facts.
		ii. there is only one common chase constant c of Atomic(F1) and Atomic(F2)
		iii.we have a histogram on a position of R1 containing constant c in R1 and the same
		in R2
		iv.	the buckets of the histogram are the same, namely B = fb1 : : :bKg
		In this case we could take
		SizeOf(AnnPlan)=\Sum{i\leqK} AvgSize(R1[bi]) \times AvgSize(R2[bi]) \times min{NumDistinct(R1[bi]),NumDistinct(R2[bi])}
		where AvgSize(Rj[bi]) is the average number of tuples in Rj per element in the bucket
		bi and NumDistinct(Rj[bi]) is the number of distinct values of the position corresponding
		to chase constant c in bucket bi of Rj.
		We take QualityOf(AnnPlan) = KHistJoin where KHistJoin is another penalty factor.
	 * 
	 * 
	 * @param left
	 * @param right
	 * @param c
	 * 		the join chase constant
	 * @return the size and the quality of the join of the input annotated plans
	 */
	private Pair<BigInteger, Double> singleEquijoinSizeQualityWithHistograms(UnaryAnnotatedPlan left, UnaryAnnotatedPlan right, Constant c) {		
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
		Preconditions.checkArgument(size > 0);
		double quality = Math.max(left.getQuality(), right.getQuality()) + this.histogramPenalty;
		return Pair.of(new BigDecimal(size).toBigInteger(), quality);
	}

	/**
	 * Returns the size and the quality of the join of the input annotated plans.
	 * The method covers the following case:
	 * 
		i.our annotated plan is of the form Compose(Atomic(F1),Atomic(F2)), where
		F1 = R1(\vec{c1}) and F2 = R2(\vec{c1}) are facts with exactly the same chase constants.
		ii.we have a histogram on a position corresponding to constant c in R1 and similarly
		in R2
		iii.the buckets of the histogram are the same, namely B = {b1,...,bK}
		SizeOf(AnnPlan) = \Sum_{i\leqK}
		min{AvgSize(R1[bi]) \times NumDistinct(R1[bi]),AvgSize(R2[bi]) \time NumDistinct(R2[bi])}
		QualityOf(AnnPlan) = KHistJoin.
	 * 
	 * 
	 * @param left
	 * @param right
	 * @return the size and the quality of the join of the input annotated plans
	 */
	private Pair<BigInteger, Double> intersectionSizeQualityWithHistograms(UnaryAnnotatedPlan left, UnaryAnnotatedPlan right) {
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
		double quality = Math.max(left.getQuality(), right.getQuality()) + this.histogramPenalty;
		return Pair.of(new BigDecimal(size).toBigInteger(), quality);
	}


	/**
	 * 
	 * @param configuration
	 * @param query
	 * @return
	 * 		the cardinality of the input annotated plan after applying the projections of the input    
	 */
	@Override
	public BigInteger cardinalityOf(DAGAnnotatedPlan configuration, Query<?> query) {
		return this.cardinalityOfGroupEstimate(configuration, query);
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
	protected BigInteger cardinalityOfIndependentEstimate(DAGAnnotatedPlan configuration, Query<?> query) {
		if(configuration.getExportedConstants().containsAll(query.getFree2Canonical().values())) {
			if(!query.getFree().isEmpty()) {
				BigDecimal cardinality = BigDecimal.ONE;
				//Estimate the cardinality of each attribute in the final projection of the input query 
				for(Constant constant:query.getFree2Canonical().values()) {
					//Find the relation attribute pair with the highest quality that map to the projected constant  
					Pair<UnaryAnnotatedPlan, Attribute> pair = this.getHighestQualityAnnotatedPlan(configuration, constant);
					//Find the cardinality of the attribute that maps to the projected constant
					Relation baseRelation = pair.getLeft().getRelation();
					int baseCardinality = this.catalog.getCardinality(baseRelation, pair.getRight());

					double selectivity = pair.getLeft().getSize().doubleValue() / this.catalog.getCardinality(baseRelation);
					cardinality = cardinality.multiply(new BigDecimal(baseCardinality)).multiply(new BigDecimal(selectivity));

					log.trace("Configuration: " + configuration + " constant: " + constant);
					log.trace("Base relation: " + pair.getLeft().getRelation() + " base attribute: " + pair.getRight());
					log.trace("Base cardinality: " + baseCardinality);
					log.trace("Selectivity: " + selectivity);
					log.trace("Total cardinality: " + cardinality);
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
	protected BigInteger cardinalityOfGroupEstimate(DAGAnnotatedPlan configuration, Query<?> query) {
		if(configuration.getExportedConstants().containsAll(query.getFree2Canonical().values())) {
			if(!query.getFree().isEmpty()) {
				//Try to assign the maximum number of query's variables to a single annotated plan 

				//Keeps the unary annotated plans to query free variables assignments
				Map<UnaryAnnotatedPlan, Collection<Constant>> assignments = Maps.newHashMap();
				Collection<UnaryAnnotatedPlan> unaryPlans = Sets.newHashSet(configuration.getUnaryAnnotatedPlans());
				Collection<Constant> constants = Sets.newHashSet(query.getFree2Canonical().values());
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
						Pair<UnaryAnnotatedPlan, Attribute> pair = this.getHighestQualityAnnotatedPlan(configuration, constant);
						//Find the cardinality of the attribute that maps to the projected constant
						Relation baseRelation = pair.getLeft().getRelation();
						int baseCardinality = this.catalog.getCardinality(baseRelation, pair.getRight());

						double selectivity = pair.getLeft().getSize().doubleValue() / this.catalog.getCardinality(baseRelation);
						projectionEstimate = projectionEstimate.multiply(new BigDecimal(baseCardinality)).multiply(BigDecimal.valueOf(selectivity));

						log.trace("Configuration: " + configuration + " constant: " + constant);
						log.trace("Base relation: " + pair.getLeft().getRelation() + " base attribute: " + pair.getRight());
						log.trace("Base cardinality: " + baseCardinality);
						log.trace("Selectivity: " + selectivity);
						log.trace("Total cardinality: " + projectionEstimate);
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
	 * 
	 * @param configuration
	 * @return the selectivity of the input annotated plan.
	 * The returned estimate is the ratio of the size of the annotated plan to the cartesian product of the base relations.
	 */
	protected double getSelectivityOf(DAGAnnotatedPlan configuration) {
		double product = 1.0;  
		for(UnaryAnnotatedPlan unary:configuration.getUnaryAnnotatedPlans()) {
			product *= this.catalog.getCardinality(unary.getRelation());
		}
		return configuration.getSize().doubleValue()/product;
	}

}
