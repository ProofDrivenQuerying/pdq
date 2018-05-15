package uk.ac.ox.cs.pdq.datasources.accessrepository;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;

/** 
 * AccessRepository is the main class of the datasources project. 
 * It maintains a cache of executable accesses. The accesses are defined in xml descriptors in a folder. 
 * @author gabor
 *
 */
public class AccessRepository {
	
	private static String DEFAULT_REPOSITORY_LOCATION= "/services";
	// There can be multiple repositories. This static field maps between locations and repositories.		
	public static Map<String,AccessRepository> cachedRepositories = new HashMap<>();
	
	// In each repository there can be multiple accesses. This field maps the name of each access with the actual access.		
	private Map<String, ExecutableAccessMethod> accesses = new HashMap<>();
	
	/** Creates or retrieves the repository pointing to the default repository location. 
	 * @return
	 * @throws JAXBException 
	 */
	public static AccessRepository getRepository() throws JAXBException {
		return getRepository(DEFAULT_REPOSITORY_LOCATION);
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
		if (!repo.exists())
			throw new RuntimeException("Datasource Repository: \"" + repo.getAbsolutePath() + "\" not found!");
		for(File f:repo.listFiles())
			if (f.getName().toLowerCase().endsWith(".xml")) addAccessFromXml(f);
	}
	
	/** Import a single access and adds it to the repository
	 * @param xmlFile
	 * @return
	 * @throws JAXBException 
	 */
	public ExecutableAccessMethod addAccessFromXml(File xmlFile) throws JAXBException {
		ExecutableAccessMethod eam = DbIOManager.importAccess(xmlFile);
		if (eam == null)
			throw new RuntimeException("Failed to import file: " + xmlFile);
		accesses.put(eam.getName(),eam);
		return eam;
	}
	
	/** Retrieves an access identified by its name.
	 * 
	 * @param name
	 * @return
	 */
	public ExecutableAccessMethod getAccess(String name) {
		return accesses.get(name);
	}
	
	/**
	 * Loops over all accesses in this repository and closes all accesses.
	 */
	public void closeAllAccesses() {
		for (ExecutableAccessMethod am:accesses.values()) {
			am.close();
		}
	}
	
}
