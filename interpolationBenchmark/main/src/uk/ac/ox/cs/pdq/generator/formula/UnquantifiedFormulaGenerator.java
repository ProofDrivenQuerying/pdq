package uk.ac.ox.cs.pdq.generator.formula;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Negation;
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
public class UnquantifiedFormulaGenerator {

	public static String variablePrefix = "x";
	private int variableCounter = 0;

	public static String freshPredicatePrefix = "p";
	private int freshPredicateCounter = 0;

	private final Integer numberOfAtoms;
	private final Integer numberOfDistinctPredicates;
	private final Integer numberOfDistinctVariables;
	private final Double probabilityOfNegation;
	private final Double probabilityOfConjunction;
	private final long seed;
	private final Random random;
	private final Integer maxArity;

	public static void main(String... args) throws Exception {
		Integer numberOfAtoms = 5;
		Integer numberOfDistinctPredicates = 3;
		Integer numberOfDistinctVariables = 3;
		Integer maxArity = 4;
		Double probabilityOfNegation = 0.25;
		Double probabilityOfConjunction = 0.5;
		long seed = 123;

		UnquantifiedFormulaGenerator generator = 
	new UnquantifiedFormulaGenerator(numberOfAtoms, numberOfDistinctPredicates, numberOfDistinctVariables, maxArity, probabilityOfNegation, probabilityOfConjunction, seed);
		Formula formula = generator.createUnquantifiedFormula();
		PrintWriter output = new PrintWriter(System.out);
		output.println(formula);
		TPTPFormulaFormat format = new TPTPFormulaFormat(output);
		format.printFOLFormula(formula, TPTPFormulaRole.AXIOM, "l_");
		output.close();
	}

	public UnquantifiedFormulaGenerator(Integer numberOfAtoms, Integer maxArity, Double probabilityOfNegation, Double probabilityOfConjunction, long seed) {
		this(numberOfAtoms, numberOfAtoms, null, maxArity, probabilityOfNegation, probabilityOfConjunction, seed);
	}

	public UnquantifiedFormulaGenerator(Integer numberOfAtoms, Integer numberOfDistinctPredicates, 
			Integer numberOfDistinctVariables,  Integer maxArity, Double probabilityOfNegation, Double probabilityOfConjunction, long seed) {
		Preconditions.checkArgument(numberOfAtoms > 0);
		Preconditions.checkArgument(numberOfDistinctPredicates > 0);
		Preconditions.checkArgument(numberOfAtoms >= numberOfDistinctPredicates);
		Preconditions.checkArgument(numberOfDistinctVariables == null || numberOfDistinctVariables > 0);
		Preconditions.checkArgument(maxArity > 0);
		Preconditions.checkArgument(probabilityOfNegation >= 0 && probabilityOfNegation <= 1);
		Preconditions.checkArgument(probabilityOfConjunction >= 0 && probabilityOfConjunction <= 1);
		this.numberOfAtoms = numberOfAtoms;
		this.numberOfDistinctPredicates = numberOfDistinctPredicates;
		this.numberOfDistinctVariables = numberOfDistinctVariables;
		this.maxArity = maxArity;
		this.probabilityOfNegation = probabilityOfNegation;
		this.probabilityOfConjunction = probabilityOfConjunction;
		this.seed = seed;
		this.random = new Random(this.seed);
	}

	public Formula createUnquantifiedFormula() {
		List<Atom> atoms = this.createAtoms(this.numberOfAtoms, this.numberOfDistinctPredicates, this.numberOfDistinctVariables, this.maxArity);
		Formula formula = null;
		Iterator<Atom> iterator = atoms.iterator();
		while(iterator.hasNext()) {
			Atom atom = iterator.next();

			//Decide if the next atom should be negated or not
			Formula literal = null;
			if(this.probabilityOfNegation > 1 - this.random.nextDouble()) {
				literal = new Negation(atom);
			}
			else {
				literal = atom;
			}

			if(formula == null) {
				formula = literal;
			}
			else {
				//Decide if we will add a conjunction or disjunction
				if(this.probabilityOfConjunction > 1 - this.random.nextDouble()) {
					formula = new Conjunction(formula, literal);
				}
				else {
					formula = new Disjunction(formula, literal);
				}
			}
		}
		return formula;
	}

