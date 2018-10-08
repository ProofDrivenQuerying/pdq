package uk.ac.ox.cs.pdq.generator.formula;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Literal;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import formats.TPTPFormulaFormat;
import formats.TPTPFormulaFormat.TPTPFormulaRole;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class DNFFormulaGenerator {

	public static String variablePrefix = "x";

	public static String freshPredicatePrefix = "p";
	private int freshPredicateCounter = 0;

	private final Integer numberOfDisjuncts;
	private final Integer numberOfAtomInDisjuncts;
	private final Double probabilityOfReusingPredicate;
	private final Double probabilityOfNegation;
	
	private final long seed;
	private final Random random;
	private final Integer maxArity;
	
	private final List<Predicate> predicatesInOtherDisjuncts = new ArrayList<>();

	public static void main(String... args) throws Exception {
		Integer numberOfConjuncts = 3;
		Integer numberOfAtomInConjuncts = 3;
		Double probabilityOfReusingPredicate = 0.5;
		Double probabilityOfNegation = 0.1;
		Integer maxArity = 3;
		long seed = 3454534;

		DNFFormulaGenerator generator = 
	new DNFFormulaGenerator(numberOfConjuncts, numberOfAtomInConjuncts, probabilityOfReusingPredicate, probabilityOfNegation, maxArity, seed);
		Formula formula = generator.createUnquantifiedFormula();
		PrintWriter output = new PrintWriter(System.out);
		output.println(formula);
		output.close();
		TPTPFormulaFormat format = new TPTPFormulaFormat(output);
		format.printFOLFormula(formula, TPTPFormulaRole.AXIOM, "l_");
	}

	public DNFFormulaGenerator(Integer numberOfDisjuncts, Integer numberOfAtomInDisjuncts, Double probabilityOfReusingPredicate,
			Double probabilityOfNegation, Integer maxArity, long seed) {
		Preconditions.checkArgument(numberOfDisjuncts > 0);
		Preconditions.checkArgument(numberOfAtomInDisjuncts > 0);
		Preconditions.checkArgument(probabilityOfReusingPredicate >= 0);
		Preconditions.checkArgument(probabilityOfNegation >= 0);
		Preconditions.checkArgument(maxArity >= 0);
		this.numberOfDisjuncts = numberOfDisjuncts;
		this.numberOfAtomInDisjuncts = numberOfAtomInDisjuncts;
		this.probabilityOfReusingPredicate = probabilityOfReusingPredicate;
		this.probabilityOfNegation = probabilityOfNegation;
		this.maxArity = maxArity;
		this.seed = seed;
		this.random = new Random(this.seed);
	}
	
	public Formula createDisjunct(Integer numberOfAtoms, List<Variable> variables) {
		List<Predicate> freshPredicates = new ArrayList<>();
		//Create the guard
		int maxArity = variables.size();
		String guardSymbol = freshPredicatePrefix + freshPredicateCounter++;
		Predicate guardPredicate = new Predicate(guardSymbol, variables.size());
		List<Variable> guardVariables = Lists.newArrayList(variables);
		Collections.shuffle(guardVariables);
		Literal guard = this.random.nextFloat() > 1-this.probabilityOfNegation ?  new Literal(LogicalSymbols.NEGATION, guardPredicate, guardVariables):
			new Literal(guardPredicate, guardVariables);
		freshPredicates.add(guardPredicate);
		
		//Create remaining atoms 
		List<Literal> atoms = Lists.newArrayList();
		for(int i = 0; i < numberOfAtoms - 1; ++i) {
			//Pick a fresh predicate with probability 75%			
			Predicate nonguardPredicate = null;
			if(this.random.nextFloat() > 1-this.probabilityOfReusingPredicate && !this.predicatesInOtherDisjuncts.isEmpty()) {
				nonguardPredicate = this.predicatesInOtherDisjuncts.get(this.random.nextInt(this.predicatesInOtherDisjuncts.size()));
				freshPredicates.add(nonguardPredicate);
			}
			else {
				String nonguardSymbol = freshPredicatePrefix + freshPredicateCounter++;
				nonguardPredicate = maxArity > 0 ? new Predicate(nonguardSymbol, this.random.nextInt(maxArity)+1) : new Predicate(nonguardSymbol, 0);
			}
			List<Variable> nonguardVariables = Lists.newArrayList();
			for(int j = 0; j < nonguardPredicate.getArity(); ++j) {
				Variable variable = guard.getVariables().get(this.random.nextInt(guard.getVariables().size()));
				nonguardVariables.add(variable);
			}
			Literal newAtom = this.random.nextFloat() > 1-this.probabilityOfNegation ? 
				new Literal(LogicalSymbols.NEGATION, nonguardPredicate, nonguardVariables):
					new Literal(nonguardPredicate, nonguardVariables);
			atoms.add(newAtom);
		}
		atoms.add(guard);
		this.predicatesInOtherDisjuncts.addAll(freshPredicates);
		return atoms.size() == 1 ? atoms.get(0) : Conjunction.of(atoms);
	}
	
	public Formula createUnquantifiedFormula() {
		List<Formula> conjunctions = Lists.newArrayList();
		List<Variable> variables = new ArrayList<>();
		for(int i = 0 ; i < this.maxArity; ++i) {
			variables.add(new Variable(variablePrefix + i));
		}
		for(int i = 0; i < this.numberOfDisjuncts; ++i) {
			conjunctions.add(this.createDisjunct(this.numberOfAtomInDisjuncts, variables));
		}
		return conjunctions.size() == 1 ? conjunctions.get(0) : Disjunction.of(conjunctions);
	}
	
}
