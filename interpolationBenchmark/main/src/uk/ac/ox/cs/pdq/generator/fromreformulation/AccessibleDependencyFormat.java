package uk.ac.ox.cs.pdq.generator.fromreformulation;

import java.util.Set;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Clause;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Literal;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;

import com.google.common.collect.Sets;

public class AccessibleDependencyFormat {

	public Dependency processDependency(Dependency formula, String predicatePrefix) {
		return (Dependency) processFOLFormula(formula, predicatePrefix);
	}
	
	public Clause processClause(Clause formula, String predicatePrefix) {
		Set<Literal> literals = Sets.newLinkedHashSet();
		for(Literal literal:formula.getLiterals()) {
			literals.add(processLiteral(literal, predicatePrefix));
		}
		return new Clause(literals);
	}

	public ConjunctiveQuery processConjunctiveQuery(ConjunctiveQuery formula, String predicatePrefix) {
		return (ConjunctiveQuery) processFOLFormula(formula, predicatePrefix);
	}

	private Formula processFOLFormula(Formula formula, String predicatePrefix) {
		if(formula instanceof ConjunctiveQuery) {
			return new ConjunctiveQuery(((ConjunctiveQuery)formula).getTopLevelQuantifiedVariables(), processFOLFormula(((ConjunctiveQuery)formula).getChildren().get(0),predicatePrefix));
		}
		else if(formula instanceof Dependency) {
			return new Dependency(processFOLFormula(((Dependency)formula).getBody(), predicatePrefix), processFOLFormula(((Dependency)formula).getHead(), predicatePrefix));
		}
		else if(formula instanceof QuantifiedFormula) {
			return new QuantifiedFormula(((QuantifiedFormula)formula).getOperator(), ((QuantifiedFormula)formula).getTopLevelQuantifiedVariables(), 
					processFOLFormula(((QuantifiedFormula)formula).getChildren().get(0), predicatePrefix));
		}
		else if(formula instanceof Implication) {
			return new Implication(processFOLFormula(((Implication)formula).getChildren().get(0), predicatePrefix),
					processFOLFormula(((Implication)formula).getChildren().get(1), predicatePrefix));
		}
		else if(formula instanceof Conjunction) {
			return new Conjunction(processFOLFormula(((Conjunction)formula).getChildren().get(0), predicatePrefix),
					processFOLFormula(((Conjunction)formula).getChildren().get(1), predicatePrefix));
		}
		else if(formula instanceof Disjunction) {
			return new Disjunction(processFOLFormula(((Disjunction)formula).getChildren().get(0), predicatePrefix),
					processFOLFormula(((Disjunction)formula).getChildren().get(1), predicatePrefix));
		}
		else if(formula instanceof Negation) {
			return new Negation(processFOLFormula(((Negation)formula).getChildren().get(0), predicatePrefix));
		}
		else if(formula instanceof Atom) {
			return processAtom((Atom)formula, predicatePrefix);
		}
		else if(formula instanceof Literal) {
			return processLiteral((Literal)formula, predicatePrefix);
		}
		else {
			throw new java.lang.RuntimeException("Unknown formula type");
		}
	}
	
	private Literal processLiteral(Literal literal, String predicatePrefix) {
		if(literal.isPositive()) {
			Predicate predicate = new Predicate(predicatePrefix + literal.getPredicate().getName(), literal.getPredicate().getArity());
			return new Literal(predicate, literal.getTerms());
		}
		else {
			Predicate predicate = new Predicate(predicatePrefix + literal.getPredicate().getName(), literal.getPredicate().getArity());
			return new Literal(LogicalSymbols.NEGATION, predicate, literal.getTerms());
		}
	}  

	private Atom processAtom(Atom atom, String predicatePrefix) {
		Predicate predicate = new Predicate(predicatePrefix + atom.getPredicate().getName(), atom.getPredicate().getArity());
		return new Atom(predicate, atom.getTerms());
	}  
	
	public Dependency createTransferringAxiom(Atom atom, String predicatePrefix) {
		Predicate predicate = new Predicate(predicatePrefix + atom.getPredicate().getName(), atom.getPredicate().getArity());
		Atom accAtom = new Atom(predicate, atom.getTerms());
		return new Dependency(atom, accAtom);
	}
	
	public Clause createTransferringAxiomClause(Atom atom, String predicatePrefix) {
		Predicate predicate = new Predicate(predicatePrefix + atom.getPredicate().getName(), atom.getPredicate().getArity());
		Atom accAtom = new Atom(predicate, atom.getTerms());
		return new Clause(new Literal(LogicalSymbols.NEGATION, atom.getPredicate(), atom.getTerms()), new Literal(accAtom.getPredicate(), accAtom.getTerms()));
	}
	
	public Clause createTransferringAxiomClause(Atom atom, String leftPredicatePrefix, String rightPredicatePrefix) {
		Predicate predicate = new Predicate(rightPredicatePrefix + atom.getPredicate().getName(), atom.getPredicate().getArity());
		Atom accAtom = new Atom(predicate, atom.getTerms());
		return new Clause(new Literal(LogicalSymbols.NEGATION, new Predicate(leftPredicatePrefix + atom.getPredicate().getName(), atom.getPredicate().getArity()), atom.getTerms()), new Literal(accAtom.getPredicate(), accAtom.getTerms()));
	}

}
