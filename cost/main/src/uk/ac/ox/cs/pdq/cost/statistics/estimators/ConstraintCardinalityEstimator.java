package uk.ac.ox.cs.pdq.cost.statistics.estimators;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.plan.Access;
import uk.ac.ox.cs.pdq.plan.Command;
import uk.ac.ox.cs.pdq.plan.CommandToTGDTranslator;
import uk.ac.ox.cs.pdq.plan.Join;
import uk.ac.ox.cs.pdq.plan.NormalisedPlan;
import uk.ac.ox.cs.pdq.plan.Project;
import uk.ac.ox.cs.pdq.plan.Rename;
import uk.ac.ox.cs.pdq.plan.Select;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.EGDChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseListState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Sets;
import com.google.common.base.Preconditions;


/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class ConstraintCardinalityEstimator {

	/** Maps attributes of the temporary tables to attributes in base relations **/
	private final Map<Attribute, Pair<Relation,Attribute>> toBaseAttribute = Maps.newHashMap();

	private final RestrictedChaser restrictedChaser = new RestrictedChaser(null);

	private final EGDChaser egdChaser = new EGDChaser(null);

	private final DBHomomorphismManager detector;
	
	private final Schema schema;
	
	private final Map<Table, Integer> cardinalityCache = Maps.newHashMap();

	public ConstraintCardinalityEstimator(Schema schema, DBHomomorphismManager detector) {
		Preconditions.checkNotNull(detector);
		Preconditions.checkNotNull(schema);
		this.detector = detector;
		this.schema = schema;
	}


	/**
	 * Entry point for cardinality estimation
	 * @param table
	 * @param plan
	 * @param schema
	 * @param catalog
	 * @return 
	 * 		the minimum among the values returned by the constraintDriven, commandDriven and simple methods.
	 */
	public Integer cardinality(Table table, NormalisedPlan plan, Catalog catalog) {
		Pair<Integer, Boolean> v = this.constraintDriven(table, plan, catalog);
		if(v.getRight()) {
			return v.getLeft();
		}
		Integer v2 = this.commandDriven(table, plan, catalog);
		Integer v3 = this.simple(table, plan, catalog);
		return Math.min(Math.min(v2, v3), v.getLeft());
	}


	/**
	 * 
	 * @param table
	 * @param plan
	 * 		The normalised plan that produces this table
	 * @param schema
	 * @param catalog
	 * 		Schema statistics
	 * @return
	 * 		the cardinality of the input table. The second argument of the pair expresses whether the returned value is accurate or not
	 */
	public Pair<Integer,Boolean> constraintDriven(Table table, NormalisedPlan plan, Catalog catalog) {
		Integer approximation = Integer.MAX_VALUE; 
		//Create the query that corresponds to the input table
		Query<?> query = new CommandToTGDTranslator().toQuery(table);	
		//Get the tgds from the input commands;
		NormalisedPlan subplan = plan.getAncestor(table);
		Collection<TGD> forwardTgds = new CommandToTGDTranslator().toTGD(subplan);		
		Collection<TGD> backwardTgds = Sets.newLinkedHashSet();
		for(TGD tgd:forwardTgds) {
			backwardTgds.add(tgd.invert());
		}
		
		//Creates a chase state that consists of the canonical database of the input query.
		ListState state = new DatabaseListState(query, (DBHomomorphismManager) this.detector);
		
		Collection<Constraint> reasonBackward = CollectionUtils.union(this.schema.getDependencies(), backwardTgds);
		reasonBackward.removeAll(this.schema.getKeyDependencies());
		this.restrictedChaser.reasonUntilTermination(state, query, reasonBackward);

		//For all schema statistics
		for(Query<?> statistic:catalog.getExpressions()) {
			//Find all strong matches of the current statistic to facts produced during chasing 			
			List<Match> matches = state.getMatches(statistic, HomomorphismConstraint.isParametrisedMatch(statistic.getFree2Canonical().keySet(), query.getFree2Canonical().values(), true));
			for(Match match:matches) {
				//Apply the match to the statistic and chase backwards
				Map<Variable, Constant> mapping = match.getMapping();

				//Chase backwards the grounded statistic
				statistic.setGrounding(mapping);
				ListState backState = new DatabaseListState(statistic, (DBHomomorphismManager) this.detector);
				Collection<Constraint> reasonForward = CollectionUtils.union(this.schema.getDependencies(), forwardTgds);
				reasonForward.removeAll(this.schema.getKeyDependencies());
				this.restrictedChaser.reasonUntilTermination(backState, statistic, reasonForward);

				//If during backward chasing we are able to reach the canonical database of the query created by the input table
				if(backState.getFacts().containsAll(query.getCanonical().getPredicates())) {
					//then the current statistic provides an accurate estimate
					return Pair.of(catalog.size(statistic), true);
				}
				//Otherwise use it as an upper bound
				approximation = approximation > catalog.size(statistic) ? catalog.size(statistic) : approximation;
			}	
		}
		//If we fail to find any strong match 
		if(approximation == Integer.MAX_VALUE) {
			//For all schema statistics
			for(Query<?> statistic:catalog.getExpressions()) {
				//Find all strong matches of the current statistic to facts produced during chasing 			
				List<Match> matches = state.getMatches(statistic, HomomorphismConstraint.isParametrisedMatch(statistic.getFree2Canonical().keySet(), query.getFree2Canonical().values(), false));
				for(Match match:matches) {
					//Otherwise use it as an upper bound
					approximation = approximation > catalog.size(statistic) ? catalog.size(statistic) : approximation;
				}	
			}
		}
		return Pair.of(approximation, false);
	}


	/**
	 * 
	 * @param table
	 * @param plan
	 * @param schema
	 * @param catalog
	 * @return
	 * 		the cardinality of the input table through analysing the commands that produced the input table. 
	 */
	public Integer commandDriven(Table table, NormalisedPlan plan, Catalog catalog) {
		//Get the command that produced the input table;
		Command command = plan.getCommand(table);
		if(command instanceof Access) {
			//If it is an input free access, return the cardinality of the base table
			if(((Access) command).getInput() == null) {
				return catalog.getCardinality(((Access) command).getRelation());
			}
			//If the access is done using exclusively schema constants then return 1;
			else if(((Access) command).getStaticInputs().size() == ((Access) command).getMethod().getInputs().size()) {
				return 1;
			}
			//else, formulate the access command as a join command
			else {				
				NormalisedPlan subplan = plan.getAncestor(table);
				//Get the tgds from the input commands;
				Collection<TGD> forwardTgds = new CommandToTGDTranslator().toTGD(subplan);
				Collection<AttributeEqualityPredicate> conjunctions = Sets.newLinkedHashSet();
				int other = 0;
				for(Typed typed:((Access) command).getInput().getHeader()) {
					int position = ((Access) command).getRelation().getAttributes().indexOf(typed);
					Preconditions.checkArgument(position >= 0);
					conjunctions.add(new AttributeEqualityPredicate(position, other));
				}
				return this.commandDriven(command, ((Join) command).getLeft(), ((Join) command).getRight(), 
						conjunctions, forwardTgds, plan, catalog);
			}
		}
		else if(command instanceof Select) {
			Table input = ((Select) command).getInput();
			//Remove a single predicate from the set of constant equality predicates
			uk.ac.ox.cs.pdq.algebra.predicates.Predicate p = ((Select) command).getPredicates();
			Preconditions.checkArgument(p instanceof ConjunctivePredicate);
			Collection<uk.ac.ox.cs.pdq.algebra.predicates.Predicate> predicates = p.flatten();

			if(predicates.size() > 1) {
				uk.ac.ox.cs.pdq.algebra.predicates.Predicate singlePredicate = predicates.iterator().next();
				Double singleSelectivity = this.singlePredicateSelectivity(singlePredicate, (Select) command, plan, catalog);

				//Create a new middleware query command using the new predicate
				predicates.remove(singlePredicate);
				Select newCommand = new Select(new ConjunctivePredicate(predicates), input);
				NormalisedPlan newPlan = new NormalisedPlan(plan.getAncestorExclusive(table), newCommand);
				return (int) (singleSelectivity * this.cardinality(newCommand.getOutput(), newPlan, catalog));
			}
			else {
				uk.ac.ox.cs.pdq.algebra.predicates.Predicate singlePredicate = predicates.iterator().next();
				Double singleSelectivity = this.singlePredicateSelectivity(singlePredicate, (Select) command, plan, catalog);
				return (int) (singleSelectivity * this.cardinality(((Select) command).getInput(), plan, catalog));
			}
		}
		else if(command instanceof Project) {
			Table input = ((Project) command).getInput();
			return this.cardinality(input, plan, catalog);
		}
		else if(command instanceof Rename) {
			Table input = ((Rename) command).getInput();
			return this.cardinality(input, plan, catalog);
		}
		else if(command instanceof Join) {
			uk.ac.ox.cs.pdq.algebra.predicates.Predicate p = ((Join) command).getPredicates();
			Preconditions.checkArgument(p instanceof ConjunctivePredicate);

			//Consider key foreign key dependencies
			//Get the tgds from the input commands;
			NormalisedPlan subplan = plan.getAncestor(table);
			Collection<TGD> forwardTgds = new CommandToTGDTranslator().toTGD(subplan);
			
			Collection<AttributeEqualityPredicate> conjunctions = Sets.newLinkedHashSet();
			conjunctions.addAll((Collection<? extends AttributeEqualityPredicate>) p.flatten());
			return this.commandDriven(command, ((Join) command).getLeft(), ((Join) command).getRight(), 
					conjunctions, forwardTgds, plan, catalog);	
		}
		throw new java.lang.IllegalArgumentException("Unknown command");

	}
	
	/**
	 * The join case of the command-driven cardinality estimation method
	 * @param left
	 * @param right
	 * @param p
	 * @param forwardTgds
	 * @param plan
	 * @param schema
	 * @param catalog
	 * @return
	 */
	public Integer commandDriven(Command command, Table left, Table right, Collection<AttributeEqualityPredicate> conjunctions, Collection<TGD> forwardTgds, NormalisedPlan plan, Catalog catalog) {
		//Find the candidate keys. These will be the attributes that are joined.
		List<Attribute> keys = Lists.newArrayList(); 
		for(AttributeEqualityPredicate singlePredicate:conjunctions) {
			int leftIndex = singlePredicate.getPosition();
			Attribute leftAttr = (Attribute) left.getHeader().get(leftIndex);
			keys.add(leftAttr);
		}
		
		//If there is a key foreign key dependency from right to left
		if(this.isKey(left, keys, CollectionUtils.union(schema.getDependencies(), forwardTgds)) &&
				this.existsInclustionDependency(right, left, forwardTgds)
				) {
			return this.cardinality(right, plan, catalog);
		}
		
		//If there is a key foreign key dependency from left to right
		if(this.isKey(right, keys, CollectionUtils.union(schema.getDependencies(), forwardTgds)) &&
				this.existsInclustionDependency(left, right, forwardTgds)
				) {
			return this.cardinality(left, plan, catalog);
		}
		
		Integer cardinality = 1;
		if(conjunctions.size() > 1) {
			//Break up into multiple single attribute joins and use independence
			for(AttributeEqualityPredicate current:conjunctions) {
				//Rename the attributes of the left-hand side table except the one that appears in the 
				//current single attribute predicate 
				List<Attribute> renaming = Lists.newArrayList();
				int position = 0;
				for(Typed attribute:left.getHeader()) {
					if(position == current.getPosition()) {
						renaming.add((Attribute)attribute);
					}
					else {
						renaming.add(this.createFreshAttribute(attribute));
					}
					++position;
				}
				Rename rename = new Rename(renaming, left);
				Table newLeft = rename.getOutput();
				Join newJoin = new Join(newLeft, right, current);
				//Add the newly created commands to the plan
				NormalisedPlan newPlan = new NormalisedPlan(plan.getAncestor(command), rename, newJoin);
				cardinality *= this.cardinality(newLeft, newPlan, catalog);
			}
		}
		else {
			AttributeEqualityPredicate singlePredicate = conjunctions.iterator().next();
			//Get the base attributes of the input predicate 
			int leftIndex = singlePredicate.getPosition();
			Attribute leftAttr = (Attribute) left.getHeader().get(leftIndex);
			Pair<Relation, Attribute> leftPair = this.toBaseAttribute(leftAttr, left, plan);

			int rightIndex = singlePredicate.getOther();
			Attribute rightAttr = (Attribute) right.getHeader().get(rightIndex);
			Pair<Relation, Attribute> rightPair = this.toBaseAttribute(rightAttr, right, plan);

			//Get the selectivity of the left attribute wrt the right attribute and vice versa
			double left2right = catalog.getCardinality(leftPair.getLeft(), leftPair.getRight()) / 
					catalog.getCardinality(rightPair.getLeft(), rightPair.getRight());

			double right2left = catalog.getCardinality(rightPair.getLeft(), rightPair.getRight()) /
					catalog.getCardinality(leftPair.getLeft(), leftPair.getRight());

			int l2r = (int) (left2right * this.cardinality(left, plan, catalog));
			int r2l = (int) (right2left * this.cardinality(right, plan, catalog));
			return Math.min(l2r, r2l);
		}
		return null;
	}

	/**
	 * 
	 * @param table
	 * @param plan
	 * @param schema
	 * @param catalog
	 * @return the cardinality of the input table using independence assumption
	 */
	public Integer simple(Table table, NormalisedPlan plan, Catalog catalog) {
		Integer cardinality = 1;
		for(Typed attribute:table.getHeader()) {
			Pair<Relation, Attribute> pair = this.toBaseAttribute((Attribute) attribute, table, plan);
			cardinality *= catalog.getCardinality(pair.getLeft(), pair.getRight());
		}
		return cardinality;
	}

	/**
	 * 
	 * @param input
	 * @return
	 * 		a fresh attribute prefixed by "???"
	 */
	private Attribute createFreshAttribute(Typed input) {
		String prefix = "???";
		Preconditions.checkArgument(input instanceof Attribute);
		return new Attribute(input.getType(), prefix + ((Attribute) input).getName());
	}

	/**
	 * 
	 * @param input
	 * @param table
	 * @param plan
	 * @return
	 * 		the relation and the attribute of the first access that produced this table attribute
	 */
	private Pair<Relation,Attribute> toBaseAttribute(Attribute input, Table table, NormalisedPlan plan) {
		Pair<Relation,Attribute> output = this.toBaseAttribute.get(input);
		if(output != null) { 
			return output; 
		}
		else {
			for(Access access: plan.getAccessCommands()) {
				int index = access.getOutput().getHeader().indexOf(input);
				if(index != -1) {
					this.toBaseAttribute.put(input, Pair.of(access.getRelation(), access.getRelation().getAttribute(index)));
					return Pair.of(access.getRelation(), access.getRelation().getAttribute(index));
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param singlePredicate
	 * @param command
	 * @param plan
	 * @param catalog
	 * @return
	 * 		the selectivity of the constant equality predicate after mapping the attribute of the input predicate to a base reltion attribute
	 */
	private double singlePredicateSelectivity(uk.ac.ox.cs.pdq.algebra.predicates.Predicate singlePredicate, Select command, NormalisedPlan plan, Catalog catalog){
		//Get the relation and the attribute of the selection predicate
		Preconditions.checkArgument(singlePredicate instanceof ConstantEqualityPredicate);
		TypedConstant<?> constant = ((ConstantEqualityPredicate)singlePredicate).getValue();
		int attrIndex = ((ConstantEqualityPredicate)singlePredicate).getPosition();
		Attribute attr = (Attribute) command.getInput().getHeader().get(attrIndex);
		Pair<Relation, Attribute> pair = this.toBaseAttribute(attr, command.getInput(), plan);
		//and estimate its selectivity using the database catalog
		return catalog.getSelectivity(pair.getLeft(), pair.getRight(), constant);
	}

	/**
	 * 
	 * @param left
	 * @param right
	 * @return
	 * 		returns true if there is an inclusion dependency from left to right on the common variables 
	 */
	public boolean existsInclustionDependency(Table left, Table right, Collection<? extends Constraint> constraints) {
		Query<?> lquery = new CommandToTGDTranslator().toQuery(left);
		
		//Find the variables shared among the tables
		//These should be preserved when checking for entailment 
		List<Attribute> _toShare = Lists.newArrayList();
		_toShare.addAll((Collection<? extends Attribute>) CollectionUtils.intersection(left.getHeader(),right.getHeader()));
		Query<?> rquery = new CommandToTGDTranslator().toQuery(right, _toShare);
		Map<Variable, Constant> _toPreserve = Utility.retain(lquery.getFree2Canonical(), Utility.typedToVariable(_toShare));
	
		//Creates a chase state that consists of the canonical database of the input query.
		ListState state = new DatabaseListState(lquery, (DBHomomorphismManager) this.detector);
		return this.restrictedChaser.entails(state, _toPreserve, rquery, constraints);
	}

	/**
	 * 
	 * @param table
	 * @param candidateKeys
	 * @return
	 * 		true if the input set of attributes is a key of the input table
	 */
	public boolean isKey(Table table, List<Attribute> candidateKeys, Collection<? extends Constraint> constraints) {
		//Create the set of EGDs that correspond to the given table and keys
		EGD egd = EGD.getEGDs(new Signature(table.getName(),table.getHeader().size()), (List<Attribute>) table.getHeader(), candidateKeys);
		
		Query<?> lquery = new ConjunctiveQuery(new Predicate(new Signature("Q", egd.getFree().size()), egd.getFree()), egd.getLeft());
		
		Query<?> rquery = new ConjunctiveQuery(new Predicate(new Signature("Q", egd.getRight().getTerms().size()), egd.getRight().getTerms()), 
				Conjunction.of(egd.getRight().getPredicates()));
		
		//Creates a chase state that consists of the canonical database of the input query.
		ListState state = new DatabaseListState(lquery, (DBHomomorphismManager) this.detector);
		return this.egdChaser.entails(state, lquery.getFree2Canonical(), rquery, constraints);
	}


}
