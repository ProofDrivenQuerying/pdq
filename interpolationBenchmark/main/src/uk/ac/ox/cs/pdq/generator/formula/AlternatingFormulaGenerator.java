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
public class AlternatingFormulaGenerator {
	
	public enum STARTING_FORMULA {
		CONJUNCTION,
		DISJUNCTION
	}

	public static String variablePrefix = "x";
	private int variableCounter = 0;

	public static String freshPredicatePrefix = "p";
	private int freshPredicateCounter = 0;

	private final Integer numberOfAlternations;
	private final Integer numberOfAtomInEachAlternatingFormulas;
	private final Double probabilityOfReusingPredicate;
	private final Double probabilityOfNegation;
	private final STARTING_FORMULA sf;

	private final long seed;
	private final Random random;
	private final Integer maxArity;

	private final List<Predicate> predicatesInOtherFormulas = new ArrayList<>();
	private final List<Variable> variablesInOtherConjuncts = new ArrayList<>();

	public static void main(String... args) throws Exception {
		Integer numberOfAlternations = 5;
		Integer numberOfAtomInEachAlternatingFormulas = 2;
		Integer maxArity = 0;
		long seed = 4545454;
		Double probabilityOfReusingPredicate = 0.5;
		Double probabilityOfNegation = 0.25;

		AlternatingFormulaGenerator generator = 
				new AlternatingFormulaGenerator(numberOfAlternations, numberOfAtomInEachAlternatingFormulas, 
						probabilityOfReusingPredicate, probabilityOfNegation, STARTING_FORMULA.DISJUNCTION, maxArity, seed);
		Formula formula = generator.createUnquantifiedFormula();
		PrintWriter output = new PrintWriter(System.out);
		output.println(formula);
		output.close();
		TPTPFormulaFormat format = new TPTPFormulaFormat(output);
		format.printFOLFormula(formula, TPTPFormulaRole.AXIOM, "l_");
	}

	public AlternatingFormulaGenerator(Integer numberOfAlternations, Integer numberOfAtomInEachAlternatingFormula, 
			Double probabilityOfReusingPredicate, Double probabilityOfNegation, STARTING_FORMULA sf, Integer maxArity, long seed) {
		Preconditions.checkArgument(numberOfAlternations >= 0);
		Preconditions.checkArgument(numberOfAtomInEachAlternatingFormula > 0);
		Preconditions.checkArgument(probabilityOfReusingPredicate >= 0);
		Preconditions.checkArgument(probabilityOfNegation >= 0);
		Preconditions.checkArgument(maxArity >= 0);
		Preconditions.checkArgument(sf != null);
		this.numberOfAlternations = numberOfAlternations;
		this.numberOfAtomInEachAlternatingFormulas = numberOfAtomInEachAlternatingFormula;
		this.probabilityOfReusingPredicate = probabilityOfReusingPredicate;
		this.probabilityOfNegation = probabilityOfNegation;
		this.sf = sf;
		this.maxArity = maxArity;
		this.seed = seed;
		this.random = new Random(this.seed);
	}
	
	protected Formula createConjunction(Integer numberOfAtoms, List<Variable> variables) {
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
			if(this.random.nextFloat() > 1-this.probabilityOfReusingPredicate && !this.predicatesInOtherFormulas.isEmpty()) {
				nonguardPredicate = this.predicatesInOtherFormulas.get(this.random.nextInt(this.predicatesInOtherFormulas.size()));
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
		this.predicatesInOtherFormulas.addAll(freshPredicates);
		return atoms.size() == 1 ? atoms.get(0) : Conjunction.of(atoms);
	}

	protected Formula createDisjunction(Integer numberOfAtoms, Integer maxArity) {
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
		Literal guard = this.random.nextFloat() > 1-this.probabilityOfNegation ?  new Literal(LogicalSymbols.NEGATION, guardPredicate, guardVariables):
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
				if(this.random.nextFloat() > 1-this.probabilityOfReusingPredicate && !this.predicatesInOtherFormulas.isEmpty()) {
					nonguardPredicate = this.predicatesInOtherFormulas.get(this.random.nextInt(this.predicatesInOtherFormulas.size()));
					while(predicatesInAtoms.contains(nonguardPredicate)) {
						nonguardPredicate = this.predicatesInOtherFormulas.get(this.random.nextInt(this.predicatesInOtherFormulas.size()));
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
				if(this.random.nextFloat() > 1-this.probabilityOfReusingPredicate && !this.predicatesInOtherFormulas.isEmpty()) {
					nonguardPredicate = this.predicatesInOtherFormulas.get(this.random.nextInt(this.predicatesInOtherFormulas.size()));
					while(predicatesInAtoms.contains(nonguardPredicate)) {
						nonguardPredicate = this.predicatesInOtherFormulas.get(this.random.nextInt(this.predicatesInOtherFormulas.size()));
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
		this.predicatesInOtherFormulas.addAll(freshPredicates);
		return literals.size() == 1 ? literals.get(0): Disjunction.of(literals);
	}

	public Formula createUnquantifiedFormula() {
		List<Variable> variables = new ArrayList<>();
		for(int i = 0 ; i < this.maxArity; ++i) {
			variables.add(new Variable(variablePrefix + i));
		}
		Formula leftFormula = this.sf.equals(STARTING_FORMULA.DISJUNCTION) ? createDisjunction(this.numberOfAtomInEachAlternatingFormulas, this.maxArity) :
			createConjunction(this.numberOfAtomInEachAlternatingFormulas, variables);
		for(int i = 0; i < this.numberOfAlternations; ++i) {
			if(leftFormula instanceof Conjunction) {
				Formula rightFormula = createConjunction(this.numberOfAtomInEachAlternatingFormulas, leftFormula.getFreeVariables());
				leftFormula = Disjunction.of(rightFormula, leftFormula); 
			}
			else {
				Formula rightFormula = createDisjunction(this.numberOfAtomInEachAlternatingFormulas, this.maxArity);
				leftFormula = Conjunction.of(rightFormula, leftFormula); 
			}	
		}
		return leftFormula;
	}

}
