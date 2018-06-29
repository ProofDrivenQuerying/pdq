package uk.ac.ox.cs.pdq.datasources.accessrepository;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;

/** 
 * AccessRepository is the main class of the datasources project. 
 * It maintains a repository executable access methods. The access methods are defined in xml descriptors in a folder. 
 * @author gabor
 *
 */
public class AccessRepository {
	
	private static String DEFAULT_REPOSITORY_LOCATION= "./services";
	// There can be multiple repositories. This static field maps between locations and repositories.		
	public static Map<String,AccessRepository> cachedRepositories = new HashMap<>();
	
	// In each repository there can be multiple access methods. This field maps the name of each access with the actual access.		
	private Map<String, ExecutableAccessMethod> accessMethods = new HashMap<>();
	private String repositoryFolderName;
	
	/** Creates or retrieves the repository pointing to the default repository location. 
	 * @return
	 * @throws JAXBException 
	 */
	public static AccessRepository getRepository() throws JAXBException {
		if (new File(DEFAULT_REPOSITORY_LOCATION).exists())
			return getRepository(DEFAULT_REPOSITORY_LOCATION);
		else return new AccessRepository();
	}
	
	/** Creates or retrieves the repository pointing to the given location. 
	 * @return
	 * @throws JAXBException 
	 */
	public static AccessRepository getRepository(String location) throws JAXBException {
		if (!cachedRepositories.containsKey(location)) {
			cachedRepositories.put(location, new AccessRepository(location));
		}
		return cachedRepositories.get(location);
	}
	
	/** Loops over the files of the repositoryFolderName, and parses the xml descriptors using the IO manager.
	 * @param repositoryFolderName
	 * @throws JAXBException 
	 */
	private AccessRepository(String repositoryFolderName) throws JAXBException {
		File repo = new File(repositoryFolderName);
		this.repositoryFolderName = repo.getAbsolutePath();
		if (!repo.exists())
			throw new RuntimeException("Datasource Repository: \"" + repo.getAbsolutePath() + "\" not found!");
		for(File f:repo.listFiles())
			if (f.getName().toLowerCase().endsWith(".xml")) addAccessFromXml(f);
	}
	
	private AccessRepository() throws JAXBException {
		this.repositoryFolderName = "EmptyRepository";
	}
	
	/** Import a single access method and adds it to the repository
	 * @param xmlFile
	 * @return
	 * @throws JAXBException 
	 */
	public ExecutableAccessMethod addAccessFromXml(File xmlFile) throws JAXBException {
		ExecutableAccessMethod eam = DbIOManager.importAccess(xmlFile);
		if (eam == null)
			throw new RuntimeException("Failed to import file: " + xmlFile);
		accessMethods.put(eam.getName(),eam);
		return eam;
	}
	
	/**
	 * Adds an access method that has already been created externally  
	 * @param eam
	 * @return
	 */
	public ExecutableAccessMethod addAccess(ExecutableAccessMethod eam) {
		accessMethods.put(eam.getName(),eam);
		return eam;
	}
	
	
	/** Retrieves an access method identified by its name.
	 * 
	 * @param name
	 * @return
	 */
	public ExecutableAccessMethod getAccess(String name) {
		return accessMethods.get(name);
	}
	
	/**
	 * Loops over all access methods in this repository and closes all accesses.
	 */
	public void closeAllAccesses() {
		for (ExecutableAccessMethod am:accessMethods.values()) {
			am.close();
		}
	}

	@Override
	public String toString() {
		return "AccessMethod repository for directory: " + this.repositoryFolderName + ".";
	}

	public static void setDefaultLocation(String newDefaultLocation) {
		DEFAULT_REPOSITORY_LOCATION = newDefaultLocation;
	}
}
