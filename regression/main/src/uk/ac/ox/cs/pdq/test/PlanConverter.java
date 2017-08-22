package uk.ac.ox.cs.pdq.test;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.adapted.AdaptedDbSchema;

public class PlanConverter {
	private static int counter = 0;
	public static void main(String[] args) {
		File srcRoot  = new File("c:\\Users\\Gabor\\git\\pdq\\regression\\test");
		File saved = new File("c:\\work\\savedPlans");
		//File src = new File("c:\\Users\\Gabor\\git\\pdq\\regression\\test\\linear\\fast\\demo\\derby\\case_002");
				
		try {
			loopOverDirectories(srcRoot,saved);
			System.out.println("Successfully processed "+counter + " schemas.");
		} catch (FileNotFoundException | JAXBException e) {
			e.printStackTrace();
		}
		
	}

	private static void loopOverDirectories(File srcRoot, File saved) throws FileNotFoundException, JAXBException {
		System.out.println("Processing ("+counter+") :"+srcRoot.getAbsolutePath());
		File[] children = srcRoot.listFiles();
		for (File child:children) {
			if (child.isDirectory()) {
				loopOverDirectories(child, new File(saved,child.getName()));
			} else {
				if ("schema.xml".equalsIgnoreCase(child.getName())) {
					saved.mkdirs();
					AdaptedDbSchema s = DbIOManager.readAdaptedSchema(child);
					DbIOManager.createCatalog(child,new File(child.getParentFile(),"catalog.properties"));			
					//child.renameTo(saved);
					DbIOManager.exportAdaptedSchemaToXml(s, new File("c:\\work\\temp.xml"));
					//DbIOManager.exportAdaptedSchemaToXml(s, child);
					counter++;
				}
			}
		}
	}

}
