package uk.ac.ox.cs.pdq.util;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Function;
import uk.ac.ox.cs.pdq.fol.FunctionTerm;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Literal;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import formats.TPTPFormulaFormat;
import formats.TPTPFormulaFormat.TPTPFormulaRole;

public class Skolemizer {

	private static String functionSymbol = "fnc";
	private static Integer functionSymbolCounter = 0;
	private static Map<Variable, FunctionTerm> map;

	public static Dependency skolemize(Dependency dependency) {
		Skolemizer.map = Maps.newHashMap();
		return new Dependency(dependency.getBody(), skolemize(dependency.getUniversal(), dependency.getHead()));
	}

	private static Formula skolemize(List<Variable> universal, Formula formula) {
		if(formula instanceof Conjunction) {
			Formula f1 = skolemize(universal, ((Conjunction)formula).getChildren().get(0));
			Formula f2 = skolemize(universal, ((Conjunction)formula).getChildren().get(1));
			return new Conjunction(f1, f2);
		}
		else if(formula instanceof Disjunction) {
			Formula f1 = skolemize(universal, ((Disjunction)formula).getChildren().get(0));
			Formula f2 = skolemize(universal, ((Disjunction)formula).getChildren().get(1));
			return new Disjunction(f1, f2);
		}
		else if(formula instanceof Implication) {
			Formula f1 = skolemize(universal, ((Implication)formula).getChildren().get(0));
			Formula f2 = skolemize(universal, ((Implication)formula).getChildren().get(1));
			return new Implication(f1, f2);
		}
		else if(formula instanceof Negation) {
			Formula f1 = skolemize(universal, ((Negation)formula).getChildren().get(0));
			return new Negation(f1);
		}
		else if(formula instanceof QuantifiedFormula) {
			Formula f1 = skolemize(universal, ((QuantifiedFormula)formula).getChildren().get(0));
			return f1;
		}
		else if(formula instanceof Atom) {
			Atom atom = (Atom)formula;
			if(!universal.containsAll(atom.getVariables())) {
				List<Term> universalTerms = Lists.newArrayList();
				universalTerms.addAll(universal);
				List<Term> newTerms = Lists.newArrayList();
				for(Term term:atom.getTerms()) {
					if(term instanceof Constant || universal.contains(term)) {
						newTerms.add(term);
					}
					else {
						if(map.get(term) != null) {
							newTerms.add(map.get(term));
						}
						else {
							Function function = new Function(functionSymbol + functionSymbolCounter++, universal.size());
							FunctionTerm functionTerm = new FunctionTerm(function, universalTerms);
							newTerms.add(functionTerm);
							map.put((Variable)term, functionTerm);
						}
					}
				}
				return new Atom(atom.getPredicate(), newTerms);
			}
			else {
				return formula;
			}
		}
		else if(formula instanceof Literal) {
			Literal atom = (Literal)formula;
			if(!universal.containsAll(atom.getVariables())) {
				List<Term> universalTerms = Lists.newArrayList();
				universalTerms.addAll(universal);
				List<Term> newTerms = Lists.newArrayList();
				for(Term term:atom.getTerms()) {
					if(term instanceof Constant || universal.contains(term)) {
						newTerms.add(term);
					}
					else {
						if(map.get(term) != null) {
							newTerms.add(map.get(term));
						}
						else {
							Function function = new Function(functionSymbol + functionSymbolCounter++, universal.size());
							FunctionTerm functionTerm = new FunctionTerm(function, universalTerms);
							newTerms.add(functionTerm);
							map.put((Variable)term, functionTerm);
						}
					}
				}
				if(atom.isPositive()) {
					return new Literal(atom.getPredicate(), newTerms);
				}
				else {
					return new Literal(LogicalSymbols.NEGATION, atom.getPredicate(), newTerms);
				}
			}
			else {
				return formula;
			}
		}
		throw new java.lang.RuntimeException("Unknown formula type");
	}
	
	public static void main(String... args) throws Exception {		
		Atom c1 = new Atom(new Predicate("c1",2), new Variable("x"), new Variable("y"));
		Atom c2 = new Atom(new Predicate("c2",2), new Variable("y"), new Variable("z"));
		Atom c3 = new Atom(new Predicate("c3",2), new Variable("z"), new Variable("w"));
		Atom c4 = new Atom(new Predicate("c4",4), new Variable("x"), new Variable("omega1"), new Variable("z"), new Variable("w"));
		Atom c5 = new Atom(new Predicate("c5",2), new Variable("omega1"), new Variable("omega2"));

		Formula body = new Conjunction(c1, new Conjunction(c2,c3));
		Formula head = new QuantifiedFormula(LogicalSymbols.EXISTENTIAL, Lists.newArrayList(new Variable("omega1"), new Variable("omega2")),
				new Disjunction(c4, c5));
		Dependency dependency = new Dependency(body, head);
		Dependency outputDependency = Skolemizer.skolemize(dependency);
		
		PrintWriter output = new PrintWriter(System.out);
		output.println(outputDependency);
		TPTPFormulaFormat format = new TPTPFormulaFormat(output);
		format.printFOLFormula(outputDependency, TPTPFormulaRole.AXIOM, "l_");
	}
}
