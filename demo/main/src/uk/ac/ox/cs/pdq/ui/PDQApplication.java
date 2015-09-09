package uk.ac.ox.cs.pdq.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.apache.log4j.Logger;

/**
 * Top level class for the GUI
 * 
 * @author Julien Leblay
 *
 */
public class PDQApplication extends Application {

	/** PDQApplication's logger. */
	private static Logger log = Logger.getLogger(PDQApplication.class);

	public static final String APPLICATION_NAME = "PDQ";
	public static final String WORK_DIRECTORY = ".pdq";
	public static final String PATH_TO_DEFAULT_SETTINGS = "/resources/settings/defaults.zip";
	public static final String DEFAULT_CONFIGURATION = "pdq-demo.properties";
	public static final String PATH_TO_ROOT_WINDOW_DEFINITION = "/resources/layouts/root-window.fxml";
	public static final String PATH_TO_RESOURCE_BUNDLE = "resources.i18n.ui";
	public static final String GLOBAL_SCHEMA = "global-schema.xml";
	public static final String SCHEMA_DIRECTORY = "schemas";
	public static final String SCHEMA_FILENAME_PREFIX = "schema";
	public static final String QUERY_FILENAME_PREFIX = "query";
	public static final String PLAN_FILENAME_PREFIX = "plan";
	public static final String SCHEMA_FILENAME_SUFFIX = ".s";
	public static final String QUERY_FILENAME_SUFFIX = ".q";
	public static final String PLAN_FILENAME_SUFFIX = ".p";
	public static final String PROOF_FILENAME_SUFFIX = ".pr";
	public static final String FILENAME_SUFFIX = ".xml";
	public static final String PROPERTIES_SUFFIX = ".properties";
	public static final String QUERY_DIRECTORY = "queries";
	public static final String PLAN_DIRECTORY = "plans";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(PDQApplication.class, (java.lang.String[])null);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
        	ResourceBundle bundle = ResourceBundle.getBundle(PATH_TO_RESOURCE_BUNDLE);
            Parent parent = (Parent) FXMLLoader.load(
            		PDQApplication.class.getResource(PATH_TO_ROOT_WINDOW_DEFINITION),
            		bundle);
            Scene scene = new Scene(parent);
            primaryStage.setScene(scene);
            primaryStage.setTitle(bundle.getString("application.title"));
            primaryStage.setOnCloseRequest((WindowEvent arg0) -> System.exit(0));
            primaryStage.show();
        } catch (Exception ex) {
            log.fatal(ex);
        	ex.printStackTrace();
            System.exit(-1);
        }
    }

	/**
	 * Sets up the user-specific work director, where all user's schema/query/
	 * plans are stored. If the folder does not exist, it is created empty,
	 * otherwise, existing schema/query/plans are loaded in memory.
	 * @return a pointer to the work directory
	 */
	static File setupWorkDirectory() {
		String homeDir = System.getenv("HOME");
		// If HOME is not set try HOMEPATH (Windows)
		if (homeDir == null) {
			homeDir = System.getenv("HOMEPATH");
		}
		if (homeDir == null) {
			log.warn("No HOME directory defined. Using '.' as default");
			homeDir = ".";
		}
		File workDir = new File(homeDir + '/' + PDQApplication.WORK_DIRECTORY);
		if (workDir.exists()) {
			if (!workDir.isDirectory()) {
				throw new UserInterfaceException("Unable to create work directory '" + workDir.getAbsolutePath() + "'");
			}
		} else {
			log.info("No defaults found. Initializing demo environment...");
			try {
				unzipDefaults(new File(homeDir));
			} catch (IOException e) {
				log.warn("Unable to default resources.settings '" + workDir.getAbsolutePath() + "'");
			}
		}
		
        if (System.setProperty("user.dir", workDir.getAbsolutePath()) == null) {
			throw new UserInterfaceException("Unable to switch working directory to '" + workDir.getAbsolutePath() + "'");
        }
		return workDir;
	}
	
	private static void unzipDefaults(File dir) throws IOException {
		byte[] buffer = new byte[2048];
		try (ZipInputStream in = new ZipInputStream(PDQApplication.class.getResourceAsStream(PATH_TO_DEFAULT_SETTINGS))) {
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				String outpath = dir.getAbsolutePath() + "/" + entry.getName();
				if (entry.isDirectory()) {
					new File(outpath).mkdirs();
				} else {
					try (FileOutputStream out = new FileOutputStream(outpath)) {
						int len = 0;
						while ((len = in.read(buffer)) > 0) {
							out.write(buffer, 0, len);
						}
					}
				}
			}
		}
	}
}
