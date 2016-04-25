package uk.ac.ox.cs.pdq.benchmark;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.common.base.Joiner;

/**
 * Translates tuples in .csv files to facts in TPTP3 format.
 * Arguments: 
 * arg1: csv input file
 * arg2: e output file
 * arg3: the table name
 * 
 * The output facts are of the form 
 * fof(factid, axiom, ( table_name(value#1, value#2, ...) )), where 
 * factid is a unique fact identifier and axiom is a reserved keyword.
 * In TPTP3 constants start only with lowercase characters. 
 * For this reason, the script encloses uppercase character-starting constants with "". 
 * The first line of the input file is neglected. 
 * 
 * @author Efthymia Tsamoura
 *
 */
public class CSVToETranslator {

	public static void main(String[] args) {

		int id = 0;
		String csvFile = args[0];
		String eFile = args[1];
		String table = args[2];
		BufferedReader reader = null;
		PrintWriter writer = null; 
		int row = 0;

		try {

			//Open the csv file for reading
			reader = new BufferedReader(new FileReader(csvFile));
			//Open the output file for writing
			writer = new PrintWriter(eFile, "UTF-8");
			String line;
			while ((line = reader.readLine()) != null) {

				if(row > 0) {
					String[] tuple = line.split(",");
					for(int i = 0; i < tuple.length; ++i ) {
						if(Character.isUpperCase(tuple[i].charAt(0))) {
							tuple[i] = "\"" + tuple[i] + "\"";
						}
					}
					//Write the output facts
					writer.println("fof(" + table + id++ + ", axiom, (" + table + "(" + Joiner.on(",").join(tuple) + ") )) .");
				}
				++row;
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
