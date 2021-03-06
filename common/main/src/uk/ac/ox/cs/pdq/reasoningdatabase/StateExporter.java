// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoningdatabase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

/**
 * Imports or exports a database state (typically for the chase) 
 * One usecase is to bulkk export the entire chase state for external
 * use.
 * 
 * Another usecase is to export the answers of a user query 
 * (e.g. on the chase)
 * the bufferedexport is a datasink that will be passed to the query 
 * executor, which will call the addfacts method of it periodically
 * to add query output facts
 * 
 * @author gabor
 */
public class StateExporter {

	private DatabaseManager instance;
	private boolean verbose = true;
	public StateExporter(DatabaseManager instance) {
		Preconditions.checkNotNull(instance);
		this.instance = instance;
	}

	/**
	 * Bulk export the current chase state to a folder.
	 * 
	 * @param directory
	 * @throws IOException
	 * @throws DatabaseException 
	 */
	public void exportTo(File directory) throws IOException, DatabaseException {
		Preconditions.checkArgument(directory.exists());
		Preconditions.checkArgument(directory.isDirectory());
		Map<String, Collection<Atom>> factsPerPredicate = sortPerPredicate(instance.getCachedFacts());
		for (String predicateName : factsPerPredicate.keySet()) {
			IOManager.exportFacts(predicateName, directory, factsPerPredicate.get(predicateName));
		}
	}

	private static Map<String, Collection<Atom>> sortPerPredicate(Collection<Atom> facts) {
		Map<String, Collection<Atom>> factsPerPredicate = new HashMap<>();
		for (Atom a : facts) {
			if (factsPerPredicate.containsKey(a.getPredicate().getName())) {
				factsPerPredicate.get(a.getPredicate().getName()).add(a);
			} else {
				Collection<Atom> list = new ArrayList<>();
				list.add(a);
				factsPerPredicate.put(a.getPredicate().getName(), list);
			}
		}
		return factsPerPredicate;
	}
	
	/** Creates a connection to an external database, reads all schema-relations from it and exports them to a file system location as csv files.
	 * @param directory
	 * @param props
	 * @throws IOException
	 * @throws DatabaseException 
	 */
	public static void exportFromDatabaseToDirectory(File directory, DatabaseParameters props, Schema schema) throws IOException, DatabaseException {
		ExternalDatabaseManager edm = new ExternalDatabaseManager(props);
		edm.setSchema(schema);
		edm.getFactsFromPhysicalDatabase(Arrays.asList(schema.getRelations()),new BufferedFactExport(directory));
	}

	/**
	 * Reads csv files representing a chase state and loads them into the instance.
	 * 
	 * @param directory
	 * @param schema
	 * @throws IOException 
	 * @throws DatabaseException 
	 */
	public void importFrom(File directory, Schema schema) throws IOException, DatabaseException {
		Preconditions.checkArgument(directory.exists());
		Preconditions.checkArgument(directory.isDirectory());

		for (Relation r : schema.getRelations()) {
			File csvFile = new File(directory, r.getName()+".csv");
			if (csvFile.exists()) {
				IOManager.importFacts(r, csvFile, instance, verbose);
			} else {
				if (verbose) System.out.println("No data found for relation " + r.getName());
			}
		}
	}
	
	/* This will be attached to a query executor, which will call
	 * the addfacts method defined below in order to send query ouput t
	 * to a file
	 * 
	 */
	public static class BufferedFactExport implements DataSink {
		private File directory;
		private String forcedFileName = null;
		private boolean filterLabelledNulls = false;
		public BufferedFactExport(File directory) {
			Preconditions.checkArgument(directory.exists());
			Preconditions.checkArgument(directory.isDirectory());
			this.directory = directory;
		}
		@Override
		public void addFacts(Collection<Atom> facts) throws IOException {
			if (forcedFileName!=null) {
				IOManager.exportFacts(forcedFileName, directory, facts,filterLabelledNulls);
			} else {
				Map<String, Collection<Atom>> factsPerPredicate = sortPerPredicate(facts);
				for (String predicateName : factsPerPredicate.keySet()) {
					IOManager.exportFacts(predicateName, directory, factsPerPredicate.get(predicateName),filterLabelledNulls);
				}
			}
		}
		public String getForcedFileName() {
			return forcedFileName;
		}
		public void setForcedFileName(String forcedFileName) {
			this.forcedFileName = forcedFileName;
		}
		public void setFilterLabelledNull(boolean filterLabelledNull) {
			this.filterLabelledNulls = filterLabelledNull;			
		}
		
	}
	
}
