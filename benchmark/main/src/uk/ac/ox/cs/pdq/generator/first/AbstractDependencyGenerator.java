package uk.ac.ox.cs.pdq.generator.first;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Provides functions for generating formulas, e.g., queries, views and dependencies.
 * 
 * @author Efthymia Tsamoura
 * @author Julien LEBLAY
 * 
 */
public abstract class AbstractDependencyGenerator {

	/** The input schema */
	protected final Schema schema;
	/** The input parameters */
	protected final BenchmarkParameters params;
	/** A random number generator */
	protected final Random random;

	public AbstractDependencyGenerator(Schema schema, BenchmarkParameters params) {
		this.schema = schema;
		this.params = params;
		this.random = new Random(params.getSeed());
	}

	/**
	 * 
	 * @param tgds
	 * @param target
	 * @return
	 * 		true if the target has the same body with one of the tgds in the input collection
	 */
	protected boolean sameBody(Collection<Constraint> tgds, TGD target) {
		for (Constraint tg : tgds) {
			TGD t = (TGD) tg;
			Set<Predicate> set1 = new LinkedHashSet<>();
			set1.addAll(t.getRight().getPredicates());
			Set<Predicate> set2 = new LinkedHashSet<>();
			set2.addAll(target.getRight().getPredicates());
			if (set1.equals(set2)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param views
	 * @param target
	 * @return
	 * 		true if the target has the same head/body with one of the views in the input collection
	 */
	protected boolean sameView(List<View> views, View target) {
		for (View mv : views) {
			Set<Predicate> set1 = new LinkedHashSet<>();
			set1.addAll(mv.getDependency().getRight().getPredicates());

			Set<Predicate> set2 = new LinkedHashSet<>();
			set2.addAll(target.getDependency().getRight().getPredicates());

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
	 * A guarded conjunction 
	 * @param candidates
	 * 		Pool of variables to use in the output conjunction 
	 * @param conjuncts
	 * 		Number of conjuncts 
	 * @param hasRepeatedRelations
	 * 		true if we allow repeated predicates
	 * @return
	 * 		
	 */
	protected ConjunctionInfo createGuardedConjunction(List<Variable> candidates, int conjuncts, boolean hasRepeatedRelations) {
		List<Predicate> atoms = new ArrayList<>();
		RelationsInfo info = this.selectRelations(conjuncts, null, hasRepeatedRelations);
		List<Relation> selectedRelations = info.getRelations();
		Relation relation = selectedRelations.get(conjuncts - 1);
		List<Relation> relations = selectedRelations.subList(0, conjuncts - 1);
		List<Variable> varSubList = candidates.subList(0, relation.getArity());
		atoms = this.createConjuncts(relations, varSubList);
		atoms.add(new Predicate(relation, varSubList));
		return new ConjunctionInfo(Utility.getVariables(atoms), atoms);

	}

	/**
	 * 
	 * @param candidates
	 * 		Pool of variables to use in the output conjunction 
	 * @param conjuncts
	 * 		Number of conjuncts 
	 * @param leftSide
	 * @param hasRepeatedRelations
	 * 		true if we allow repeated predicates
	 * @return
	 */
	protected ConjunctionInfo createUnGuardedConjunction(List<Variable> candidates, int conjuncts, List<Predicate> leftSide, boolean hasRepeatedRelations) {
		List<Predicate> atoms = new ArrayList<>();
		RelationsInfo info = this.selectRelations(conjuncts, leftSide, hasRepeatedRelations);
		List<Relation> selectedRelations = info.getRelations();
		atoms = this.createConjuncts(selectedRelations, candidates);
		return new ConjunctionInfo(Utility.getVariables(atoms), atoms);
	}
	
	/**
	 * 
	 * @param n
	 * @return
	 * 		the list <x_0, x_1, ...., x_{n}> of variables
	 */
	protected List<Variable> createVariables(int n) {
		List<Variable> variables = new ArrayList<>();
		for (int index = 0; index < n; ++index) {
			variables.add(new Variable("x" + index));
		}
		return variables;
	}

	/**
	 * 
	 * @param n
	 * @param input
	 * @return
	 * 		a list of n variables consisting of randomly selected variables from the input and y_i ones
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
	 * 
	 * @param conjuncts
	 * 		a number of relations to select
	 * @param leftSide
	 * @param hasRepeatedRelations
	 * 		true if we allow repeated predicates
	 * @return
	 */
	protected RelationsInfo selectRelations(int conjuncts, List<Predicate> leftSide, boolean hasRepeatedRelations) {
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
			for(Predicate l: leftSide) {
				leftRelations.add((Relation) l.getSignature());
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
	 * 
	 * @param relations
	 * @param variables
	 * @return
	 * 		a list of predicates coming from the input relations and populated with variables from the input list
	 */
	protected List<Predicate> createConjuncts(List<Relation> relations, List<Variable> variables) {
		List<Predicate> conjuncts = new ArrayList<>();
		Relation relation = null;
		for (int r = 0; r < relations.size(); ++r) {
			relation = relations.get(r);
			List<Variable> arguments = new ArrayList<>();
			for (int index = 0; index < relation.getArity(); ++index) {
				arguments.add(variables.get(this.random.nextInt(variables.size())));
			}
			conjuncts.add(new Predicate(relation, arguments));
		}
		return conjuncts;
	}
	
	/**
	 * 
	 * @param atoms
	 * @return
	 * 		a list of chain predicates coming from the input relations. Each predicate has one join variable with its successor
	 */
	protected List<Predicate> createChainConjuncts(List<Predicate> atoms) {
		int xCounter = 0;
		List<Predicate> chainAtoms = new ArrayList<>();
		Term joinTerm = null;
		for (Predicate atom : atoms) {
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
			chainAtoms.add(new Predicate(atom.getSignature(), nTerms));
		}

		return chainAtoms;
	}

	/**
	 * Pick a body.size() * ratio variable out of body's variables.
	 * 
	 * @param ratio
	 * @param body
	 * @return
	 */
	protected List<Variable> pickFreeVariables(Collection<Predicate> body) {
		Set<Variable> result = new LinkedHashSet<>();
		if (this.params.getFreeVariable() > 0.0) {
			Set<Term> allTerms = new LinkedHashSet<>();
			for (Predicate a : body) {
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


	public class RelationsInfo {

		List<Relation> relations;
		int maxArity;

		public RelationsInfo(List<Relation> relations, int maxArity) {
			this.relations = relations;
			this.maxArity = maxArity;
		}

		public List<Relation> getRelations() {
			return this.relations;
		}

		public int getMaxArity() {
			return this.maxArity;
		}
	}


	public class ConjunctionInfo {

		List<Variable> variables;
		List<Predicate> conjuncts;

		public ConjunctionInfo(List<Variable> variable, List<Predicate> conjuncts) {
			this.variables = variable;
			this.conjuncts = conjuncts;
		}

		public List<Variable> getVariables() {
			return this.variables;
		}

		public List<Predicate> getConjuncts() {
			return this.conjuncts;
		}
	}
}
