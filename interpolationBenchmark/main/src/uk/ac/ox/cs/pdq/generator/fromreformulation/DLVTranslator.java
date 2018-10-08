package uk.ac.ox.cs.pdq.generator.fromreformulation;

import java.io.PrintWriter;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Literal;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTree;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTreeAtomNode;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTreeBinaryNode;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTreeNegationNode;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTreeNode;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTreeQuantifiedNode;
import uk.ac.ox.cs.pdq.util.Skolemizer;
import uk.ac.ox.cs.pdq.util.Utility;

public class DLVTranslator {

	private final static String atomPrefix = "zcl_";
	private static int atomCounter = 0;
	private SyntaxTree syntaxTree= null;
	private boolean syntaxTreeIsUpdated = false;

	public static void main(String... args) throws Exception {
//		Atom c1 = new Atom(new Predicate("c1",2), new Variable("x"), new Variable("y"));
//		Atom c2 = new Atom(new Predicate("c2",2), new Variable("y"), new Variable("z"));
//		Atom c3 = new Atom(new Predicate("c3",2), new Variable("z"), new Variable("w"));
//		Atom c4 = new Atom(new Predicate("c4",4), new Variable("x"), new Variable("y"), new Variable("z"), new Variable("w"));
//
//		Formula body = c4;
//		Formula head = new Conjunction(c1, new Conjunction(c2,c3));
//		Dependency dependency = new Dependency(body, head);
//
//		List<Dependency> dependencies = new Clausifier().getDependenciesWithoutConjunctionInHead(dependency);
//		System.out.println(Joiner.on("\n").join(dependencies));
		
//		Atom c1 = new Atom(new Predicate("c1",2), new Variable("x"), new Variable("y"));
//		Atom c2 = new Atom(new Predicate("c2",2), new Variable("y"), new Variable("z"));
//		Atom c3 = new Atom(new Predicate("c3",2), new Variable("z"), new Variable("w"));
//		Atom c4 = new Atom(new Predicate("c4",4), new Variable("x"), new Variable("y"), new Variable("z"), new Variable("w"));
//		Atom c5 = new Atom(new Predicate("c5",2), new Variable("y"), new Variable("y"));
//
//		Formula body = new Disjunction(c4, c5);
//		Formula head = new Conjunction(c1, new Conjunction(c2,c3));
//		Dependency dependency = new Dependency(body, head);
//
//		List<Clause> clauses = new Clausifier().clausify(dependency);
//		System.out.println(Joiner.on("\n").join(clauses));
		
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
		
		List<Dependency> clauses = new DLVTranslator().translate(outputDependency);
		System.out.println(Joiner.on("\n").join(clauses));
	}
	

	public List<Dependency> translate(Dependency dependency) {
		Preconditions.checkArgument(Utility.isConjunctionOfAtoms(dependency.getBody()) || Utility.isDisjunctionOfAtoms(dependency.getBody()), "Can only clausify dependencies with only conjunctions or disjunctions in the body");
		List<Dependency> clauses = Lists.newArrayList();
		//The returned dependencies do not have conjunction in the head 
		List<Dependency> dependencies = getDependenciesWithoutConjunctionInHead(dependency);

		for(Dependency d: dependencies) {
			List<Literal> bodyLiterals = getBodyLiterals(d.getBody());
			if(d.getBody() instanceof Conjunction) {
				clauses.add(new Dependency(d.getBody(), d.getHead()));
			}
			else {
				for(Literal bodyLiteral:bodyLiterals) {
					clauses.add(new Dependency(bodyLiteral, d.getHead()));
				}
			}
		}
		return clauses;
	}

