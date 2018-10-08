package uk.ac.ox.cs.pdq.generator.fromreformulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

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
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTree;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTreeAtomNode;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTreeBinaryNode;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTreeNegationNode;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTreeNode;
import uk.ac.ox.cs.pdq.generator.syntaxtree.SyntaxTreeQuantifiedNode;
import uk.ac.ox.cs.pdq.util.Skolemizer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import formats.SMTLibFormulaFormat;
import formats.TPTPFormulaFormat;
import formats.VampireFormulaFormat;
import formats.TPTPFormulaFormat.TPTPFormulaRole;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class DependencyGeneration {

	public static String querySymbol = "Q";
	public static String existentialPrefix = "omega";
	private int existentialCounter = 0;

	public static String freshPredicatePrefix = "f";
	private int freshPredicateCounter = 0;

	private final Formula targetReformulation;
	private final Integer lengthOfResolutionPath;
	private final Integer branchingFactor;
	private final Double probabilityOfReusingPredicate;
	private final Integer headLiterals;
	private final Double probabilityOfNegation;
	private final Double probabilityOfConjunction;
	private final long seed;
	private final Random random;
	private final Integer maxArity;
	private List<Predicate> predicatesUsedSoFar = Lists.newArrayList();

	private Set<Dependency> dependencies = Sets.newLinkedHashSet();
	
	private Set<Dependency> forwardDependencies = Sets.newLinkedHashSet();
	private Set<Dependency> backwardDependencies = Sets.newLinkedHashSet();
	private ConjunctiveQuery query;


	public DependencyGeneration(Formula targetReformulation, Integer lengthOfResolutionPath, Integer branchingFactor, Double probabilityOfReusingPredicate,
			Integer headLiterals, Integer maxArity, Double probabilityOfNegation, Double probabilityOfConjunction, long seed) {
		Preconditions.checkArgument(targetReformulation != null);
		Preconditions.checkArgument(isNotImplicationOrExistential(targetReformulation));
		Preconditions.checkArgument(lengthOfResolutionPath >= 0);
		Preconditions.checkArgument(branchingFactor >= 0);
		Preconditions.checkArgument(probabilityOfReusingPredicate > 0);
		Preconditions.checkArgument(headLiterals > 0);
		Preconditions.checkArgument(maxArity >= 0);
		Preconditions.checkArgument(probabilityOfNegation >= 0 && probabilityOfNegation <= 1);
		Preconditions.checkArgument(probabilityOfConjunction >= 0 && probabilityOfConjunction <= 1);
		this.targetReformulation = targetReformulation;
		this.lengthOfResolutionPath = lengthOfResolutionPath;
		this.branchingFactor = branchingFactor;
		this.probabilityOfReusingPredicate = probabilityOfReusingPredicate;
		this.headLiterals = headLiterals;
		this.maxArity = maxArity;
		this.probabilityOfNegation = probabilityOfNegation;
		this.probabilityOfConjunction = probabilityOfConjunction;
		this.seed = seed;
		this.random = new Random(this.seed);
	}

	private static boolean isNotImplicationOrExistential(Formula formula) {
		if(formula instanceof Conjunction || formula instanceof Implication || formula instanceof Disjunction) {
			return isNotImplicationOrExistential(formula.getChildren().get(0)) && isNotImplicationOrExistential(formula.getChildren().get(1));
		}
		else if(formula instanceof Negation) {
			return isNotImplicationOrExistential(formula.getChildren().get(0));
		}
		else if(formula instanceof Literal) {
			return true;
		}
		else if(formula instanceof Atom) {
			return true;
		}
		else if(formula instanceof QuantifiedFormula) {
			if(((QuantifiedFormula) formula).isExistential()) {
				return false;
			}
			else {
				return isNotImplicationOrExistential(formula.getChildren().get(0));
			}
		}
		return false;
	}

	public void createQueryAndDependencies() {
		SyntaxTree syntaxTree = SyntaxTree.createSyntaxTree(this.targetReformulation);
		SyntaxTree.estimateNodeDepth(syntaxTree);
		Map<Integer, SyntaxTreeNode> nodesMap = syntaxTree.getNodesMap();
		Map<Integer, Atom> nodeAtoms = Maps.newLinkedHashMap();
		Map<Integer, String> nodeSymbols = Maps.newLinkedHashMap();
		Map<Integer, Integer> dependenciesDepth = Maps.newLinkedHashMap();
		Map<Integer, Integer> dependenciesChildren = Maps.newLinkedHashMap();

		for(Entry<Integer, SyntaxTreeNode> entry:nodesMap.entrySet()) {
			Integer id = entry.getKey();
			SyntaxTreeNode node = entry.getValue();
			if(node.equals(syntaxTree.getRoot())) {
				nodeSymbols.put(id, querySymbol);
			}
			else if(!node.isLeaf()) {
				nodeSymbols.put(id, freshPredicatePrefix + freshPredicateCounter++);
				nodeAtoms.put(id, null);
			}
			else {
				nodeAtoms.put(id, ((SyntaxTreeAtomNode)node).getAtom());
			}
		}

		this.createMinimalSetofReformulationDependencies(syntaxTree, syntaxTree.getRoot(), nodesMap, nodeAtoms, nodeSymbols, dependenciesDepth);

		int rootId = syntaxTree.getRoot().getId();
		this.query = new ConjunctiveQuery(nodeAtoms.get(rootId).getVariables(), nodeAtoms.get(rootId));

		//Find the length of the resolution path if we apply only the input dependencies
		int currentLengthOfResolutionPath = Integer.MIN_VALUE;
		for(Integer value:dependenciesDepth.values()) {
			if(currentLengthOfResolutionPath < value) {
				currentLengthOfResolutionPath = value;
			}
		}

		//Find all predicates that appear in dependencies so far 
		for(Atom atom:nodeAtoms.values()) {
			if(!atom.getPredicate().getName().equals(querySymbol)) {
				this.predicatesUsedSoFar.add(atom.getPredicate());
			}
		}

		int i = 0;
		while(i < this.lengthOfResolutionPath) {
			int index = this.random.nextInt(this.dependencies.size());
			Dependency dependency = (Dependency) this.dependencies.toArray()[index];
			this.dependencies.remove(dependency);

			Formula headOfFirstPathDependency = this.createHeadOfDependency(this.headLiterals, this.predicatesUsedSoFar, dependency.getUniversal(), true, true);
			Dependency firstPathDependency = new Dependency(dependency.getBody(), headOfFirstPathDependency);
			this.dependencies.add(firstPathDependency);

			Dependency secondPathDependency = null;
			if(headOfFirstPathDependency instanceof QuantifiedFormula) {
				secondPathDependency = new Dependency(headOfFirstPathDependency.getChildren().get(0), dependency.getHead());
			}
			else {
				secondPathDependency = new Dependency(headOfFirstPathDependency, dependency.getHead());
			}
			this.dependencies.add(secondPathDependency);

			int depth = dependenciesDepth.get(dependency.getId());
			dependenciesDepth.put(firstPathDependency.getId(), depth);
			dependenciesDepth.put(secondPathDependency.getId(), depth+1);
			dependenciesDepth.remove(dependency.getId());
			++i;
		}

		for(Dependency dependency:this.dependencies) {
			dependenciesChildren.put(dependency.getId(), 0);
		}

		List<Dependency> garbageDependencies = Lists.newArrayList(this.dependencies);
		while(!garbageDependencies.isEmpty()) {
			Dependency dependency = garbageDependencies.get(0);
			Integer id = dependency.getId();
			int children = dependenciesChildren.get(id);
			if(dependenciesDepth.get(id) < this.lengthOfResolutionPath && children < this.branchingFactor) {
				//Build the syntax tree of the head of the dependency ignoring its quantification
				SyntaxTree syntaxThreeOfHead = null;
				if(dependency.getHead() instanceof QuantifiedFormula) {
					Formula head = ((QuantifiedFormula)dependency.getHead()).getChildren().get(0);
					syntaxThreeOfHead = SyntaxTree.createSyntaxTree(head);
				}
				else {
					syntaxThreeOfHead = SyntaxTree.createSyntaxTree(dependency.getHead());
				}
				List<Formula> subformulasOfHead = Lists.newArrayList();
				findNegationFreeSubformulas(syntaxThreeOfHead.getRoot(), subformulasOfHead);
				Collections.shuffle(subformulasOfHead, this.random);
				int maximumNumberOfChildren = subformulasOfHead.size();
				while(children < this.branchingFactor && children < maximumNumberOfChildren) {
					//Pick a subformula \phi from the head of the dependency
					Formula phi = subformulasOfHead.get(0);
					subformulasOfHead.remove(0);

					Set<Variable> bodyVariables = Sets.newHashSet();
					for(Atom atom:phi.getAtoms()) {
						bodyVariables.addAll(atom.getVariables());
					}

					Formula headOfNewDependency = this.createHeadOfDependency(this.headLiterals, this.predicatesUsedSoFar, Lists.newArrayList(bodyVariables), true, true);
					Dependency newDependency = new Dependency(phi, headOfNewDependency);
					this.dependencies.add(newDependency);
					garbageDependencies.add(newDependency);

					dependenciesDepth.put(newDependency.getId(), dependenciesDepth.get(id) + 1);
					//We should identify the formulas \psi for each there exists a substitution \sigma s.t. the \sigma(body of \psi) =  head of \phi
					dependenciesChildren.put(newDependency.getId(), 0);
					dependenciesChildren.put(id, children++);

					//Find all predicates that appear in dependencies so far 
					for(Atom atom:headOfNewDependency.getAtoms()) {
						if(!this.predicatesUsedSoFar.contains(atom.getPredicate())) {
							this.predicatesUsedSoFar.add(atom.getPredicate());
						}
					}
				}
			}
			garbageDependencies.remove(0);
		}
	}

	private void createMinimalSetofReformulationDependencies(SyntaxTree syntaxTree, SyntaxTreeNode syntaxTreeNode, Map<Integer, SyntaxTreeNode> nodesMap, Map<Integer, Atom> nodeAtoms, Map<Integer, String> nodeSymbols, Map<Integer, Integer> dependenciesDepth) {
		if(syntaxTreeNode instanceof SyntaxTreeBinaryNode) {
			Integer leftId = syntaxTreeNode.getChildren().get(0).getId();
			Integer rightId = syntaxTreeNode.getChildren().get(1).getId();
			if(nodeAtoms.get(leftId)==null) {
				createMinimalSetofReformulationDependencies(syntaxTree, syntaxTreeNode.getChildren().get(0), nodesMap, nodeAtoms, nodeSymbols, dependenciesDepth);
			}
			if(nodeAtoms.get(rightId)==null) {
				createMinimalSetofReformulationDependencies(syntaxTree, syntaxTreeNode.getChildren().get(1), nodesMap, nodeAtoms, nodeSymbols, dependenciesDepth);
			}
			Integer id = syntaxTreeNode.getId();
			Set<Term> terms = Sets.newLinkedHashSet(nodeAtoms.get(leftId).getTerms());
			terms.addAll(nodeAtoms.get(rightId).getTerms());			
			Predicate predicate = new Predicate(nodeSymbols.get(id), terms.size());

			Atom atom = new Atom(predicate, terms);
			nodeAtoms.put(id, atom);

			Atom leftAtom = nodeAtoms.get(leftId);
			Atom rightAtom = nodeAtoms.get(rightId);

			Dependency forward = null;
			if(((SyntaxTreeBinaryNode) syntaxTreeNode).getOperator().equals(LogicalSymbols.AND)) {
				forward = new Dependency(atom, new Conjunction(leftAtom,rightAtom));
			}
			else if(((SyntaxTreeBinaryNode) syntaxTreeNode).getOperator().equals(LogicalSymbols.OR)) {
				forward = new Dependency(atom, Disjunction.of(leftAtom,rightAtom));
			}
			else {
				throw new java.lang.RuntimeException("Input reformulation must be implication free");
			}

			Atom batom = null;
			Atom bleftAtom = null;
			Atom brightAtom = null;

			if(atom.getPredicate().getName().equals(querySymbol)) {
				batom = atom;
			}
			else {
				batom = new Atom(new Predicate(atom.getPredicate().getName() + "_p", atom.getPredicate().getArity()), atom.getTerms());
			}

			if(syntaxTreeNode.getChildren().get(0).isLeaf()) {
				bleftAtom = leftAtom;
			}
			else {
				bleftAtom = new Atom(new Predicate(leftAtom.getPredicate().getName() + "_p", leftAtom.getPredicate().getArity()), leftAtom.getTerms());
			}

			if(syntaxTreeNode.getChildren().get(1).isLeaf()) {
				brightAtom = rightAtom;
			}
			else {
				brightAtom = new Atom(new Predicate(rightAtom.getPredicate().getName() + "_p", rightAtom.getPredicate().getArity()), rightAtom.getTerms());
			}

			Dependency backward = null;
			if(((SyntaxTreeBinaryNode) syntaxTreeNode).getOperator().equals(LogicalSymbols.AND)) {
				backward = new Dependency(Conjunction.of(bleftAtom,brightAtom), batom);
			}
			else if(((SyntaxTreeBinaryNode) syntaxTreeNode).getOperator().equals(LogicalSymbols.OR)) {
				backward = new Dependency(Disjunction.of(bleftAtom,brightAtom), batom);
			}
			else {
				throw new java.lang.RuntimeException("Input reformulation must be implication free");
			}
			this.dependencies.add(forward);
			this.dependencies.add(backward);
			
			this.forwardDependencies.add(forward);
			this.backwardDependencies.add(backward);
			
			dependenciesDepth.put(forward.getId(), syntaxTreeNode.getDepth());
			dependenciesDepth.put(backward.getId(), 2*SyntaxTree.estimateMaxDepth(syntaxTree) - syntaxTreeNode.getDepth() - 1);
		}
		else if(syntaxTreeNode instanceof SyntaxTreeNegationNode) {
			Integer leftId = syntaxTreeNode.getChildren().get(0).getId();
			if(nodeAtoms.get(leftId)==null) {
				createMinimalSetofReformulationDependencies(syntaxTree, syntaxTreeNode.getChildren().get(0), nodesMap, nodeAtoms, nodeSymbols, dependenciesDepth);
			}
			Integer id = syntaxTreeNode.getId();
			Set<Term> terms = Sets.newLinkedHashSet(nodeAtoms.get(leftId).getTerms());	
			Predicate predicate = new Predicate(nodeSymbols.get(id), terms.size());
			Atom atom = new Atom(predicate, terms);
			nodeAtoms.put(id, atom);
			Atom leftAtom = nodeAtoms.get(leftId);
			Dependency forward = new Dependency(atom, Negation.of(leftAtom));
			Atom batom = null;
			Atom bleftAtom = null;
			if(atom.getPredicate().getName().equals(querySymbol)) {
				batom = atom;
			}
			else {
				batom = new Atom(new Predicate(atom.getPredicate().getName() + "_p", atom.getPredicate().getArity()), atom.getTerms());
			}
			
			if(syntaxTreeNode.getChildren().get(0).isLeaf()) {
				bleftAtom = leftAtom;
			}
			else {
				bleftAtom = new Atom(new Predicate(leftAtom.getPredicate().getName() + "_p", leftAtom.getPredicate().getArity()), leftAtom.getTerms());
			}
			
			Dependency backward = new Dependency(Negation.of(bleftAtom), batom);
			this.dependencies.add(forward);
			this.dependencies.add(backward);
			
			this.forwardDependencies.add(forward);
			this.backwardDependencies.add(backward);
			
			dependenciesDepth.put(forward.getId(), syntaxTreeNode.getDepth());
			dependenciesDepth.put(backward.getId(), 2*SyntaxTree.estimateMaxDepth(syntaxTree) - syntaxTreeNode.getDepth() - 1);
		}
		else if(syntaxTreeNode instanceof SyntaxTreeAtomNode) {
			return;
		}
		else if(syntaxTreeNode instanceof SyntaxTreeQuantifiedNode) {
			throw new java.lang.RuntimeException("Input reformulation must be quantifier free");
		}
	}

	private Formula createHeadOfDependency(int headAtoms, List<Predicate> v, List<Variable> variables, boolean useAllVariables, boolean useFreshVariables) {
		List<Atom> atoms = this.createAtoms(headAtoms, v, variables, useAllVariables, useFreshVariables);
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
		Set<Variable> existentials = Sets.newHashSet();
		for(Atom atom:atoms) {
			existentials.addAll(atom.getVariables());
		}
		existentials.removeAll(variables);
		if(!existentials.isEmpty()) {
			return new QuantifiedFormula(LogicalSymbols.EXISTENTIAL, Lists.newArrayList(existentials), formula); 
		}
		else {
			return formula;
		}
	}

	private List<Predicate> pickPredicates(int numberOfPredicates, List<Predicate> inputPredicates, int totalArity) {
		Preconditions.checkArgument(numberOfPredicates * this.maxArity > totalArity, "Total arity cannot exceed numberOfAtoms * this.maxArity > totalArity");
		int numberOfTries = 0, numberOfMaxTries = 1000;
		int totalPredicateArity = 0;
		List<Predicate> predicates = Lists.newArrayList();
		do {
			totalPredicateArity = 0;
			predicates.clear();
			while(predicates.size() < numberOfPredicates) {
				Predicate predicate = null;
				if(this.probabilityOfReusingPredicate > 1 - this.random.nextDouble() && !inputPredicates.isEmpty()) {
					int index = this.random.nextInt(inputPredicates.size());
					predicate = inputPredicates.get(index);
					inputPredicates.remove(index);
				}
				else {
					String symbol = freshPredicatePrefix + freshPredicateCounter++;
					int arity = this.random.nextInt(this.maxArity) + 1;
					predicate = new Predicate(symbol, arity);
				}
				predicates.add(predicate);
				totalPredicateArity += predicate.getArity();
			}
		} while(totalArity > totalPredicateArity && numberOfMaxTries > numberOfTries++);
		if(totalArity > totalPredicateArity || predicates.size() < numberOfPredicates) {
			throw new java.lang.RuntimeException("Cannot find a combination of #numberOfAtoms atoms with total arity #totalArity");
		}
		return predicates;
	}

	private List<Atom> createAtoms(int numberOfAtoms, List<Predicate> inputPredicates, List<Variable> variables, boolean useAllVariables, boolean useFreshVariables) {
		//Each variable should appear in #atomsHeadVariablesAppear atoms. This way we avoid cartesian products
		int atomsHeadVariablesAppear = 2;
		List<Atom> atoms = Lists.newArrayList();
		//Pick the predicates that will appear in the head
		List<Predicate> predicates = useAllVariables == true ? this.pickPredicates(numberOfAtoms, inputPredicates, atomsHeadVariablesAppear * variables.size()) : 
			this.pickPredicates(numberOfAtoms, inputPredicates, variables.size());

		if(!useAllVariables) {
			variables = variables.subList(0, variables.size()/2);
		}

		List<Pair<Predicate, ArrayList<Variable>>> predicatesToVariables = Lists.newArrayList();
		for(Predicate predicate:predicates) {
			predicatesToVariables.add(Pair.of(predicate,Lists.<Variable>newArrayList()));
		}

		int i = 0;
		do{
			Variable variable = null;
			if(i < variables.size()) {
				variable = variables.get(i);
			}
			else {
				variable = new Variable(existentialPrefix + this.existentialCounter++);
			}
			int variableOccurances = 0;
			boolean exit = false;
			do {
				int index = this.random.nextInt(predicates.size());
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
			++i;
		}while(atoms.size() < numberOfAtoms);

		if(atoms.size() < numberOfAtoms) {
			throw new java.lang.RuntimeException("Cannot create a list of #numberOfAtoms atoms");
		}
		return atoms;
	}

	public static void findNegationFreeSubformulas(SyntaxTreeNode node, List<Formula> formulas) {
		if(node instanceof SyntaxTreeAtomNode) {
			formulas.add(((SyntaxTreeAtomNode) node).getAtom());
		}
		else if(node instanceof SyntaxTreeQuantifiedNode) {
			throw new java.lang.RuntimeException("This method does not work over syntax trees with quantification");
		}
		else if(node instanceof SyntaxTreeBinaryNode) {
			formulas.add(((SyntaxTreeBinaryNode) node).getRootedFormula());
			findNegationFreeSubformulas(node.getChildren().get(0), formulas);
			findNegationFreeSubformulas(node.getChildren().get(1), formulas);
		}
		else if(node instanceof SyntaxTreeNegationNode) {
			formulas.add(((SyntaxTreeBinaryNode) node).getRootedFormula());
		}
	}

	public ConjunctiveQuery getQuery() {
		return this.query;
	}

	public Set<Dependency> getDependencies() {
		return this.dependencies;
	}

	public void writeTPTPOutput(String outputFile) {
		Preconditions.checkArgument(this.query != null && this.dependencies != null, "Query and/or dependencies are null. "
				+ "You should first call the createQueryAndDependencies() method");
		PrintWriter output = null;
		try {
			output = new PrintWriter(new File(outputFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TPTPFormulaFormat format = new TPTPFormulaFormat(output);

		output.print("%%%%%%%%%%%%%%%%%%%%% Target reformulation: " + targetReformulation + " %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");

		output.print("%%%%%%%%%%%%%%%%%%%%% Original query %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n");
		output.print("%" + query);
		output.print("\n");
		for(Clause clause:new Clausifier().clausify(query)) {
			format.printCNFFormula(clause, TPTPFormulaRole.AXIOM, "l_");
			output.print("\n\n");
		}
		output.print("\n\n\n");

		output.print("%%%%%%%%%%%%%%%%%%%%% Original axioms %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		List<Clause> clausifiedDependencies = Lists.newArrayList();
		for(Dependency dependency: dependencies) {
			Dependency outputDependency = Skolemizer.skolemize(dependency);
			output.print("%Non-clausified dependency");
			output.print("\n");
			output.print("%" + outputDependency.toString());
			output.print("\n");
			output.print("%------------------------Clausification start------------------------%");
			output.print("\n");
			List<Clause> clauses = new Clausifier().clausify(outputDependency);
			for(Clause clausifiedDependency:clauses) {
				output.print("%" + clausifiedDependency.toString());
				output.print("\n");
				format.printCNFFormula(clausifiedDependency, TPTPFormulaRole.AXIOM, "l_");
				output.print("\n\n");
				clausifiedDependencies.add(clausifiedDependency);
			}
			output.print("%------------------------Clausification end ------------------------%");
			output.print("\n\n\n");
		}

		AccessibleDependencyFormat accFormat = new AccessibleDependencyFormat();
		output.print("%%%%%%%%%%%%%%%%%%%%% Transfering axioms %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		List<Variable> variables = Lists.newArrayList();
		for(int i = 0; i < this.maxArity; ++i) {
			variables.add(new Variable("X"+i));
		}
		for(Atom atom:targetReformulation.getAtoms()) {
			Clause clause = accFormat.createTransferringAxiomClause(atom, "acc_");
			output.print("%" + clause.toString());
			output.print("\n");
			format.printCNFFormula(clause, TPTPFormulaRole.AXIOM, "r_");
			output.print("\n\n\n");
		}

		output.print("%%%%%%%%%%%%%%%%%%%%% Copy of the axioms %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		for(Clause clause:clausifiedDependencies) {
			Clause accClause = accFormat.processClause(clause, "acc_");
			output.print("%" + accClause.toString());
			output.print("\n");
			format.printCNFFormula(accClause, TPTPFormulaRole.AXIOM, "r_");
			output.print("\n\n\n");
		}

		output.print("%%%%%%%%%%%%%%%%%%%%% Negated copy of the query %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n");
		ConjunctiveQuery accQuery = accFormat.processConjunctiveQuery(query, "acc_");
		output.print("%" + accQuery);
		output.print("\n");
		for(Clause clause:new Clausifier().clausify(accQuery)) {
			format.printCNFFormula(clause, TPTPFormulaRole.CONJECTURE, "r_");
			output.print("\n\n");
		}
		output.print("\n");

		output.close();
	}
	
	public void writeTPTPOutputDebugMode(String outputFile) {
		Preconditions.checkArgument(this.query != null && this.dependencies != null, "Query and/or dependencies are null. "
				+ "You should first call the createQueryAndDependencies() method");
		PrintWriter output = null;
		try {
			output = new PrintWriter(new File(outputFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TPTPFormulaFormat format = new TPTPFormulaFormat(output);

		output.print("%%%%%%%%%%%%%%%%%%%%% Target reformulation: " + targetReformulation + " %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");

		output.print("%%%%%%%%%%%%%%%%%%%%% Original query %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n");
		output.print("%" + query);
		output.print("\n");
		for(Clause clause:new Clausifier().clausify(query)) {
			format.printCNFFormula(clause, TPTPFormulaRole.AXIOM, "l_");
			output.print("\n\n");
		}
		output.print("\n\n\n");

		output.print("%%%%%%%%%%%%%%%%%%%%% Original axioms %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		List<Clause> clausifiedDependencies = Lists.newArrayList();
		for(Dependency dependency: this.forwardDependencies) {
			Dependency outputDependency = Skolemizer.skolemize(dependency);
			output.print("%Non-clausified dependency");
			output.print("\n");
			output.print("%" + outputDependency.toString());
			output.print("\n");
			output.print("%------------------------Clausification start------------------------%");
			output.print("\n");
			List<Clause> clauses = new Clausifier().clausify(outputDependency);
			for(Clause clausifiedDependency:clauses) {
				output.print("%" + clausifiedDependency.toString());
				output.print("\n");
				format.printCNFFormula(clausifiedDependency, TPTPFormulaRole.AXIOM, "l_");
				output.print("\n\n");
				clausifiedDependencies.add(clausifiedDependency);
			}
			output.print("%------------------------Clausification end ------------------------%");
			output.print("\n\n\n");
		}

		AccessibleDependencyFormat accFormat = new AccessibleDependencyFormat();
		output.print("%%%%%%%%%%%%%%%%%%%%% Transfering axioms %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		List<Variable> variables = Lists.newArrayList();
		for(int i = 0; i < this.maxArity; ++i) {
			variables.add(new Variable("X"+i));
		}
		for(Atom atom:targetReformulation.getAtoms()) {
			Clause clause = accFormat.createTransferringAxiomClause(atom, "acc_");
			output.print("%" + clause.toString());
			output.print("\n");
			format.printCNFFormula(clause, TPTPFormulaRole.AXIOM, "r_");
			output.print("\n\n\n");
		}
		
		output.print("%%%%%%%%%%%%%%%%%%%%% Copy of the axioms %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		for(Dependency dependency: this.backwardDependencies) {
			Dependency outputDependency = Skolemizer.skolemize(dependency);
			output.print("%Non-clausified dependency");
			output.print("\n");
			Dependency accDependency = accFormat.processDependency(outputDependency, "acc_");
			output.print("%" + accDependency.toString());
			output.print("\n");
			output.print("%------------------------Clausification start------------------------%");
			output.print("\n");
			List<Clause> clauses = new Clausifier().clausify(accDependency);
			for(Clause clausifiedDependency:clauses) {
				output.print("%" + clausifiedDependency.toString());
				output.print("\n");
				format.printCNFFormula(clausifiedDependency, TPTPFormulaRole.AXIOM, "r_");
				output.print("\n\n");
				clausifiedDependencies.add(clausifiedDependency);
			}
			output.print("%------------------------Clausification end ------------------------%");
			output.print("\n\n\n");
		}

		output.print("%%%%%%%%%%%%%%%%%%%%% Negated copy of the query %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n");
		ConjunctiveQuery accQuery = accFormat.processConjunctiveQuery(query, "acc_");
		output.print("%" + accQuery);
		output.print("\n");
		for(Clause clause:new Clausifier().clausify(accQuery)) {
			format.printCNFFormula(clause, TPTPFormulaRole.CONJECTURE, "r_");
			output.print("\n\n");
		}
		output.print("\n");

		output.close();
	}
	
	public void writeSMTLibOutput(String outputFile) {
		Preconditions.checkArgument(this.query != null && this.dependencies != null, "Query and/or dependencies are null. "
				+ "You should first call the createQueryAndDependencies() method");
		PrintWriter output = null;
		try {
			output = new PrintWriter(new File(outputFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> definedFunctions = Lists.newArrayList();
		SMTLibFormulaFormat format = new SMTLibFormulaFormat(output);

		output.print(";%%%%%%%%%%%%%%%%%%%%% Target reformulation: " + targetReformulation + " %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");

		output.print(";%%%%%%%%%%%%%%%%%%%%% Original query %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n");
		output.print(";%" + query);
		output.print("\n");
		for(Clause clause:new Clausifier().clausify(query)) {
			for(Literal literal:clause.getLiterals()) {
				if(!definedFunctions.contains("l_"+literal.getPredicate().getName())) {
					format.printPredicate(literal.getPredicate(), "l_");
					output.print("\n");
					definedFunctions.add("l_"+literal.getPredicate().getName());
				}
			}
			format.printCNFFormula(clause, "l_");
			output.print("\n\n");
		}
		output.print("\n\n\n");

		output.print(";%%%%%%%%%%%%%%%%%%%%% Original axioms %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		List<Clause> clausifiedDependencies = Lists.newArrayList();
		for(Dependency dependency: dependencies) {
			Dependency outputDependency = Skolemizer.skolemize(dependency);
			output.print(";%Non-clausified dependency");
			output.print("\n");
			output.print(";%" + outputDependency.toString());
			output.print("\n");
			output.print(";%------------------------Clausification start------------------------%");
			output.print("\n");
			List<Clause> clauses = new Clausifier().clausify(outputDependency);
			for(Clause clausifiedDependency:clauses) {
				for(Literal literal:clausifiedDependency.getLiterals()) {
					if(!definedFunctions.contains("l_"+literal.getPredicate().getName())) {
						format.printPredicate(literal.getPredicate(), "l_");
						output.print("\n");
						definedFunctions.add("l_"+literal.getPredicate().getName());
					}
				}
				output.print(";%" + clausifiedDependency.toString());
				output.print("\n");
				format.printCNFFormula(clausifiedDependency, "l_");
				output.print("\n\n");
				clausifiedDependencies.add(clausifiedDependency);
			}
			output.print(";%------------------------Clausification end ------------------------%");
			output.print("\n\n\n");
		}

		AccessibleDependencyFormat accFormat = new AccessibleDependencyFormat();
		output.print(";%%%%%%%%%%%%%%%%%%%%% Transfering axioms %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		List<Variable> variables = Lists.newArrayList();
		for(int i = 0; i < this.maxArity; ++i) {
			variables.add(new Variable("X"+i));
		}
		for(Atom atom:targetReformulation.getAtoms()) {
			Clause clause = accFormat.createTransferringAxiomClause(atom, "l_", "r_");
			output.print(";%" + clause.toString());
			output.print("\n");
			for(Literal literal:clause.getLiterals()) {
				if(!definedFunctions.contains(""+literal.getPredicate().getName())) {
					format.printPredicate(literal.getPredicate(), "");
					output.print("\n");
					definedFunctions.add(""+literal.getPredicate().getName());
				}
			}
			format.printCNFFormula(clause, "");
			output.print("\n\n\n");
		}

		output.print(";%%%%%%%%%%%%%%%%%%%%% Copy of the axioms %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		for(Clause clause:clausifiedDependencies) {
			Clause accClause = accFormat.processClause(clause, "r_");
			output.print(";%" + accClause.toString());
			output.print("\n");
			for(Literal literal:accClause.getLiterals()) {
				if(!definedFunctions.contains(literal.getPredicate().getName())) {
					format.printPredicate(literal.getPredicate(), "");
					output.print("\n");
					definedFunctions.add(literal.getPredicate().getName());
				}
			}
			format.printCNFFormula(accClause, "");
			output.print("\n\n\n");
		}

		output.print(";%%%%%%%%%%%%%%%%%%%%% Negated copy of the query %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n");
		ConjunctiveQuery accQuery = accFormat.processConjunctiveQuery(query, "r_");
		output.print(";%" + accQuery);
		output.print("\n");
		for(Clause clause:new Clausifier().clausify(accQuery)) {
			for(Literal literal:clause.getLiterals()) {
				if(!definedFunctions.contains(literal.getPredicate().getName())) {
					format.printPredicate(literal.getPredicate(), "");
					output.print("\n");
					definedFunctions.add(literal.getPredicate().getName());
				}
			}
			format.printNegatedCNFFormula(clause, "");
			output.print("\n\n");
		}
		output.print("\n");
		
		output.print("(check-sat)");
		output.print("\n");
		output.print("(exit)");

		output.close();
	}
	
	public void writeVampireOutput(String outputFile) {
		Preconditions.checkArgument(this.query != null && this.dependencies != null, "Query and/or dependencies are null. "
				+ "You should first call the createQueryAndDependencies() method");
		
		List<String> targetReformulationPredicates = new ArrayList<>();
		for(Atom atom:targetReformulation.getAtoms()) 
			targetReformulationPredicates.add(atom.getPredicate().getName());
		
		PrintWriter leftRulesOutput = null;
		PrintWriter rightRulesOutput = null;
		PrintWriter predicatesOutput = null;
		PrintWriter output = null;
		StringWriter leftRulesWriter = new StringWriter();
		StringWriter rightRulesWriter = new StringWriter();
		StringWriter predicateWriter = new StringWriter();
		
		try {
			leftRulesOutput = new PrintWriter(leftRulesWriter);
			rightRulesOutput = new PrintWriter(rightRulesWriter);
			predicatesOutput = new PrintWriter(predicateWriter);
			output = new PrintWriter(new File(outputFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> definedPredicates = Lists.newArrayList();
		TPTPFormulaFormat tptpFormatLeft = new TPTPFormulaFormat(leftRulesOutput);
		TPTPFormulaFormat tptpFormatRight = new TPTPFormulaFormat(rightRulesOutput);
		
		VampireFormulaFormat vampireFormat = new VampireFormulaFormat(predicatesOutput);

		for(Clause clause:new Clausifier().clausify(query)) {
			for(Literal literal:clause.getLiterals()) {
				if(!definedPredicates.contains(literal.getPredicate().getName()) 
						&& !targetReformulationPredicates.contains(literal.getPredicate().getName())) {
					vampireFormat.printPredicate(literal.getPredicate(), "left");
					predicatesOutput.print("\n");
					definedPredicates.add(literal.getPredicate().getName());
				}
			}
			tptpFormatLeft.printCNFFormula(clause, TPTPFormulaRole.AXIOM, "l_");
			leftRulesOutput.print("\n");
		}

		
		List<Clause> clausifiedDependencies = Lists.newArrayList();
		for(Dependency dependency: dependencies) {
			Dependency outputDependency = Skolemizer.skolemize(dependency);
			List<Clause> clauses = new Clausifier().clausify(outputDependency);
			for(Clause clausifiedDependency:clauses) {
				for(Literal literal:clausifiedDependency.getLiterals()) {
					if(!definedPredicates.contains(literal.getPredicate().getName()) 
							&& !targetReformulationPredicates.contains(literal.getPredicate().getName())) {
						vampireFormat.printPredicate(literal.getPredicate(), "left");
						predicatesOutput.print("\n");
						definedPredicates.add(literal.getPredicate().getName());
					}
				}
				tptpFormatLeft.printCNFFormula(clausifiedDependency, TPTPFormulaRole.AXIOM, "l_");
				leftRulesOutput.print("\n");
				clausifiedDependencies.add(clausifiedDependency);
			}
		}

		AccessibleDependencyFormat accFormat = new AccessibleDependencyFormat();
		List<Variable> variables = Lists.newArrayList();
		for(int i = 0; i < this.maxArity; ++i) {
			variables.add(new Variable("X"+i));
		}
		for(Atom atom:targetReformulation.getAtoms()) {
			Clause clause = accFormat.createTransferringAxiomClause(atom, "acc_");
			for(Literal literal:clause.getLiterals()) {
				if(!definedPredicates.contains(literal.getPredicate().getName()) && !targetReformulationPredicates.contains(literal.getPredicate().getName())) {
					vampireFormat.printPredicate(literal.getPredicate(), "right");
					predicatesOutput.print("\n");
					definedPredicates.add(literal.getPredicate().getName());
				}
			}
			tptpFormatRight.printCNFFormula(clause, TPTPFormulaRole.AXIOM, "r_");
			rightRulesOutput.print("\n");
		}

		for(Clause clause:clausifiedDependencies) {
			Clause accClause = accFormat.processClause(clause, "acc_");
			for(Literal literal:accClause.getLiterals()) {
				if(!definedPredicates.contains(literal.getPredicate().getName()) && !targetReformulationPredicates.contains(literal.getPredicate().getName())) {
					vampireFormat.printPredicate(literal.getPredicate(), "right");
					predicatesOutput.print("\n");
					definedPredicates.add(literal.getPredicate().getName());
				}
			}
			tptpFormatRight.printCNFFormula(accClause, TPTPFormulaRole.AXIOM, "r_");
			rightRulesOutput.print("\n");
		}

		ConjunctiveQuery accQuery = accFormat.processConjunctiveQuery(query, "acc_");
		for(Clause clause:new Clausifier().clausify(accQuery)) {
			tptpFormatRight.printCNFFormula(clause, TPTPFormulaRole.CONJECTURE, "r_");
			rightRulesOutput.print("\n");
		}
		
		output.print("%%%%%%%%%%%%%%%% Target reformulation: " + targetReformulation + " %%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		
		output.println("vampire(option,show_interpolant,on).\n");
		output.println(predicateWriter.toString());
		output.println("vampire(left_formula).");
		output.print(leftRulesWriter.toString());
		output.println("vampire(end_formula).\n");
		output.println("vampire(right_formula).");
		output.print(rightRulesWriter.toString());
		output.println("vampire(end_formula).");
		output.close();
	}
	
	public Set<Dependency> getForwardDependencies() {
		return this.forwardDependencies;
	}
	
	public Set<Dependency> getBackwardDependencies() {
		return this.backwardDependencies;
	}

}
