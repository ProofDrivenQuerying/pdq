package uk.ac.ox.cs.pdq.test;

import static uk.ac.ox.cs.pdq.ui.PDQApplication.QUERY_DIRECTORY;
import static uk.ac.ox.cs.pdq.ui.PDQApplication.QUERY_FILENAME_SUFFIX;
import static uk.ac.ox.cs.pdq.ui.PDQApplication.SCHEMA_DIRECTORY;
import static uk.ac.ox.cs.pdq.ui.PDQApplication.SCHEMA_FILENAME_SUFFIX;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import org.junit.Test;


import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.ui.UserInterfaceException;
import uk.ac.ox.cs.pdq.ui.io.ObservableQueryReader;
import uk.ac.ox.cs.pdq.ui.io.ObservableSchemaReader;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryReader;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryWriter;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;


public class LoadingAndSavingTest {
	
	private String queryTextArea = "SELECT a0.activity_comment FROM activityFree AS a0";
	private ObservableSchema currentSchema = null;
	private ObservableQuery currentQuery = new ObservableQuery("name", "description", null);
	private File workDirectory = new File("/users/marler/.pdq");

	// testSave method re-factored from PDQController.saveSelectedQuery
	@Test
	public void testSave() {

		try
		{
			loadSchema();
			String str = this.queryTextArea;
			SQLLikeQueryReader qr = new SQLLikeQueryReader(this.currentSchema.getSchema());
			ConjunctiveQuery cjq = qr.fromString(str);
			currentQuery.setQuery(cjq);
			saveQuery(currentQuery);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	// testLoad method re-factored from PDQController.loadQueries
	@Test
	public void testLoad() {

		try
		{
			loadSchema();
			ObservableSchema s = this.currentSchema;
			File queryDir = new File(this.workDirectory.getAbsolutePath() + '/' + QUERY_DIRECTORY);
			for (File queryFile : listFiles(queryDir, makePrefix(s), QUERY_FILENAME_SUFFIX)) {
				try (FileInputStream in = new FileInputStream(queryFile.getAbsolutePath())) {
					ObservableQueryReader queryReader = new ObservableQueryReader(s.getSchema());
					ObservableQuery q = queryReader.read(queryFile);
					q.setFile(queryFile);
					this.currentQuery = q;
					this.queryTextArea = SQLLikeQueryWriter.convert(this.currentQuery.getFormula(), this.currentSchema.getSchema());
					break;
				} catch (IOException e) {
					throw new UserInterfaceException(e.getMessage(), e);
				}
			}
			System.out.println("Loaded: " + this.queryTextArea);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
	
	// saveQuery method re-factored from PDQController
	private void saveQuery(ObservableQuery query) {
		File file = query.getFile();
		if (file == null) {
			String filename = null;
			int i = 0;
			do {
				filename = this.workDirectory.getAbsolutePath() + '/' + QUERY_DIRECTORY + '/'
						+ makePrefix(this.currentSchema) + (i++) + QUERY_FILENAME_SUFFIX;
			} while ((file = new File(filename)).exists());
			System.out.println("Saved: " + filename);
			query.setFile(file);
		}
		query.store();
	}
	
	// makePrefix method re-factored from PDQController
	private static String makePrefix(ObservableSchema schema) {
		return schema.getFile().getName().replace(SCHEMA_FILENAME_SUFFIX, "") + "_";
	}
	
	// loadSchema method altered so that only one schema is loaded
	private void loadSchema() {
		File schemaDir = new File(this.workDirectory.getAbsolutePath() + '/' + SCHEMA_DIRECTORY);
		for (File schemaFile : listFiles(schemaDir, "", SCHEMA_FILENAME_SUFFIX)) {
			ObservableSchemaReader schemaReader = new ObservableSchemaReader();
			this.currentSchema = schemaReader.read(schemaFile);
			this.currentSchema.setFile(schemaFile);
			break;
		}
	}
	
	// listFiles method re-factored from PDQController
	private static File[] listFiles(File directory, final String prefix, final String suffix) {
		Preconditions.checkArgument(directory.isDirectory(),
				"Invalid internal schema directory " + directory.getAbsolutePath());
		return directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(prefix) && name.endsWith(suffix);
			}
		});
	}
}