	private List<Literal> getBodyLiterals(Formula formula) { 
		List<Literal> literals = Lists.newArrayList();
		if(formula instanceof Conjunction || formula instanceof Disjunction) {
			literals.addAll(getBodyLiterals(formula.getChildren().get(0)));
			literals.addAll(getBodyLiterals(formula.getChildren().get(1)));
		}
		else if(formula instanceof Negation) {
			throw new java.lang.RuntimeException("Negation is not currently supported");
		}
		else if(formula instanceof Atom) {
			Atom atom = (Atom)formula;
			literals.add(new Literal(atom.getPredicate(), atom.getTerms()));
		}
		else if(formula instanceof Literal) {
			Literal literal = (Literal)formula;
			literals.add(literal);
		}
		else {
			throw new java.lang.RuntimeException("Do not support formulas with implication");
		}
		return literals;
	}

	private List<Dependency> getDependenciesWithoutConjunctionInHead(Dependency dependency) {
		if(dependency.getHead() instanceof QuantifiedFormula) {
			this.syntaxTree = SyntaxTree.createSyntaxTree(dependency.getHead().getChildren().get(0));
		}
		else {
			this.syntaxTree = SyntaxTree.createSyntaxTree(dependency.getHead());
		}

		List<Dependency> dependencies = Lists.newArrayList();
		do {
			this.syntaxTreeIsUpdated = false;
			dependencies.addAll(this.processHead(this.syntaxTree.getRoot()));
		}while(this.syntaxTreeIsUpdated);

		Formula body = dependency.getBody();
		Formula newHead = this.syntaxTree.getRoot().getRootedFormula();
		if(dependency.getHead() instanceof QuantifiedFormula) {
			dependencies.add(new Dependency(body, new QuantifiedFormula(LogicalSymbols.EXISTENTIAL, 
					((QuantifiedFormula)dependency.getHead()).getTopLevelQuantifiedVariables(), newHead)));
		}
		else {
			dependencies.add(new Dependency(body, newHead));
		}
		return dependencies;
	}

	private List<Dependency> processHead(SyntaxTreeNode parent) {
		List<Dependency> dependencies = Lists.newArrayList();
		if(parent instanceof SyntaxTreeAtomNode) {
		}
		else if(parent instanceof SyntaxTreeQuantifiedNode) {
			throw new java.lang.RuntimeException("The head of the dependency should be quantifier free");
		}
		else if(parent instanceof SyntaxTreeNegationNode) {
			dependencies.addAll(processHead(parent.getChildren().get(0)));
		}
		else if(parent instanceof SyntaxTreeBinaryNode) {
			if(((SyntaxTreeBinaryNode)parent).getOperator().equals(LogicalSymbols.IMPLIES)) {
				throw new java.lang.RuntimeException("The head of the dependency should be implication free");
			}
			else if(((SyntaxTreeBinaryNode)parent).getOperator().equals(LogicalSymbols.OR)) {
				dependencies.addAll(processHead(parent.getChildren().get(0)));
				dependencies.addAll(processHead(parent.getChildren().get(1)));
			}
			else if(((SyntaxTreeBinaryNode)parent).getOperator().equals(LogicalSymbols.AND)) {
				dependencies.addAll(processHead(parent.getChildren().get(0)));
				dependencies.addAll(processHead(parent.getChildren().get(1)));
				List<Variable> variables = parent.getRootedFormula().getFreeVariables();
				Atom atom = new Atom(new Predicate(atomPrefix + atomCounter++, variables.size()), variables);
				Formula bodyOfBackwardDependency = Conjunction.of(parent.getChildren().get(0).getRootedFormula(), parent.getChildren().get(1).getRootedFormula());
				if(!this.syntaxTreeIsUpdated) {
					dependencies.add(new Dependency(atom, parent.getChildren().get(0).getRootedFormula()));
					dependencies.add(new Dependency(atom, parent.getChildren().get(1).getRootedFormula()));
					dependencies.add(new Dependency(bodyOfBackwardDependency, atom));
					this.syntaxTree = this.syntaxTree.replaceNode(parent, new SyntaxTreeAtomNode(atom));
					this.syntaxTreeIsUpdated = true;
				}
				return dependencies;
			}
		}
		return dependencies;
	}
}
