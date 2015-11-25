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
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.plan.AccessCommand;
import uk.ac.ox.cs.pdq.plan.Command;
import uk.ac.ox.cs.pdq.plan.CommandToTGDTranslator;
import uk.ac.ox.cs.pdq.plan.JoinCommand;
import uk.ac.ox.cs.pdq.plan.ProjectCommand;
import uk.ac.ox.cs.pdq.plan.RenameCommand;
import uk.ac.ox.cs.pdq.plan.SelectCommand;
import uk.ac.ox.cs.pdq.plan.SequentialPlan;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.EGDChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseListState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;
import uk.ac.ox.cs.pdq.reasoning.utility.ReasonerUtility;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Typed;

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
	public Integer cardinality(Table table, SequentialPlan plan, Catalog catalog) {
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
	public Pair<Integer,Boolean> constraintDriven(Table table, SequentialPlan plan, Catalog catalog) {
		Integer approximation = Integer.MAX_VALUE; 
		//Create the query that corresponds to the input table
		Query<?> query = new CommandToTGDTranslator().toQuery(table);	
		//Get the tgds from the input commands;
		SequentialPlan subplan = plan.getAncestor(table);
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
	public Integer commandDriven(Table table, SequentialPlan plan, Catalog catalog) {
		//Get the command that produced the input table;
		Command command = plan.getCommand(table);
		if(command instanceof AccessCommand) {
			//If it is an input free access, return the cardinality of the base table
			if(((AccessCommand) command).getInput() == null) {
				return catalog.getCardinality(((AccessCommand) command).getRelation());
			}
			//If the access is done using exclusively schema constants then return 1;
			else if(((AccessCommand) command).getStaticInputs().size() == ((AccessCommand) command).getMethod().getInputs().size()) {
				return 1;
			}
			//else, formulate the access command as a join command
			else {				
				SequentialPlan subplan = plan.getAncestor(table);
				//Get the tgds from the input commands;
				Collection<TGD> forwardTgds = new CommandToTGDTranslator().toTGD(subplan);
				Collection<AttributeEqualityPredicate> conjunctions = Sets.newLinkedHashSet();
				int other = 0;
				for(Typed typed:((AccessCommand) command).getInput().getHeader()) {
					int position = ((AccessCommand) command).getRelation().getAttributes().indexOf(typed);
					Preconditions.checkArgument(position >= 0);
					conjunctions.add(new AttributeEqualityPredicate(position, other));
				}
				return this.commandDriven(command, ((JoinCommand) command).getLeft(), ((JoinCommand) command).getRight(), 
						conjunctions, forwardTgds, plan, catalog);
			}
		}
		else if(command instanceof SelectCommand) {
			Table input = ((SelectCommand) command).getInput();
			//Remove a single predicate from the set of constant equality predicates
			uk.ac.ox.cs.pdq.algebra.predicates.Predicate p = ((SelectCommand) command).getPredicates();
			Preconditions.checkArgument(p instanceof ConjunctivePredicate);
			Collection<uk.ac.ox.cs.pdq.algebra.predicates.Predicate> predicates = p.flatten();

			if(predicates.size() > 1) {
				uk.ac.ox.cs.pdq.algebra.predicates.Predicate singlePredicate = predicates.iterator().next();
				Double singleSelectivity = this.singlePredicateSelectivity(singlePredicate, (SelectCommand) command, plan, catalog);

				//Create a new middleware query command using the new predicate
				predicates.remove(singlePredicate);
				SelectCommand newCommand = new SelectCommand(new ConjunctivePredicate(predicates), input);
				SequentialPlan newPlan = new SequentialPlan(plan.getAncestorExclusive(table), newCommand);
				return (int) (singleSelectivity * this.cardinality(newCommand.getOutput(), newPlan, catalog));
			}
			else {
				uk.ac.ox.cs.pdq.algebra.predicates.Predicate singlePredicate = predicates.iterator().next();
				Double singleSelectivity = this.singlePredicateSelectivity(singlePredicate, (SelectCommand) command, plan, catalog);
				return (int) (singleSelectivity * this.cardinality(((SelectCommand) command).getInput(), plan, catalog));
			}
		}
		else if(command instanceof ProjectCommand) {
			Table input = ((ProjectCommand) command).getInput();
			return this.cardinality(input, plan, catalog);
		}
		else if(command instanceof RenameCommand) {
			Table input = ((RenameCommand) command).getInput();
			return this.cardinality(input, plan, catalog);
		}
		else if(command instanceof JoinCommand) {
			uk.ac.ox.cs.pdq.algebra.predicates.Predicate p = ((JoinCommand) command).getPredicates();
			Preconditions.checkArgument(p instanceof ConjunctivePredicate);

			//Consider key foreign key dependencies
			//Get the tgds from the input commands;
			SequentialPlan subplan = plan.getAncestor(table);
			Collection<TGD> forwardTgds = new CommandToTGDTranslator().toTGD(subplan);
			
			Collection<AttributeEqualityPredicate> conjunctions = Sets.newLinkedHashSet();
			conjunctions.addAll((Collection<? extends AttributeEqualityPredicate>) p.flatten());
			return this.commandDriven(command, ((JoinCommand) command).getLeft(), ((JoinCommand) command).getRight(), 
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
	public Integer commandDriven(Command command, Table left, Table right, Collection<AttributeEqualityPredicate> conjunctions, Collection<TGD> forwardTgds, SequentialPlan plan, Catalog catalog) {
		//Find the candidate keys. These will be the attributes that are joined.
		List<Attribute> keys = Lists.newArrayList(); 
		for(AttributeEqualityPredicate singlePredicate:conjunctions) {
			int leftIndex = singlePredicate.getPosition();
			Attribute leftAttr = (Attribute) left.getHeader().get(leftIndex);
			keys.add(leftAttr);
		}
		
		//If there is a key foreign key dependency from right to left
		if(new ReasonerUtility().isKey(left, keys, CollectionUtils.union(this.schema.getDependencies(), forwardTgds), this.egdChaser, this.detector) &&
				new ReasonerUtility().existsInclustionDependency(right, left, forwardTgds, this.restrictedChaser, this.detector)
				) {
			return this.cardinality(right, plan, catalog);
		}
		
		//If there is a key foreign key dependency from left to right
		if(new ReasonerUtility().isKey(right, keys, CollectionUtils.union(this.schema.getDependencies(), forwardTgds), this.egdChaser, this.detector) &&
				new ReasonerUtility().existsInclustionDependency(left, right, forwardTgds, this.restrictedChaser, this.detector)
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
				RenameCommand rename = new RenameCommand(renaming, left);
				Table newLeft = rename.getOutput();
				JoinCommand newJoin = new JoinCommand(newLeft, right, current);
				//Add the newly created commands to the plan
				SequentialPlan newPlan = new SequentialPlan(plan.getAncestor(command), rename, newJoin);
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
	public Integer simple(Table table, SequentialPlan plan, Catalog catalog) {
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
	private Pair<Relation,Attribute> toBaseAttribute(Attribute input, Table table, SequentialPlan plan) {
		Pair<Relation,Attribute> output = this.toBaseAttribute.get(input);
		if(output != null) { 
			return output; 
		}
		else {
			for(AccessCommand access: plan.getAccessCommands()) {
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
	private double singlePredicateSelectivity(uk.ac.ox.cs.pdq.algebra.predicates.Predicate singlePredicate, SelectCommand command, SequentialPlan plan, Catalog catalog){
		//Get the relation and the attribute of the selection predicate
		Preconditions.checkArgument(singlePredicate instanceof ConstantEqualityPredicate);
		TypedConstant<?> constant = ((ConstantEqualityPredicate)singlePredicate).getValue();
		int attrIndex = ((ConstantEqualityPredicate)singlePredicate).getPosition();
		Attribute attr = (Attribute) command.getInput().getHeader().get(attrIndex);
		Pair<Relation, Attribute> pair = this.toBaseAttribute(attr, command.getInput(), plan);
		//and estimate its selectivity using the database catalog
		return catalog.getSelectivity(pair.getLeft(), pair.getRight(), constant);
	}

}