	private List<Predicate> pickPredicates(Integer numberOfPredicates, Integer numberOfDistinctPredicates, Integer totalArity) {
		Preconditions.checkArgument(numberOfPredicates * this.maxArity > totalArity, "Total arity cannot exceed numberOfAtoms * this.maxArity > totalArity");
		int numberOfTries = 0, numberOfMaxTries = 1000;
		int totalPredicateArity = 0;
		List<Predicate> predicates = Lists.newArrayList();
		do {
			totalPredicateArity = 0;
			predicates.clear();
			while(predicates.size() < numberOfDistinctPredicates) {
				Predicate predicate = null;
				String symbol = freshPredicatePrefix + freshPredicateCounter++;
				int arity = this.random.nextInt(this.maxArity) + 1;
				predicate = new Predicate(symbol, arity);
				predicates.add(predicate);
				totalPredicateArity += predicate.getArity();
			}
			while(predicates.size() < numberOfPredicates) {
				int index = this.random.nextInt(predicates.size());
				Predicate predicate = predicates.get(index);
				predicates.add(predicate);
				totalPredicateArity += predicate.getArity();
			}
		} while(totalArity > totalPredicateArity && numberOfMaxTries > numberOfTries++);
		if(totalArity > totalPredicateArity || predicates.size() < numberOfDistinctPredicates) {
			throw new java.lang.RuntimeException("Cannot find a combination of #numberOfAtoms atoms with total arity #totalArity");
		}
		return predicates;
	}

	private List<Atom> createAtoms(Integer numberOfAtoms, Integer numberOfDistinctPredicates, Integer numberOfDistinctVariables, Integer maxArity) {
		//Each variable should appear in #atomsHeadVariablesAppear atoms. This way we avoid cartesian products
		int atomsHeadVariablesAppear = 2;
		List<Atom> atoms = Lists.newArrayList();
		//Pick the predicates that will appear in the head
		List<Predicate> predicates = this.pickPredicates(numberOfAtoms, numberOfDistinctPredicates, numberOfDistinctVariables == null ? Integer.MIN_VALUE : numberOfDistinctVariables * atomsHeadVariablesAppear);

		List<Pair<Predicate, ArrayList<Variable>>> predicatesToVariables = Lists.newArrayList();
		for(Predicate predicate:predicates) {
			predicatesToVariables.add(Pair.of(predicate,Lists.<Variable>newArrayList()));
		}

		int variableCounter = 0;
		List<Variable> usedVariables = Lists.newArrayList();
		do{
			Variable variable = null;
			if(numberOfDistinctVariables != null && variableCounter > numberOfDistinctVariables - 1) {
				variableCounter = 0;
			}

			if(numberOfDistinctVariables != null && usedVariables.size() == numberOfDistinctVariables) {
				variable = usedVariables.get(variableCounter);
			}
			else {
				variable = new Variable(variablePrefix + this.variableCounter++);
				usedVariables.add(variable);
			}

			int variableOccurances = 0;
			boolean exit = false;
			do {
				int index = this.random.nextInt(predicatesToVariables.size());
				Pair<Predicate, ArrayList<Variable>> pair = predicatesToVariables.get(index);
				pair.getValue().add(variable);
				if(pair.getKey().getArity() == pair.getValue().size()) {
					atoms.add(new Atom(pair.getKey(), pair.getValue()));
					predicatesToVariables.remove(index);
				}
				if(++variableOccurances == atomsHeadVariablesAppear || numberOfAtoms == 1 || atoms.size() == numberOfAtoms) {
					variableOccurances = 0;
					exit = true;
				}
			}while(exit==false);
			variableCounter++;
		}while(atoms.size() < numberOfAtoms);

		if(atoms.size() < numberOfAtoms) {
			throw new java.lang.RuntimeException("Cannot create a list of #numberOfAtoms atoms");
		}
		return atoms;
	}
}
