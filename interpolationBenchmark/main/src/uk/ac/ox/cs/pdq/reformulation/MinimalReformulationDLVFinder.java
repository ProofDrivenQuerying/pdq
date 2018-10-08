package uk.ac.ox.cs.pdq.reformulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.generator.fromreformulation.DLVTranslator;
import uk.ac.ox.cs.pdq.util.Skolemizer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import formats.DLVFormulaFormat;

/**
 * Finds minimal reformulations of length up to k
 * 
 * @author Efthymia Tsamoura
 *
 */
public class MinimalReformulationDLVFinder {

	private final ConjunctiveQuery query;
	private final Formula targetReformulation;
	private final Set<Dependency> forwardDependencies;
	private final Set<Dependency> backwardDependencies;
	private final String executable;
	private final List<String> vocabulary;

	public MinimalReformulationDLVFinder(ConjunctiveQuery query, Formula targetReformulation,
			Set<Dependency> forwardDependencies, Set<Dependency> backwardDependencies, String executable) {
		Preconditions.checkArgument(forwardDependencies != null);
		Preconditions.checkArgument(backwardDependencies != null);
		Preconditions.checkArgument(query != null);
		Preconditions.checkArgument(query.getAtoms().size()==1);
		Preconditions.checkArgument(targetReformulation != null);
		Preconditions.checkArgument(executable != null);
		this.query = query;
		this.targetReformulation = targetReformulation;
		this.forwardDependencies = forwardDependencies;
		this.backwardDependencies = backwardDependencies;
		this.vocabulary = Lists.newArrayList();
		for(Atom atom:targetReformulation.getAtoms()) {
			this.vocabulary.add(atom.getPredicate().getName());
		}
		this.executable = executable;
	}


	public Set<List<Atom>> findMinimalReformulation() {
		//Do saturation with E with arguments a file containing the forward axioms and the query
		String inputFile = "Input.txt";

		this.writeFile(null, this.forwardDependencies, 
				Sets.<Atom>newHashSet(this.query.getAtoms().get(0).ground(this.query.getSubstitutionToCanonicalConstants())), inputFile);

		//Call DLV
		Runtime rt = Runtime.getRuntime();
		String[] commands = {this.executable, "-silent", inputFile};
		Process proc;
		try {
			Set<List<Atom>> worlds = Sets.newHashSet();
			proc = rt.exec(commands);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			// read the output from the command
			String s = null;
			while ((s = stdInput.readLine()) != null) {
				List<Atom> facts = this.parseFacts(s, this.vocabulary);
				worlds.add(facts);
				this.writeFile(this.query, this.backwardDependencies, facts, inputFile);
				if(!this.checkIfProofFound(inputFile)) {
					return null;
				}
			}
			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				System.out.println("Error messages\n");
				System.out.println(s);
			}
			return worlds;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;
	}

	private void writeFile(ConjunctiveQuery query, Set<Dependency> dependencies, Collection<Atom> facts, String outputFile) {
		PrintWriter output = null;
		try {
			output = new PrintWriter(new File(outputFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DLVFormulaFormat format = new DLVFormulaFormat(output);

		output.print("%%%%%%%%%%%%%%%%%%%%% Target reformulation: " + targetReformulation + " %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");

		output.print("%%%%%%%%%%%%%%%%%%%%% Original axioms %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		for(Dependency dependency: dependencies) {
			Dependency outputDependency = Skolemizer.skolemize(dependency);
			output.print("%Non-clausified dependency");
			output.print("\n");
			output.print("%" + outputDependency.toString());
			output.print("\n");
			output.print("%------------------------Clausification start------------------------%");
			output.print("\n");
			for(Dependency clausifiedDependency:new DLVTranslator().translate(outputDependency)) {
				output.print("%" + clausifiedDependency.toString());
				output.print("\n");
				format.printFOLFormula(clausifiedDependency);
				output.print("\n\n");
			}
			output.print("%------------------------Clausification end ------------------------%");
			output.print("\n\n\n");
		}

		output.print("%%%%%%%%%%%%%%%%%%%%% Facts %%%%%%%%%%%%%%%%%%%%%");
		output.print("\n\n\n");
		for(Atom atom:facts) {
			output.print("%" + atom.toString());
			output.print("\n");
			format.printFOLFormula(atom);
			output.print("\n");
			output.print("\n");
		}

		if(query != null) {
			output.print("%%%%%%%%%%%%%%%%%%%%% Goal %%%%%%%%%%%%%%%%%%%%%");
			output.print("\n");
			output.print("%" + query);
			output.print("\n");
			format.printNegatedQuery(query);
			output.print("\n\n");
		}

		output.close();
	}

	private List<Atom> parseFacts(String line, List<String> vocabulary) {
		List<Atom> facts = Lists.newArrayList();
		if(!line.startsWith("#") && !line.isEmpty()) {

			List<Integer> indices1 = Utility.findIndexes(line, "{");
			List<Integer> indices2 = Utility.findIndexes(line, "}");
			line = line.substring(indices1.get(0)+1, indices2.get(0));

			Set<Atom> parsedAtoms = Sets.newHashSet();
			String[] literals = line.split(", ");
			for(String literal:literals) {

				int startIndex = literal.indexOf("(");
				int endIndex = literal.indexOf(")");

				if(startIndex < 0 || endIndex < 0) {
					String name = literal;
					if(vocabulary.contains(name)) {
						List<UntypedConstant> constants = new ArrayList<UntypedConstant>();
						Atom parsedAtom = new Atom(new Predicate(name, constants.size()), constants);
						parsedAtoms.add(parsedAtom);
						facts.add(parsedAtom);
					}
				}
				else {
					String name = null;
					if(literal.split("\\(")[0].startsWith("~")) {
						throw new java.lang.RuntimeException("Negation is not supported");
					}
					else {
						name = literal.substring(0, startIndex);
					}
					if(vocabulary.contains(name)) {
						List<UntypedConstant> constants = Lists.newArrayList();
						for(String variable:literal.substring(startIndex+1, endIndex).split(",")) {
							if(variable.contains("\"")) {
								variable = variable.replaceAll("\"", "");
							}
							constants.add(new UntypedConstant(variable));
						}
						Atom parsedAtom = new Atom(new Predicate(name, constants.size()), constants);
						parsedAtoms.add(parsedAtom);
						facts.add(parsedAtom);
					}
				}
			}
		}
		return facts;
	}

	private boolean checkIfProofFound(String file) {
		Runtime rt = Runtime.getRuntime();
		String[] commands = {this.executable, "-silent", file};
		Process proc;
		try {
			proc = rt.exec(commands);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			// read the output from the command
			String s = null;
			while ((s = stdInput.readLine()) != null) {
				if(s.isEmpty()) {
					return true;
				}
			}

			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
				return false;
			}
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
