package uk.ac.ox.cs.pdq.test.datasources.services;

import java.io.File;

import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.AccessMethod;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.ServiceRoot;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.ServiceGroupsRoot;
import uk.ac.ox.cs.pdq.datasources.services.RESTExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.services.ServiceManager;
import uk.ac.ox.cs.pdq.datasources.utility.Table;

public class ServiceXmlTest {
	
	@Test
	public void testServiceXml() {
		
		try
		{
			File schemaFile1 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "service-groups.xml");
			File schemaFile2 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "reactome-speciesList.xml");
			File outputFile = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "yahoo-services2.xml");
			ServiceGroupsRoot sgr = ServiceManager.importServiceGroups(schemaFile1);
			ServiceRoot sr = ServiceManager.importAccessMethod(schemaFile2);
			System.out.println(sr.toString());
			for(int i = 0; i < sr.getAccessMethod().length; i++)
			{
				AccessMethod am = sr.getAccessMethod()[i];
				RESTExecutableAccessMethod ream = new RESTExecutableAccessMethod(sgr, sr, am);
				Table t = ream.access();
				System.out.println(t);
			}
			ServiceManager.exportAccessMethod(sr, outputFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
