package uk.ac.ox.cs.pdq.plan;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConstantEqualityPredicate;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Translates normalised plan commands to tgds
 * @author Efthymia Tsamoura
 *
 */
public class CommandToTGDTranslator {
	
	public Collection<TGD> toTGD(NormalisedPlan plan) {
		Preconditions.checkNotNull(plan);
		Collection<TGD> tgds = Sets.newLinkedHashSet();
		for(Command command:plan.getCommands()) {
			tgds.add(this.toTGD(command));
		}
		return tgds;
	}

	public TGD toTGD(Command command) {
		if(command instanceof AccessCommand) {
			return this.toTGD((AccessCommand)command); 
		}
		else if(command instanceof ProjectCommand) {
			return this.toTGD((ProjectCommand)command); 
		}
		else if(command instanceof SelectCommand) {
			return this.toTGD((SelectCommand)command); 
		}
		else if(command instanceof JoinCommand) {
			return this.toTGD((JoinCommand)command); 
		}
		else if(command instanceof RenameCommand) {
			return this.toTGD((RenameCommand)command); 
		}
		throw new java.lang.IllegalArgumentException("Unknown command " + command);
	}


	public TGD toTGD(AccessCommand command) {
		//The access is input free
		if(command.getInput() == null) {
			//Prepare the right-hand side
			Predicate relation = new Predicate(command.getRelation(), command.getColumns());
			//Prepare the left-hand side
			Predicate ti = new Predicate(new Signature(command.getOutput().getName(), command.getRelation().getArity()), command.getColumns());
			return new TGD(Conjunction.of(relation), Conjunction.of(ti));
		}
		else {
			//Get the terms of the input table
			List<Term> suppliedInputs = Utility.typedToTerms(command.getInput().getHeader());
			//Get the schema constants appearing in input positions of the relation
			Map<Integer, TypedConstant<?>> staticInputs = command.getStaticInputs();

			Predicate relation = null;
			if(staticInputs == null) {
				relation = new Predicate(command.getRelation(), command.getColumns());
			} 
			else {
//				//Put the schema constants inside the relation atom 
//				List<Term> terms = Lists.newArrayList(command.getColumns());
//				for(Entry<Integer, TypedConstant<?>> entry:staticInputs.entrySet()) {
//					terms.set(entry.getKey(), entry.getValue());
//				}
				throw new java.lang.UnsupportedOperationException();
			}
			Predicate ti = new Predicate(new Signature(command.getOutput().getName(), command.getRelation().getArity()), command.getColumns());
			Predicate tj = new Predicate(new Signature(command.getInput().getName(), command.getInput().getHeader().size()), suppliedInputs);
			return new TGD(Conjunction.of(relation, tj), Conjunction.of(ti));
		}
	}

	public TGD toTGD(ProjectCommand command) {
		//Get the attributes to project and create variables
		List<Term> projected = Utility.typedToTerms(command.getOutput().getHeader());
		Predicate ti = new Predicate(new Signature(command.getOutput().getName(), projected.size()), projected);
		
		List<Term> inputs = Utility.typedToTerms(command.getInput().getHeader());
		Predicate tj = new Predicate(new Signature(command.getInput().getName(), inputs.size()), inputs);
		
		return new TGD(Conjunction.of(tj), Conjunction.of(ti));
	}

	public TGD toTGD(SelectCommand command) {		
		List<Term> inputs = Utility.typedToTerms(command.getInput().getHeader());
		uk.ac.ox.cs.pdq.algebra.predicates.Predicate predicates = command.getPredicates();
		if(predicates instanceof ConjunctivePredicate) {
			Iterator<uk.ac.ox.cs.pdq.algebra.predicates.Predicate> iterator = ((ConjunctivePredicate) predicates).iterator();
			while(iterator.hasNext()) {
				uk.ac.ox.cs.pdq.algebra.predicates.Predicate p = iterator.next();
				if(p instanceof AttributeEqualityPredicate) {
					int other = ((AttributeEqualityPredicate) p).getOther();
					int position = ((AttributeEqualityPredicate) p).getPosition();
					inputs.set(position, inputs.get(other));
				}
				//Create a new constant equality predicate
				else if(p instanceof ConstantEqualityPredicate) {
					int position = ((ConstantEqualityPredicate) p).getPosition();
					TypedConstant<?> value = ((ConstantEqualityPredicate) p).getValue();
					inputs.set(position, value);	
				}
			}
		}
		List<Term> outputs = Utility.typedToTerms(command.getOutput().getHeader());
		Predicate ti = new Predicate(new Signature(command.getOutput().getName(), outputs.size()), outputs);
		
		Predicate tj = new Predicate(new Signature(command.getInput().getName(), inputs.size()), inputs);
		return new TGD(Conjunction.of(tj), Conjunction.of(ti));
	}

