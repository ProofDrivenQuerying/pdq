// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// TODO: Auto-generated Javadoc
/**
 * Top level class for the GUI.
 *
 * @author Julien Leblay
 */
public class PDQApplication extends Application {

	/** PDQApplication's logger. */
	private static Logger log = Logger.getLogger(PDQApplication.class);

	/** The Constant APPLICATION_NAME. */
	public static final String APPLICATION_NAME = "PDQ";
	
	/** The Constant WORK_DIRECTORY. */
	public static final String WORK_DIRECTORY = ".pdq";
	
	/** The Constant PATH_TO_DEFAULT_SETTINGS. */
	public static final String PATH_TO_DEFAULT_SETTINGS = "/resources/settings/defaults.zip";
	
	/** The Constant DEFAULT_CONFIGURATION. */
	public static final String DEFAULT_CONFIGURATION = "pdq-demo.properties";
	
	/** The Constant PATH_TO_ROOT_WINDOW_DEFINITION. */
	public static final String PATH_TO_ROOT_WINDOW_DEFINITION = "/resources/layouts/root-window.fxml";
	
	/** The Constant PATH_TO_RESOURCE_BUNDLE. */
	public static final String PATH_TO_RESOURCE_BUNDLE = "resources.i18n.ui";
	
	/** The Constant GLOBAL_SCHEMA. */
	public static final String GLOBAL_SCHEMA = "global-schema.xml";
	
	/** The Constant SCHEMA_DIRECTORY. */
	public static final String SCHEMA_DIRECTORY = "schemas";
	
	/** The Constant SCHEMA_FILENAME_PREFIX. */
	public static final String SCHEMA_FILENAME_PREFIX = "schema";
	
	/** The Constant QUERY_FILENAME_PREFIX. */
	public static final String QUERY_FILENAME_PREFIX = "query";
	
	/** The Constant PLAN_FILENAME_PREFIX. */
	public static final String PLAN_FILENAME_PREFIX = "plan";
	
	/** The Constant SCHEMA_FILENAME_SUFFIX. */
	public static final String SCHEMA_FILENAME_SUFFIX = ".s";
	
	/** The Constant QUERY_FILENAME_SUFFIX. */
	public static final String QUERY_FILENAME_SUFFIX = ".q";
	
	/** The Constant PLAN_FILENAME_SUFFIX. */
	public static final String PLAN_FILENAME_SUFFIX = ".p";
	
	/** The Constant PROOF_FILENAME_SUFFIX. */
	public static final String PROOF_FILENAME_SUFFIX = ".pr";
	
	/** The Constant FILENAME_SUFFIX. */
	public static final String FILENAME_SUFFIX = ".xml";
	
	/** The Constant PROPERTIES_SUFFIX. */
	public static final String PROPERTIES_SUFFIX = ".properties";
	
	/** The Constant QUERY_DIRECTORY. */
	public static final String QUERY_DIRECTORY = "queries";
	
	/** The Constant PLAN_DIRECTORY. */
	public static final String PLAN_DIRECTORY = "plans";

    /**
     * The main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(PDQApplication.class, (java.lang.String[])null);
    }

    /* (non-Javadoc)
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
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
	 * Sets up the user-specific work directory, where all user's schema/query/
	 * plans are stored. If the folder does not exist, it is created empty,
	 * otherwise, existing schema/query/plans are loaded in memory.
	 * @return a pointer to the work directory
	 */
    private static String OS = System.getProperty("os.name").toLowerCase();

     public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }

	static File setupWorkDirectory() {
		String homeDir = ".";
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
	
	/**
	 * Unzip defaults.
	 *
	 * @param dir the dir
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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
		}catch (Exception e){
			log.warn("[Error with default.zip]",e);
		}
	}
}
