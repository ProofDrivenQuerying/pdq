package uk.ac.ox.cs.pdq.test.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.wrappers.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;
import uk.ac.ox.cs.pdq.runtime.exec.AccessException;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.base.Preconditions;

/**
 * Class that checks if the data is consistent w.r.t. the schema dependencies
 * @author Efthymia Tsamoura
 * 
 * The homomorphisms are found using SQL queries. The database facts (relation tuples) have to be stored in a database.
 * This database is created by a DBHomomorphismManager object  
 *
 */

public final class DataValidationImplementation extends DataValidation{

	private final DBHomomorphismManager manager;
	private final List<Constraint> ics;

	/**
	 * Constructor for DataValidationImplementation.
	 * @param schema Schema
	 * @param manager DBHomomorphismManager
	 */
	public DataValidationImplementation(Schema schema, DBHomomorphismManager manager) {
		super(schema);
		this.manager = manager;
		this.ics = schema.getDependencies();
		Preconditions.checkArgument(this.manager != null);
		Preconditions.checkArgument(this.ics != null);
	}

	/**
	 * @return Boolean
	 * @throws AccessException
	 * @throws PlannerException
	 */
	@Override
	public Boolean validate() throws AccessException, PlannerException {
		this.save();
		for(Constraint ic: this.ics) {
			this.validate(ic);
		}
		return true;
	}


	/**
	 * Saves each database fact in the database.
	 * This is done by calling the updateDB function of the member AbstractHomomorphismStatementBuilder object
	 * @throws AccessException
	 * @throws PlannerException
	 */
	private void save() throws AccessException, PlannerException {
		for(Relation relation: this.schema.getRelations()) {
			Table table = ((RelationAccessWrapper) relation).access();
			this.saveRelation(relation, table);
		}
	}

	/**
	 * @param relation Relation
	 * @param table Table
	 * @throws PlannerException
	 */
	private void saveRelation(Relation relation, Table table) throws PlannerException {
		Collection<Predicate> atoms = new HashSet<>();
		for(Tuple tuple: table.getData()) {
			atoms.add(Predicate.makeFact(relation, tuple));
		}
		this.manager.addFacts(atoms);
	}

	/**
	 * 
	 * @param constraint Constraint
	 * @throws PlannerException
	 * @throws AccessException
	 */
	private void validate(Constraint constraint) throws PlannerException, AccessException {
		// Checks if the there exists at least one set of facts that satisfies the left-hand side of the input dependency
		List<Match> matchings = this.manager.getMatches(constraint);
		if (!matchings.isEmpty()) {
			/*
			 * For each set of facts F1 that satisfy the left-hand side of the input dependency check whether or not 
			 * there exists another set of facts F2 that satisfies the right-hand side of the input dependency w.r.t F1 
			 */
			for (Match m: matchings) {
				List<Match> subMatchings = this.manager.getMatches(this.invert(constraint), HomomorphismConstraint.createMapConstraint(m.getMapping()));
				if (subMatchings.isEmpty()) {
					throw new java.lang.IllegalArgumentException("Data does not satisfy constraint " + constraint.toString() );
				}
			}
		}
	}
	
	/**
	 * @param ic Constraint
	 * @return Constraint
	 */
	private static Constraint invert(Constraint ic) {
		Conjunction<Predicate> left = (Conjunction<Predicate>) ic.getLeft();
		Conjunction<Predicate> right = (Conjunction<Predicate>) ic.getRight();
		return new TGD(right, left);
	}

}
