package uk.ac.ox.cs.pdq.benchmark;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Translates constraints and views from the customised format to DLV format.
 * 
 * 	A constraint might look like this:
	c0(DC):-m87004(X1,X2,X7,X8),m298004(X2,X3,X9,X10),m113004(X3,X4,X11,X12),m299004(X0,X1,X5,X6)
	This is interpreted as m87004(X1,X2,X7,X8) being the body of the constraint 
	and m298004(X2,X3,X9,X10),m113004(X3,X4,X11,X12),m299004(X0,X1,X5,X6) being the head (C0(DC) is essentially ignored).
	A view might look like this:
	v0(X1,X2,X7,X8):-m87004(X1,X2,X7,X8),m298004(X2,X3,X9,X10),m113004(X3,X4,X11,X12)
 * Arguments: 
 * arg1: input constraint file
 * arg2: output constraint file
 * arg3: input view file
 * arg4: output view file
 * @author Efthymia Tsamoura
 *
 */
public class SyntheticToDLVTranslator {

	public static void main(String[] args) {

		String inputConstraintsFile = "C:\\Users\\tsamoura\\Dropbox\\chaseBench\\datasets\\synthetic_constraints\\chain\\run_0-100\\constraints_for_q_0.txt";
		String onputConstraintsFile = "C:\\Users\\tsamoura\\Dropbox\\chaseBench\\datasets\\synthetic_constraints\\scenario\\DLV\\synthetic_constraints-0-100-scenario.dlv";
		
		translateConstraints(inputConstraintsFile, onputConstraintsFile);
		
//		String inputViewsFile = args[2];
//		String outputViewsFile = args[3];
//		
//		translateConstraints(inputViewsFile, outputViewsFile);

	}

	/**
	 * 
	 * @param input
	 * @param output
	 */
	public static void translateConstraints(String input, String output) {
		BufferedReader reader = null;
		PrintWriter writer = null; 
		try {
			reader = new BufferedReader(new FileReader(input));
			writer = new PrintWriter(output, "UTF-8");
			String line;
			while ((line = reader.readLine()) != null) {

				//A constraint might look like this:
				//c0(DC):-m87004(X1,X2,X7,X8),m298004(X2,X3,X9,X10),m113004(X3,X4,X11,X12),m299004(X0,X1,X5,X6)
				//This is interpreted as m87004(X1,X2,X7,X8) being the body of the constraint 
				//and m298004(X2,X3,X9,X10),m113004(X3,X4,X11,X12),m299004(X0,X1,X5,X6) being the head (C0(DC) is essentially ignored).

				String[] split = line.split(":-");
				if(split.length != 2) {
					throw new java.lang.IllegalStateException("Unparsable constraint");
				}
				else {
					String[] split2 = split[1].split("\\)");
					if(split2.length < 2) {
						throw new java.lang.IllegalStateException("Malformed constraint");
					}
					else {
						int indexOf = split[1].indexOf(")");
						String body = split[1].substring(0, indexOf+1);
						String head = split[1].substring(indexOf+2, split[1].length());
						
						List<String> newHeadAtoms = Lists.newArrayList();  
						
						//Parse each atom in the head of the rule
						//and skolemise the existential variables
						String function = "f_";
						int functionIndex = 0;
						int startIndex = 0;
						int endIndex = 0;
						Map<String, String> existentialToSkolem = Maps.newHashMap();
						
						while(startIndex < head.length()) {
							endIndex = head.indexOf(")", startIndex); 
							String atom = head.substring(startIndex, endIndex + 1);
							
							startIndex = endIndex + 2;
							
							//Find the atom's variables and its predicate;
							int atomBeginIndex = atom.indexOf("(");
							String predicate = atom.substring(0, atomBeginIndex);
							
							int atomEndIndex = atom.indexOf(")");
							String varString = atom.substring(atomBeginIndex + 1, atomEndIndex);
							String[] variables = varString.split(",");
							
							//Find the existentially/universally quantified variables
							List<String> existential = Lists.newArrayList(); 
							List<String> universal = Lists.newArrayList(); 
							for(String variable:variables) {
								if(!body.contains(variable)) {
									existential.add(variable);
								}
								else {
									universal.add(variable);
								}
							}
							//Create a new atom with skolemised variables
							List<String> newVariables = Lists.newArrayList(); 
							for(String variable:variables) {
								if(universal.contains(variable)) {
									newVariables.add(variable);
								}
								else {
									String skolem = existentialToSkolem.get(variable);
									if(skolem == null) {
										skolem = function + functionIndex++ + "(" + Joiner.on(",").join(universal) + ")";
										existentialToSkolem.put(variable, skolem);
									}
									newVariables.add(skolem);
								}
							}
							String newAtom = predicate + "(" + Joiner.on(",").join(newVariables) + ")";
							newHeadAtoms.add(newAtom);
						}
						writer.println( head + ":-" + Joiner.on(",").join(newHeadAtoms) + " .");
					}
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	/**
	 * 
	 * @param input
	 * @param output
	 */
	public static void translateViews(String input, String output) {
		BufferedReader reader = null;
		PrintWriter writer = null; 
		try {
			reader = new BufferedReader(new FileReader(input));
			writer = new PrintWriter(output, "UTF-8");
			String line;
			while ((line = reader.readLine()) != null) {
				
				//A view might look like this:
				//v0(X1,X2,X7,X8):-m87004(X1,X2,X7,X8),m298004(X2,X3,X9,X10),m113004(X3,X4,X11,X12)
				writer.println( line + " .");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


}
