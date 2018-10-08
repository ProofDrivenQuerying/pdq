package uk.ac.ox.cs.pdq.reformulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import formats.TPTPFormulaFormat;
import formats.TPTPFormulaFormat.TPTPFormulaRole;
import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Clause;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Literal;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.generator.fromreformulation.Clausifier;
import uk.ac.ox.cs.pdq.generator.fromreformulation.DependencyGeneration;
import uk.ac.ox.cs.pdq.util.Skolemizer;

/**
 * Finds minimal reformulations of length up to k
 * 
 * @author Efthymia Tsamoura
 *
 */
public class MinimalReformulationEFinder {

	private final ConjunctiveQuery query;
	private final Formula targetReformulation;
	private final Set<Dependency> forwardDependencies;
	private final Set<Dependency> backwardDependencies;
	private final String executable;
	private final List<String> vocabulary;
	private final Integer k;

	public MinimalReformulationEFinder(ConjunctiveQuery query, Formula targetReformulation,
			Set<Dependency> forwardDependencies, Set<Dependency> backwardDependencies, String executable, Integer k) {
		Preconditions.checkArgument(forwardDependencies != null);
		Preconditions.checkArgument(backwardDependencies != null);
		Preconditions.checkArgument(query != null);
		Preconditions.checkArgument(targetReformulation != null);
		Preconditions.checkArgument(executable != null);
		Preconditions.checkArgument(k != null);
		this.query = query;
		this.targetReformulation = targetReformulation;
		this.forwardDependencies = forwardDependencies;
		this.backwardDependencies = backwardDependencies;
		this.vocabulary = Lists.newArrayList();
		for(Atom atom:targetReformulation.getAtoms()) {
			this.vocabulary.add(atom.getPredicate().getName());
		}
		this.executable = executable;
		this.k = k;
	}


	public Set<Clause> findMinimalReformulation() {
		//Do saturation with E with arguments a file containing the forward axioms and the query
		String inputFile = "Input.txt";
		String outputFile = "Output.txt";
		this.writeFile(null, this.forwardDependencies, new Clausifier().clausify(query), inputFile);
		//Call E
		try {
			Process process = new ProcessBuilder(this.executable,
					"--tptp3-format",
					"--print-saturated",
					"-s",
					"--proof-object=1",
					"--disable-eq-factoring",
					"--restrict-literal-comparisons",
					"--no-preprocessing",
					inputFile,
					"-o" + outputFile 
					).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Clause> clauses = this.parseClauses(outputFile, this.vocabulary);
		Preconditions.checkArgument(!clauses.isEmpty(), "No clause on the input vocabulary!");
		for(int i = 1; i <= this.k; ++i) {
			List<Set<Clause>> subsets = Utility.getSubsets(clauses, i);
			for(Set<Clause> subset:subsets) {
				this.writeFile(this.query, this.backwardDependencies, subset, inputFile);
				//Call E
				try {
					Process process = new ProcessBuilder(this.executable,
							"--tptp3-format",
							"-s",
							"--proof-object=1",
							"--disable-eq-factoring",
							"--restrict-literal-comparisons",
							inputFile,
							"-o" + outputFile 
							).start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if(this.checkIfProofFound(outputFile)) {
					return subset;
				}
			}
		}
		return null;
	}

	private void writeFile(ConjunctiveQuery query, Set<Dependency> dependencies, Collection<Clause> clauses, String outputFile) {
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
			for(Clause clausifiedDependency:new Clausifier().clausify(outputDependency)) {
				output.print("%" + clausifiedDependency.toString());
				output.print("\n");
				format.printCNFFormula(clausifiedDependency, TPTPFormulaRole.AXIOM, "l_");
				output.print("\n\n");
				clausifiedDependencies.add(clausifiedDependency);
			}
			output.print("%------------------------Clausification end ------------------------%");
			output.print("\n\n\n");
		}

		output.print("%%%%%%%%%%%%%%%%%%%%% Clauses %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		for(Clause clause:clauses) {
			output.print("%" + clause.toString());
			output.print("\n");
			format.printCNFFormula(clause, TPTPFormulaRole.AXIOM, "l_");
			output.print("\n");
			output.print("\n");
		}

		if(query != null) {
			output.print("%%%%%%%%%%%%%%%%%%%%% Goal %%%%%%%%%%%%%%%%%%%%%");
			output.print("\n");
			output.print("%" + query);
			output.print("\n");
			for(Clause clause:new Clausifier().clausify(query)) {
				format.printCNFFormula(clause, TPTPFormulaRole.CONJECTURE, "r_");
				output.print("\n\n");
			}
			output.print("\n");
		}

		output.close();
	}

	private List<Clause> parseClauses(String file, List<String> vocabulary) {
		String line = null;
		try {
			List<Clause> clauses = Lists.newArrayList();
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) {
				if(!line.startsWith("#") && !line.isEmpty()) {

					List<Integer> indices1 = Utility.findIndexes(line, "(");
					List<Integer> indices2 = Utility.findIndexes(line, ")");
					line = line.substring(indices1.get(1)+1, indices2.get(indices2.size()-2));

					Set<Literal> parsedLiterals = Sets.newHashSet();
					String[] literals = line.split("\\|");
					for(String literal:literals) {

						int startIndex = literal.indexOf("(");
						int endIndex = literal.indexOf(")");

						if(startIndex < 0 || endIndex < 0) {
							throw new java.lang.RuntimeException("Malformed literal");
						}
						else {
							LogicalSymbols symbol = null;
							String name = null;
							if(literal.split("\\(")[0].startsWith("~")) {
								name = literal.substring(1, startIndex);
								symbol= LogicalSymbols.NEGATION;
							}
							else {
								name = literal.substring(0, startIndex);
							}
							if(vocabulary.contains(name)) {
								List<Variable> variables = Lists.newArrayList();
								for(String variable:literal.substring(startIndex+1, endIndex).split(",")) {
									variables.add(new Variable(variable));
								}
								Literal parsedLiteral = symbol == null ? new Literal(new Predicate(name, variables.size()), variables) :
									new Literal(symbol, new Predicate(name, variables.size()), variables);
								parsedLiterals.add(parsedLiteral);
							}
							else {
								parsedLiterals.clear();
								break;
							}
						}
					}
					if(!parsedLiterals.isEmpty()) {
						clauses.add(new Clause(parsedLiterals));
					}
				}
			}
			bufferedReader.close();
			return clauses;
		}
		catch(FileNotFoundException ex) {      
			ex.printStackTrace(System.out);
		}
		catch(IOException ex) {
			ex.printStackTrace(System.out);
		}
		return null;
	}

	private boolean checkIfProofFound(String file) {
		String line = null;
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) {
				if(line.contains("# Proof found!")) {
					return true;
				}
			}
			bufferedReader.close();
			return false;
		}
		catch(FileNotFoundException ex) {      
			ex.printStackTrace(System.out);
		}
		catch(IOException ex) {
			ex.printStackTrace(System.out);
		}
		return false;
	}
}
