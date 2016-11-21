package uk.ac.ox.cs.pdq.generator.tgdsfromquery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * Provides functions for generating formulas, e.g., queries, views and dependencies.
 * 
 * @author Efthymia Tsamoura
 * @author Julien LEBLAY
 * 
 */
public abstract class AbstractDependencyGenerator {

	/**  The input schema. */
	protected final Schema schema;
	
	/**  The input parameters. */
	protected final BenchmarkParameters params;
	
	/**  A random number generator. */
	protected final Random random;

	/**
	 * Instantiates a new abstract dependency generator.
	 *
	 * @param schema the schema
	 * @param params the params
	 */
	public AbstractDependencyGenerator(Schema schema, BenchmarkParameters params) {
		this.schema = schema;
		this.params = params;
		this.random = new Random(params.getSeed());
	}

	/**
	 * Same body.
	 *
	 * @param tgds the tgds
	 * @param target the target
	 * @return 		true if the target has the same body with one of the tgds in the input collection
	 */
	protected boolean sameBody(Collection<Dependency> tgds, TGD target) {
		for (Dependency tg : tgds) {
			TGD t = (TGD) tg;
			Set<Atom> set1 = new LinkedHashSet<>();
			set1.addAll(t.getHead().getAtoms());
			Set<Atom> set2 = new LinkedHashSet<>();
			set2.addAll(target.getHead().getAtoms());
			if (set1.equals(set2)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Same view.
	 *
	 * @param views the views
	 * @param target the target
	 * @return 		true if the target has the same head/body with one of the views in the input collection
	 */
	protected boolean sameView(List<View> views, View target) {
		for (View mv : views) {
			Set<Atom> set1 = new LinkedHashSet<>();
			set1.addAll(mv.getDependency().getHead().getAtoms());

			Set<Atom> set2 = new LinkedHashSet<>();
			set2.addAll(target.getDependency().getHead().getAtoms());

			Set<Variable> s1 = new LinkedHashSet<>();
			s1.addAll(mv.getDependency().getExistential());

			Set<Variable> s2 = new LinkedHashSet<>();
			s2.addAll(target.getDependency().getExistential());

			if (s1.equals(s2) && set1.equals(set2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A guarded conjunction .
	 *
	 * @param candidates 		Pool of variables to use in the output conjunction 
	 * @param conjuncts 		Number of conjuncts 
	 * @param hasRepeatedRelations 		true if we allow repeated predicates
	 * @return the conjunction info
	 */
	protected ConjunctionInfo createGuardedConjunction(List<Variable> candidates, int conjuncts, boolean hasRepeatedRelations) {
		List<Atom> atoms = new ArrayList<>();
		RelationsInfo info = this.selectRelations(conjuncts, null, hasRepeatedRelations);
		List<Relation> selectedRelations = info.getRelations();
		Relation relation = selectedRelations.get(conjuncts - 1);
		List<Relation> relations = selectedRelations.subList(0, conjuncts - 1);
		List<Variable> varSubList = candidates.subList(0, relation.getArity());
		atoms = this.createConjuncts(relations, varSubList);
		atoms.add(new Atom(relation, varSubList));
		return new ConjunctionInfo(Utility.getVariables(atoms), atoms);

	}

	/**
	 * Creates the un guarded conjunction.
	 *
	 * @param candidates 		Pool of variables to use in the output conjunction 
	 * @param conjuncts 		Number of conjuncts 
	 * @param leftSide the left side
	 * @param hasRepeatedRelations 		true if we allow repeated predicates
	 * @return the conjunction info
	 */
	protected ConjunctionInfo createUnGuardedConjunction(List<Variable> candidates, int conjuncts, List<Atom> leftSide, boolean hasRepeatedRelations) {
		List<Atom> atoms = new ArrayList<>();
		RelationsInfo info = this.selectRelations(conjuncts, leftSide, hasRepeatedRelations);
		List<Relation> selectedRelations = info.getRelations();
		atoms = this.createConjuncts(selectedRelations, candidates);
		return new ConjunctionInfo(Utility.getVariables(atoms), atoms);
	}
	
	/**
	 * Creates the variables.
	 *
	 * @param n the n
	 * @return 		the list <x_0, x_1, ...., x_{n}> of variables
	 */
	protected List<Variable> createVariables(int n) {
		List<Variable> variables = new ArrayList<>();
		for (int index = 0; index < n; ++index) {
			variables.add(new Variable("x" + index));
		}
		return variables;
	}

	/**
	 * Creates the variables.
	 *
	 * @param n the n
	 * @param input the input
	 * @return 		a list of n variables consisting of randomly selected variables from the input and y_i ones
	 */
	protected List<Variable> createVariables(int n, List<Variable> input) {
		List<Variable> variables = new ArrayList<>();
		for (int index = 0; index < n; ++index) {
			if (this.random.nextDouble() > 0.5) {
				variables.add(input.get(this.random.nextInt(input.size())));
			} else {
				variables.add(new Variable("y" + index));
			}
		}
		return variables;
	}

	/**
	 * Select relations.
	 *
	 * @param conjuncts 		a number of relations to select
	 * @param leftSide the left side
	 * @param hasRepeatedRelations 		true if we allow repeated predicates
	 * @return the relations info
	 */
	protected RelationsInfo selectRelations(int conjuncts, List<Atom> leftSide, boolean hasRepeatedRelations) {
		int nbRelations = this.schema.getRelations().size();
		// Without this check, the method goes into an infinite loop
		if (conjuncts > nbRelations) {
			throw new IllegalArgumentException("Attempting to create a query of "
					+ conjuncts + " conjuncts with a schema of "
					+ nbRelations + " relations");
		}

		List<Relation> relations = new ArrayList<>();
		List<Relation> leftRelations = new ArrayList<>();
		if(leftSide != null) {
			for(Atom l: leftSide) {
				leftRelations.add((Relation) l.getPredicate());
			}
		}

		List<Relation> rels = this.schema.getRelationsByArity(this.schema.getMaxArity());
		Relation guard = rels.get(this.random.nextInt(rels.size()));

		Relation relation = null;
		for (int c = 0; c < conjuncts - 1; ++c) {
			do {
				int arity = this.random.nextInt(this.schema.getMaxArity()) + 1;
				rels = this.schema.getRelationsByArity(arity);
				if (!rels.isEmpty()) {
					int r = this.random.nextInt(rels.size());
					relation = rels.get(r);
				}
			} while (relation == null
					|| (!hasRepeatedRelations && relations.contains(relation))
					|| (!hasRepeatedRelations && guard.equals(relation))
					// TODO: Need permanent fix - the condition makes drag this into an infinity loop. 
					|| (!hasRepeatedRelations && leftRelations.contains(relation))
					);
			relations.add(relation);
		}

		relations.add(guard);
		return new RelationsInfo(relations, guard.getArity());
	}

	/**
	 * Creates the conjuncts.
	 *
	 * @param relations the relations
	 * @param variables the variables
	 * @return 		a list of predicates coming from the input relations and populated with variables from the input list
	 */
	protected List<Atom> createConjuncts(List<Relation> relations, List<Variable> variables) {
		List<Atom> conjuncts = new ArrayList<>();
		Relation relation = null;
		for (int r = 0; r < relations.size(); ++r) {
			relation = relations.get(r);
			List<Variable> arguments = new ArrayList<>();
			for (int index = 0; index < relation.getArity(); ++index) {
				arguments.add(variables.get(this.random.nextInt(variables.size())));
			}
			conjuncts.add(new Atom(relation, arguments));
		}
		return conjuncts;
	}
	
	/**
	 * Creates the chain conjuncts.
	 *
	 * @param atoms the atoms
	 * @return 		a list of chain atoms coming from the input relations. Each atom has one join variable with its successor
	 */
	protected List<Atom> createChainConjuncts(List<Atom> atoms) {
		int xCounter = 0;
		List<Atom> chainAtoms = new ArrayList<>();
		Term joinTerm = null;
		for (Atom atom : atoms) {
			List<Term> nTerms = new ArrayList<>();
			for (int index = 0; index < atom.getTerms().size(); ++index) {
				if ((joinTerm == null) || index > 0) {
					nTerms.add(new Variable("x" + xCounter));
					joinTerm = new Variable("x" + xCounter);
					xCounter++;
				} else if (index == 0) {
					nTerms.add(joinTerm);
				}
			}
			chainAtoms.add(new Atom(atom.getPredicate(), nTerms));
		}
		return chainAtoms;
	}

	/**
	 * Pick a body.size() * ratio variable out of body's variables.
	 *
	 * @param body the body
	 * @return the list
	 */
	protected List<Variable> pickFreeVariables(Collection<Atom> body) {
		Set<Variable> result = new LinkedHashSet<>();
		if (this.params.getFreeVariable() > 0.0) {
			Set<Term> allTerms = new LinkedHashSet<>();
			for (Atom a : body) {
				allTerms.addAll(a.getTerms());
			}
			int resultSize = (int) Math.max(1, allTerms.size() * this.params.getFreeVariable());
			List<Variable> varIds = Utility.getVariables(body);
			while (result.size() < resultSize) {
				result.add(varIds.get(this.random.nextInt(varIds.size())));
			}
		}
		return new ArrayList<>(result);
	}


	/**
	 * The Class RelationsInfo.
	 */
	public class RelationsInfo {

		/** The relations. */
		List<Relation> relations;
		
		/** The max arity. */
		int maxArity;

		/**
		 * Instantiates a new relations info.
		 *
		 * @param relations the relations
		 * @param maxArity the max arity
		 */
		public RelationsInfo(List<Relation> relations, int maxArity) {
			this.relations = relations;
			this.maxArity = maxArity;
		}

		/**
		 * Gets the relations.
		 *
		 * @return the relations
		 */
		public List<Relation> getRelations() {
			return this.relations;
		}

		/**
		 * Gets the max arity.
		 *
		 * @return the max arity
		 */
		public int getMaxArity() {
			return this.maxArity;
		}
	}


	/**
	 * The Class ConjunctionInfo.
	 */
	public class ConjunctionInfo {

		/** The variables. */
		List<Variable> variables;
		
		/** The conjuncts. */
		List<Atom> conjuncts;

		/**
		 * Instantiates a new conjunction info.
		 *
		 * @param variable the variable
		 * @param conjuncts the conjuncts
		 */
		public ConjunctionInfo(List<Variable> variable, List<Atom> conjuncts) {
			this.variables = variable;
			this.conjuncts = conjuncts;
		}

		/**
		 * Gets the variables.
		 *
		 * @return the variables
		 */
		public List<Variable> getVariables() {
			return this.variables;
		}

		/**
		 * Gets the conjuncts.
		 *
		 * @return the conjuncts
		 */
		public List<Atom> getConjuncts() {
			return this.conjuncts;
		}
	}
}
