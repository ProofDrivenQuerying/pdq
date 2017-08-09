package uk.ac.ox.cs.pdq.test.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.datasources.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.TriggerProperty;
import uk.ac.ox.cs.pdq.runtime.util.AccessException;

// TODO: Auto-generated Javadoc
/**
 * Class that checks if the data is consistent w.r.t. the schema dependencies
 * The homomorphisms are found using SQL queries. The database facts (relation tuples) have to be stored in a database.
 * This database is created by a DBHomomorphismManager object.
 * 
 * @author georgek
 * @author Efthymia Tsamoura
 */

public final class DataValidationImplementation extends DataValidation{

	/** The chaseState. */
	private final ChaseInstance manager;
	
	/** The ics. */
	private final Dependency[] dependencies;

	/**
	 * Constructor for DataValidationImplementation.
	 * @param schema Schema
	 * @param chaseState DBHomomorphismManager
	 */
	public DataValidationImplementation(Schema schema, ChaseInstance manager) {
		super(schema);
		this.manager = manager;
		this.dependencies = schema.getDependencies();
		Preconditions.checkArgument(this.manager != null);
		Preconditions.checkArgument(this.dependencies != null);
	}

	/**
	 * Validate.
	 *
	 * @return Boolean
	 * @throws AccessException the access exception
	 * @throws PlannerException the planner exception
	 */
	@Override
	public Boolean validate() throws AccessException, PlannerException {
		this.save();
		for(Dependency ic: this.dependencies) {
			this.validate(ic);
		}
		return true;
	}


	/**
	 * Saves each database fact in the database.
	 * This is done by calling the updateDB function of the member AbstractHomomorphismStatementBuilder object
	 *
	 * @throws AccessException the access exception
	 * @throws PlannerException the planner exception
	 */
	private void save() throws AccessException, PlannerException {
		for(Relation relation: this.schema.getRelations()) {
			Table table = ((RelationAccessWrapper) relation).access();
			this.saveRelation(relation, table);
		}
	}

	/**
	 * Save relation.
	 *
	 * @param relation Relation
	 * @param table Table
	 * @throws PlannerException the planner exception
	 */
	private void saveRelation(Relation relation, Table table) throws PlannerException {
		Collection<Atom> atoms = new HashSet<>();
		for(Tuple tuple: table.getData()) {
			atoms.add(makeFact(relation, tuple));
		}
		this.manager.addFacts(atoms);
	}
	
	/**
	 * Make fact.
	 *
	 * @param predicate Predicate
	 * @param tuple Tuple
	 * @return PredicateFormula
	 */
	public static Atom makeFact(Predicate predicate, Tuple tuple) {
		TypedConstant[] terms = new TypedConstant[tuple.size()];
		for (int i = 0, l = tuple.size(); i < l; i++) 
			terms[i++] = TypedConstant.create(tuple.getValue(i));
		return Atom.create(predicate, terms);
	}

	/**
	 * Validate.
	 *
	 * @param constraint Constraint
	 * @throws PlannerException the planner exception
	 * @throws AccessException the access exception
	 */
	private void validate(Dependency constraint) throws PlannerException, AccessException {
		// Checks if the there exists at least one set of facts that satisfies the left-hand side of the input dependency
		List<Match> matchings = this.manager.getTriggers(new Dependency[]{constraint}, TriggerProperty.ACTIVE, null);
		if (!matchings.isEmpty()) {
			/*
			 * For each set of facts F1 that satisfy the left-hand side of the input dependency check whether or not 
			 * there exists another set of facts F2 that satisfies the right-hand side of the input dependency w.r.t F1 
			 */
			for (Match match: matchings) {
				List<Match> subMatchings = this.manager.getTriggers(new Dependency[]{Dependency.create(constraint.getHeadAtoms(), constraint.getBodyAtoms())},TriggerProperty.ACTIVE,null);//, HomomorphismProperty.createMapProperty(m.getMapping()));
				if (subMatchings.isEmpty()) {
					throw new java.lang.IllegalArgumentException("Data does not satisfy constraint " + constraint.toString() );
				}
			}
		}
	}
	
//	/**
//	 * Invert.
//	 *
//	 * @param ic Constraint
//	 * @return Constraint
//	 */
//	private Dependency invert(Dependency ic) {
//		return TGD.create(ic.getHead(), ic.getBody());
//	}

}
