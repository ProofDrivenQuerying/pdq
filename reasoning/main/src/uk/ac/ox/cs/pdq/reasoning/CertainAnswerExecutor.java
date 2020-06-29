// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.StateExporter.BufferedFactExport;

/**
 * @author gabor This class allows users to find the certain answers for
 *  queries on a database. This is implemented by taking the chase of the database, executing
 *  the query as usual, and filtering out nulls
 */
public class CertainAnswerExecutor {
	public static final String DEFAULT_OUTPUT_DIR = "results";

	private DatabaseManager databaseManager;

	/**
	 * Initiates this class using DatabaseParameters to create new connection.
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	public CertainAnswerExecutor(DatabaseParameters parameters) throws DatabaseException {
		this(new ExternalDatabaseManager(parameters));
	}

	/**
	 * Initiates this class using an already existing database connection.
	 * 
	 * @param databaseManager
	 * @throws DatabaseException
	 */
	public CertainAnswerExecutor(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	/**
	 * Finds the certain answers to a single query and saves the results into a csv file.
	 * 
	 * @param q
	 * @param outputFile
	 * @throws DatabaseException 
	 * @throws IOException 
	 */
	public void findCertainAnswersQuery(ConjunctiveQuery q, File outputFile) throws DatabaseException, IOException {
		if (databaseManager instanceof ExternalDatabaseManager) {
			BufferedFactExport exporter = new BufferedFactExport(outputFile.getAbsoluteFile().getParentFile());
			exporter.setForcedFileName(outputFile.getName());
			exporter.setFilterLabelledNull(true);
			((ExternalDatabaseManager)databaseManager).answerConjunctiveQuery(q,exporter);
		} else {
			List<Atom> facts = new ArrayList<>();
			List<Match> matches = databaseManager.answerConjunctiveQuery(q);
			for (Match m : matches) {
				Atom a = m.toAtom();
				if (a.isNotANull())
					facts.add(m.toAtom());
			}
			IOManager.exportFacts(outputFile.getName(), outputFile.getAbsoluteFile().getParentFile(), facts);
		}
	}

	public void findCertainAnswersQuery(File queryFile, File outputFile) throws JAXBException, DatabaseException, IOException {
		findCertainAnswersQuery(IOManager.importQuery(queryFile), outputFile);
	}

	/**
	 * One by one finds certain answers of all queries in the queryfolder and creates a csv result
	 * file in the output folder. The csv files will be named according to the name
	 * of the query file.
	 * 
	 * @param queryFolder
	 * @param outputFolder
	 * @throws JAXBException
	 * @throws IOException 
	 * @throws DatabaseException 
	 */
	public void findCertainAnswersQueries(File queryFolder, File outputFolder) throws JAXBException, DatabaseException, IOException {
		if (outputFolder == null)
			outputFolder = new File(queryFolder, DEFAULT_OUTPUT_DIR);
		if (!outputFolder.exists())
			outputFolder.mkdirs();
		if (!queryFolder.exists())
			throw new FileNotFoundException("Query folder not found: " + queryFolder.getAbsolutePath());
		boolean foundAny = false;
		File[] queries = null;
		if (queryFolder.isDirectory()) {
			queries = queryFolder.listFiles();
		} else {
			queries = new File[1];
			queries[0] = queryFolder;
		}
		for (File q : queries) {
			if (q.getName().endsWith(".xml") || q.getName().endsWith(".txt") ) {
				String nameWithoutExtension = q.getName().substring(0, q.getName().length() - 4);
				ConjunctiveQuery cq = IOManager.importQuery(q);
				if (cq != null) {
					foundAny = true;
					findCertainAnswersQuery(cq, new File(outputFolder, nameWithoutExtension));
				}
			}
		}
		if (!foundAny) {
			throw new FileNotFoundException("Couldn't find any queries in folder: " + queryFolder.getAbsolutePath());
		}
	}

}
