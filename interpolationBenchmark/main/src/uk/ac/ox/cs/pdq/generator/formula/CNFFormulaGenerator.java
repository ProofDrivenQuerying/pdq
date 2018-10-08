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
public class CNFFormulaGenerator {

	public static String variablePrefix = "x";
	private int variableCounter = 0;

	public static String freshPredicatePrefix = "p";
	private int freshPredicateCounter = 0;

	private final Integer numberOfConjuncts;
	private final Integer numberOfAtomInConjuncts;
	private final Double probabilityOfReusingPredicate;
	private final Double probabilityOfNegation;

	private final long seed;
	private final Random random;
	private final Integer maxArity;

	private final List<Predicate> predicatesInOtherConjuncts = new ArrayList<>();
	private final List<Variable> variablesInOtherConjuncts = new ArrayList<>();

	public static void main(String... args) throws Exception {
		Integer numberOfConjuncts = 5;
		Integer numberOfAtomInConjuncts = 2;
		Integer maxArity = 0;
		long seed = 5345345;
		Double probabilityOfReusingPredicate = 0.25;
		Double probabilityOfNegation = 0.25;

		CNFFormulaGenerator generator = 
				new CNFFormulaGenerator(numberOfConjuncts, numberOfAtomInConjuncts, probabilityOfReusingPredicate, probabilityOfNegation, maxArity, seed);
		Formula formula = generator.createUnquantifiedFormula();
		PrintWriter output = new PrintWriter(System.out);
		output.println(formula);
		output.close();
		TPTPFormulaFormat format = new TPTPFormulaFormat(output);
		format.printFOLFormula(formula, TPTPFormulaRole.AXIOM, "l_");
	}

	public CNFFormulaGenerator(Integer numberOfConjuncts, Integer numberOfAtomInConjuncts, 
			Double probabilityOfReusingPredicate, Double probabilityOfNegation, Integer maxArity, long seed) {
		Preconditions.checkArgument(numberOfConjuncts > 0);
		Preconditions.checkArgument(numberOfAtomInConjuncts > 0);
		Preconditions.checkArgument(probabilityOfReusingPredicate >= 0);
		Preconditions.checkArgument(probabilityOfNegation >= 0);
		Preconditions.checkArgument(maxArity >= 0);
		this.numberOfConjuncts = numberOfConjuncts;
		this.numberOfAtomInConjuncts = numberOfAtomInConjuncts;
		this.probabilityOfReusingPredicate = probabilityOfReusingPredicate;
		this.probabilityOfNegation = probabilityOfNegation;
		this.maxArity = maxArity;
		this.seed = seed;
		this.random = new Random(this.seed);
	}

	public Formula createConjunct(Integer numberOfAtoms, Integer maxArity) {
		List<Predicate> freshPredicates = new ArrayList<>();
		//Create the guard
		String guardSymbol = freshPredicatePrefix + freshPredicateCounter++;
		Predicate guardPredicate = maxArity > 0 ? new Predicate(guardSymbol, this.random.nextInt(maxArity) + 1): new Predicate(guardSymbol, 0);
		List<Variable> guardVariables = Lists.newArrayList();
		freshPredicates.add(guardPredicate);
		for(int i = 0; i < guardPredicate.getArity(); ++i) {
			Variable variable = null;
			//Pick a fresh variable with probability 50%
			variable = this.random.nextFloat() > 0 && !this.variablesInOtherConjuncts.isEmpty() ? 
					this.variablesInOtherConjuncts.get(this.random.nextInt(this.variablesInOtherConjuncts.size())) : 
						new Variable(variablePrefix + this.variableCounter++);	
					guardVariables.add(variable);
		}
		Literal guard =  this.random.nextFloat() > 1-this.probabilityOfNegation ?  new Literal(LogicalSymbols.NEGATION, guardPredicate, guardVariables):
			new Literal(guardPredicate, guardVariables);
		this.variablesInOtherConjuncts.addAll(guardVariables);
		
		//Create remaining atoms 
		List<Literal> literals = Lists.newArrayList();
		List<Predicate> predicatesInAtoms = Lists.newArrayList();
		for(int i = 0; i < numberOfAtoms - 1; ++i) {
			Predicate nonguardPredicate = null;
			List<Variable> nonguardVariables = Lists.newArrayList();
			if(maxArity > 0) {
				//Pick a fresh predicate 
				if(this.random.nextFloat() > 1-this.probabilityOfReusingPredicate && !this.predicatesInOtherConjuncts.isEmpty()) {
					nonguardPredicate = this.predicatesInOtherConjuncts.get(this.random.nextInt(this.predicatesInOtherConjuncts.size()));
					while(predicatesInAtoms.contains(nonguardPredicate)) {
						nonguardPredicate = this.predicatesInOtherConjuncts.get(this.random.nextInt(this.predicatesInOtherConjuncts.size()));
					}
				}
				else {
					String nonguardSymbol = freshPredicatePrefix + freshPredicateCounter++;
					nonguardPredicate = new Predicate(nonguardSymbol, guardPredicate.getArity());
					freshPredicates.add(nonguardPredicate);
				}
				nonguardVariables = Lists.newArrayList(guardVariables);
				Collections.shuffle(nonguardVariables, this.random);
			}
			else {
				if(this.random.nextFloat() > 1-this.probabilityOfReusingPredicate && !this.predicatesInOtherConjuncts.isEmpty()) {
					nonguardPredicate = this.predicatesInOtherConjuncts.get(this.random.nextInt(this.predicatesInOtherConjuncts.size()));
					while(predicatesInAtoms.contains(nonguardPredicate)) {
						nonguardPredicate = this.predicatesInOtherConjuncts.get(this.random.nextInt(this.predicatesInOtherConjuncts.size()));
					}
				}
				else {
					String nonguardSymbol = freshPredicatePrefix + freshPredicateCounter++;
					nonguardPredicate = new Predicate(nonguardSymbol, 0);
					freshPredicates.add(nonguardPredicate);
				}
			}
			Literal newAtom = this.random.nextFloat() > 1-this.probabilityOfNegation ?  new Literal(LogicalSymbols.NEGATION, nonguardPredicate, nonguardVariables):
						new Literal(nonguardPredicate, nonguardVariables);
			literals.add(newAtom);
			this.variablesInOtherConjuncts.addAll(nonguardVariables);
		}
		literals.add(guard);
		this.predicatesInOtherConjuncts.addAll(freshPredicates);
		return literals.size() == 1 ? literals.get(variableCounter) : Disjunction.of(literals);
	}

	public Formula createUnquantifiedFormula() {
		List<Formula> conjunctions = Lists.newArrayList();
		for(int i = 0; i < this.numberOfConjuncts; ++i) {
			conjunctions.add(this.createConjunct(this.numberOfAtomInConjuncts, this.maxArity));
		}
		return conjunctions.size() == 1 ? conjunctions.get(0) : Conjunction.of(conjunctions);
	}

}
