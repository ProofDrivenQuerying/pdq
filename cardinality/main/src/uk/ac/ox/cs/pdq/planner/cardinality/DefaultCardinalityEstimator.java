package uk.ac.ox.cs.pdq.planner.cardinality;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.UnaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseEGDState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseEGDState.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class DefaultCardinalityEstimator implements CardinalityEstimator {
	
	private final Catalog catalog;
	
	private final double nonKeyPenalty = 1;
	
	private final double selectivityPenalty = 1;
	
	private final double distinctPenalty = 1;
	
	public DefaultCardinalityEstimator(Catalog catalog) {
		Preconditions.checkNotNull(catalog);
		this.catalog = catalog;
	}

	@Override
	public Pair<Integer,Double> sizeQualityOf(UnaryAnnotatedPlan configuration) {
		Relation relation = configuration.getRelation();
		return Pair.of(this.catalog.getCardinality(relation),this.catalog.getQuality(relation));
	}

	@Override
	public Pair<Integer,Double> sizeQualityOf(DAGAnnotatedPlan left, DAGAnnotatedPlan right, Chaser egd, HomomorphismDetector detector, Collection<? extends Constraint> dependencies) {
		//Find constants that are shared among the annotated plans
		Collection<Constant> constants = CollectionUtils.intersection(left.getOutput(), right.getOutput());
		Boolean rightKey = null;
		Boolean leftKey = null;
		if(this.hasID(constants, left, right) && (rightKey = this.isKey(constants, right, egd, detector, dependencies)) ) {
			return Pair.of(left.getSize(), left.getQuality());
		}
		else if(this.hasID(constants, right, left) && (leftKey = this.isKey(constants, left, egd, detector, dependencies)) ) {
			return Pair.of(right.getSize(), right.getQuality());
		}
		
		if(rightKey == null) {
			rightKey = this.isKey(constants, right, egd, detector, dependencies);
		}
		if(rightKey) {
			return Pair.of(left.getSize(), left.getQuality() + this.nonKeyPenalty);
		}
		
		if(leftKey == null) {
			leftKey = this.isKey(constants, left, egd, detector, dependencies);
		}
		if(leftKey) {
			return Pair.of(right.getSize(), right.getQuality() + this.nonKeyPenalty);
		}
		
		else {
			if(constants.size() == 1) {
				Constant constant = constants.iterator().next();
				return this.singleEquijoinSizeQuality(left, right, constant);
			}
			else if(constants.size() > 1) {
				
				Pair<Integer, Double> sizeQuality = Pair.of(Integer.MAX_VALUE, Double.MAX_VALUE);
				for(Constant constant:constants) {
					 Pair<Integer, Double> estimate = this.singleEquijoinSizeQuality(left, right, constant);
					 if(sizeQuality.getLeft() > estimate.getLeft() && sizeQuality.getRight() > estimate.getRight()) {
						 sizeQuality = estimate;
					 }
				}
				return Pair.of(sizeQuality.getLeft(), sizeQuality.getRight() + this.distinctPenalty);
			}
			if(left.getOutput().equals(right.getOutput())) {
				return Pair.of(Math.min(left.getSize(), right.getSize()),
						Math.max(left.getQuality(), right.getQuality()));
			}
		}
		return null;
	}

	@Override
	public double adjustedQualityOf(UnaryAnnotatedPlan configuration) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double adjustedQualityOf(DAGAnnotatedPlan left, DAGAnnotatedPlan right) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public CardinalityEstimator clone() {
		return null;
	}

	private boolean isKey(Collection<Constant> keys, DAGAnnotatedPlan configuration, Chaser egd, HomomorphismDetector detector, Collection<? extends Constraint> dependencies) {
		//Map to each constant in keys a new name
		Map<Constant,Skolem> map = Maps.newHashMap();
		for(Constant constant:keys) {
			if(constant instanceof Skolem) {
				map.put(constant, new Skolem(((Skolem) constant).getName()));
			}
		}

		//Create a copy of the facts in the input configuration with the renamed constants
		Collection<Predicate> copiedFacts = Sets.newHashSet();
		for(Predicate fact:configuration.getOutputFacts()) {
			if(CollectionUtils.containsAny(fact.getTerms(), keys)) {
				List<Term> copiedTerms = Lists.newArrayList();
				for(Term originalTerm:fact.getTerms()) {
					if(map.get(originalTerm) != null) {
						copiedTerms.add(map.get(originalTerm));
					}
					else {
						copiedTerms.add(originalTerm);
					}
				}
				copiedFacts.add(new Predicate(fact.getSignature(), copiedTerms));
			}
		}
		
		DatabaseEGDState state = new DatabaseEGDState((DBHomomorphismManager)detector, CollectionUtils.union(configuration.getOutputFacts(), copiedFacts));
		egd.reasonUntilTermination(state, null, dependencies);
		
		EqualConstantsClasses classes = state.getConstantClasses();
		for(Constant key:keys) {
			if(!classes.getClass(key).equals(classes.getClass(map.get(key)))) {
				return false;
			}
		}
		return true;
	}
	
	private boolean hasID(Collection<Constant> constants, DAGAnnotatedPlan left, DAGAnnotatedPlan right) {
		return right.getOutputFacts().containsAll(left.getOutputFacts());
	}
	
	private Pair<Relation,Attribute> getHightestQualityScoreRelation(DAGAnnotatedPlan annotatedPlan, Constant constant) {
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
	
	private Pair<Integer, Double> singleEquijoinSizeQuality(DAGAnnotatedPlan left, DAGAnnotatedPlan right, Constant constant) {
		Pair<Relation, Attribute> leftPair = getHightestQualityScoreRelation(left, constant);
		Pair<Relation, Attribute> rightPair = getHightestQualityScoreRelation(right, constant);
		int size  = (left.getSize() * right.getSize()) / 
				Math.max(this.catalog.getCardinality(leftPair.getLeft(), leftPair.getRight()), 
						this.catalog.getCardinality(rightPair.getLeft(), rightPair.getRight()));
		double quality = Math.max(left.getQuality(), right.getQuality()) + this.selectivityPenalty;
		
		return Pair.of(size, quality);
	}

}