	public TGD toTGD(JoinCommand command) {
		List<Term> outputs = Utility.typedToTerms(command.getOutput().getHeader());
		Predicate ti = new Predicate(new Signature(command.getOutput().getName(), outputs.size()), outputs);
		
		List<Term> left = Utility.typedToTerms(command.getLeft().getHeader());
		Predicate tj = new Predicate(new Signature(command.getLeft().getName(), left.size()), left);
		
		List<Term> right = Utility.typedToTerms(command.getRight().getHeader());
		Predicate tk = new Predicate(new Signature(command.getRight().getName(), right.size()), right);
		
		return new TGD(Conjunction.of(tj, tk), Conjunction.of(ti));
	}

	public TGD toTGD(RenameCommand command) {
		//Get the renamed attributes and create variables
		List<Term> renamed = Utility.typedToTerms(command.getOutput().getHeader());
		Predicate ti = new Predicate(new Signature(command.getOutput().getName(), renamed.size()), renamed);
		
		List<Term> inputs = Utility.typedToTerms(command.getInput().getHeader());
		Predicate tj = new Predicate(new Signature(command.getInput().getName(), inputs.size()), inputs);
		
		return new TGD(Conjunction.of(tj), Conjunction.of(ti));
	}
	
	/**
	 * 
	 * @param table
	 * @return
	 * 		a query created from the input table.
	 * 		The query's free variables are the all the terms created from the table's attributes
	 */
	public Query<?> toQuery(Table table) {
		return this.toQuery(table, (List<Attribute>) table.getHeader());
	}
	
	public Query<?> toQuery(Table table, List<Attribute> free) {
		//Get the attributes to project and create variables
		List<Term> _free = Utility.typedToTerms(free);
		List<Term> _body = Utility.typedToTerms(table.getHeader());
		Predicate ti = new Predicate(new Signature(table.getName(), _body.size()), _body);
		return new ConjunctiveQuery("Q", _free, Conjunction.of(ti));
	}
	
	public Query<?> toQuery(List<Term> free, Table... tables) {
		Collection<Predicate> ti = Lists.newArrayList();
		for(Table table:tables) {
			List<Term> terms = Utility.typedToTerms(table.getHeader());
			ti.add( new Predicate(new Signature(table.getName(), terms.size()), terms));
		}
		return new ConjunctiveQuery("Q", free, Conjunction.of(ti));
	}
	
	public Query<?> toQuery(Relation relation, Attribute... free) {
		return this.toQuery(relation, Lists.newArrayList(free));
	}
	
	public Query<?> toQuery(Relation relation, List<Attribute> free) {
		Collection<Predicate> ti = Lists.newArrayList();
		List<Term> _free = Utility.typedToTerms(free);
		List<Term> _body = Utility.typedToTerms(Lists.newArrayList(relation.getAttributes()));
		ti.add(new Predicate(new Signature(relation.getName(), _body.size()), _body));
		return new ConjunctiveQuery("Q", _free, Conjunction.of(ti));
	}
	
	public Query<?> toQuery(Relation relation, Map<Attribute, TypedConstant> constantsMap, List<Attribute> free) {
		Collection<Predicate> ti = Lists.newArrayList();
		List<Term> _free = Utility.typedToTerms(free);
		List<Term> _body = Utility.typedToTerms(Lists.newArrayList(relation.getAttributes()));
		for(Entry<Attribute, TypedConstant> entry:constantsMap.entrySet()) {
			int indexOf = relation.getAttributes().indexOf(entry.getKey());
			Preconditions.checkArgument(indexOf >= 0);
			_body.set(indexOf, entry.getValue());
		}
		ti.add(new Predicate(new Signature(relation.getName(), _body.size()), _body));
		return new ConjunctiveQuery("Q", _free, Conjunction.of(ti));
	}

}
